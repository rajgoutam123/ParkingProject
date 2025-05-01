// AdminServiceApplication.java
package com.adminservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AdminServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminServiceApplication.class, args);
    }
}

// AdminController.java
package com.adminservice.controller;

import com.adminservice.dto.AdminRequest;
import com.adminservice.dto.AdminResponse;
import com.adminservice.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminService adminService;

    @PostMapping
    public ResponseEntity<AdminResponse> createAdmin(@Valid @RequestBody AdminRequest request, @RequestHeader("X-User-Id") String userId) {
        log.debug("Creating admin user by: {}", userId);
        return ResponseEntity.ok(adminService.createAdmin(request, userId));
    }

    @GetMapping
    public ResponseEntity<List<AdminResponse>> getAllAdmins(@RequestHeader("X-User-Id") String userId) {
        log.debug("Fetching all admins requested by: {}", userId);
        return ResponseEntity.ok(adminService.getAllAdmins(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminResponse> getAdminById(@PathVariable Long id, @RequestHeader("X-User-Id") String userId) {
        log.debug("Fetching admin with id: {} by: {}", id, userId);
        return ResponseEntity.ok(adminService.getAdminById(id, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminResponse> updateAdmin(@PathVariable Long id, @Valid @RequestBody AdminRequest request, @RequestHeader("X-User-Id") String userId) {
        log.debug("Updating admin with id: {} by: {}", id, userId);
        return ResponseEntity.ok(adminService.updateAdmin(id, request, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable Long id, @RequestHeader("X-User-Id") String userId) {
        log.debug("Deleting admin with id: {} by: {}", id, userId);
        adminService.deleteAdmin(id, userId);
        return ResponseEntity.noContent().build();
    }
}

// AdminService.java
package com.adminservice.service;

import com.adminservice.dto.AdminRequest;
import com.adminservice.dto.AdminResponse;

import java.util.List;

public interface AdminService {
    AdminResponse createAdmin(AdminRequest request, String userId);
    List<AdminResponse> getAllAdmins(String userId);
    AdminResponse getAdminById(Long id, String userId);
    AdminResponse updateAdmin(Long id, AdminRequest request, String userId);
    void deleteAdmin(Long id, String userId);
}

// AdminServiceImpl.java
package com.adminservice.service.impl;

import com.adminservice.dto.AdminRequest;
import com.adminservice.dto.AdminResponse;
import com.adminservice.entity.Admin;
import com.adminservice.repository.AdminRepository;
import com.adminservice.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;

    @Override
    public AdminResponse createAdmin(AdminRequest request, String userId) {
        try {
            Admin admin = new Admin();
            admin.setName(request.getName());
            admin.setEmail(request.getEmail());
            return new AdminResponse(adminRepository.save(admin));
        } catch (Exception e) {
            log.error("Error creating admin: {}", e.getMessage());
            throw new RuntimeException("Error creating admin");
        }
    }

    @Override
    public List<AdminResponse> getAllAdmins(String userId) {
        try {
            return adminRepository.findAll().stream().map(AdminResponse::new).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching all admins: {}", e.getMessage());
            throw new RuntimeException("Error fetching all admins");
        }
    }

    @Override
    public AdminResponse getAdminById(Long id, String userId) {
        try {
            Admin admin = adminRepository.findById(id).orElseThrow(() -> new RuntimeException("Admin not found"));
            return new AdminResponse(admin);
        } catch (Exception e) {
            log.error("Error fetching admin by id {}: {}", id, e.getMessage());
            throw new RuntimeException("Error fetching admin");
        }
    }

    @Override
    public AdminResponse updateAdmin(Long id, AdminRequest request, String userId) {
        try {
            Admin admin = adminRepository.findById(id).orElseThrow(() -> new RuntimeException("Admin not found"));
            admin.setName(request.getName());
            admin.setEmail(request.getEmail());
            return new AdminResponse(adminRepository.save(admin));
        } catch (Exception e) {
            log.error("Error updating admin {}: {}", id, e.getMessage());
            throw new RuntimeException("Error updating admin");
        }
    }

    @Override
    public void deleteAdmin(Long id, String userId) {
        try {
            adminRepository.deleteById(id);
        } catch (Exception e) {
            log.error("Error deleting admin {}: {}", id, e.getMessage());
            throw new RuntimeException("Error deleting admin");
        }
    }
}

// Admin.java (Entity)
package com.adminservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;
}

// AdminRequest.java
package com.adminservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminRequest {
    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;
}

// AdminResponse.java
package com.adminservice.dto;

import com.adminservice.entity.Admin;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminResponse {
    private Long id;
    private String name;
    private String email;

    public AdminResponse(Admin admin) {
        this.id = admin.getId();
        this.name = admin.getName();
        this.email = admin.getEmail();
    }
}

// AdminRepository.java
package com.adminservice.repository;

import com.adminservice.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {
}

// application.yml
spring:
  application:
    name: admin-service
  datasource:
    url: jdbc:mysql://localhost:3306/admin_service
    username: root
    password: password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

server:
  port: 8084

springdoc:
  swagger-ui:
    path: /swagger-ui.html
