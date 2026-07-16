package com.semester3.attendance.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ClockOutRequest {
    // Reserved for future fields (e.g. location, notes). Currently the
    // employeeId + today's date is enough to find the open attendance record.
}