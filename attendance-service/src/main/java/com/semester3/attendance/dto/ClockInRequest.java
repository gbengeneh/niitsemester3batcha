package com.semester3.attendance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClockInRequest {

    @NotNull(message = "employeeId is required")
    private Long employeeId;
}
