package com.learnershigh.credit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.learnershigh.credit.dto.CreditSummaryResponse;
import com.learnershigh.credit.dto.RiskLevel;
import com.learnershigh.credit.entity.CourseStatus;
import com.learnershigh.credit.entity.CourseType;
import com.learnershigh.credit.entity.GraduationRule;
import com.learnershigh.credit.entity.StudentCourse;
import com.learnershigh.credit.repository.AssessmentRepository;
import com.learnershigh.credit.repository.GraduationRuleRepository;
import com.learnershigh.credit.repository.StudentCourseRepository;

// api-contract.md 4.1 응답 모양과 집계 규칙을 확인한다
@ExtendWith(MockitoExtension.class)
class CreditSummaryServiceTest {

	@Mock
	private StudentCourseRepository studentCourseRepository;

	@Mock
	private AssessmentRepository assessmentRepository;

	@Mock
	private GraduationRuleRepository graduationRuleRepository;

	@Mock
	private CreditCalculationService calculationService;

	private CreditSummaryService service;

	@BeforeEach
	void setUp() {
		service = new CreditSummaryService(studentCourseRepository, assessmentRepository, graduationRuleRepository, calculationService);
		when(assessmentRepository.findByStudentCourseIdIn(any())).thenReturn(List.of());
	}

	// getId()는 수강 중인 과목에만, getCourseType()은 이수 완료 과목에만 쓰인다
	private StudentCourse course(long id, CourseType courseType, Integer credits, CourseStatus status) {
		StudentCourse course = mock(StudentCourse.class);
		lenient().when(course.getId()).thenReturn(id);
		lenient().when(course.getCourseType()).thenReturn(courseType);
		when(course.getCredits()).thenReturn(credits);
		when(course.getStatus()).thenReturn(status);
		return course;
	}

	private GraduationRule graduationRule(int totalCredits, int subjectCredits, int creativeCredits) {
		GraduationRule rule = mock(GraduationRule.class);
		when(rule.getTotalCredits()).thenReturn(totalCredits);
		when(rule.getSubjectCredits()).thenReturn(subjectCredits);
		when(rule.getCreativeCredits()).thenReturn(creativeCredits);
		return rule;
	}

	@Test
	void 완료학점과_진행학점을_나눠_집계하고_경고는_수강중과목만_센다() {
		StudentCourse completedSubject = course(1L, CourseType.COMMON, 4, CourseStatus.COMPLETED);
		StudentCourse completedCreative = course(2L, CourseType.CREATIVE, 3, CourseStatus.COMPLETED);
		StudentCourse riskCourse = course(3L, CourseType.COMMON, 4, CourseStatus.IN_PROGRESS);
		StudentCourse cautionCourse = course(4L, CourseType.COMMON, 4, CourseStatus.IN_PROGRESS);
		when(studentCourseRepository.findByStudentIdOrderByIdAsc(any()))
				.thenReturn(List.of(completedSubject, completedCreative, riskCourse, cautionCourse));
		GraduationRule graduationRule = graduationRule(192, 174, 18);
		when(graduationRuleRepository.findFirstByOrderByEffectiveYearDesc()).thenReturn(Optional.of(graduationRule));
		when(calculationService.evaluate(riskCourse, List.of()))
				.thenReturn(new CourseEvaluation(30.0, 100.0, RiskLevel.RISK, 45.0));
		when(calculationService.evaluate(cautionCourse, List.of()))
				.thenReturn(new CourseEvaluation(45.0, 100.0, RiskLevel.CAUTION, null));

		CreditSummaryResponse response = service.getSummary();

		assertThat(response.graduationCredits()).isEqualTo(192);
		assertThat(response.completedCredits()).isEqualTo(7);
		assertThat(response.inProgressCredits()).isEqualTo(8);
		assertThat(response.remainingCredits()).isEqualTo(192 - 7 - 8);
		assertThat(response.subjectCredits().earned()).isEqualTo(4);
		assertThat(response.subjectCredits().required()).isEqualTo(174);
		assertThat(response.creativeCredits().earned()).isEqualTo(3);
		assertThat(response.creativeCredits().required()).isEqualTo(18);
		assertThat(response.alert().riskCount()).isEqualTo(1);
		assertThat(response.alert().cautionCount()).isEqualTo(1);
	}

}
