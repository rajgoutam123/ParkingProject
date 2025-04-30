// parking-service/src/main/java/com/parking/parkingservice/ParkingServiceApplication.java
package com.parking.parkingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ParkingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ParkingServiceApplication.class, args);
    }
}

// parking-service/src/main/java/com/parking/parkingservice/entity/ParkingLot.java
package com.parking.parkingservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParkingLot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Parking lot name is required")
    private String name;

    @NotBlank(message = "Parking lot address is required")
    private String address;

    @OneToMany(mappedBy = "parkingLot", cascade = CascadeType.ALL)
    private List<ParkingSpot> spots;
}

// parking-service/src/main/java/com/parking/parkingservice/entity/ParkingSpot.java
package com.parking.parkingservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParkingSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Spot number is required")
    private String spotNumber;

    private boolean available;

    @ManyToOne
    @JoinColumn(name = "parking_lot_id")
    private ParkingLot parkingLot;
}

// parking-service/src/main/java/com/parking/parkingservice/repository/ParkingLotRepository.java
package com.parking.parkingservice.repository;

import com.parking.parkingservice.entity.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {
}

// parking-service/src/main/java/com/parking/parkingservice/repository/ParkingSpotRepository.java
package com.parking.parkingservice.repository;

import com.parking.parkingservice.entity.ParkingSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {
    List<ParkingSpot> findByParkingLotIdAndAvailableTrue(Long parkingLotId);
}

// parking-service/src/main/java/com/parking/parkingservice/service/ParkingService.java
package com.parking.parkingservice.service;

import com.parking.parkingservice.entity.ParkingLot;
import com.parking.parkingservice.entity.ParkingSpot;
import com.parking.parkingservice.repository.ParkingLotRepository;
import com.parking.parkingservice.repository.ParkingSpotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParkingService {

    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSpotRepository parkingSpotRepository;

    public ParkingLot createParkingLot(ParkingLot parkingLot) {
        ParkingLot saved = parkingLotRepository.save(parkingLot);
        log.info("Created parking lot: {}", saved);
        return saved;
    }

    public ParkingSpot addParkingSpot(Long parkingLotId, ParkingSpot spot) {
        ParkingLot lot = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() -> new RuntimeException("Parking Lot not found"));
        spot.setParkingLot(lot);
        ParkingSpot saved = parkingSpotRepository.save(spot);
        log.info("Added parking spot: {} to lot: {}", saved, lot.getId());
        return saved;
    }

    public List<ParkingSpot> getAvailableSpots(Long parkingLotId) {
        List<ParkingSpot> spots = parkingSpotRepository.findByParkingLotIdAndAvailableTrue(parkingLotId);
        log.info("Available spots in lot {}: {}", parkingLotId, spots.size());
        return spots;
    }

    public ParkingSpot updateSpotAvailability(Long spotId, boolean available) {
        ParkingSpot spot = parkingSpotRepository.findById(spotId)
                .orElseThrow(() -> new RuntimeException("Spot not found"));
        spot.setAvailable(available);
        ParkingSpot saved = parkingSpotRepository.save(spot);
        log.info("Updated availability for spot {}: {}", spotId, available);
        return saved;
    }
}

// parking-service/src/main/java/com/parking/parkingservice/controller/ParkingController.java
package com.parking.parkingservice.controller;

import com.parking.parkingservice.entity.ParkingLot;
import com.parking.parkingservice.entity.ParkingSpot;
import com.parking.parkingservice.service.ParkingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parking")
@RequiredArgsConstructor
public class ParkingController {

    private final ParkingService parkingService;

    @PostMapping("/lot")
    public ParkingLot createParkingLot(@RequestBody @Valid ParkingLot parkingLot) {
        return parkingService.createParkingLot(parkingLot);
    }

    @PostMapping("/lot/{lotId}/spot")
    public ParkingSpot addParkingSpot(@PathVariable Long lotId, @RequestBody @Valid ParkingSpot parkingSpot) {
        return parkingService.addParkingSpot(lotId, parkingSpot);
    }

    @GetMapping("/lot/{lotId}/spots")
    public List<ParkingSpot> getAvailableSpots(@PathVariable Long lotId) {
        return parkingService.getAvailableSpots(lotId);
    }

    @PutMapping("/spot/{spotId}/availability")
    public ParkingSpot updateSpotAvailability(@PathVariable Long spotId, @RequestParam boolean available) {
        return parkingService.updateSpotAvailability(spotId, available);
    }
}

// parking-service/src/main/java/com/parking/parkingservice/exception/GlobalExceptionHandler.java
package com.parking.parkingservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}
