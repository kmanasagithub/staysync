package com.example.makeMyTrip.service;

import com.example.makeMyTrip.dto.HotelDto;
import com.example.makeMyTrip.dto.HotelInfoDto;
import com.example.makeMyTrip.entity.Hotel;

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
