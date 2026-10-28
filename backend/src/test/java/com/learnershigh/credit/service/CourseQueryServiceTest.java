package com.learnershigh.credit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.learnershigh.common.DemoStudent;
import com.learnershigh.credit.dto.CourseItemResponse;
import com.learnershigh.credit.dto.RequiredCoursesResponse;
import com.learnershigh.credit.dto.SemesterCoursesResponse;
import com.learnershigh.credit.entity.CourseStatus;
import com.learnershigh.credit.entity.CourseType;
import com.learnershigh.credit.entity.StudentCourse;
import com.learnershigh.credit.repository.AssessmentRepository;
import com.learnershigh.credit.repository.StudentCourseRepository;

// api-contract.md 4.2, 4.3 응답 모양과 집계·필터 규칙을 확인한다
@ExtendWith(MockitoExtension.class)
class CourseQueryServiceTest {

	@Mock
	private StudentCourseRepository studentCourseRepository;

	@Mock
	private AssessmentRepository assessmentRepository;

	@Mock
	private CourseItemAssembler assembler;

	private CourseQueryService service;

	@BeforeEach
	void setUp() {
		service = new CourseQueryService(studentCourseRepository, assessmentRepository, assembler);
	}

	// getCredits()는 GET courses(totalCredits)에만, getCourseType()·getStatus()는 required-courses 필터에만 쓰인다
	private StudentCourse course(long id, CourseType courseType, Integer credits, CourseStatus status) {
		StudentCourse course = mock(StudentCourse.class);
		lenient().when(course.getId()).thenReturn(id);
		lenient().when(course.getCourseType()).thenReturn(courseType);
		lenient().when(course.getCredits()).thenReturn(credits);
		lenient().when(course.getStatus()).thenReturn(status);
		return course;
	}

	private CourseItemResponse item(long id) {
		return new CourseItemResponse(String.valueOf(id), "이름", "COMMON", 4, "IN_PROGRESS", null, "NO_DATA", null);
	}

	@Test
	void 학기별_과목_학점합과_항목을_돌려준다() {
		StudentCourse first = course(1L, CourseType.COMMON, 4, CourseStatus.IN_PROGRESS);
		StudentCourse second = course(2L, CourseType.GENERAL, 3, CourseStatus.IN_PROGRESS);
		when(studentCourseRepository.findByStudentIdAndGradeYearAndTermOrderByIdAsc(DemoStudent.ID, 1, 2))
				.thenReturn(List.of(first, second));
		when(assessmentRepository.findByStudentCourseIdIn(List.of(1L, 2L))).thenReturn(List.of());
		when(assembler.toItem(first, List.of())).thenReturn(item(1));
		when(assembler.toItem(second, List.of())).thenReturn(item(2));

		SemesterCoursesResponse response = service.getCourses(1, 2);

		assertThat(response.gradeYear()).isEqualTo(1);
		assertThat(response.term()).isEqualTo(2);
		assertThat(response.totalCredits()).isEqualTo(7);
		assertThat(response.courses()).extracting(CourseItemResponse::getId).containsExactly("1", "2");
	}

	@Test
	void 그학기에_과목이_없으면_빈목록과_0학점() {
		when(studentCourseRepository.findByStudentIdAndGradeYearAndTermOrderByIdAsc(DemoStudent.ID, 1, 2)).thenReturn(List.of());

		SemesterCoursesResponse response = service.getCourses(1, 2);

		assertThat(response.courses()).isEmpty();
		assertThat(response.totalCredits()).isZero();
	}

	@Test
	void 남은필수과목은_이수완료가_아닌_공통과목만_센다() {
		StudentCourse commonInProgress = course(1L, CourseType.COMMON, 4, CourseStatus.IN_PROGRESS);
		StudentCourse commonCompleted = course(2L, CourseType.COMMON, 4, CourseStatus.COMPLETED);
		StudentCourse generalInProgress = course(3L, CourseType.GENERAL, 3, CourseStatus.IN_PROGRESS);
		when(studentCourseRepository.findByStudentIdOrderByIdAsc(DemoStudent.ID))
				.thenReturn(List.of(commonInProgress, commonCompleted, generalInProgress));
		when(assessmentRepository.findByStudentCourseIdIn(List.of(1L))).thenReturn(List.of());
		when(assembler.toItem(commonInProgress, List.of())).thenReturn(item(1));

		RequiredCoursesResponse response = service.getRequiredCourses();

		assertThat(response.remainingCount()).isEqualTo(1);
		assertThat(response.courses()).extracting(CourseItemResponse::getId).containsExactly("1");
	}

	@Test
	void 공통과목이_모두_이수완료면_빈목록() {
		StudentCourse commonCompleted = course(1L, CourseType.COMMON, 4, CourseStatus.COMPLETED);
		when(studentCourseRepository.findByStudentIdOrderByIdAsc(DemoStudent.ID)).thenReturn(List.of(commonCompleted));

		RequiredCoursesResponse response = service.getRequiredCourses();

		assertThat(response.remainingCount()).isZero();
		assertThat(response.courses()).isEmpty();
	}

}
