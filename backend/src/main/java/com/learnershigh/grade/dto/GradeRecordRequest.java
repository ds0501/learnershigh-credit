package com.learnershigh.grade.dto;

import java.math.BigDecimal;

// POST /records(수동 추가), PUT /records/{id}(수동 수정) 요청 본문.
// 수정 시에는 gradeYear/term/subjectName은 무시한다(과목 정체는 바꿀 수 없다).
public record GradeRecordRequest(
		Integer gradeYear,
		Integer term,
		String subjectName,
		Integer rawScore,
		BigDecimal subjectAverage,
		BigDecimal stdDev,
		Integer gradeLevel) {
}
