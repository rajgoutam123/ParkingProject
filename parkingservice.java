// parking-service/src/main/java/com/parking/ParkingServiceApplication.java
package com.parking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
public class ParkingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ParkingServiceApplication.class, args);
    }
}

// Validation for ParkingLot.java
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

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotBlank(message = "Address cannot be blank")
    private String address;

    @OneToMany(mappedBy = "parkingLot", cascade = CascadeType.ALL)
    private List<ParkingSpot> spots;
}

// Validation for ParkingSpot.java
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

    @NotBlank(message = "Spot number cannot be blank")
    private String spotNumber;

    private boolean available;

    @ManyToOne
    @JoinColumn(name = "parking_lot_id")
    private ParkingLot parkingLot;
}

// Global Exception Handler
package com.parking.parkingservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}

// Swagger Configuration
package com.parking.parkingservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info().title("Parking Service API")
                .description("API for managing parking lots and spots")
                .version("v1"));
    }
}

// Unit Test for ParkingService
package com.parking.parkingservice.service;

import com.parking.parkingservice.entity.ParkingLot;
import com.parking.parkingservice.repository.ParkingLotRepository;
import com.parking.parkingservice.repository.ParkingSpotRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

public class ParkingServiceTest {

    @Mock
    ParkingLotRepository lotRepo;

    @Mock
    ParkingSpotRepository spotRepo;

    @InjectMocks
    ParkingService parkingService;

    public ParkingServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createParkingLot_success() {
        ParkingLot lot = ParkingLot.builder().name("Lot A").address("Main St").build();
        when(lotRepo.save(any(ParkingLot.class))).thenReturn(lot);
        ParkingLot created = parkingService.createParkingLot(lot);
        assertEquals("Lot A", created.getName());
    }
}

// Updated application.yml
server:
  port: 8082

spring:
  application:
    name: parking-service

  datasource:
    url: jdbc:mysql://localhost:3306/parking_service_db
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

logging:
  level:
    com.parking: DEBUG

# Enable Eureka
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
