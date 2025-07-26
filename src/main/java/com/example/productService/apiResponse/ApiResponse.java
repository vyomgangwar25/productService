package com.example.productService.apiResponse;

import java.util.List;

import org.springframework.http.HttpStatus;

import lombok.Data;

@Data
public class ApiResponse {
	private HttpStatus status;
	private String message;
	private List<?> response;

	public ApiResponse() {

	}

	public ApiResponse(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}

}
