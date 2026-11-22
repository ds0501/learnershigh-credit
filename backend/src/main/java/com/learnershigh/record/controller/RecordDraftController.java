package com.learnershigh.record.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.learnershigh.record.dto.AdoptGuidelineRequest;
import com.learnershigh.record.dto.CreateRecordDraftRequest;
import com.learnershigh.record.dto.GuidelineSuggestionsResponse;
import com.learnershigh.record.dto.RecordDraftListResponse;
import com.learnershigh.record.dto.RecordDraftResponse;
import com.learnershigh.record.dto.SaveRecordDraftRequest;
import com.learnershigh.record.service.RecordDraftService;
import com.learnershigh.record.service.RecordGuidelineService;

// 생기부 관리: 학기별 세특 초안
@RestController
@RequestMapping("/api/student/record/drafts")
public class RecordDraftController {

	private final RecordDraftService draftService;
	private final RecordGuidelineService guidelineService;

	public RecordDraftController(RecordDraftService draftService, RecordGuidelineService guidelineService) {
		this.draftService = draftService;
		this.guidelineService = guidelineService;
	}

	@GetMapping
	public RecordDraftListResponse list(@RequestParam(required = false) String semester) {
		return draftService.list(semester);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public RecordDraftResponse create(@RequestBody CreateRecordDraftRequest request) {
		return draftService.create(request);
	}

	@GetMapping("/{draftId}")
	public RecordDraftResponse get(@PathVariable String draftId) {
		return draftService.get(draftId);
	}

	@PutMapping("/{draftId}")
	public RecordDraftResponse save(@PathVariable String draftId, @RequestBody SaveRecordDraftRequest request) {
		return draftService.save(draftId, request);
	}

	@PostMapping("/{draftId}/submit")
	public RecordDraftResponse submit(@PathVariable String draftId) {
		return draftService.submit(draftId);
	}

	@PostMapping("/{draftId}/withdraw")
	public RecordDraftResponse withdraw(@PathVariable String draftId) {
		return draftService.withdraw(draftId);
	}

	// 방향 추천 3안(AI, 실패하면 로드맵 기반 고정 안내)
	@PostMapping("/{draftId}/guidelines")
	public GuidelineSuggestionsResponse suggestGuidelines(@PathVariable String draftId) {
		return guidelineService.suggestForDraft(draftId);
	}

	// 채택한 방향 저장
	@PutMapping("/{draftId}/guideline")
	public RecordDraftResponse adoptGuideline(@PathVariable String draftId, @RequestBody AdoptGuidelineRequest request) {
		return guidelineService.adopt(draftId, request);
	}

}
