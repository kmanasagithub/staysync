package com.trip.staysync.service;

import com.trip.staysync.dto.BookingDto;
import com.trip.staysync.dto.BookingRequest;
import com.trip.staysync.dto.GuestDto;
import com.trip.staysync.dto.HotelReportDto;
import com.trip.staysync.entity.*;
import com.trip.staysync.entity.enums.BookingStatus;
import com.trip.staysync.exception.ResouceNotFoundException;
import com.trip.staysync.exception.UnAuthorizedException;
import com.trip.staysync.repository.*;
import com.trip.staysync.strategies.PricingService;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import jakarta.transaction.Transactional;
import com.stripe.model.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;

import static com.trip.staysync.util.AppUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService{

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;
    private final GuestRepository guestRepository;
    private final CheckoutService checkoutService;
    private final PricingService pricingService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Transactional
    @Override
    public BookingDto initialiseBooking(BookingRequest bookingrequest) {

        log.info("Initialising booking for Hotel: {}, room: {}, date {}-{}",bookingrequest.getHotelId(),bookingrequest.getRoomId(),
                bookingrequest.getCheckInDate(),bookingrequest.getCheckOutDate());

        Hotel hotel = hotelRepository.findById(bookingrequest.getHotelId())
                .orElseThrow(() ->  new ResouceNotFoundException("Hotel not found with id: "+bookingrequest.getHotelId()));

        Room room = roomRepository.findById(bookingrequest.getRoomId())
                .orElseThrow(() ->  new ResouceNotFoundException("Room not found with id: "+bookingrequest.getRoomId()));

        List<Inventory> inventoryList = inventoryRepository.findAndLockAvailableInventory(
                room.getId(),bookingrequest.getCheckInDate(),bookingrequest.getCheckOutDate(),bookingrequest.getRoomsCount()
        );

        System.out.println(bookingrequest);
        System.out.println("Start Date = " + bookingrequest.getCheckInDate());
        System.out.println("End Date = " + bookingrequest.getCheckOutDate());

        long daysCount = ChronoUnit.DAYS.between(bookingrequest.getCheckInDate(),bookingrequest.getCheckOutDate())+1;

        if(inventoryList.size() != daysCount) {
            throw new IllegalStateException("Room is not available anymore");
        }

        //Reserve the room/ update the booked count of inventories
        inventoryRepository.initBooking(room.getId(),bookingrequest.getCheckInDate()
        ,bookingrequest.getCheckOutDate(), bookingrequest.getRoomsCount());


        //TODO: calculate dynamic amount
        BigDecimal priceForOneRoom = pricingService.calculateTotalPrice(inventoryList);
        BigDecimal totalPrice = priceForOneRoom.multiply(BigDecimal.valueOf(bookingrequest.getRoomsCount()));

        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingrequest.getCheckInDate())
                .checkOutDate(bookingrequest.getCheckOutDate())
                .user(getCurrentUser())
                .roomCount(bookingrequest.getRoomsCount())
                .amount(totalPrice)
                .build();

        booking = bookingRepository.save(booking);
        return  modelMapper.map(booking, BookingDto.class);

    }

    @Override
    @Transactional
    public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList) {

        log.info("Adding Guests for booking with ID: : {}",bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->  new ResouceNotFoundException("Booking not found with id: "+bookingId));

        User user = getCurrentUser();

        if(!user.equals(booking.getUser())) {
            throw new UnAuthorizedException("Booking does not belong to this user with id: "+user.getId());
        }

        if(hasBookingExpired(booking)) {
           throw new IllegalStateException("Booking has already expired");
        }

        if(booking.getBookingStatus() != BookingStatus.RESERVED) {
            throw new IllegalStateException("Booking is not under reserved state, cannot add guests");
        }

        for(GuestDto guestDto : guestDtoList) {
            Guest guest = modelMapper.map(guestDto,Guest.class);
            guest.setUser(user);
            guest = guestRepository.save(guest);
            booking.getGuests().add(guest);
        }

        booking.setBookingStatus(BookingStatus.GUESTS_ADDED);

        booking = bookingRepository.save(booking);
        return modelMapper.map(booking,BookingDto.class);
    }

    @Override
    @Transactional
    public String initiatePayments(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new ResouceNotFoundException("Booking not found with id: "+bookingId)
        );
        User user = getCurrentUser();

        if(!user.equals(booking.getUser())) {
            throw new UnAuthorizedException("Booking does not belong to this user with id: "+user.getId());
        }
        if(hasBookingExpired(booking)) {
            throw new IllegalStateException("Booking has already expired");
        }

        String sessionUrl = checkoutService.getCheckoutSession(booking,
                frontendUrl+"/payments/sucess",frontendUrl+"/payments/failure");

        booking.setBookingStatus(BookingStatus.PAYMENT_PENDING);
        bookingRepository.save(booking);

        return sessionUrl;
    }

    @Override
    @Transactional
    public void capturePayment(Event event) {
        if("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if(session == null) return;

            String sessionId = session.getId();
            Booking booking = bookingRepository.findByPaymentSessionId(sessionId).orElseThrow(
                    () -> new ResouceNotFoundException("Booking not Found with the Session ID : {}" + sessionId)
            );

            booking.setBookingStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(),booking.getCheckInDate(),
                    booking.getCheckOutDate(),booking.getRoomCount());

            inventoryRepository.confirmBooking(booking.getRoom().getId(), booking.getCheckOutDate(), booking.getCheckInDate(),
                    booking.getRoomCount());

            log.info("Successfully confirmed the booking for booking ID : {}",booking.getId());
        }else{
            log.warn("Unhandled event type: {}",event.getType());
        }
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new ResouceNotFoundException("Booking not found with id: "+bookingId)
        );
        User user = getCurrentUser();

        if(!user.equals(booking.getUser())) {
            throw new UnAuthorizedException("Booking does not belong to this user with id: "+user.getId());
        }

        if(booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only Confirmed bookings can be cancelled");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(),booking.getCheckInDate(),
                booking.getCheckOutDate(),booking.getRoomCount());

        inventoryRepository.cancelBooking(booking.getRoom().getId(), booking.getCheckOutDate(), booking.getCheckInDate(),
                booking.getRoomCount());

//      handle the refund
        try{
            Session session = Session.retrieve(booking.getPaymentSessionId());
            RefundCreateParams refundCreateParams = RefundCreateParams.builder()
                    .setPaymentIntent(session.getPaymentIntent())
                    .build();

            Refund.create(refundCreateParams);
        }catch (StripeException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public String getBookingStatus(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new ResouceNotFoundException("Booking not found with id: "+bookingId)
        );
        User user = getCurrentUser();

        if(!user.equals(booking.getUser())) {
            throw new UnAuthorizedException("Booking does not belong to this user with id: "+user.getId());
        }

        return booking.getBookingStatus().name();
    }

    @Override
    public List<BookingDto> getAllBookingsByHotelId(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResouceNotFoundException("Hotel not found with the ID : {}"+hotelId));

        User user = getCurrentUser();
        log.info("Getting all Bookings for the hotel with ID : {}",hotelId);

        if(!user.equals(hotel.getOwner())) throw new AccessDeniedException("You are not the Owner of hotel with ID : {} "+hotelId);

        List<Booking> bookings = bookingRepository.findByHotel(hotel);

        return bookings.stream().map((element) -> modelMapper.map(element,BookingDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public HotelReportDto getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResouceNotFoundException("Hotel not found with the ID : {}"+hotelId));

        User user = getCurrentUser();
        log.info("Generating report for the hotel with ID : {}",hotelId);

        if(!user.equals(hotel.getOwner())) throw new AccessDeniedException("You are not the Owner of hotel with ID : {} "+hotelId);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Booking> bookings = bookingRepository.findByHotelAndCreatedAtBetween(hotel,startDateTime,endDateTime);

        Long totalConfirmedBookings = bookings.stream()
                .filter(booking -> booking.getBookingStatus() == BookingStatus.CONFIRMED)
                .count();

        BigDecimal totalRevenueOfConfirmedBookings = bookings.stream()
                .filter(booking -> booking.getBookingStatus() == BookingStatus.CONFIRMED)
                .map(Booking::getAmount)
                .reduce(BigDecimal.ZERO,BigDecimal::add);

        BigDecimal avgRevenue = totalConfirmedBookings == 0 ? BigDecimal.ZERO : totalRevenueOfConfirmedBookings
                .divide(BigDecimal.valueOf(totalConfirmedBookings), RoundingMode.HALF_UP);

        return new HotelReportDto(totalConfirmedBookings,totalRevenueOfConfirmedBookings,avgRevenue);
    }

    @Override
    public List<BookingDto> getMyBookings() {
        User user = getCurrentUser();

        return bookingRepository.findByUser(user)
                 .stream()
                 .map((element) ->  modelMapper.map(element, BookingDto.class))
                 .collect(Collectors.toList());
    }

    public boolean hasBookingExpired(Booking booking) {
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }
}
