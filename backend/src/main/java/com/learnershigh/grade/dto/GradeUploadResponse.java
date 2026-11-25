package com.learnershigh.grade.dto;

import java.util.List;

public record GradeUploadResponse(
		String uploadId,
		Integer recognizedCount,
		Integer failedCount,
		List<GradeRecordResponse> records) {
}
