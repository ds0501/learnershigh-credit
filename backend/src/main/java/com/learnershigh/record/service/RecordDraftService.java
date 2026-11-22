package com.learnershigh.record.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.learnershigh.common.DemoStudent;
import com.learnershigh.common.exception.NotFoundException;
import com.learnershigh.common.exception.ValidationException;
import com.learnershigh.credit.entity.CourseType;
import com.learnershigh.credit.entity.StudentCourse;
import com.learnershigh.credit.repository.StudentCourseRepository;
import com.learnershigh.credit.service.CourseCatalogService;
import com.learnershigh.record.dto.CreateRecordDraftRequest;
import com.learnershigh.record.dto.RecordDraftCardResponse;
import com.learnershigh.record.dto.RecordDraftListResponse;
import com.learnershigh.record.dto.RecordDraftResponse;
import com.learnershigh.record.dto.SaveRecordDraftRequest;
import com.learnershigh.record.entity.DraftContent;
import com.learnershigh.record.entity.DraftSource;
import com.learnershigh.record.entity.RecordDraft;
import com.learnershigh.record.repository.RecordDraftRepository;

// 생기부 관리: 학기별 세특 초안 목록, 작성·저장·제출 (데모 학생 고정). 선생님 확인은 아직 없다
@Service
public class RecordDraftService {

	static final Set<String> SEMESTERS = Set.of("1-1", "1-2", "2-1", "2-2", "3-1", "3-2");
	// record_draft.subject_name 길이
	static final int SUBJECT_NAME_MAX_LENGTH = 100;

	private final RecordDraftRepository draftRepository;
	private final StudentCourseRepository studentCourseRepository;
	private final CourseCatalogService catalogService;

	public RecordDraftService(
			RecordDraftRepository draftRepository, StudentCourseRepository studentCourseRepository, CourseCatalogService catalogService) {
		this.draftRepository = draftRepository;
		this.studentCourseRepository = studentCourseRepository;
		this.catalogService = catalogService;
	}

	// 학기별 세특 카드. 그 학기에 등록한 과목(창체 제외)을 등록 순서로 먼저 두고, 초안이 없으면 NONE 카드로 보여 준다.
	// 등록 과목과 이어지지 않은 초안(과거 기록, 과목을 지운 초안 등)은 그 뒤에 만든 순서로 둔다
	@Transactional(readOnly = true)
	public RecordDraftListResponse list(String semester) {
		String validSemester = requireSemester(semester);
		int[] gradeAndTerm = parseSemester(validSemester);
		List<StudentCourse> courses = studentCourseRepository
				.findByStudentIdAndGradeYearAndTermOrderByIdAsc(DemoStudent.ID, gradeAndTerm[0], gradeAndTerm[1]).stream()
				.filter(course -> course.getCourseType() != CourseType.CREATIVE)
				.toList();
		List<RecordDraft> drafts = draftRepository.findByStudentIdAndSemesterOrderByIdAsc(DemoStudent.ID, validSemester);

		Set<Long> usedDraftIds = new HashSet<>();
		List<RecordDraftCardResponse> items = new ArrayList<>();
		for (StudentCourse course : courses) {
			String courseName = catalogService.resolveCourseName(course);
			RecordDraft matched = drafts.stream()
					.filter(draft -> !usedDraftIds.contains(draft.getId()))
					.filter(draft -> course.getId().equals(draft.getCourseId())
							|| (draft.getCourseId() == null && normalize(draft.getSubjectName()).equals(normalize(courseName))))
					.findFirst()
					.orElse(null);
			if (matched == null) {
				items.add(new RecordDraftCardResponse(
						null, validSemester, courseName, String.valueOf(course.getId()), null, RecordDraftCardResponse.STATUS_NONE, null));
			} else {
				usedDraftIds.add(matched.getId());
				items.add(card(matched));
			}
		}
		drafts.stream().filter(draft -> !usedDraftIds.contains(draft.getId())).map(RecordDraftService::card).forEach(items::add);
		return new RecordDraftListResponse(validSemester, items);
	}

	@Transactional(readOnly = true)
	public RecordDraftResponse get(String draftId) {
		return RecordDraftResponse.from(findOwnDraft(draftId));
	}

	@Transactional
	public RecordDraftResponse create(CreateRecordDraftRequest request) {
		Map<String, String> errors = new LinkedHashMap<>();
		String semester = request.semester() == null ? "" : request.semester().trim();
		if (!SEMESTERS.contains(semester)) {
			errors.put("semester", "학기를 선택하세요.");
		}
		DraftSource source = parseSource(request.source());
		if (source == null) {
			errors.put("source", "기록 종류(NEW 또는 PAST)를 선택하세요.");
		}

		String subjectName = request.subjectName() == null ? "" : request.subjectName().trim();
		Long courseId = null;
		// 새 초안이 등록 과목에서 시작하면 과목명은 등록 과목의 이름을 쓴다
		if (source == DraftSource.NEW && request.courseId() != null && !request.courseId().isBlank()) {
			StudentCourse course = findOwnCourse(request.courseId());
			courseId = course.getId();
			subjectName = catalogService.resolveCourseName(course);
			if (errors.get("semester") == null && !semester.equals(course.getGradeYear() + "-" + course.getTerm())) {
				errors.put("courseId", "선택한 학기에 등록한 과목이 아닙니다.");
			}
		}
		if (subjectName.isEmpty()) {
			errors.put("subjectName", "과목명을 입력하세요.");
		} else if (subjectName.length() > SUBJECT_NAME_MAX_LENGTH) {
			errors.put("subjectName", "과목명은 " + SUBJECT_NAME_MAX_LENGTH + "자 이하로 입력하세요.");
		} else if (errors.get("semester") == null && draftRepository.existsByStudentIdAndSemesterAndSubjectName(DemoStudent.ID, semester, subjectName)) {
			errors.put("subjectName", "이 학기에 같은 과목의 세특이 이미 있습니다.");
		}
		if (!errors.isEmpty()) {
			throw new ValidationException("입력값을 확인하세요.", errors);
		}

		DraftContent content = toContent(request.motive(), request.process(), request.result(), request.careerLink(),
				request.guidelineTitle(), request.guidelineDetail());
		// 과거 기록은 확정된 내용을 옮겨 적는 것이라 내용이 있어야 한다
		if (source == DraftSource.PAST && content.isBlank()) {
			throw new ValidationException("과거 기록은 내용을 한 칸 이상 입력하세요.", Map.of());
		}
		RecordDraft draft = RecordDraft.create(DemoStudent.ID, semester, subjectName, courseId, source, content, LocalDateTime.now());
		return RecordDraftResponse.from(draftRepository.save(draft));
	}

	// DRAFT, REVISION_REQUESTED일 때만 저장할 수 있다
	@Transactional
	public RecordDraftResponse save(String draftId, SaveRecordDraftRequest request) {
		RecordDraft draft = findOwnDraft(draftId);
		requireEditable(draft);
		SaveRecordDraftRequest body = request == null ? new SaveRecordDraftRequest(null, null, null, null, null, null) : request;
		draft.updateContent(toContent(body.motive(), body.process(), body.result(), body.careerLink(),
				body.guidelineTitle(), body.guidelineDetail()), LocalDateTime.now());
		return RecordDraftResponse.from(draft);
	}

	// 4칸이 모두 비어 있으면 거부한다
	@Transactional
	public RecordDraftResponse submit(String draftId) {
		RecordDraft draft = findOwnDraft(draftId);
		requireEditable(draft);
		DraftContent content = new DraftContent(draft.getMotive(), draft.getProcess(), draft.getResult(), draft.getCareerLink(),
				draft.getGuidelineTitle(), draft.getGuidelineDetail());
		if (content.isBlank()) {
			throw new ValidationException("내용을 한 칸 이상 입력한 뒤 제출하세요.", Map.of());
		}
		draft.submit(LocalDateTime.now());
		return RecordDraftResponse.from(draft);
	}

	private static void requireEditable(RecordDraft draft) {
		if (!draft.getStatus().isEditableByStudent()) {
			throw new ValidationException("제출했거나 확인이 끝난 세특은 수정할 수 없습니다.", Map.of());
		}
	}

	private static RecordDraftCardResponse card(RecordDraft draft) {
		return new RecordDraftCardResponse(
				String.valueOf(draft.getId()),
				draft.getSemester(),
				draft.getSubjectName(),
				draft.getCourseId() == null ? null : String.valueOf(draft.getCourseId()),
				draft.getSource().name(),
				draft.getStatus().name(),
				draft.getUpdatedAt());
	}

	// 4칸은 null이면 빈 문자열(입력 그대로 저장), 가이드라인은 공백이면 null
	private static DraftContent toContent(
			String motive, String process, String result, String careerLink, String guidelineTitle, String guidelineDetail) {
		return new DraftContent(orEmpty(motive), orEmpty(process), orEmpty(result), orEmpty(careerLink),
				blankToNull(guidelineTitle), blankToNull(guidelineDetail));
	}

	private static String orEmpty(String value) {
		return value == null ? "" : value;
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private static String normalize(String name) {
		return name == null ? "" : name.replaceAll("\\s+", "").toLowerCase();
	}

	private static DraftSource parseSource(String value) {
		if (value == null) {
			return null;
		}
		try {
			return DraftSource.valueOf(value.trim());
		} catch (IllegalArgumentException exception) {
			return null;
		}
	}

	private static String requireSemester(String semester) {
		String value = semester == null ? "" : semester.trim();
		if (!SEMESTERS.contains(value)) {
			throw new ValidationException("입력값을 확인하세요.", Map.of("semester", "학기를 선택하세요."));
		}
		return value;
	}

	private static int[] parseSemester(String semester) {
		String[] parts = semester.split("-");
		return new int[] { Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) };
	}

	private RecordDraft findOwnDraft(String draftId) {
		Long id = parseId(draftId, "세특을 찾을 수 없습니다.");
		return draftRepository.findById(id)
				.filter(draft -> DemoStudent.ID.equals(draft.getStudentId()))
				.orElseThrow(() -> new NotFoundException("세특을 찾을 수 없습니다."));
	}

	private StudentCourse findOwnCourse(String courseId) {
		Long id = parseId(courseId, "과목을 찾을 수 없습니다.");
		return studentCourseRepository.findById(id)
				.filter(course -> DemoStudent.ID.equals(course.getStudentId()))
				.orElseThrow(() -> new NotFoundException("과목을 찾을 수 없습니다."));
	}

	private static Long parseId(String value, String notFoundMessage) {
		try {
			return Long.valueOf(value);
		} catch (NumberFormatException | NullPointerException exception) {
			throw new NotFoundException(notFoundMessage);
		}
	}

}
