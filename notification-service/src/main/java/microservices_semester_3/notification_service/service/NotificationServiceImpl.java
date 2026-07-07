package microservices_semester_3.notification_service.service;

import lombok.extern.slf4j.Slf4j;
import microservices_semester_3.notification_service.dto.EmailRequest;
import microservices_semester_3.notification_service.dto.PushRequest;
import microservices_semester_3.notification_service.dto.SmsRequest;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Override
    public void sendEmail(EmailRequest request) {
        log.info("📧 SIMULATING EMAIL DELIVERY");
        log.info("To: {}", request.getTo());
        log.info("Subject: {}", request.getSubject());
        log.info("Body: {}", request.getBody());
        log.info("------------------------------------------------");
    }

    @Override
    public void sendSms(SmsRequest request) {
        log.info("📱 SIMULATING SMS DELIVERY");
        log.info("Phone: {}", request.getPhoneNumber());
        log.info("Message: {}", request.getMessage());
        log.info("------------------------------------------------");
    }

    @Override
    public void sendPush(PushRequest request) {
        log.info("🔔 SIMULATING PUSH NOTIFICATION");
        log.info("User ID: {}", request.getUserId());
        log.info("Title: {}", request.getTitle());
        log.info("Message: {}", request.getMessage());
        log.info("------------------------------------------------");
    }
}
