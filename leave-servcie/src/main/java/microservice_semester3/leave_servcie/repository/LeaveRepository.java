package microservice_semester3.leave_servcie.repository;

import microservice_semester3.leave_servcie.entity.Leave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long> {
    //For  history of employee
    List<Leave> findByEmployeeId(Long employeeId);
}
