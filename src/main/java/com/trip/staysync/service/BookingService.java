package com.trip.staysync.service;


import com.stripe.model.Event;
import com.trip.staysync.dto.BookingDto;
import com.trip.staysync.dto.BookingRequest;
import com.trip.staysync.dto.GuestDto;
import com.trip.staysync.dto.HotelReportDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface BookingService {
    BookingDto initialiseBooking(BookingRequest bookingrequest);

    BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);

    String initiatePayments(Long bookingId);

    void capturePayment(Event event);

    void cancelBooking(Long bookingId);

    String getBookingStatus(Long bookingId);

    List<BookingDto> getAllBookingsByHotelId(Long hotelId);

    HotelReportDto getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate);

    List<BookingDto> getMyBookings();
}
