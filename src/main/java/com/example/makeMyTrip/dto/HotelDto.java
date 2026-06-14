package com.example.makeMyTrip.dto;

import com.example.makeMyTrip.entity.HotelContactInfo;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class HotelDto {
    private Long id;
    private String name;
    private String city;
    private Set<String> photos;
    private Set<String> amenities;
    private Boolean active;
    private HotelContactInfo contactInfo;
}
