package com.trip.staysync.service;

import com.trip.staysync.dto.*;
import com.trip.staysync.entity.Room;
import org.springframework.data.domain.Page;
import java.util.List;

public interface InventoryService {
    void initializeRoomForAYear(Room room);

    void deleteAllInventories(Room room);

    Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest);

    List<InventoryDto> getAllInventoryByRoom(Long roomId);

    void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto);
}
