package com.learnershigh.grade.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.learnershigh.grade.dto.GradeRecordRequest;
import com.learnershigh.grade.dto.GradeRecordResponse;
import com.learnershigh.grade.dto.GradeUploadResponse;
import com.learnershigh.grade.service.GradeManagementService;
import com.learnershigh.grade.service.GradeQueryService;
import com.learnershigh.grade.service.GradeUploadService;

// 성적 관리: PDF 업로드, 성적 목록 조회, 수동 추가·수정·삭제 (docs/api-contract.md 9장)
@RestController
@RequestMapping("/api/student/grade")
public class GradeController {

	private final GradeUploadService uploadService;
	private final GradeQueryService queryService;
	private final GradeManagementService managementService;

	public GradeController(GradeUploadService uploadService, GradeQueryService queryService, GradeManagementService managementService) {
		this.uploadService = uploadService;
		this.queryService = queryService;
		this.managementService = managementService;
	}

	@PostMapping("/uploads")
	@ResponseStatus(HttpStatus.CREATED)
	public GradeUploadResponse upload(@RequestPart("file") MultipartFile file) {
		return uploadService.upload(file);
	}

	@GetMapping("/records")
	public List<GradeRecordResponse> list(@RequestParam Integer gradeYear, @RequestParam Integer term) {
		return queryService.list(gradeYear, term);
	}

	@PostMapping("/records")
	@ResponseStatus(HttpStatus.CREATED)
	public GradeRecordResponse add(@RequestBody GradeRecordRequest request) {
		return managementService.add(request);
	}

	@PutMapping("/records/{recordId}")
	public GradeRecordResponse update(@PathVariable String recordId, @RequestBody GradeRecordRequest request) {
		return managementService.update(recordId, request);
	}

	@DeleteMapping("/records/{recordId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable String recordId) {
		managementService.delete(recordId);
	}

}
