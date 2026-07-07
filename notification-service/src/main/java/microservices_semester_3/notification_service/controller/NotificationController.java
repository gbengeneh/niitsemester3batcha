package microservices_semester_3.notification_service.controller;

import lombok.RequiredArgsConstructor;
import microservices_semester_3.notification_service.dto.EmailRequest;
import microservices_semester_3.notification_service.dto.PushRequest;
import microservices_semester_3.notification_service.dto.SmsRequest;
import microservices_semester_3.notification_service.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/email")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequest request) {
        notificationService.sendEmail(request);
        return ResponseEntity.ok("Mock email logged successfully.");
    }

    @PostMapping("/sms")
    public ResponseEntity<String> sendSms(@RequestBody SmsRequest request) {
        notificationService.sendSms(request);
        return ResponseEntity.ok("Mock SMS logged successfully.");
    }

    @PostMapping("/push")
    public ResponseEntity<String> sendPush(@RequestBody PushRequest request) {
        notificationService.sendPush(request);
        return ResponseEntity.ok("Mock push notification logged successfully.");
    }
}
