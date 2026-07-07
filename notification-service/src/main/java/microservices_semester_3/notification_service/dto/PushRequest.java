package microservices_semester_3.notification_service.dto;

import lombok.Data;

@Data
public class PushRequest {
    private String userId;
    private String title;
    private String message;
}