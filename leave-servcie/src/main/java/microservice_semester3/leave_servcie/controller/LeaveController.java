package microservice_semester3.leave_servcie.controller;

// REMOVED: import lombok.RequiredArgsConstructor;
import microservice_semester3.leave_servcie.dto.LeaveRequest;
import microservice_semester3.leave_servcie.dto.LeaveResponse;
import microservice_semester3.leave_servcie.entity.Leave;
import microservice_semester3.leave_servcie.service.LeaveService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leaves")
public class LeaveController {

    private final LeaveService leaveService;

    // Fixed: Manual construction injection for native Spring compatibility
    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @PostMapping
    public ResponseEntity<Leave> applyForLeave(@RequestBody LeaveRequest request) {
        Leave createdLeave = leaveService.createLeave(request);
        return new ResponseEntity<>(createdLeave, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveResponse> getLeaveDetails(@PathVariable Long id) {
        LeaveResponse response = leaveService.getLeaveById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<Leave>> getLeavesByEmployee(@PathVariable Long employeeId) {
        List<Leave> leaves = leaveService.getLeavesByEmployeeId(employeeId);
        return ResponseEntity.ok(leaves);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<Leave> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        Leave updatedLeave = leaveService.updateLeaveStatus(id, status);
        return ResponseEntity.ok(updatedLeave);
    }
}
