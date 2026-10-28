package com.learnershigh.common.exception;

import java.util.Map;

// API 오류 응답({ message, fieldErrors })의 기반 예외. ValidationException(400), NotFoundException(404)가 상속한다.
public abstract class ApiException extends RuntimeException {

	private final Map<String, String> fieldErrors;

	protected ApiException(String message, Map<String, String> fieldErrors) {
		super(message);
		this.fieldErrors = fieldErrors;
	}

	public Map<String, String> getFieldErrors() {
		return fieldErrors;
	}

}
