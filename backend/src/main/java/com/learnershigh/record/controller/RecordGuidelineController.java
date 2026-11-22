package com.learnershigh.record.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learnershigh.record.dto.GuidelinePreviewRequest;
import com.learnershigh.record.dto.GuidelineSuggestionsResponse;
import com.learnershigh.record.service.RecordGuidelineService;

// 초안이 아직 없는 카드에서 세특 방향 추천(DB에 아무것도 만들지 않음). 채택하면 POST /drafts로 초안과 함께 저장한다
@RestController
@RequestMapping("/api/student/record/guidelines")
public class RecordGuidelineController {

	private final RecordGuidelineService guidelineService;

	public RecordGuidelineController(RecordGuidelineService guidelineService) {
		this.guidelineService = guidelineService;
	}

	@PostMapping
	public GuidelineSuggestionsResponse suggest(@RequestBody GuidelinePreviewRequest request) {
		return guidelineService.suggestForSubject(request);
	}

}
