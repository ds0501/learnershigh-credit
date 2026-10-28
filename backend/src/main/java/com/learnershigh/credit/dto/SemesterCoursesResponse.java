package com.learnershigh.credit.dto;

import java.util.List;

// GET /api/student/credit/courses?gradeYear=&term= 응답 (api-contract.md 4.2)
public record SemesterCoursesResponse(Integer gradeYear, Integer term, Integer totalCredits, List<CourseItemResponse> courses) {
}
