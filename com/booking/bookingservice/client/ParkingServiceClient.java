package com.booking.bookingservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "parking-service")
public interface ParkingServiceClient {

    @PutMapping("/api/parking/spot/{spotId}/availability")
    void updateSpotAvailability(@PathVariable("spotId") Long spotId, @RequestParam("available") boolean available);
}
