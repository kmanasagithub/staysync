package com.trip.staysync.service;

import com.trip.staysync.dto.HotelDto;
import com.trip.staysync.dto.HotelInfoDto;
import com.trip.staysync.entity.Hotel;

import java.util.List;

public interface HotelService {
    HotelDto createNewHotel(HotelDto hotelDto);

    HotelDto getHotelById(Long id);

    HotelDto updateHotelById(Long id,HotelDto hotelDto);

    void deleteHotelById(Long id);

    void activeHotel(Long hotelId);

    HotelInfoDto getHotelInfoById(long hotelId);

    List<HotelDto> getAllHotels();
}
