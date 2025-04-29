package com.booking.bookingservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long parkingLotId;
    private Long parkingSpotId;

    private LocalDateTime bookingTime;
    private LocalDateTime endTime;

    private String status; // ACTIVE, CANCELLED
}
