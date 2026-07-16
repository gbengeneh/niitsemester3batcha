package com.semester3.payroll_services.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"employee_id", "pay_period"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "basic_salary", precision = 12, scale = 2, nullable = false)
    private BigDecimal basicSalary;

    @Column(precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal allowance = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal bonus = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal tax = BigDecimal.ZERO;

    @Column(name = "net_salary", precision = 12, scale = 2, nullable = false)
    private BigDecimal netSalary;

    @Column(name = "pay_period", nullable = false, length = 7)
    private String payPeriod;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
