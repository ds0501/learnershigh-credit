package com.learnershigh.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 모든 컨트롤러의 오류 응답을 { message, fieldErrors } 모양으로 통일한다.
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ValidationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleValidation(ValidationException exception) {
		return new ErrorResponse(exception.getMessage(), exception.getFieldErrors());
	}

	@ExceptionHandler(NotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponse handleNotFound(NotFoundException exception) {
		return new ErrorResponse(exception.getMessage(), exception.getFieldErrors());
	}

}
