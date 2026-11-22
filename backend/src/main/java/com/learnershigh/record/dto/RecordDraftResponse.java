package com.learnershigh.record.dto;

import java.time.LocalDateTime;

import com.learnershigh.record.entity.RecordDraft;

// 세특 초안 상세 (GET·PUT /drafts/{id}, POST /drafts, POST /drafts/{id}/submit 응답)
public record RecordDraftResponse(
		String id,
		String semester,
		String subjectName,
		String courseId,
		String source,
		String status,
		String motive,
		String process,
		String result,
		String careerLink,
		String guidelineTitle,
		String guidelineDetail,
		String teacherComment,
		LocalDateTime submittedAt,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {

	public static RecordDraftResponse from(RecordDraft draft) {
		return new RecordDraftResponse(
				String.valueOf(draft.getId()),
				draft.getSemester(),
				draft.getSubjectName(),
				draft.getCourseId() == null ? null : String.valueOf(draft.getCourseId()),
				draft.getSource().name(),
				draft.getStatus().name(),
				draft.getMotive(),
				draft.getProcess(),
				draft.getResult(),
				draft.getCareerLink(),
				draft.getGuidelineTitle(),
				draft.getGuidelineDetail(),
				draft.getTeacherComment(),
				draft.getSubmittedAt(),
				draft.getCreatedAt(),
				draft.getUpdatedAt());
	}

}
