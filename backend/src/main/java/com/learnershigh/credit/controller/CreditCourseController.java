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
import com.learnershigh.credit.dto.CreditSummaryResponse;
import com.learnershigh.credit.dto.RequiredCoursesResponse;
import com.learnershigh.credit.dto.SemesterCoursesResponse;
import com.learnershigh.credit.service.CourseCatalogService;
import com.learnershigh.credit.service.CourseQueryService;
import com.learnershigh.credit.service.CourseRegistrationService;
import com.learnershigh.credit.service.CreditSummaryService;

// 과목 조회·검색·등록·학점 현황 요약 (api-contract.md 4.1~4.5)
@RestController
@RequestMapping("/api/student/credit")
public class CreditCourseController {

	private final CourseCatalogService catalogService;
	private final CourseRegistrationService registrationService;
	private final CreditSummaryService summaryService;
	private final CourseQueryService queryService;

	public CreditCourseController(
			CourseCatalogService catalogService,
			CourseRegistrationService registrationService,
			CreditSummaryService summaryService,
			CourseQueryService queryService) {
		this.catalogService = catalogService;
		this.registrationService = registrationService;
		this.summaryService = summaryService;
		this.queryService = queryService;
	}

	@GetMapping("/summary")
	public CreditSummaryResponse getSummary() {
		return summaryService.getSummary();
	}

	@GetMapping("/courses")
	public SemesterCoursesResponse getCourses(@RequestParam Integer gradeYear, @RequestParam Integer term) {
		return queryService.getCourses(gradeYear, term);
	}

	@GetMapping("/required-courses")
	public RequiredCoursesResponse getRequiredCourses() {
		return queryService.getRequiredCourses();
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
