package com.learnershigh.common.exception;

import java.util.Map;

// 요청 검증 실패. 400으로 응답하고 필드별 오류 문구를 담는다.
public class ValidationException extends ApiException {

	public ValidationException(String message, Map<String, String> fieldErrors) {
		super(message, fieldErrors);
	}

}
