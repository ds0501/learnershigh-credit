package com.learnershigh.goal.dto;

// PUT /api/student/goal 요청 본문. targetUniversity는 선택(없거나 공백이면 저장하지 않음)
public record SaveStudentGoalRequest(String departmentId, String targetUniversity) {
}
