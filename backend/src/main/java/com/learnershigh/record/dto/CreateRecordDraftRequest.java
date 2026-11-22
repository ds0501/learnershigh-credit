package com.learnershigh.record.dto;

// POST /api/student/record/drafts 요청 본문.
// source NEW: 등록 과목(courseId) 또는 과목명으로 새 초안(DRAFT). source PAST: 과거 기록(학기, 과목명, 4칸)을 CONFIRMED로 저장
public record CreateRecordDraftRequest(
		String semester,
		String subjectName,
		String courseId,
		String source,
		String motive,
		String process,
		String result,
		String careerLink,
		String guidelineTitle,
		String guidelineDetail) {
}
