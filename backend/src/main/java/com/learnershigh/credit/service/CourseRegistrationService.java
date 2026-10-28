package com.learnershigh.credit.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.learnershigh.common.DemoStudent;
import com.learnershigh.common.exception.ValidationException;
import com.learnershigh.credit.dto.CourseItemResponse;
import com.learnershigh.credit.dto.CourseRegistrationRequest;
import com.learnershigh.credit.entity.CourseCatalog;
import com.learnershigh.credit.entity.CourseStatus;
import com.learnershigh.credit.entity.CourseType;
import com.learnershigh.credit.entity.GradeDisplay;
import com.learnershigh.credit.entity.StudentCourse;
import com.learnershigh.credit.repository.StudentCourseRepository;

// 과목 등록 (PRD F2, api-contract.md 4.5). frontend의
// domain/courseRegistration.ts + mocks/credit.ts registerCourse와 같은 검증 규칙을 따른다.
@Service
public class CourseRegistrationService {

	// 직접 입력일 때 고를 수 있는 유형 (frontend COURSE_TYPES와 같음). SPECIALIZED는 카탈로그 선택으로만 들어온다
	private static final Set<String> DIRECT_COURSE_TYPES =
			Set.of("COMMON", "GENERAL", "CAREER", "FUSION", "CREATIVE");

	private final StudentCourseRepository studentCourseRepository;
	private final CourseCatalogService catalogService;

	public CourseRegistrationService(StudentCourseRepository studentCourseRepository, CourseCatalogService catalogService) {
		this.studentCourseRepository = studentCourseRepository;
		this.catalogService = catalogService;
	}

	public CourseItemResponse register(CourseRegistrationRequest request) {
		Map<String, String> preCheckErrors = new LinkedHashMap<>();

		String resolvedName = "";
		CourseType resolvedCourseType = CourseType.COMMON;
		Long resolvedCatalogId = null;

		if (request.catalogId() != null && !request.catalogId().isBlank()) {
			CourseCatalog catalog = findCatalog(request.catalogId());
			if (catalog != null) {
				resolvedName = catalog.getName();
				resolvedCourseType = catalog.getCourseType();
				resolvedCatalogId = catalog.getId();
			} else {
				preCheckErrors.put("name", "과목 목록에서 찾을 수 없는 과목입니다.");
			}
		} else {
			resolvedName = request.customName() == null ? "" : request.customName().trim();
			if (request.courseType() != null && DIRECT_COURSE_TYPES.contains(request.courseType())) {
				resolvedCourseType = CourseType.valueOf(request.courseType());
			} else {
				preCheckErrors.put("courseType", "과목 유형을 선택하세요.");
			}
		}

		boolean creative = resolvedCourseType == CourseType.CREATIVE;
		Integer writtenWeight = creative ? 0 : request.writtenWeight();
		Integer performanceWeight = creative ? 0 : request.performanceWeight();

		List<StudentCourse> sameTerm =
				studentCourseRepository.findByStudentIdAndGradeYearAndTerm(DemoStudent.ID, request.gradeYear(), request.term());
		List<String> sameTermNames = sameTerm.stream().map(this::resolveName).toList();

		Map<String, String> errors = new LinkedHashMap<>();
		validateDraft(request, resolvedName, writtenWeight, performanceWeight, creative, sameTermNames, errors);
		errors.putAll(preCheckErrors);

		if (!isValidStatus(request.status())) {
			errors.put("status", "이수 상태를 선택하세요.");
		}
		if (!creative && !isValidGradeDisplay(request.gradeDisplay())) {
			errors.put("gradeDisplay", "성적 표기 방식을 선택하세요.");
		}

		if (!errors.isEmpty()) {
			throw new ValidationException("입력값을 확인하세요.", errors);
		}

		GradeDisplay gradeDisplay = creative ? null : GradeDisplay.valueOf(request.gradeDisplay());
		String customName = resolvedCatalogId != null ? null : resolvedName;

		StudentCourse created = new StudentCourse(
				DemoStudent.ID,
				resolvedCatalogId,
				customName,
				resolvedCourseType,
				request.credits(),
				request.gradeYear(),
				request.term(),
				CourseStatus.valueOf(request.status()),
				writtenWeight,
				performanceWeight,
				gradeDisplay,
				0,
				null);
		created = studentCourseRepository.save(created);

		// 평가가 없는 새 과목: achievementRate는 null, riskLevel은 NO_DATA(창체는 NORMAL), requiredAverage는 없음
		return new CourseItemResponse(
				String.valueOf(created.getId()),
				resolvedName,
				resolvedCourseType.name(),
				created.getCredits(),
				created.getStatus().name(),
				null,
				creative ? "NORMAL" : "NO_DATA",
				null);
	}

	private CourseCatalog findCatalog(String catalogId) {
		try {
			return catalogService.findById(Long.valueOf(catalogId)).orElse(null);
		} catch (NumberFormatException exception) {
			return null;
		}
	}

	private String resolveName(StudentCourse course) {
		if (course.getCatalogId() != null) {
			return catalogService.findById(course.getCatalogId()).map(CourseCatalog::getName).orElse("");
		}
		return course.getCustomName() == null ? "" : course.getCustomName();
	}

	// 중복 비교용 과목명 (공백과 대소문자 무시, frontend normalizeCourseName과 같음)
	private String normalize(String name) {
		return name.replaceAll("\\s+", "").toLowerCase();
	}

	private boolean isWeight(Integer value) {
		return value != null && value >= 0 && value <= 100;
	}

	private boolean isValidStatus(String status) {
		return "IN_PROGRESS".equals(status) || "COMPLETED".equals(status);
	}

	private boolean isValidGradeDisplay(String gradeDisplay) {
		return "ACHIEVEMENT_ONLY".equals(gradeDisplay)
				|| "ACHIEVEMENT_AND_RANK".equals(gradeDisplay)
				|| "THREE_LEVEL".equals(gradeDisplay);
	}

	private void validateDraft(
			CourseRegistrationRequest request,
			String name,
			Integer writtenWeight,
			Integer performanceWeight,
			boolean creative,
			List<String> sameTermNames,
			Map<String, String> errors) {
		Integer gradeYear = request.gradeYear();
		Integer term = request.term();
		if (gradeYear == null || gradeYear < 1 || gradeYear > 3 || term == null || (term != 1 && term != 2)) {
			errors.put("term", "학기를 선택하세요.");
		}

		String trimmedName = name.trim();
		if (trimmedName.isEmpty()) {
			errors.put("name", "과목명을 입력하세요.");
		} else if (sameTermNames.stream().anyMatch(existing -> normalize(existing).equals(normalize(trimmedName)))) {
			errors.put("name", "같은 학기에 이미 등록된 과목입니다.");
		}

		Integer credits = request.credits();
		if (credits == null || credits < 1) {
			errors.put("credits", "학점은 1 이상의 정수여야 합니다.");
		}

		if (!creative) {
			boolean validWeights = isWeight(writtenWeight) && isWeight(performanceWeight);
			if (!validWeights || writtenWeight + performanceWeight != 100) {
				errors.put("writtenWeight", "지필과 수행 반영 비율의 합은 100%여야 합니다.");
			}
		}
	}

}
