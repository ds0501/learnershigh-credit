package com.learnershigh.record.dto;

// PUT /api/student/record/drafts/{id}/guideline 요청 본문. 채택한 방향을 guideline_title, guideline_detail에 저장한다
public record AdoptGuidelineRequest(String title, String detail) {
}
