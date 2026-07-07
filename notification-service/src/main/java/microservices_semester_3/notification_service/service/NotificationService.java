package microservices_semester_3.notification_service.service;


import microservices_semester_3.notification_service.dto.EmailRequest;
import microservices_semester_3.notification_service.dto.PushRequest;
import microservices_semester_3.notification_service.dto.SmsRequest;

public interface NotificationService {
    void sendEmail(EmailRequest request);
    void sendSms(SmsRequest request);
    void sendPush(PushRequest request);
}
