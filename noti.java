// payment-notification/src/main/java/com/notification/controller/NotificationController.java
package com.notification.controller;

import com.notification.dto.NotificationRequest;
import com.notification.service.NotificationService;
import jakarta.validation.Valid;
lombok.RequiredArgsConstructor;
lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notify")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<String> sendNotification(@Valid @RequestBody NotificationRequest request) {
        log.debug("Sending notification: {}", request);
        try {
            notificationService.sendEmailNotification(request);
            return ResponseEntity.ok("Notification sent successfully");
        } catch (Exception e) {
            log.error("Error while sending notification", e);
            return ResponseEntity.internalServerError().body("Failed to send notification");
        }
    }
}

// payment-notification/src/main/java/com/notification/dto/NotificationRequest.java
package com.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
lombok.Data;

@Data
public class NotificationRequest {
    @NotBlank
    private String subject;

    @NotBlank
    private String body;

    @NotBlank
    @Email
    private String toEmail;
}

// payment-notification/src/main/java/com/notification/service/NotificationService.java
package com.notification.service;

import com.notification.dto.NotificationRequest;

public interface NotificationService {
    void sendEmailNotification(NotificationRequest request);
}

// payment-notification/src/main/java/com/notification/service/impl/NotificationServiceImpl.java
package com.notification.service.impl;

import com.notification.dto.NotificationRequest;
import com.notification.service.NotificationService;
lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final JavaMailSender mailSender;

    public NotificationServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendEmailNotification(NotificationRequest request) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(request.getToEmail());
            message.setSubject(request.getSubject());
            message.setText(request.getBody());
            mailSender.send(message);
            log.debug("Email sent to {}", request.getToEmail());
        } catch (Exception e) {
            log.error("Failed to send email to {}", request.getToEmail(), e);
            throw new RuntimeException("Email sending failed", e);
        }
    }
}

// payment-notification/src/main/resources/application.yml
spring:
  application:
    name: notification-service
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-email-password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

server:
  port: 8085

// payment-notification/pom.xml (dependencies only)
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-mail</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.1.0</version>
    </dependency>
</dependencies>

// Note: You must configure a valid Gmail or SMTP account in application.yml to actually send emails.
