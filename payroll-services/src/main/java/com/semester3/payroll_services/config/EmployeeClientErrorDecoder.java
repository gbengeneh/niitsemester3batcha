package com.semester3.payroll_services.config;

import com.semester3.payroll_services.exception.EmployeeNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;

/**
 * Translates raw Feign HTTP error responses from Employee Service
 * into meaningful application exceptions instead of generic FeignExceptions.
 */
public class EmployeeClientErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 404) {
            return new EmployeeNotFoundException(null); // employeeId filled in by caller if needed
        }
        return defaultDecoder.decode(methodKey, response);
    }
}
