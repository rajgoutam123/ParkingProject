// BookingController.java
package com.bookingservice.controller;

import com.bookingservice.entity.Booking;
import com.bookingservice.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<Booking> createBooking(@Valid @RequestBody Booking booking,
                                                 @RequestHeader("X-User-Id") String userId) {
        log.debug("Creating booking for user: {}", userId);
        booking.setUserId(userId);
        return ResponseEntity.ok(bookingService.createBooking(booking));
    }

    @GetMapping("/user")
    public ResponseEntity<List<Booking>> getBookingsByUserId(@RequestHeader("X-User-Id") String userId) {
        log.debug("Fetching bookings for user: {}", userId);
        return ResponseEntity.ok(bookingService.getBookingsByUserId(userId));
    }

    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        log.debug("Fetching all bookings");
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
        log.debug("Fetching booking by id: {}", id);
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Booking> updateBooking(@PathVariable Long id,
                                                 @Valid @RequestBody Booking booking,
                                                 @RequestHeader("X-User-Id") String userId) {
        log.debug("Updating booking id: {} by user: {}", id, userId);
        return ResponseEntity.ok(bookingService.updateBooking(id, booking, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id,
                                              @RequestHeader("X-User-Id") String userId) {
        log.debug("Deleting booking id: {} by user: {}", id, userId);
        bookingService.deleteBooking(id, userId);
        return ResponseEntity.ok().build();
    }
}

// BookingService.java
package com.bookingservice.service;

import com.bookingservice.entity.Booking;
import java.util.List;

public interface BookingService {
    Booking createBooking(Booking booking);
    List<Booking> getBookingsByUserId(String userId);
    List<Booking> getAllBookings();
    Booking getBookingById(Long id);
    Booking updateBooking(Long id, Booking booking, String userId);
    void deleteBooking(Long id, String userId);
}

// BookingServiceImpl.java
package com.bookingservice.service.impl;

import com.bookingservice.entity.Booking;
import com.bookingservice.exception.BookingNotFoundException;
import com.bookingservice.repository.BookingRepository;
import com.bookingservice.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    @Override
    public Booking createBooking(Booking booking) {
        try {
            log.debug("Saving booking: {}", booking);
            return bookingRepository.save(booking);
        } catch (Exception e) {
            log.error("Error creating booking: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Booking> getBookingsByUserId(String userId) {
        try {
            return bookingRepository.findByUserId(userId);
        } catch (Exception e) {
            log.error("Error fetching bookings for user {}: {}", userId, e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Booking> getAllBookings() {
        try {
            return bookingRepository.findAll();
        } catch (Exception e) {
            log.error("Error fetching all bookings: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Booking getBookingById(Long id) {
        try {
            return bookingRepository.findById(id).orElseThrow(() ->
                new BookingNotFoundException("Booking not found with id: " + id));
        } catch (Exception e) {
            log.error("Error fetching booking by id {}: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    public Booking updateBooking(Long id, Booking booking, String userId) {
        try {
            Booking existing = bookingRepository.findById(id).orElseThrow(() ->
                new BookingNotFoundException("Booking not found with id: " + id));
            if (!existing.getUserId().equals(userId)) {
                throw new RuntimeException("Unauthorized to update this booking");
            }
            existing.setDetails(booking.getDetails());
            return bookingRepository.save(existing);
        } catch (Exception e) {
            log.error("Error updating booking id {}: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    public void deleteBooking(Long id, String userId) {
        try {
            Booking booking = bookingRepository.findById(id).orElseThrow(() ->
                new BookingNotFoundException("Booking not found with id: " + id));
            if (!booking.getUserId().equals(userId)) {
                throw new RuntimeException("Unauthorized to delete this booking");
            }
            bookingRepository.delete(booking);
        } catch (Exception e) {
            log.error("Error deleting booking id {}: {}", id, e.getMessage());
            throw e;
        }
    }
}
