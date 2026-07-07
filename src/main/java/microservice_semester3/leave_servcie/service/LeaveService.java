package microservice_semester3.leave_servcie.service;


import microservice_semester3.leave_servcie.dto.LeaveRequest;
import microservice_semester3.leave_servcie.dto.LeaveResponse;
import microservice_semester3.leave_servcie.entity.Leave;

import java.util.List;

public interface LeaveService {
    Leave createLeave(LeaveRequest request);
    LeaveResponse getLeaveById(Long id);
    List<Leave> getLeavesByEmployeeId(Long employeeId);
    Leave updateLeaveStatus(Long id, String status);
}
