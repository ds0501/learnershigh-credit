package com.learnershigh.credit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.learnershigh.credit.dto.CourseDetailResponse;
import com.learnershigh.credit.dto.CourseItemResponse;
import com.learnershigh.credit.entity.Assessment;
import com.learnershigh.credit.entity.StudentCourse;
import com.learnershigh.credit.repository.AssessmentRepository;

// 과목 상세 조회 (api-contract.md 4.6)
@ExtendWith(MockitoExtension.class)
class CourseDetailServiceTest {

	@Mock
	private StudentCourseFinder courseFinder;

	@Mock
	private AssessmentRepository assessmentRepository;

	@Mock
	private CourseItemAssembler assembler;

	private CourseDetailService service;

	@BeforeEach
	void setUp() {
		service = new CourseDetailService(courseFinder, assessmentRepository, assembler);
	}

	@Test
	void 과목과_평가목록을_상세로_조립한다() {
		StudentCourse course = mock(StudentCourse.class);
		when(course.getId()).thenReturn(16L);
		when(courseFinder.findOrThrow("16")).thenReturn(course);
		Assessment assessment = mock(Assessment.class);
		when(assessmentRepository.findByStudentCourseIdOrderByIdAsc(16L)).thenReturn(List.of(assessment));
		CourseDetailResponse expected = new CourseDetailResponse(
				new CourseItemResponse("16", "통합과학2", "COMMON", 4, "IN_PROGRESS", 35.0, "RISK", 45.0), 1, 2, 60, 40, List.of());
		when(assembler.toDetail(course, List.of(assessment))).thenReturn(expected);

		CourseDetailResponse response = service.getDetail("16");

		assertThat(response).isSameAs(expected);
	}

}
