package com.learnershigh.record.dto;

import java.util.List;

// GET /api/student/record/drafts?semester= 응답
public record RecordDraftListResponse(String semester, List<RecordDraftCardResponse> items) {
}
