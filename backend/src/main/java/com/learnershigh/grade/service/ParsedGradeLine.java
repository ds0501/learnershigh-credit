package com.learnershigh.grade.service;

import java.math.BigDecimal;

// PDF 한 줄에서 인식한 과목 성적 한 건. gradeYear/term은 그 줄 위의 "N학년 N학기" 구간 머리글에서 가져온다.
public record ParsedGradeLine(
		Integer gradeYear,
		Integer term,
		String subjectName,
		Integer rawScore,
		BigDecimal subjectAverage,
		BigDecimal stdDev,
		Integer gradeLevel) {
}
