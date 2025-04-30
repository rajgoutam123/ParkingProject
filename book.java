// booking-service/src/main/java/com/booking/BookingServiceApplication.java
package com.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BookingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookingServiceApplication.class, args);
    }
}

// booking-service/src/main/java/com/booking/entity/Booking.java
package com.booking.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    private Long userId;

    @NotNull
    private Long parkingLotId;

    @NotNull
    private Long parkingSpotId;

    private LocalDateTime bookingTime;
    private LocalDateTime endTime;

    @NotNull
    private String status; // ACTIVE, CANCELLED
}

// booking-service/src/main/java/com/booking/repository/BookingRepository.java
package com.booking.repository;

import com.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
}

// booking-service/src/main/java/com/booking/client/ParkingServiceClient.java
package com.booking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "parking-service", url = "http://localhost:8082")
public interface ParkingServiceClient {

    @PutMapping("/api/parking/spot/{spotId}/availability")
    void updateSpotAvailability(@PathVariable("spotId") Long spotId, @RequestParam("available") boolean available);
}

// booking-service/src/main/java/com/booking/service/BookingService.java
package com.booking.service;

import com.booking.client.ParkingServiceClient;
import com.booking.entity.Booking;
import com.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ParkingServiceClient parkingServiceClient;

    public Booking createBooking(Booking booking) {
        booking.setBookingTime(LocalDateTime.now());
        booking.setStatus("ACTIVE");
        parkingServiceClient.updateSpotAvailability(booking.getParkingSpotId(), false);
        Booking saved = bookingRepository.save(booking);
        log.info("Created booking: {}", saved);
        return saved;
    }

    public List<Booking> getBookingsByUser(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserId(userId);
        log.info("Fetched bookings for user {}: {}", userId, bookings);
        return bookings;
    }

    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus("CANCELLED");
        parkingServiceClient.updateSpotAvailability(booking.getParkingSpotId(), true);
        Booking saved = bookingRepository.save(booking);
        log.info("Cancelled booking: {}", saved);
        return saved;
    }
}

// booking-service/src/main/java/com/booking/controller/BookingController.java
package com.booking.controller;

import com.booking.entity.Booking;
import com.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public Booking createBooking(@RequestBody @Valid Booking booking) {
        return bookingService.createBooking(booking);
    }

    @GetMapping("/user/{userId}")
    public List<Booking> getUserBookings(@PathVariable Long userId) {
        return bookingService.getBookingsByUser(userId);
    }

    @PutMapping("/{bookingId}/cancel")
    public Booking cancelBooking(@PathVariable Long bookingId) {
        return bookingService.cancelBooking(bookingId);
    }
}

// booking-service/src/main/java/com/booking/exception/GlobalExceptionHandler.java
package com.booking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}

// booking-service/src/main/resources/application.yml
server:
  port: 8083

spring:
  application:
    name: booking-service

  datasource:
    url: jdbc:mysql://localhost:3306/booking_service_db
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

logging:
  level:
    com.booking: DEBUG
