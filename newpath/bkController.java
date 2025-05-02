package com.booking.controller;

import com.booking.entity.Booking;
import com.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
@Tag(name = "Booking Controller", description = "APIs for managing parking spot bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Create a new booking")
    public Booking createBooking(
            @RequestBody @Valid Booking booking) {
        // Remove header requirement, get user ID from service layer
        return bookingService.createBooking(booking);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all bookings for a user")
    public List<Booking> getUserBookings(@PathVariable Long userId) {
        return bookingService.getBookingsByUser(userId);
    }

    @PutMapping("/{bookingId}/cancel/{userId}")
    @Operation(summary = "Cancel a booking")
    public Booking cancelBooking(
            @PathVariable Long bookingId,
            @PathVariable Long userId) {
        return bookingService.cancelBooking(bookingId, userId);
    }

    @GetMapping("/{bookingId}/user/{userId}")
    @Operation(summary = "Get booking by ID")
    public Booking getBookingById(
            @PathVariable Long bookingId,
            @PathVariable Long userId) {
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all bookings (Admin only)")
    public List<Booking> getAllBookings() {
        // Move admin check to service layer
        return bookingService.getAllBookings();
    }
}
