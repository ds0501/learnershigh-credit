package com.learnershigh.credit.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.learnershigh.credit.dto.CatalogItemResponse;
import com.learnershigh.credit.dto.CourseItemResponse;
import com.learnershigh.credit.dto.CourseRegistrationRequest;
import com.learnershigh.credit.service.CourseCatalogService;
import com.learnershigh.credit.service.CourseRegistrationService;

// 과목 검색·등록 (api-contract.md 4.4, 4.5)
@RestController
@RequestMapping("/api/student/credit")
public class CreditCourseController {

	private final CourseCatalogService catalogService;
	private final CourseRegistrationService registrationService;

	public CreditCourseController(CourseCatalogService catalogService, CourseRegistrationService registrationService) {
		this.catalogService = catalogService;
		this.registrationService = registrationService;
	}

	@GetMapping("/catalog")
	public List<CatalogItemResponse> searchCatalog(@RequestParam(required = false) String query) {
		return catalogService.search(query);
	}

	@PostMapping("/courses")
	@ResponseStatus(HttpStatus.CREATED)
	public CourseItemResponse registerCourse(@RequestBody CourseRegistrationRequest request) {
		return registrationService.register(request);
	}

}
