package com.example.makeMyTrip.service;

import com.example.makeMyTrip.dto.*;
import com.example.makeMyTrip.entity.Room;
import org.springframework.data.domain.Page;
import java.util.List;

public interface InventoryService {
    void initializeRoomForAYear(Room room);

    void deleteAllInventories(Room room);

    Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest);

    List<InventoryDto> getAllInventoryByRoom(Long roomId);

    void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto);
}
