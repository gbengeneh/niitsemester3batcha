package com.semester3.payroll_services.mapper;

import com.semester3.payroll_services.dto.PayrollResponse;
import com.semester3.payroll_services.entity.Payroll;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct generates the implementation of this interface at compile time
 * (look for PayrollMapperImpl.java in target/generated-sources after building).
 */
@Mapper(componentModel = "spring")
public interface PayrollMapper {

    PayrollResponse toResponse(Payroll payroll);

    List<PayrollResponse> toResponseList(List<Payroll> payrolls);
}
