// ParkingServiceApplication.java
package com.parking.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ParkingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ParkingServiceApplication.class, args);
    }
}

// controller/ParkingLotController.java
package com.parking.service.controller;

import com.parking.service.model.ParkingLot;
import com.parking.service.service.ParkingLotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parking/lot")
@RequiredArgsConstructor
@Slf4j
public class ParkingLotController {

    private final ParkingLotService parkingLotService;

    @PostMapping
    public ResponseEntity<ParkingLot> createLot(@RequestHeader("X-User-Id") String userId,
                                                @Valid @RequestBody ParkingLot lot) {
        log.debug("Creating parking lot by user: {}", userId);
        return ResponseEntity.ok(parkingLotService.createParkingLot(lot));
    }

    @GetMapping
    public ResponseEntity<List<ParkingLot>> getAllLots(@RequestHeader("X-User-Id") String userId) {
        log.debug("Fetching all parking lots for user: {}", userId);
        return ResponseEntity.ok(parkingLotService.getAllParkingLots());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLot(@RequestHeader("X-User-Id") String userId,
                                          @PathVariable Long id) {
        log.debug("Deleting parking lot with ID {} by user: {}", id, userId);
        parkingLotService.deleteParkingLot(id);
        return ResponseEntity.ok().build();
    }
}

// model/ParkingLot.java
package com.parking.service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingLot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Location is required")
    private String location;
}

// repository/ParkingLotRepository.java
package com.parking.service.repository;

import com.parking.service.model.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {
}

// service/ParkingLotService.java
package com.parking.service.service;

import com.parking.service.model.ParkingLot;

import java.util.List;

public interface ParkingLotService {
    ParkingLot createParkingLot(ParkingLot lot);
    List<ParkingLot> getAllParkingLots();
    void deleteParkingLot(Long id);
}

// service/impl/ParkingLotServiceImpl.java
package com.parking.service.service.impl;

import com.parking.service.model.ParkingLot;
import com.parking.service.repository.ParkingLotRepository;
import com.parking.service.service.ParkingLotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParkingLotServiceImpl implements ParkingLotService {

    private final ParkingLotRepository lotRepository;

    @Override
    public ParkingLot createParkingLot(ParkingLot lot) {
        try {
            log.debug("Saving parking lot: {}", lot);
            return lotRepository.save(lot);
        } catch (Exception e) {
            log.error("Failed to save parking lot", e);
            throw new RuntimeException("Error saving parking lot");
        }
    }

    @Override
    public List<ParkingLot> getAllParkingLots() {
        try {
            log.debug("Fetching all parking lots");
            return lotRepository.findAll();
        } catch (Exception e) {
            log.error("Failed to fetch parking lots", e);
            throw new RuntimeException("Error fetching lots");
        }
    }

    @Override
    public void deleteParkingLot(Long id) {
        try {
            log.debug("Deleting parking lot with ID: {}", id);
            lotRepository.deleteById(id);
        } catch (Exception e) {
            log.error("Failed to delete parking lot", e);
            throw new RuntimeException("Error deleting parking lot");
        }
    }
}

// resources/application.yml
spring:
  application:
    name: parking-service
  datasource:
    url: jdbc:mysql://localhost:3306/parking_service
    username: root
    password: root
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

server:
  port: 8082

springdoc:
  swagger-ui:
    path: /swagger-ui.html
    display-request-duration: true
