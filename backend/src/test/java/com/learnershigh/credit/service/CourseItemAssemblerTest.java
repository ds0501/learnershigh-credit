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

import com.learnershigh.credit.dto.CourseItemResponse;
import com.learnershigh.credit.dto.RiskLevel;
import com.learnershigh.credit.entity.CourseStatus;
import com.learnershigh.credit.entity.CourseType;
import com.learnershigh.credit.entity.StudentCourse;

// CourseEvaluation을 api-contract.md 3장 CourseItem 모양으로 바꾸는 매핑만 확인한다
@ExtendWith(MockitoExtension.class)
class CourseItemAssemblerTest {

	@Mock
	private CourseCatalogService catalogService;

	@Mock
	private CreditCalculationService calculationService;

	private CourseItemAssembler assembler;

	@BeforeEach
	void setUp() {
		assembler = new CourseItemAssembler(catalogService, calculationService);
	}

	@Test
	void 과목과_평가를_CourseItem_모양으로_바꾼다() {
		StudentCourse course = mock(StudentCourse.class);
		when(course.getId()).thenReturn(7L);
		when(course.getCourseType()).thenReturn(CourseType.COMMON);
		when(course.getCredits()).thenReturn(4);
		when(course.getStatus()).thenReturn(CourseStatus.IN_PROGRESS);
		when(catalogService.resolveCourseName(course)).thenReturn("공통수학2");
		when(calculationService.evaluate(course, List.of())).thenReturn(new CourseEvaluation(35.0, 100.0, RiskLevel.RISK, 45.0));

		CourseItemResponse response = assembler.toItem(course, List.of());

		assertThat(response.getId()).isEqualTo("7");
		assertThat(response.getName()).isEqualTo("공통수학2");
		assertThat(response.getCourseType()).isEqualTo("COMMON");
		assertThat(response.getCredits()).isEqualTo(4);
		assertThat(response.getStatus()).isEqualTo("IN_PROGRESS");
		assertThat(response.getAchievementRate()).isEqualTo(35.0);
		assertThat(response.getRiskLevel()).isEqualTo("RISK");
		assertThat(response.getRequiredAverage()).isEqualTo(45.0);
	}

}
