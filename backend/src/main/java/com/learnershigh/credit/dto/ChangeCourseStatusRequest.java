package com.learnershigh.credit.dto;

// PATCH /courses/{courseId} 요청 본문 (api-contract.md 4.7)
public record ChangeCourseStatusRequest(String status) {
}
