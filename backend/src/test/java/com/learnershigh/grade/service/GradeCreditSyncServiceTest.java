package com.learnershigh.grade.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.learnershigh.credit.entity.Assessment;
import com.learnershigh.credit.entity.CourseCatalog;
import com.learnershigh.credit.entity.CourseType;
import com.learnershigh.credit.entity.StudentCourse;
import com.learnershigh.credit.repository.AssessmentRepository;
import com.learnershigh.credit.repository.CourseCatalogRepository;
import com.learnershigh.credit.repository.StudentCourseRepository;
import com.learnershigh.grade.entity.GradeRecord;

// 성적표 원점수를 credit 모듈에 반영하는 매칭·자동 등록 규칙 (docs/PROGRESS.md 설계 결정 참고)
@ExtendWith(MockitoExtension.class)
class GradeCreditSyncServiceTest {

	@Mock
	private StudentCourseRepository studentCourseRepository;

	@Mock
	private CourseCatalogRepository courseCatalogRepository;

	@Mock
	private AssessmentRepository assessmentRepository;

	private GradeCreditSyncService service;

	@BeforeEach
	void setUp() {
		service = new GradeCreditSyncService(studentCourseRepository, courseCatalogRepository, assessmentRepository);
	}

	private GradeRecord record(String subjectName, Integer rawScore) {
		return new GradeRecord("0", 1, 1, subjectName, rawScore, null, null, null, "MANUAL", null);
	}

	@Test
	void 등록된_과목의_기존_평가를_원점수_비례로_덮어쓴다() {
		StudentCourse course = mock(StudentCourse.class);
		when(course.getId()).thenReturn(1L);
		when(course.getCatalogId()).thenReturn(null);
		when(course.getCustomName()).thenReturn("공통국어1");
		when(studentCourseRepository.findByStudentIdAndGradeYearAndTermOrderByIdAsc("0", 1, 1)).thenReturn(List.of(course));

		Assessment written = mock(Assessment.class);
		when(written.getKind()).thenReturn("WRITTEN");
		when(written.getTitle()).thenReturn("중간고사");
		when(written.getWeightPercent()).thenReturn(30);
		when(written.getMaxScore()).thenReturn(100);
		when(written.getEvaluatedOn()).thenReturn(java.time.LocalDate.of(2026, 4, 1));
		when(assessmentRepository.findByStudentCourseIdOrderByIdAsc(1L)).thenReturn(List.of(written));

		GradeRecord record = record("공통국어1", 90);
		service.sync(record);

		verify(written).update("WRITTEN", "중간고사", 30, 100, 90, java.time.LocalDate.of(2026, 4, 1));
		assertThat(record.getAppliedToCredit()).isTrue();
		assertThat(record.getMatchedStudentCourseId()).isEqualTo(1L);
		assertThat(record.getUnmatchedReason()).isNull();
	}

	@Test
	void 매칭된_과목에_평가가_없으면_반영하지_않는다() {
		StudentCourse course = mock(StudentCourse.class);
		when(course.getId()).thenReturn(1L);
		when(course.getCatalogId()).thenReturn(null);
		when(course.getCustomName()).thenReturn("공통국어1");
		when(studentCourseRepository.findByStudentIdAndGradeYearAndTermOrderByIdAsc("0", 1, 1)).thenReturn(List.of(course));
		when(assessmentRepository.findByStudentCourseIdOrderByIdAsc(1L)).thenReturn(List.of());

		GradeRecord record = record("공통국어1", 90);
		service.sync(record);

		assertThat(record.getAppliedToCredit()).isFalse();
		assertThat(record.getUnmatchedReason()).isEqualTo("NO_ASSESSMENT");
		assertThat(record.getMatchedStudentCourseId()).isEqualTo(1L);
	}

	@Test
	void 같은_이름의_과목이_여러건이면_반영하지_않는다() {
		StudentCourse course1 = mock(StudentCourse.class);
		when(course1.getCatalogId()).thenReturn(null);
		when(course1.getCustomName()).thenReturn("공통국어1");
		StudentCourse course2 = mock(StudentCourse.class);
		when(course2.getCatalogId()).thenReturn(null);
		when(course2.getCustomName()).thenReturn("공통국어1");
		when(studentCourseRepository.findByStudentIdAndGradeYearAndTermOrderByIdAsc("0", 1, 1)).thenReturn(List.of(course1, course2));

		GradeRecord record = record("공통국어1", 90);
		service.sync(record);

		assertThat(record.getAppliedToCredit()).isFalse();
		assertThat(record.getUnmatchedReason()).isEqualTo("AMBIGUOUS");
		verify(assessmentRepository, never()).findByStudentCourseIdOrderByIdAsc(any());
	}

	@Test
	void 등록된_과목이_없어도_표준_과목_목록에_있으면_새로_등록하고_반영한다() {
		when(studentCourseRepository.findByStudentIdAndGradeYearAndTermOrderByIdAsc("0", 1, 1)).thenReturn(List.of());

		CourseCatalog catalog = mock(CourseCatalog.class);
		when(catalog.getId()).thenReturn(50L);
		when(catalog.getName()).thenReturn("독서와 작문");
		when(catalog.getCourseType()).thenReturn(CourseType.GENERAL);
		when(courseCatalogRepository.findAllByOrderByIdAsc()).thenReturn(List.of(catalog));

		StudentCourse saved = mock(StudentCourse.class);
		when(saved.getId()).thenReturn(99L);
		when(studentCourseRepository.save(any(StudentCourse.class))).thenReturn(saved);

		GradeRecord record = record("독서와 작문", 92);
		service.sync(record);

		assertThat(record.getAppliedToCredit()).isTrue();
		assertThat(record.getMatchedStudentCourseId()).isEqualTo(99L);
		assertThat(record.getUnmatchedReason()).isNull();
		verify(assessmentRepository).save(any(Assessment.class));
	}

	@Test
	void 표준_과목_목록에도_없으면_매칭_실패로_남긴다() {
		when(studentCourseRepository.findByStudentIdAndGradeYearAndTermOrderByIdAsc("0", 1, 1)).thenReturn(List.of());
		when(courseCatalogRepository.findAllByOrderByIdAsc()).thenReturn(List.of());

		GradeRecord record = record("존재하지않는과목", 92);
		service.sync(record);

		assertThat(record.getAppliedToCredit()).isFalse();
		assertThat(record.getUnmatchedReason()).isEqualTo("NOT_FOUND");
		verify(studentCourseRepository, never()).save(any());
	}

	@Test
	void 전문교과는_과목_유형_제약때문에_자동_등록하지_않는다() {
		when(studentCourseRepository.findByStudentIdAndGradeYearAndTermOrderByIdAsc("0", 1, 1)).thenReturn(List.of());

		CourseCatalog catalog = mock(CourseCatalog.class);
		when(catalog.getName()).thenReturn("전문 수학");
		when(catalog.getCourseType()).thenReturn(CourseType.SPECIALIZED);
		when(courseCatalogRepository.findAllByOrderByIdAsc()).thenReturn(List.of(catalog));

		GradeRecord record = record("전문 수학", 92);
		service.sync(record);

		assertThat(record.getAppliedToCredit()).isFalse();
		assertThat(record.getUnmatchedReason()).isEqualTo("NOT_FOUND");
		verify(studentCourseRepository, never()).save(any());
	}

	@Test
	void 원점수가_없으면_매칭을_시도하지_않는다() {
		GradeRecord record = record("공통국어1", null);

		service.sync(record);

		assertThat(record.getAppliedToCredit()).isFalse();
		assertThat(record.getUnmatchedReason()).isNull();
		verify(studentCourseRepository, never()).findByStudentIdAndGradeYearAndTermOrderByIdAsc(any(), any(), any());
	}

}
