package com.semester3.payroll_services;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PayrollServicesApplication {

	public static void main(String[] args) {
		SpringApplication.run(PayrollServicesApplication.class, args);
	}

}