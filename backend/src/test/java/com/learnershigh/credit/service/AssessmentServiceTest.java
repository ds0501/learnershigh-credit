package com.learnershigh.credit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.learnershigh.common.exception.NotFoundException;
import com.learnershigh.common.exception.ValidationException;
import com.learnershigh.credit.dto.AssessmentRequest;
import com.learnershigh.credit.dto.AssessmentResponse;
import com.learnershigh.credit.entity.Assessment;
import com.learnershigh.credit.entity.StudentCourse;
import com.learnershigh.credit.repository.AssessmentRepository;

// 평가 추가·수정·삭제 검증 규칙 (api-contract.md 4.9~4.11). 통합과학2(지필 60%, 수행 40%) 예시를 그대로 쓴다
@ExtendWith(MockitoExtension.class)
class AssessmentServiceTest {

	@Mock
	private StudentCourseFinder courseFinder;

	@Mock
	private AssessmentRepository assessmentRepository;

	private AssessmentService service;

	private StudentCourse course;

	@BeforeEach
	void setUp() {
		service = new AssessmentService(courseFinder, assessmentRepository);
		course = mock(StudentCourse.class);
		when(course.getId()).thenReturn(16L);
		lenient().when(course.getWrittenWeight()).thenReturn(60);
		lenient().when(course.getPerformanceWeight()).thenReturn(40);
		when(courseFinder.findOrThrow("16")).thenReturn(course);
	}

	private AssessmentRequest request(String kind, String title, Integer weightPercent, Integer maxScore, Integer score, String evaluatedOn) {
		return new AssessmentRequest(kind, title, weightPercent, maxScore, score, evaluatedOn);
	}

	// 반영 비율 합 계산에서 쓰는 기존 평가 하나 (id, kind, weightPercent만 중요)
	private Assessment existing(long id, String kind, int weightPercent) {
		Assessment assessment = mock(Assessment.class);
		lenient().when(assessment.getId()).thenReturn(id);
		lenient().when(assessment.getKind()).thenReturn(kind);
		lenient().when(assessment.getWeightPercent()).thenReturn(weightPercent);
		return assessment;
	}

	// ownership 확인(findOwnedAssessment)에서만 쓰는 평가 (studentCourseId만 중요)
	private Assessment ofOtherCourse(long studentCourseId) {
		Assessment assessment = mock(Assessment.class);
		when(assessment.getStudentCourseId()).thenReturn(studentCourseId);
		return assessment;
	}

	@Test
	void 평가추가_성공() {
		Assessment written = existing(37, "WRITTEN", 30);
		Assessment performance = existing(38, "PERFORMANCE", 20);
		when(assessmentRepository.findByStudentCourseIdOrderByIdAsc(16L)).thenReturn(List.of(written, performance));
		when(assessmentRepository.save(ArgumentMatchers.any())).thenAnswer(invocation -> {
			Assessment saved = invocation.getArgument(0);
			assertThat(saved.getTitle()).isEqualTo("수행평가 2");
			assertThat(saved.getTaskId()).isNull();
			return new Assessment(16L, saved.getKind(), saved.getTitle(), saved.getWeightPercent(), saved.getMaxScore(), saved.getScore(), null, saved.getEvaluatedOn());
		});

		AssessmentResponse response = service.add("16", request("PERFORMANCE", "  수행평가 2  ", 20, 100, 80, "2026-10-28"));

		assertThat(response.kind()).isEqualTo("PERFORMANCE");
		assertThat(response.title()).isEqualTo("수행평가 2");
		assertThat(response.score()).isEqualTo(80);
		assertThat(response.evaluatedOn()).isEqualTo("2026-10-28");
		assertThat(response.taskId()).isNull();
	}

	@Test
	void 평가이름이_공백이면_검증오류() {
		when(assessmentRepository.findByStudentCourseIdOrderByIdAsc(16L)).thenReturn(List.of());

		ValidationException exception = assertThrows(ValidationException.class,
				() -> service.add("16", request("WRITTEN", "   ", 10, 100, 50, "2026-10-28")));

		assertThat(exception.getFieldErrors()).containsEntry("title", "평가 이름을 입력하세요.");
	}

	@Test
	void 날짜형식이_잘못되면_검증오류() {
		when(assessmentRepository.findByStudentCourseIdOrderByIdAsc(16L)).thenReturn(List.of());

		ValidationException exception = assertThrows(ValidationException.class,
				() -> service.add("16", request("WRITTEN", "기말고사", 10, 100, 50, "2026/10/28")));

		assertThat(exception.getFieldErrors()).containsEntry("evaluatedOn", "날짜를 입력하세요.");
	}

	@Test
	void 만점이_0이하면_검증오류() {
		when(assessmentRepository.findByStudentCourseIdOrderByIdAsc(16L)).thenReturn(List.of());

		ValidationException exception = assertThrows(ValidationException.class,
				() -> service.add("16", request("WRITTEN", "기말고사", 10, 0, null, "2026-10-28")));

		assertThat(exception.getFieldErrors()).containsEntry("maxScore", "만점은 0보다 커야 합니다.");
	}

	@Test
	void 득점이_만점보다_크면_검증오류() {
		when(assessmentRepository.findByStudentCourseIdOrderByIdAsc(16L)).thenReturn(List.of());

		ValidationException exception = assertThrows(ValidationException.class,
				() -> service.add("16", request("WRITTEN", "기말고사", 30, 100, 120, "2026-12-16")));

		assertThat(exception.getFieldErrors()).containsEntry("score", "득점은 만점(100점) 이하여야 합니다.");
	}

	@Test
	void 득점이_음수면_검증오류() {
		when(assessmentRepository.findByStudentCourseIdOrderByIdAsc(16L)).thenReturn(List.of());

		ValidationException exception = assertThrows(ValidationException.class,
				() -> service.add("16", request("WRITTEN", "기말고사", 30, 100, -1, "2026-12-16")));

		assertThat(exception.getFieldErrors()).containsEntry("score", "득점은 0 이상이어야 합니다.");
	}

	@Test
	void 반영비율이_0이하면_검증오류() {
		when(assessmentRepository.findByStudentCourseIdOrderByIdAsc(16L)).thenReturn(List.of());

		ValidationException exception = assertThrows(ValidationException.class,
				() -> service.add("16", request("WRITTEN", "기말고사", 0, 100, 50, "2026-12-16")));

		assertThat(exception.getFieldErrors()).containsEntry("weightPercent", "반영 비율은 0보다 커야 합니다.");
	}

	@Test
	void 반영비율_합이_한도를_넘으면_검증오류() {
		Assessment first = existing(38, "PERFORMANCE", 20);
		Assessment second = existing(45, "PERFORMANCE", 20);
		when(assessmentRepository.findByStudentCourseIdOrderByIdAsc(16L)).thenReturn(List.of(first, second));

		ValidationException exception = assertThrows(ValidationException.class,
				() -> service.add("16", request("PERFORMANCE", "수행평가 3", 10, 100, null, "2026-11-20")));

		assertThat(exception.getFieldErrors())
				.containsEntry("weightPercent", "수행 반영 비율 합이 40%를 넘습니다. (이미 40% 사용, 남은 0%)");
	}

	@Test
	void 수정할때는_자기자신의_반영비율은_빼고_합을_센다() {
		Assessment editing = existing(45, "PERFORMANCE", 20);
		when(editing.getStudentCourseId()).thenReturn(16L);
		// update() 호출 뒤의 모양(실제 엔티티는 메서드 호출로 바뀌지만, mock은 상태가 없어 미리 결과값을 지정한다)
		when(editing.getScore()).thenReturn(60);
		when(editing.getEvaluatedOn()).thenReturn(LocalDate.of(2026, 10, 28));
		Assessment other = existing(38, "PERFORMANCE", 20);
		when(assessmentRepository.findById(45L)).thenReturn(Optional.of(editing));
		when(assessmentRepository.findByStudentCourseIdOrderByIdAsc(16L)).thenReturn(List.of(other, editing));
		when(assessmentRepository.save(editing)).thenReturn(editing);

		AssessmentResponse response = service.update("16", "45", request("PERFORMANCE", "수행평가 2", 20, 100, 60, "2026-10-28"));

		verify(editing).update("PERFORMANCE", "수행평가 2", 20, 100, 60, LocalDate.of(2026, 10, 28));
		assertThat(response.score()).isEqualTo(60);
	}

	@Test
	void 수정대상이_다른과목의_평가면_404() {
		Assessment other = ofOtherCourse(13L);
		when(assessmentRepository.findById(45L)).thenReturn(Optional.of(other));

		NotFoundException exception = assertThrows(NotFoundException.class,
				() -> service.update("16", "45", request("PERFORMANCE", "수행평가 2", 20, 100, 60, "2026-10-28")));

		assertThat(exception.getMessage()).isEqualTo("평가를 찾을 수 없습니다.");
		verify(assessmentRepository, never()).save(ArgumentMatchers.any());
	}

	@Test
	void 평가삭제_성공() {
		Assessment target = ofOtherCourse(16L);
		when(assessmentRepository.findById(45L)).thenReturn(Optional.of(target));

		service.delete("16", "45");

		verify(assessmentRepository).delete(target);
	}

	@Test
	void 삭제대상이_그과목의_평가가_아니면_404() {
		Assessment other = ofOtherCourse(13L);
		when(assessmentRepository.findById(45L)).thenReturn(Optional.of(other));

		assertThrows(NotFoundException.class, () -> service.delete("16", "45"));
		verify(assessmentRepository, never()).delete(ArgumentMatchers.any());
	}

}
