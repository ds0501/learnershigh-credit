package com.learnershigh.credit.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.learnershigh.credit.dto.AssessmentRequest;
import com.learnershigh.credit.dto.AssessmentResponse;
import com.learnershigh.credit.dto.CatalogItemResponse;
import com.learnershigh.credit.dto.ChangeCourseStatusRequest;
import com.learnershigh.credit.dto.CourseDetailResponse;
import com.learnershigh.credit.dto.CourseItemResponse;
import com.learnershigh.credit.dto.CourseRegistrationRequest;
import com.learnershigh.credit.dto.CreditSummaryResponse;
import com.learnershigh.credit.dto.RequiredCoursesResponse;
import com.learnershigh.credit.dto.SemesterCoursesResponse;
import com.learnershigh.credit.service.AssessmentService;
import com.learnershigh.credit.service.CourseCatalogService;
import com.learnershigh.credit.service.CourseDetailService;
import com.learnershigh.credit.service.CourseManagementService;
import com.learnershigh.credit.service.CourseQueryService;
import com.learnershigh.credit.service.CourseRegistrationService;
import com.learnershigh.credit.service.CreditSummaryService;

// 과목 조회·검색·등록·수정·삭제, 평가 추가·수정·삭제, 학점 현황 요약 (api-contract.md 4.1~4.11)
@RestController
@RequestMapping("/api/student/credit")
public class CreditCourseController {

	private final CourseCatalogService catalogService;
	private final CourseRegistrationService registrationService;
	private final CreditSummaryService summaryService;
	private final CourseQueryService queryService;
	private final CourseDetailService detailService;
	private final CourseManagementService managementService;
	private final AssessmentService assessmentService;

	public CreditCourseController(
			CourseCatalogService catalogService,
			CourseRegistrationService registrationService,
			CreditSummaryService summaryService,
			CourseQueryService queryService,
			CourseDetailService detailService,
			CourseManagementService managementService,
			AssessmentService assessmentService) {
		this.catalogService = catalogService;
		this.registrationService = registrationService;
		this.summaryService = summaryService;
		this.queryService = queryService;
		this.detailService = detailService;
		this.managementService = managementService;
		this.assessmentService = assessmentService;
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

	@GetMapping("/courses/{courseId}")
	public CourseDetailResponse getCourseDetail(@PathVariable String courseId) {
		return detailService.getDetail(courseId);
	}

	@PatchMapping("/courses/{courseId}")
	public CourseItemResponse changeCourseStatus(@PathVariable String courseId, @RequestBody ChangeCourseStatusRequest request) {
		return managementService.changeStatus(courseId, request.status());
	}

	@DeleteMapping("/courses/{courseId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteCourse(@PathVariable String courseId) {
		managementService.delete(courseId);
	}

	@PostMapping("/courses/{courseId}/assessments")
	@ResponseStatus(HttpStatus.CREATED)
	public AssessmentResponse addAssessment(@PathVariable String courseId, @RequestBody AssessmentRequest request) {
		return assessmentService.add(courseId, request);
	}

	@PutMapping("/courses/{courseId}/assessments/{assessmentId}")
	public AssessmentResponse updateAssessment(
			@PathVariable String courseId, @PathVariable String assessmentId, @RequestBody AssessmentRequest request) {
		return assessmentService.update(courseId, assessmentId, request);
	}

	@DeleteMapping("/courses/{courseId}/assessments/{assessmentId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteAssessment(@PathVariable String courseId, @PathVariable String assessmentId) {
		assessmentService.delete(courseId, assessmentId);
	}

}
