package com.learnershigh.common.exception;

import java.util.Map;

// 대상을 찾지 못함. 404로 응답하고 fieldErrors는 항상 빈 객체다.
public class NotFoundException extends ApiException {

	public NotFoundException(String message) {
		super(message, Map.of());
	}

}
