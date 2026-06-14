package com.trip.staysync.dto;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelInfoDto {
    private HotelDto hotels;
    private List<RoomDto> rooms;
}
