package microservices_semester_3.notification_service.dto;

import lombok.Data;

@Data
public class SmsRequest {
    private String phoneNumber;
    private String message;
}
