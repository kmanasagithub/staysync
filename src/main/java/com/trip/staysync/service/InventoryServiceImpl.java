package com.trip.staysync.service;

import com.trip.staysync.dto.*;
import com.trip.staysync.entity.Hotel;
import com.trip.staysync.entity.Inventory;
import com.trip.staysync.entity.Room;
import com.trip.staysync.entity.User;
import com.trip.staysync.exception.ResouceNotFoundException;
import com.trip.staysync.repository.HotelMinPriceRepository;
import com.trip.staysync.repository.InventoryRepository;
import com.trip.staysync.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.trip.staysync.util.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService{

    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final RoomRepository roomRepository;

    @Override
    public void initializeRoomForAYear(Room room) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusYears(1);

        for(;!today.isAfter(endDate);today = today.plusDays(1)) {
            Inventory inventory = Inventory.builder()
                    .hotel(room.getHotel())
                    .room(room)
                    .bookedCount(0)
                    .reservedCount(0)
                    .city(room.getHotel().getCity())
                    .date(today)
                    .price(room.getBasePrice())
                    .surgeFactor(BigDecimal.ONE)
                    .totalCount(room.getTotalCount())
                    .closed(false)
                    .build();

            inventoryRepository.save(inventory);
        }
    }

    @Override
    public void deleteAllInventories(Room room) {
        log.info("Deleting the inventories of room with id: {}",room.getId());
        inventoryRepository.deleteByRoom(room);
    }

    @Override
    public Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest) {
        Pageable pageable = PageRequest.of(hotelSearchRequest.getPage(), hotelSearchRequest.getSize());

        log.info("Searching hotels for {} vity, form P{ startDate to endDate", hotelSearchRequest.getCity(),hotelSearchRequest.getStartDate(),hotelSearchRequest.getEndDate());
        long dateCount = ChronoUnit.DAYS.between(hotelSearchRequest.getStartDate(),hotelSearchRequest.getEndDate())+1;


        Page<HotelPriceDto> hotelPage = hotelMinPriceRepository.findHotelsWithAvailableInventory(hotelSearchRequest.getCity(),hotelSearchRequest.getStartDate(),
        hotelSearchRequest.getEndDate(),hotelSearchRequest.getRoomsCount(),dateCount,pageable);
        return hotelPage;
    }

    @Override
    public List<InventoryDto> getAllInventoryByRoom(Long roomId) {
        log.info("Geting All inventory by room for room with id : {} "+roomId);

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResouceNotFoundException("Room not Found with id : "+roomId));
        User user = getCurrentUser();
        if(!user.equals(room.getHotel().getOwner())){
            throw new AccessDeniedException("YOu are not the owner of the room with ID : "+roomId);
        }

        return inventoryRepository.findByRoomOrderByDate(room)
                .stream()
                .map((element) -> modelMapper.map(element,InventoryDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto) {
        log.info("Updating All inventory by room for room with id : {} between date range: {} - {}  "+roomId
        ,updateInventoryRequestDto.getStartDate(),updateInventoryRequestDto.getEndDate());

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResouceNotFoundException("Room not Found with id : "+roomId));
        User user = getCurrentUser();
        if(!user.equals(room.getHotel().getOwner())){
            throw new AccessDeniedException("YOu are not the owner of the room with ID : "+roomId);
        }

        inventoryRepository.getInventoryAndLockedForUpdate(roomId,updateInventoryRequestDto.getStartDate(),
                updateInventoryRequestDto.getEndDate());

        inventoryRepository.updateInventory(roomId,updateInventoryRequestDto.getStartDate()
        ,updateInventoryRequestDto.getEndDate(),updateInventoryRequestDto.getClosed(),updateInventoryRequestDto.getSurgeFactor());


    }
}
