package com.learnershigh.record.dto;

// POST /api/student/record/guidelines 요청 본문. 초안이 아직 없는 카드에서 방향 추천을 받을 때 쓴다(DB에 아무것도 만들지 않음)
public record GuidelinePreviewRequest(String semester, String subjectName, String courseId) {
}
