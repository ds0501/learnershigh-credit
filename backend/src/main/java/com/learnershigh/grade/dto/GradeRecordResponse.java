package com.learnershigh.grade.dto;

import java.math.BigDecimal;

import com.learnershigh.grade.entity.GradeRecord;

public record GradeRecordResponse(
		String id,
		Integer gradeYear,
		Integer term,
		String subjectName,
		Integer rawScore,
		BigDecimal subjectAverage,
		BigDecimal stdDev,
		Integer gradeLevel,
		String source,
		Boolean appliedToCredit,
		String unmatchedReason,
		String matchedCourseId) {

	public static GradeRecordResponse from(GradeRecord record) {
		return new GradeRecordResponse(
				String.valueOf(record.getId()),
				record.getGradeYear(),
				record.getTerm(),
				record.getSubjectName(),
				record.getRawScore(),
				record.getSubjectAverage(),
				record.getStdDev(),
				record.getGradeLevel(),
				record.getSource(),
				record.getAppliedToCredit(),
				record.getUnmatchedReason(),
				record.getMatchedStudentCourseId() == null ? null : String.valueOf(record.getMatchedStudentCourseId()));
	}

}
