package com.learnershigh.credit.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learnershigh.credit.dto.DepartmentDetailResponse;
import com.learnershigh.credit.dto.DepartmentSummaryResponse;
import com.learnershigh.credit.service.DepartmentService;

// 학과 로드맵: 학과 검색과 추천 과목 상세
@RestController
@RequestMapping("/api/student/credit/departments")
public class DepartmentController {

	private final DepartmentService departmentService;

	public DepartmentController(DepartmentService departmentService) {
		this.departmentService = departmentService;
	}

	@GetMapping
	public List<DepartmentSummaryResponse> search(@RequestParam(required = false) String query) {
		return departmentService.search(query);
	}

	@GetMapping("/{departmentId}")
	public DepartmentDetailResponse getDetail(@PathVariable String departmentId) {
		return departmentService.getDetail(departmentId);
	}

}
