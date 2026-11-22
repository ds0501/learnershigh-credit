package com.learnershigh.record.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.learnershigh.common.DemoStudent;
import com.learnershigh.common.exception.NotFoundException;
import com.learnershigh.common.exception.ValidationException;
import com.learnershigh.credit.entity.CourseCatalog;
import com.learnershigh.credit.entity.DepartmentSubject;
import com.learnershigh.credit.entity.StudentCourse;
import com.learnershigh.credit.repository.CourseCatalogRepository;
import com.learnershigh.credit.repository.DepartmentSubjectRepository;
import com.learnershigh.credit.repository.StudentCourseRepository;
import com.learnershigh.credit.service.CourseCatalogService;
import com.learnershigh.goal.dto.StudentGoalResponse;
import com.learnershigh.goal.service.StudentGoalService;
import com.learnershigh.record.dto.AdoptGuidelineRequest;
import com.learnershigh.record.dto.GuidelinePreviewRequest;
import com.learnershigh.record.dto.GuidelineSuggestion;
import com.learnershigh.record.dto.GuidelineSuggestionsResponse;
import com.learnershigh.record.dto.RecordDraftResponse;
import com.learnershigh.record.entity.RecordDraft;
import com.learnershigh.record.repository.RecordDraftRepository;

// 세특 방향 추천과 채택. 입력은 희망 학과, 목표 대학, 과목명, 희망 학과 로드맵에서의 지정(핵심/권장).
// AI가 실패하면 로드맵 기반 고정 안내 3안으로 대체해 항상 3안을 돌려준다
@Service
public class RecordGuidelineService {

	static final int SUGGESTION_COUNT = 3;
	static final int MIN_ITEMS = 2;
	static final int MAX_ITEMS = 3;
	// record_draft.guideline_title 길이
	static final int TITLE_MAX_LENGTH = 200;

	private final RecordDraftRepository draftRepository;
	private final StudentCourseRepository studentCourseRepository;
	private final CourseCatalogRepository catalogRepository;
	private final CourseCatalogService catalogService;
	private final DepartmentSubjectRepository departmentSubjectRepository;
	private final StudentGoalService goalService;
	private final GuidelineAiClient aiClient;

	public RecordGuidelineService(
			RecordDraftRepository draftRepository,
			StudentCourseRepository studentCourseRepository,
			CourseCatalogRepository catalogRepository,
			CourseCatalogService catalogService,
			DepartmentSubjectRepository departmentSubjectRepository,
			StudentGoalService goalService,
			GuidelineAiClient aiClient) {
		this.draftRepository = draftRepository;
		this.studentCourseRepository = studentCourseRepository;
		this.catalogRepository = catalogRepository;
		this.catalogService = catalogService;
		this.departmentSubjectRepository = departmentSubjectRepository;
		this.goalService = goalService;
		this.aiClient = aiClient;
	}

	// 이미 있는 초안의 과목으로 방향 추천
	public GuidelineSuggestionsResponse suggestForDraft(String draftId) {
		RecordDraft draft = findOwnDraft(draftId);
		return suggest(buildContext(draft.getSubjectName(), draft.getCourseId()));
	}

	// 초안이 아직 없는 카드(등록 과목) 또는 과목명으로 방향 추천. DB에는 아무것도 만들지 않는다
	public GuidelineSuggestionsResponse suggestForSubject(GuidelinePreviewRequest request) {
		Map<String, String> errors = new LinkedHashMap<>();
		String semester = request == null || request.semester() == null ? "" : request.semester().trim();
		if (!RecordDraftService.SEMESTERS.contains(semester)) {
			errors.put("semester", "학기를 선택하세요.");
		}
		String subjectName = request == null || request.subjectName() == null ? "" : request.subjectName().trim();
		Long courseId = null;
		if (request != null && request.courseId() != null && !request.courseId().isBlank()) {
			StudentCourse course = findOwnCourse(request.courseId());
			courseId = course.getId();
			subjectName = catalogService.resolveCourseName(course);
		}
		if (subjectName.isEmpty()) {
			errors.put("subjectName", "과목명을 입력하세요.");
		}
		if (!errors.isEmpty()) {
			throw new ValidationException("입력값을 확인하세요.", errors);
		}
		return suggest(buildContext(subjectName, courseId));
	}

	// 채택한 방향을 저장한다. 4칸은 건드리지 않는다. 학생이 고칠 수 있는 상태(DRAFT, REVISION_REQUESTED)에서만
	@Transactional
	public RecordDraftResponse adopt(String draftId, AdoptGuidelineRequest request) {
		RecordDraft draft = findOwnDraft(draftId);
		if (!draft.getStatus().isEditableByStudent()) {
			throw new ValidationException("제출했거나 확인이 끝난 세특은 수정할 수 없습니다.", Map.of());
		}
		String title = request == null || request.title() == null ? "" : request.title().trim();
		if (title.isEmpty()) {
			throw new ValidationException("입력값을 확인하세요.", Map.of("title", "채택할 방향의 제목이 없습니다."));
		}
		if (title.length() > TITLE_MAX_LENGTH) {
			throw new ValidationException("입력값을 확인하세요.", Map.of("title", "방향 제목은 " + TITLE_MAX_LENGTH + "자 이하여야 합니다."));
		}
		String detail = request.detail() == null || request.detail().isBlank() ? null : request.detail().trim();
		draft.adoptGuideline(title, detail, LocalDateTime.now());
		return RecordDraftResponse.from(draft);
	}

	GuidelineSuggestionsResponse suggest(GuidelineContext context) {
		Optional<List<GuidelineSuggestion>> fromAi = aiClient.suggest(context).flatMap(RecordGuidelineService::normalize);
		return fromAi
				.map(suggestions -> new GuidelineSuggestionsResponse(GuidelineSuggestionsResponse.AI, suggestions))
				.orElseGet(() -> new GuidelineSuggestionsResponse(GuidelineSuggestionsResponse.FALLBACK, FallbackGuidelines.build(context)));
	}

	// AI 응답이 약속한 모양(3안, 예시·포인트 2~3개, 빈 칸 없음)인지 확인한다. 4개 이상이면 앞 3개만, 모자라면 실패로 본다
	static Optional<List<GuidelineSuggestion>> normalize(List<GuidelineSuggestion> suggestions) {
		if (suggestions == null || suggestions.size() < SUGGESTION_COUNT) {
			return Optional.empty();
		}
		List<GuidelineSuggestion> result = suggestions.stream().limit(SUGGESTION_COUNT).map(suggestion -> {
			if (suggestion == null || isBlank(suggestion.title()) || isBlank(suggestion.reason())) {
				return null;
			}
			List<String> examples = cleanItems(suggestion.examples());
			List<String> points = cleanItems(suggestion.points());
			if (examples == null || points == null) {
				return null;
			}
			return new GuidelineSuggestion(suggestion.title().trim(), suggestion.reason().trim(), examples, points);
		}).toList();
		return result.stream().anyMatch(item -> item == null) ? Optional.empty() : Optional.of(result);
	}

	private static List<String> cleanItems(List<String> items) {
		if (items == null) {
			return null;
		}
		List<String> cleaned = items.stream().filter(item -> !isBlank(item)).map(String::trim).limit(MAX_ITEMS).toList();
		return cleaned.size() < MIN_ITEMS ? null : cleaned;
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	// 희망 학과·목표 대학과, 희망 학과 로드맵에서 이 과목의 지정(핵심/권장)을 모은다
	GuidelineContext buildContext(String subjectName, Long courseId) {
		StudentGoalResponse goal = goalService.getGoal();
		if (goal == null) {
			return new GuidelineContext(subjectName, null, null, null, null, null, null, null);
		}
		Long departmentId = Long.valueOf(goal.department().id());
		Optional<DepartmentSubject> roadmapSubject = findCatalogId(subjectName, courseId)
				.flatMap(catalogId -> departmentSubjectRepository.findByDepartmentIdOrderByRatioPercentDesc(departmentId).stream()
						.filter(subject -> catalogId.equals(subject.getCatalogId()))
						.findFirst());
		return new GuidelineContext(
				subjectName,
				goal.department().name(),
				goal.department().field(),
				goal.targetUniversity(),
				roadmapSubject.map(DepartmentSubject::getDesignationType).orElse(null),
				roadmapSubject.map(DepartmentSubject::getRequiredUniversityCount).orElse(null),
				roadmapSubject.map(DepartmentSubject::getTotalUniversityCount).orElse(null),
				roadmapSubject.map(subject -> subject.getRatioPercent().doubleValue()).orElse(null));
	}

	// 등록 과목이면 그 과목의 표준 과목 ID, 아니면 과목명(공백·대소문자 무시)이 같은 표준 과목
	private Optional<Long> findCatalogId(String subjectName, Long courseId) {
		if (courseId != null) {
			Optional<Long> fromCourse = studentCourseRepository.findById(courseId).map(StudentCourse::getCatalogId);
			if (fromCourse.isPresent()) {
				return fromCourse;
			}
		}
		String normalized = normalize(subjectName);
		return catalogRepository.findAllByOrderByIdAsc().stream()
				.filter(catalog -> normalize(catalog.getName()).equals(normalized))
				.map(CourseCatalog::getId)
				.findFirst();
	}

	private static String normalize(String name) {
		return name == null ? "" : name.replaceAll("\\s+", "").toLowerCase();
	}

	private RecordDraft findOwnDraft(String draftId) {
		return parseId(draftId).flatMap(draftRepository::findById)
				.filter(draft -> DemoStudent.ID.equals(draft.getStudentId()))
				.orElseThrow(() -> new NotFoundException("세특을 찾을 수 없습니다."));
	}

	private StudentCourse findOwnCourse(String courseId) {
		return parseId(courseId).flatMap(studentCourseRepository::findById)
				.filter(course -> DemoStudent.ID.equals(course.getStudentId()))
				.orElseThrow(() -> new NotFoundException("과목을 찾을 수 없습니다."));
	}

	private static Optional<Long> parseId(String value) {
		try {
			return Optional.of(Long.valueOf(value));
		} catch (NumberFormatException | NullPointerException exception) {
			return Optional.empty();
		}
	}

}
