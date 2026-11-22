package com.learnershigh.record.dto;

// PUT /api/student/record/drafts/{id} 요청 본문. 보낸 값으로 통째로 바꾼다(없는 칸은 빈 문자열)
public record SaveRecordDraftRequest(
		String motive, String process, String result, String careerLink, String guidelineTitle, String guidelineDetail) {
}
