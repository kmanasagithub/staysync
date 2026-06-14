package com.example.makeMyTrip.dto;

import com.example.makeMyTrip.entity.Hotel;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class RoomDto {
    private Long id;
    private String type;
    private BigDecimal basePrice;
    private Integer totalCount;
    private Integer capacity;
    private Set<String> photos;
    private Set<String> amenities;
}
