package com.learnershigh.credit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import com.learnershigh.common.exception.ValidationException;
import com.learnershigh.credit.dto.CourseItemResponse;
import com.learnershigh.credit.entity.CourseStatus;
import com.learnershigh.credit.entity.StudentCourse;
import com.learnershigh.credit.repository.AssessmentRepository;
import com.learnershigh.credit.repository.StudentCourseRepository;

// 이수 상태 변경·과목 삭제 (api-contract.md 4.7, 4.8)
@ExtendWith(MockitoExtension.class)
class CourseManagementServiceTest {

	@Mock
	private StudentCourseFinder courseFinder;

	@Mock
	private StudentCourseRepository studentCourseRepository;

	@Mock
	private AssessmentRepository assessmentRepository;

	@Mock
	private CourseItemAssembler assembler;

	private CourseManagementService service;

	@BeforeEach
	void setUp() {
		service = new CourseManagementService(courseFinder, studentCourseRepository, assessmentRepository, assembler);
	}

	@Test
	void 이수상태를_바꾸고_바뀐_과목을_돌려준다() {
		StudentCourse course = mock(StudentCourse.class);
		when(course.getId()).thenReturn(16L);
		when(courseFinder.findOrThrow("16")).thenReturn(course);
		when(studentCourseRepository.save(course)).thenReturn(course);
		when(assessmentRepository.findByStudentCourseIdIn(List.of(16L))).thenReturn(List.of());
		CourseItemResponse expected = new CourseItemResponse("16", "통합과학2", "COMMON", 4, "COMPLETED", 35.0, "RISK", 45.0);
		when(assembler.toItem(course, List.of())).thenReturn(expected);

		CourseItemResponse response = service.changeStatus("16", "COMPLETED");

		verify(course).changeStatus(CourseStatus.COMPLETED);
		assertThat(response).isSameAs(expected);
	}

	@Test
	void 잘못된_상태값이면_400_과목을_바꾸지_않는다() {
		StudentCourse course = mock(StudentCourse.class);
		when(courseFinder.findOrThrow("16")).thenReturn(course);

		ValidationException exception = assertThrows(ValidationException.class, () -> service.changeStatus("16", "DONE"));

		assertThat(exception.getFieldErrors()).containsEntry("status", "이수 상태를 선택하세요.");
		verify(course, never()).changeStatus(org.mockito.ArgumentMatchers.any());
		verify(studentCourseRepository, never()).save(org.mockito.ArgumentMatchers.any());
	}

	@Test
	void 과목과_평가를_함께_지운다() {
		StudentCourse course = mock(StudentCourse.class);
		when(course.getId()).thenReturn(22L);
		when(courseFinder.findOrThrow("22")).thenReturn(course);

		service.delete("22");

		verify(assessmentRepository).deleteByStudentCourseId(22L);
		verify(studentCourseRepository).delete(course);
	}

}
