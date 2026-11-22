package com.learnershigh.record.dto;

import java.time.LocalDateTime;

// 학기별 세특 목록의 카드 하나. 초안이 아직 없는 등록 과목은 id·source·updatedAt이 null이고 status가 NONE이다
public record RecordDraftCardResponse(
		String id,
		String semester,
		String subjectName,
		String courseId,
		String source,
		String status,
		LocalDateTime updatedAt) {

	// 등록 과목 중 초안이 없는 것의 상태
	public static final String STATUS_NONE = "NONE";

}
