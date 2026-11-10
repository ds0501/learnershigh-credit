package com.learnershigh.credit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.learnershigh.common.DemoStudent;
import com.learnershigh.common.exception.NotFoundException;
import com.learnershigh.credit.entity.StudentCourse;
import com.learnershigh.credit.repository.StudentCourseRepository;

// 경로의 courseId로 과목을 찾는 공용 로직 (api-contract.md 4.6~4.11 "없는 과목은 404")
@ExtendWith(MockitoExtension.class)
class StudentCourseFinderTest {

	@Mock
	private StudentCourseRepository studentCourseRepository;

	private StudentCourseFinder finder;

	@BeforeEach
	void setUp() {
		finder = new StudentCourseFinder(studentCourseRepository);
	}

	@Test
	void 있는_과목을_찾는다() {
		StudentCourse course = mock(StudentCourse.class);
		when(course.getStudentId()).thenReturn(DemoStudent.ID);
		when(studentCourseRepository.findById(16L)).thenReturn(Optional.of(course));

		assertThat(finder.findOrThrow("16")).isSameAs(course);
	}

	@Test
	void 없는_과목이면_404() {
		when(studentCourseRepository.findById(99L)).thenReturn(Optional.empty());

		NotFoundException exception = assertThrows(NotFoundException.class, () -> finder.findOrThrow("99"));

		assertThat(exception.getMessage()).isEqualTo("과목을 찾을 수 없습니다.");
		assertThat(exception.getFieldErrors()).isEmpty();
	}

	@Test
	void 숫자가_아닌_courseId도_404() {
		assertThrows(NotFoundException.class, () -> finder.findOrThrow("abc"));
	}

	@Test
	void 다른_학생의_과목이면_404() {
		StudentCourse course = mock(StudentCourse.class);
		when(course.getStudentId()).thenReturn("1");
		when(studentCourseRepository.findById(16L)).thenReturn(Optional.of(course));

		assertThrows(NotFoundException.class, () -> finder.findOrThrow("16"));
	}

}
