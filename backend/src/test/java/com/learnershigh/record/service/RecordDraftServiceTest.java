package com.learnershigh.record.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.learnershigh.common.DemoStudent;
import com.learnershigh.common.exception.NotFoundException;
import com.learnershigh.common.exception.ValidationException;
import com.learnershigh.credit.entity.CourseType;
import com.learnershigh.credit.entity.StudentCourse;
import com.learnershigh.credit.repository.StudentCourseRepository;
import com.learnershigh.credit.service.CourseCatalogService;
import com.learnershigh.record.dto.CreateRecordDraftRequest;
import com.learnershigh.record.dto.RecordDraftCardResponse;
import com.learnershigh.record.dto.RecordDraftResponse;
import com.learnershigh.record.dto.SaveRecordDraftRequest;
import com.learnershigh.record.entity.DraftContent;
import com.learnershigh.record.entity.DraftSource;
import com.learnershigh.record.entity.DraftStatus;
import com.learnershigh.record.entity.RecordDraft;
import com.learnershigh.record.repository.RecordDraftRepository;

// 세특 초안 목록·작성·저장·제출 (데모 학생)
@ExtendWith(MockitoExtension.class)
class RecordDraftServiceTest {

	@Mock
	private RecordDraftRepository draftRepository;

	@Mock
	private StudentCourseRepository studentCourseRepository;

	@Mock
	private CourseCatalogService catalogService;

	private RecordDraftService service;

	private static final DraftContent EMPTY = new DraftContent("", "", "", "", null, null);

	@BeforeEach
	void setUp() {
		service = new RecordDraftService(draftRepository, studentCourseRepository, catalogService);
	}

	private StudentCourse course(long id, String name, CourseType type, int gradeYear, int term) {
		StudentCourse course = mock(StudentCourse.class);
		lenient().when(course.getId()).thenReturn(id);
		lenient().when(course.getStudentId()).thenReturn(DemoStudent.ID);
		lenient().when(course.getCourseType()).thenReturn(type);
		lenient().when(course.getGradeYear()).thenReturn(gradeYear);
		lenient().when(course.getTerm()).thenReturn(term);
		lenient().when(catalogService.resolveCourseName(course)).thenReturn(name);
		return course;
	}

	private RecordDraft draft(long id, String semester, String subjectName, Long courseId, DraftSource source, DraftStatus status, DraftContent content) {
		RecordDraft draft = RecordDraft.create(DemoStudent.ID, semester, subjectName, courseId, source, content, LocalDateTime.of(2026, 11, 1, 9, 0));
		ReflectionTestUtils.setField(draft, "id", id);
		ReflectionTestUtils.setField(draft, "status", status);
		return draft;
	}

	private CreateRecordDraftRequest create(String semester, String subjectName, String courseId, String source, String motive) {
		return new CreateRecordDraftRequest(semester, subjectName, courseId, source, motive, "", "", "", null, null);
	}

	// 목록

	@Test
	void 목록_등록_과목은_초안이_없으면_NONE_있으면_초안_상태_창체는_빼고_과거_기록은_뒤에() {
		StudentCourse korean = course(11L, "공통국어2", CourseType.COMMON, 1, 2);
		StudentCourse math = course(12L, "공통수학2", CourseType.COMMON, 1, 2);
		StudentCourse creative = course(20L, "창의적 체험활동", CourseType.CREATIVE, 1, 2);
		when(studentCourseRepository.findByStudentIdAndGradeYearAndTermOrderByIdAsc(DemoStudent.ID, 1, 2))
				.thenReturn(List.of(korean, math, creative));
		RecordDraft mathDraft = draft(5L, "1-2", "공통수학2", 12L, DraftSource.NEW, DraftStatus.SUBMITTED, EMPTY);
		RecordDraft past = draft(7L, "1-2", "독서", null, DraftSource.PAST, DraftStatus.CONFIRMED, EMPTY);
		when(draftRepository.findByStudentIdAndSemesterOrderByIdAsc(DemoStudent.ID, "1-2")).thenReturn(List.of(mathDraft, past));

		List<RecordDraftCardResponse> items = service.list("1-2").items();

		assertThat(items).extracting(RecordDraftCardResponse::subjectName).containsExactly("공통국어2", "공통수학2", "독서");
		assertThat(items.get(0)).isEqualTo(new RecordDraftCardResponse(null, "1-2", "공통국어2", "11", null, "NONE", null));
		assertThat(items.get(1).id()).isEqualTo("5");
		assertThat(items.get(1).status()).isEqualTo("SUBMITTED");
		assertThat(items.get(2).source()).isEqualTo("PAST");
		assertThat(items.get(2).status()).isEqualTo("CONFIRMED");
	}

	@Test
	void 목록_과목_연결이_끊긴_초안은_같은_이름의_등록_과목에_붙는다() {
		StudentCourse korean = course(11L, "공통국어2", CourseType.COMMON, 1, 2);
		when(studentCourseRepository.findByStudentIdAndGradeYearAndTermOrderByIdAsc(DemoStudent.ID, 1, 2)).thenReturn(List.of(korean));
		RecordDraft orphan = draft(3L, "1-2", "공통 국어2", null, DraftSource.NEW, DraftStatus.DRAFT, EMPTY);
		when(draftRepository.findByStudentIdAndSemesterOrderByIdAsc(DemoStudent.ID, "1-2")).thenReturn(List.of(orphan));

		List<RecordDraftCardResponse> items = service.list("1-2").items();

		assertThat(items).hasSize(1);
		assertThat(items.get(0).id()).isEqualTo("3");
	}

	@Test
	void 목록_학기가_틀리면_400() {
		ValidationException exception = assertThrows(ValidationException.class, () -> service.list("4-1"));
		assertThat(exception.getFieldErrors()).isEqualTo(Map.of("semester", "학기를 선택하세요."));
	}

	// 만들기

	@Test
	void 새_초안은_등록_과목_이름으로_DRAFT() {
		StudentCourse math = course(12L, "공통수학2", CourseType.COMMON, 1, 2);
		when(studentCourseRepository.findById(12L)).thenReturn(Optional.of(math));
		when(draftRepository.save(any(RecordDraft.class))).thenAnswer(invocation -> invocation.getArgument(0));

		RecordDraftResponse response = service.create(create("1-2", "무시됨", "12", "NEW", "함수 그래프가 궁금해서"));

		assertThat(response.subjectName()).isEqualTo("공통수학2");
		assertThat(response.courseId()).isEqualTo("12");
		assertThat(response.source()).isEqualTo("NEW");
		assertThat(response.status()).isEqualTo("DRAFT");
		assertThat(response.motive()).isEqualTo("함수 그래프가 궁금해서");
		assertThat(response.process()).isEmpty();
	}

	@Test
	void 과거_기록은_CONFIRMED로_저장() {
		when(draftRepository.save(any(RecordDraft.class))).thenAnswer(invocation -> invocation.getArgument(0));

		RecordDraftResponse response = service.create(create("1-1", "  통합과학1 ", null, "PAST", "실험 보고서를 썼다"));

		assertThat(response.status()).isEqualTo("CONFIRMED");
		assertThat(response.source()).isEqualTo("PAST");
		assertThat(response.subjectName()).isEqualTo("통합과학1");
		assertThat(response.courseId()).isNull();
	}

	@Test
	void 과거_기록인데_내용이_모두_비면_400() {
		ValidationException exception = assertThrows(ValidationException.class, () -> service.create(create("1-1", "통합과학1", null, "PAST", "  ")));
		assertThat(exception.getMessage()).isEqualTo("과거 기록은 내용을 한 칸 이상 입력하세요.");
		verify(draftRepository, never()).save(any());
	}

	@Test
	void 같은_학기_같은_과목은_다시_만들_수_없다() {
		when(draftRepository.existsByStudentIdAndSemesterAndSubjectName(DemoStudent.ID, "1-1", "통합과학1")).thenReturn(true);

		ValidationException exception = assertThrows(ValidationException.class, () -> service.create(create("1-1", "통합과학1", null, "PAST", "내용")));

		assertThat(exception.getFieldErrors()).isEqualTo(Map.of("subjectName", "이 학기에 같은 과목의 세특이 이미 있습니다."));
	}

	@Test
	void 학기_종류_과목명이_틀리면_400() {
		ValidationException exception = assertThrows(ValidationException.class, () -> service.create(create("9-9", " ", null, "OTHER", "")));
		assertThat(exception.getFieldErrors()).isEqualTo(Map.of(
				"semester", "학기를 선택하세요.",
				"source", "기록 종류(NEW 또는 PAST)를 선택하세요.",
				"subjectName", "과목명을 입력하세요."));
	}

	@Test
	void 다른_학기에_등록한_과목으로_만들면_400_없는_과목이면_404() {
		StudentCourse math = course(12L, "공통수학2", CourseType.COMMON, 1, 2);
		when(studentCourseRepository.findById(12L)).thenReturn(Optional.of(math));
		ValidationException wrongSemester = assertThrows(ValidationException.class, () -> service.create(create("2-1", null, "12", "NEW", "")));
		assertThat(wrongSemester.getFieldErrors()).containsEntry("courseId", "선택한 학기에 등록한 과목이 아닙니다.");

		when(studentCourseRepository.findById(999L)).thenReturn(Optional.empty());
		assertThrows(NotFoundException.class, () -> service.create(create("1-2", null, "999", "NEW", "")));
	}

	// 저장·제출

	@Test
	void 작성_중이거나_수정_요청이면_저장할_수_있다() {
		RecordDraft draft = draft(5L, "1-2", "공통수학2", 12L, DraftSource.NEW, DraftStatus.REVISION_REQUESTED, EMPTY);
		when(draftRepository.findById(5L)).thenReturn(Optional.of(draft));

		RecordDraftResponse response = service.save("5", new SaveRecordDraftRequest("동기", "과정", "결과", "간호학과", " ", null));

		assertThat(response.motive()).isEqualTo("동기");
		assertThat(response.careerLink()).isEqualTo("간호학과");
		assertThat(response.guidelineTitle()).isNull();
		assertThat(response.status()).isEqualTo("REVISION_REQUESTED");
	}

	@Test
	void 제출했거나_확인된_세특은_저장도_제출도_거부() {
		for (DraftStatus status : List.of(DraftStatus.SUBMITTED, DraftStatus.CONFIRMED)) {
			RecordDraft draft = draft(5L, "1-2", "공통수학2", 12L, DraftSource.NEW, status, new DraftContent("동기", "", "", "", null, null));
			when(draftRepository.findById(5L)).thenReturn(Optional.of(draft));

			ValidationException saveError = assertThrows(ValidationException.class,
					() -> service.save("5", new SaveRecordDraftRequest("바꿈", "", "", "", null, null)));
			assertThat(saveError.getMessage()).isEqualTo("제출했거나 확인이 끝난 세특은 수정할 수 없습니다.");
			assertThrows(ValidationException.class, () -> service.submit("5"));
			assertThat(draft.getMotive()).isEqualTo("동기");
			assertThat(draft.getStatus()).isEqualTo(status);
		}
	}

	@Test
	void 제출하면_SUBMITTED와_제출_시각() {
		RecordDraft draft = draft(5L, "1-2", "공통수학2", 12L, DraftSource.NEW, DraftStatus.DRAFT, new DraftContent("", "", "결과만 썼다", "", null, null));
		when(draftRepository.findById(5L)).thenReturn(Optional.of(draft));

		RecordDraftResponse response = service.submit("5");

		assertThat(response.status()).isEqualTo("SUBMITTED");
		assertThat(response.submittedAt()).isNotNull();
	}

	@Test
	void 네_칸이_모두_비면_제출_거부() {
		RecordDraft draft = draft(5L, "1-2", "공통수학2", 12L, DraftSource.NEW, DraftStatus.DRAFT, new DraftContent(" ", "", "\n", "", null, null));
		when(draftRepository.findById(5L)).thenReturn(Optional.of(draft));

		ValidationException exception = assertThrows(ValidationException.class, () -> service.submit("5"));

		assertThat(exception.getMessage()).isEqualTo("내용을 한 칸 이상 입력한 뒤 제출하세요.");
		assertThat(draft.getStatus()).isEqualTo(DraftStatus.DRAFT);
	}

	// 제출 취소

	@Test
	void 제출_취소하면_DRAFT로_돌아가고_제출_시각이_지워지며_다시_저장할_수_있다() {
		RecordDraft draft = draft(5L, "1-2", "공통수학2", 12L, DraftSource.NEW, DraftStatus.DRAFT, new DraftContent("동기", "", "", "", null, null));
		draft.submit(LocalDateTime.of(2026, 11, 20, 10, 0));
		ReflectionTestUtils.setField(draft, "teacherComment", "구체적으로");
		when(draftRepository.findById(5L)).thenReturn(Optional.of(draft));

		RecordDraftResponse response = service.withdraw("5");

		assertThat(response.status()).isEqualTo("DRAFT");
		assertThat(response.submittedAt()).isNull();
		assertThat(response.teacherComment()).isEqualTo("구체적으로");
		assertThat(service.save("5", new SaveRecordDraftRequest("고친 동기", "", "", "", null, null)).motive()).isEqualTo("고친 동기");
	}

	@Test
	void 제출_상태가_아니면_제출_취소_거부_확인_완료와_과거_기록은_계속_수정_불가() {
		for (DraftStatus status : List.of(DraftStatus.DRAFT, DraftStatus.REVISION_REQUESTED, DraftStatus.CONFIRMED)) {
			RecordDraft draft = draft(5L, "1-2", "공통수학2", 12L, DraftSource.NEW, status, new DraftContent("동기", "", "", "", null, null));
			when(draftRepository.findById(5L)).thenReturn(Optional.of(draft));

			ValidationException exception = assertThrows(ValidationException.class, () -> service.withdraw("5"));

			assertThat(exception.getMessage()).isEqualTo("제출한 세특만 제출을 취소할 수 있습니다.");
			assertThat(exception.getFieldErrors()).isEmpty();
			assertThat(draft.getStatus()).isEqualTo(status);
		}

		RecordDraft past = draft(7L, "1-1", "독서", null, DraftSource.PAST, DraftStatus.CONFIRMED, new DraftContent("기록", "", "", "", null, null));
		when(draftRepository.findById(7L)).thenReturn(Optional.of(past));
		assertThrows(ValidationException.class, () -> service.withdraw("7"));
		assertThrows(ValidationException.class, () -> service.save("7", new SaveRecordDraftRequest("바꿈", "", "", "", null, null)));
		assertThat(past.getStatus()).isEqualTo(DraftStatus.CONFIRMED);
		assertThat(past.getMotive()).isEqualTo("기록");
	}

	@Test
	void 없는_세특_제출_취소는_404() {
		when(draftRepository.findById(404L)).thenReturn(Optional.empty());
		assertThrows(NotFoundException.class, () -> service.withdraw("404"));
	}

	@Test
	void 없는_세특이면_404() {
		when(draftRepository.findById(404L)).thenReturn(Optional.empty());
		assertThrows(NotFoundException.class, () -> service.get("404"));
		assertThrows(NotFoundException.class, () -> service.get("abc"));
	}

}
