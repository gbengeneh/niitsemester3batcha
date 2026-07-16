package microservice_semester3.leave_servcie.service;

import lombok.RequiredArgsConstructor;
// REMOVED: import lombok.Value;
import microservice_semester3.leave_servcie.entity.LeaveType;

import microservice_semester3.leave_servcie.dto.EmployeeDto;
import microservice_semester3.leave_servcie.dto.LeaveRequest;
import microservice_semester3.leave_servcie.dto.LeaveResponse;
import microservice_semester3.leave_servcie.entity.Leave;
import microservice_semester3.leave_servcie.entity.LeaveStatus;
import microservice_semester3.leave_servcie.exception.ResourceNotFoundException;
import microservice_semester3.leave_servcie.repository.LeaveRepository;
import org.springframework.beans.factory.annotation.Value; // ADDED: Correct Spring import
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRepository leaveRepository;
    private final WebClient.Builder webClientBuilder;

    // Fixed: Standard String fields injected properly by Spring
    @Value("${services.employee-service.url}")
    private String employeeServiceUrl;

    @Value("${services.notification-service.url}")
    private String notificationServiceUrl;

    @Override
    public Leave createLeave(LeaveRequest request) {
        // Plain, pure Java initialization—zero background magic required
        Leave leave = new Leave();
        leave.setEmployeeId(request.getEmployeeId());
        leave.setLeaveType(request.getLeaveType());
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setReason(request.getReason());
        leave.setStatus(LeaveStatus.PENDING); // Default state

        return leaveRepository.save(leave);
    }

    @Override
    public LeaveResponse getLeaveById(Long id) {
        Leave leave = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave record not found with id: " + id));

        EmployeeDto employee = webClientBuilder.build()
                .get()
                .uri(employeeServiceUrl + "/api/v1/employees/" + leave.getEmployeeId())
                .headers(this::forwardAuthorization)
                .retrieve()
                .bodyToMono(EmployeeDto.class)
                .block();

        LeaveResponse response = new LeaveResponse();
        response.setId(leave.getId());
        response.setEmployeeId(leave.getEmployeeId());
        response.setLeaveType(leave.getLeaveType());
        response.setStartDate(leave.getStartDate());
        response.setEndDate(leave.getEndDate());
        response.setStatus(leave.getStatus());
        response.setReason(leave.getReason());
        response.setEmployee(employee);
        return response;
    }

    @Override
    public List<Leave> getLeavesByEmployeeId(Long employeeId) {
        return leaveRepository.findByEmployeeId(employeeId);
    }

    @Override
    public Leave updateLeaveStatus(Long id, String status) {
        Leave leave = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave record not found with id: " + id));

        leave.setStatus(LeaveStatus.valueOf(status.toUpperCase()));
        Leave updatedLeave = leaveRepository.save(leave);

        try {
            webClientBuilder.build()
                    .post()
                    .uri(notificationServiceUrl + "/api/v1/notifications/email")
                    .bodyValue(Map.of(
                            "to", "employee_" + leave.getEmployeeId() + "@company.com",
                            "subject", "Leave Status Update",
                            "body", "Your leave request has been " + leave.getStatus()
                    ))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .subscribe();
        } catch (Exception e) {
            System.err.println("Failed to trigger alert: " + e.getMessage());
        }

        return updatedLeave;
    }

    // employee-service now requires a valid JWT on every call, so the token on
    // the incoming request is forwarded on the outbound call. There is no
    // gateway in this system to do this centrally, so each caller does it.
    private void forwardAuthorization(org.springframework.http.HttpHeaders headers) {
        Object requestAttrs = RequestContextHolder.getRequestAttributes();
        if (requestAttrs instanceof ServletRequestAttributes servletAttrs) {
            String authHeader = servletAttrs.getRequest().getHeader("Authorization");
            if (authHeader != null) {
                headers.set("Authorization", authHeader);
            }
        }
    }
}