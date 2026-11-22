package com.learnershigh.record.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import com.learnershigh.record.entity.DraftContent;
import com.learnershigh.record.entity.DraftSource;
import com.learnershigh.record.entity.DraftStatus;
import com.learnershigh.record.entity.RecordDraft;
import com.learnershigh.record.repository.RecordDraftRepository;

// 세특 방향 추천·채택. AI 호출은 가짜(FakeAi)로 바꿔 실제 API를 부르지 않는다
@ExtendWith(MockitoExtension.class)
class RecordGuidelineServiceTest {

	@Mock
	private RecordDraftRepository draftRepository;

	@Mock
	private StudentCourseRepository studentCourseRepository;

	@Mock
	private CourseCatalogRepository catalogRepository;

	@Mock
	private CourseCatalogService catalogService;

	@Mock
	private DepartmentSubjectRepository departmentSubjectRepository;

	@Mock
	private StudentGoalService goalService;

	private FakeAi ai;
	private RecordGuidelineService service;

	// AI 대신 정해 둔 결과를 돌려주고, 받은 입력을 기록한다
	static class FakeAi implements GuidelineAiClient {
		Optional<List<GuidelineSuggestion>> result = Optional.empty();
		final List<GuidelineContext> received = new ArrayList<>();

		@Override
		public Optional<List<GuidelineSuggestion>> suggest(GuidelineContext context) {
			received.add(context);
			return result;
		}
	}

	@BeforeEach
	void setUp() {
		ai = new FakeAi();
		service = new RecordGuidelineService(draftRepository, studentCourseRepository, catalogRepository, catalogService,
				departmentSubjectRepository, goalService, ai);
	}

	private static GuidelineSuggestion suggestion(String title) {
		return new GuidelineSuggestion(title, "이유", List.of("예시1", "예시2"), List.of("포인트1", "포인트2"));
	}

	private RecordDraft draft(long id, Long courseId, String subjectName, DraftStatus status) {
		RecordDraft draft = RecordDraft.create(DemoStudent.ID, "1-2", subjectName, courseId, DraftSource.NEW,
				new DraftContent("동기", "과정", "", "", null, null), LocalDateTime.of(2026, 11, 1, 9, 0));
		ReflectionTestUtils.setField(draft, "id", id);
		ReflectionTestUtils.setField(draft, "status", status);
		lenient().when(draftRepository.findById(id)).thenReturn(Optional.of(draft));
		return draft;
	}

	// 희망 학과 컴퓨터공학과(26), 목표 대학 서울대학교. 로드맵에서 통합과학2(catalog 12)가 핵심, 8/12곳
	private void goalWithRoadmap() {
		when(goalService.getGoal()).thenReturn(
				new StudentGoalResponse(new StudentGoalResponse.GoalDepartment("26", "컴퓨터공학과", "공학"), "서울대학교"));
		DepartmentSubject roadmap = mock(DepartmentSubject.class);
		lenient().when(roadmap.getCatalogId()).thenReturn(12L);
		lenient().when(roadmap.getDesignationType()).thenReturn("CORE");
		lenient().when(roadmap.getRequiredUniversityCount()).thenReturn(8);
		lenient().when(roadmap.getTotalUniversityCount()).thenReturn(12);
		lenient().when(roadmap.getRatioPercent()).thenReturn(new BigDecimal("66.67"));
		when(departmentSubjectRepository.findByDepartmentIdOrderByRatioPercentDesc(26L)).thenReturn(List.of(roadmap));
	}

	private StudentCourse course(long id, long catalogId, String name) {
		StudentCourse course = mock(StudentCourse.class);
		lenient().when(course.getId()).thenReturn(id);
		lenient().when(course.getCatalogId()).thenReturn(catalogId);
		lenient().when(course.getStudentId()).thenReturn(DemoStudent.ID);
		lenient().when(catalogService.resolveCourseName(course)).thenReturn(name);
		lenient().when(studentCourseRepository.findById(id)).thenReturn(Optional.of(course));
		return course;
	}

	@Test
	void AI가_3안을_주면_그대로_돌려주고_입력에_희망_학과_목표_대학_과목_로드맵_지정이_들어간다() {
		goalWithRoadmap();
		course(16L, 12L, "통합과학2");
		draft(5L, 16L, "통합과학2", DraftStatus.DRAFT);
		ai.result = Optional.of(List.of(suggestion("가"), suggestion("나"), suggestion("다")));

		GuidelineSuggestionsResponse response = service.suggestForDraft("5");

		assertThat(response.generatedBy()).isEqualTo("AI");
		assertThat(response.suggestions()).extracting(GuidelineSuggestion::title).containsExactly("가", "나", "다");
		GuidelineContext input = ai.received.get(0);
		assertThat(input.subjectName()).isEqualTo("통합과학2");
		assertThat(input.departmentName()).isEqualTo("컴퓨터공학과");
		assertThat(input.targetUniversity()).isEqualTo("서울대학교");
		assertThat(input.designationType()).isEqualTo("CORE");
		assertThat(input.designationLabel()).isEqualTo("핵심");
		assertThat(ClaudeGuidelineAiClient.userMessage(input))
				.contains("과목: 통합과학2", "희망 학과: 컴퓨터공학과", "목표 대학: 서울대학교", "핵심 과목 (조사 대학 12곳 중 8곳 지정)", "완성된 세특 문장은 쓰지 마");
	}

	@Test
	void 프롬프트에_완성된_세특_문장을_쓰지_말라고_명시한다() {
		assertThat(ClaudeGuidelineAiClient.SYSTEM_PROMPT).contains("완성된 세특 문장을 절대 쓰지 마라", "방향만 제안한다");
	}

	@Test
	void AI가_실패하면_로드맵_기반_고정_안내_3안으로_대체() {
		goalWithRoadmap();
		course(16L, 12L, "통합과학2");
		draft(5L, 16L, "통합과학2", DraftStatus.DRAFT);
		ai.result = Optional.empty();

		GuidelineSuggestionsResponse response = service.suggestForDraft("5");

		assertThat(response.generatedBy()).isEqualTo("FALLBACK");
		assertThat(response.suggestions()).hasSize(3);
		assertThat(response.suggestions().get(0).reason()).contains("컴퓨터공학과 조사 대학 12곳 중 8곳이 통합과학2를 핵심 과목으로 둬요.");
		response.suggestions().forEach(item -> {
			assertThat(item.examples()).hasSizeBetween(2, 3);
			assertThat(item.points()).hasSizeBetween(2, 3);
		});
	}

	@Test
	void AI_응답_모양이_틀리면_고정_안내로_대체_4안_이상이면_앞_3안만() {
		when(goalService.getGoal()).thenReturn(null);
		draft(5L, null, "독서", DraftStatus.DRAFT);

		ai.result = Optional.of(List.of(suggestion("가"), suggestion("나")));
		assertThat(service.suggestForDraft("5").generatedBy()).isEqualTo("FALLBACK");

		ai.result = Optional.of(List.of(suggestion("가"), suggestion("나"),
				new GuidelineSuggestion("다", "이유", List.of("예시 하나뿐"), List.of("포인트1", "포인트2"))));
		assertThat(service.suggestForDraft("5").generatedBy()).isEqualTo("FALLBACK");

		ai.result = Optional.of(List.of(suggestion("가"), suggestion("나"), suggestion("다"), suggestion("라")));
		GuidelineSuggestionsResponse response = service.suggestForDraft("5");
		assertThat(response.generatedBy()).isEqualTo("AI");
		assertThat(response.suggestions()).extracting(GuidelineSuggestion::title).containsExactly("가", "나", "다");
	}

	@Test
	void 고정_안내의_조사는_받침에_맞춘다() {
		assertThat(FallbackGuidelines.josa("음악", "과", "와")).isEqualTo("과");
		assertThat(FallbackGuidelines.josa("확률과 통계", "을", "를")).isEqualTo("를");
		assertThat(FallbackGuidelines.josa("소프트웨어학과", "을", "를")).isEqualTo("를");
	}

	@Test
	void 숫자로_끝나는_과목명은_숫자를_읽는_소리로_조사를_고른다() {
		// 받침 없음: 2(이) 4(사) 5(오) 9(구)
		assertThat("공통수학2" + FallbackGuidelines.josa("공통수학2", "을", "를")).isEqualTo("공통수학2를");
		assertThat("공통수학2" + FallbackGuidelines.josa("공통수학2", "과", "와")).isEqualTo("공통수학2와");
		assertThat("한국사2" + FallbackGuidelines.josa("한국사2", "을", "를")).isEqualTo("한국사2를");
		assertThat("과학탐구실험2" + FallbackGuidelines.josa("과학탐구실험2", "을", "를")).isEqualTo("과학탐구실험2를");
		for (String digit : List.of("2", "4", "5", "9")) {
			assertThat(FallbackGuidelines.josa("과목" + digit, "을", "를")).as(digit).isEqualTo("를");
		}
		// 받침 있음: 0(영) 1(일) 3(삼) 6(육) 7(칠) 8(팔)
		assertThat("물리학1" + FallbackGuidelines.josa("물리학1", "을", "를")).isEqualTo("물리학1을");
		assertThat("물리학1" + FallbackGuidelines.josa("물리학1", "과", "와")).isEqualTo("물리학1과");
		for (String digit : List.of("0", "1", "3", "6", "7", "8")) {
			assertThat(FallbackGuidelines.josa("과목" + digit, "을", "를")).as(digit).isEqualTo("을");
		}
	}

	@Test
	void 로마_숫자와_영문자도_읽는_소리로_판단하고_모르면_둘_다_쓴다() {
		assertThat(FallbackGuidelines.josa("영어Ⅰ", "을", "를")).isEqualTo("을");
		assertThat(FallbackGuidelines.josa("미적분Ⅱ", "을", "를")).isEqualTo("를");
		assertThat(FallbackGuidelines.josa("AI 기초", "을", "를")).isEqualTo("를");
		assertThat(FallbackGuidelines.josa("AI", "을", "를")).isEqualTo("를");
		assertThat(FallbackGuidelines.josa("Python", "을", "를")).isEqualTo("을");
		assertThat(FallbackGuidelines.josa("생명과학(심화)", "을", "를")).isEqualTo("를");
		assertThat(FallbackGuidelines.josa("음악 ", "과", "와")).isEqualTo("과");
		assertThat(FallbackGuidelines.josa("C++", "을", "를")).isEqualTo("을(를)");
		assertThat(FallbackGuidelines.josa("", "을", "를")).isEqualTo("을(를)");
	}

	@Test
	void 고정_안내_문장에_숫자_과목명의_조사가_자연스럽게_나온다() {
		GuidelineContext notInRoadmap = new GuidelineContext("공통수학2", "소프트웨어학과", "공학", null, null, null, null, null);
		List<GuidelineSuggestion> suggestions = FallbackGuidelines.build(notInRoadmap);
		assertThat(suggestions.get(0).reason()).startsWith("소프트웨어학과 로드맵에서 공통수학2를 지정한 대학은 없지만");
		assertThat(suggestions.get(1).title()).isEqualTo("공통수학2와 소프트웨어학과를 잇는 탐구 주제 정하기");

		GuidelineContext inRoadmap = new GuidelineContext("물리학1", "컴퓨터공학과", "공학", null, "CORE", 8, 12, 66.67);
		assertThat(FallbackGuidelines.build(inRoadmap).get(0).reason()).startsWith("컴퓨터공학과 조사 대학 12곳 중 8곳이 물리학1을 핵심 과목으로 둬요.");
		assertThat(FallbackGuidelines.build(inRoadmap).get(1).title()).isEqualTo("물리학1과 컴퓨터공학과를 잇는 탐구 주제 정하기");
	}

	@Test
	void 희망_학과가_없으면_학과_없이_과목_중심_고정_안내() {
		when(goalService.getGoal()).thenReturn(null);
		draft(5L, null, "독서", DraftStatus.DRAFT);

		GuidelineSuggestionsResponse response = service.suggestForDraft("5");

		assertThat(ai.received.get(0).hasDepartment()).isFalse();
		assertThat(response.suggestions().get(0).reason()).startsWith("희망 학과가 아직 없어 과목 자체에 집중했어요.");
	}

	@Test
	void 초안이_없는_카드도_등록_과목으로_추천받는다_DB에는_만들지_않는다() {
		goalWithRoadmap();
		course(16L, 12L, "통합과학2");
		ai.result = Optional.of(List.of(suggestion("가"), suggestion("나"), suggestion("다")));

		GuidelineSuggestionsResponse response = service.suggestForSubject(new GuidelinePreviewRequest("1-2", null, "16"));

		assertThat(response.generatedBy()).isEqualTo("AI");
		assertThat(ai.received.get(0).subjectName()).isEqualTo("통합과학2");
		assertThat(ai.received.get(0).designationType()).isEqualTo("CORE");
		org.mockito.Mockito.verifyNoInteractions(draftRepository);
	}

	@Test
	void 초안_없는_추천도_과목명으로_로드맵을_찾는다() {
		goalWithRoadmap();
		CourseCatalog science = mock(CourseCatalog.class);
		when(science.getId()).thenReturn(12L);
		when(science.getName()).thenReturn("통합과학2");
		when(catalogRepository.findAllByOrderByIdAsc()).thenReturn(List.of(science));

		service.suggestForSubject(new GuidelinePreviewRequest("1-2", "통합 과학2", null));

		assertThat(ai.received.get(0).designationType()).isEqualTo("CORE");
	}

	@Test
	void 초안_없는_추천_입력이_틀리면_400_없는_과목이면_404() {
		ValidationException exception = assertThrows(ValidationException.class,
				() -> service.suggestForSubject(new GuidelinePreviewRequest("9-9", " ", null)));
		assertThat(exception.getFieldErrors()).isEqualTo(Map.of("semester", "학기를 선택하세요.", "subjectName", "과목명을 입력하세요."));

		when(studentCourseRepository.findById(999L)).thenReturn(Optional.empty());
		assertThrows(NotFoundException.class, () -> service.suggestForSubject(new GuidelinePreviewRequest("1-2", null, "999")));
	}

	@Test
	void 채택하면_guideline만_저장하고_4칸은_그대로() {
		RecordDraft draft = draft(5L, 16L, "통합과학2", DraftStatus.DRAFT);

		RecordDraftResponse response = service.adopt("5", new AdoptGuidelineRequest(" 실험 설계 깊게 파기 ", "이유: ...\n- 예시"));

		assertThat(response.guidelineTitle()).isEqualTo("실험 설계 깊게 파기");
		assertThat(response.guidelineDetail()).isEqualTo("이유: ...\n- 예시");
		assertThat(draft.getMotive()).isEqualTo("동기");
		assertThat(draft.getProcess()).isEqualTo("과정");
		assertThat(draft.getStatus()).isEqualTo(DraftStatus.DRAFT);
	}

	@Test
	void 제출했거나_확인된_세특에는_채택_불가_제목이_없으면_400_없는_초안은_404() {
		draft(5L, 16L, "통합과학2", DraftStatus.SUBMITTED);
		assertThrows(ValidationException.class, () -> service.adopt("5", new AdoptGuidelineRequest("방향", null)));
		draft(6L, 16L, "통합과학2", DraftStatus.CONFIRMED);
		assertThrows(ValidationException.class, () -> service.adopt("6", new AdoptGuidelineRequest("방향", null)));

		draft(7L, 16L, "통합과학2", DraftStatus.REVISION_REQUESTED);
		ValidationException blank = assertThrows(ValidationException.class, () -> service.adopt("7", new AdoptGuidelineRequest("  ", null)));
		assertThat(blank.getFieldErrors()).containsKey("title");

		when(draftRepository.findById(404L)).thenReturn(Optional.empty());
		assertThrows(NotFoundException.class, () -> service.adopt("404", new AdoptGuidelineRequest("방향", null)));
		assertThrows(NotFoundException.class, () -> service.suggestForDraft("abc"));
	}

}
