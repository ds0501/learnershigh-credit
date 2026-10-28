package com.learnershigh.common.exception;

import java.util.Map;

// 오류 응답 본문. { "message": ..., "fieldErrors": { 필드: 문구 } }
public record ErrorResponse(String message, Map<String, String> fieldErrors) {
}
