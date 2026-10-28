package com.learnershigh.credit.dto;

import java.util.List;

// GET /api/student/credit/required-courses 응답 (api-contract.md 4.3)
public record RequiredCoursesResponse(Integer remainingCount, List<CourseItemResponse> courses) {
}
