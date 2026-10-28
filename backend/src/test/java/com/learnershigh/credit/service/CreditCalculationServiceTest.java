package com.learnershigh.credit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.learnershigh.credit.dto.RiskLevel;
import com.learnershigh.credit.entity.Assessment;
import com.learnershigh.credit.entity.CompletionRule;
import com.learnershigh.credit.entity.CourseType;
import com.learnershigh.credit.entity.StudentCourse;
import com.learnershigh.credit.repository.CompletionRuleRepository;

// PRD 6장 이수 위험 계산 규칙. frontend domain/completion.test.ts와 같은 케이스를 쓴다
@ExtendWith(MockitoExtension.class)
class CreditCalculationServiceTest {

	@Mock
	private CompletionRuleRepository completionRuleRepository;

	private CreditCalculationService service;

	private void setUp() {
		service = new CreditCalculationService(completionRuleRepository);
	}

	// 출석률 미달·NO_DATA·CREATIVE 케이스는 minAchievement·cautionBelowRate까지 읽지 않는다
	private void givenRule(CourseType courseType, double minAttendance, Double minAchievement, Double cautionBelowRate) {
		CompletionRule rule = mock(CompletionRule.class);
		lenient().when(rule.getMinAttendance()).thenReturn(BigDecimal.valueOf(minAttendance));
		lenient().when(rule.getMinAchievement()).thenReturn(minAchievement == null ? null : BigDecimal.valueOf(minAchievement));
		lenient().when(rule.getCautionBelowRate()).thenReturn(cautionBelowRate == null ? null : BigDecimal.valueOf(cautionBelowRate));
		lenient().when(completionRuleRepository.findFirstByCourseTypeOrderByEffectiveYearDesc(courseType)).thenReturn(Optional.of(rule));
	}

	// totalSessions가 null이면 출석률 계산에서 absentCount를 읽지 않는다 (attendanceRate 참고)
	private StudentCourse course(CourseType courseType, Integer totalSessions, Integer absentCount) {
		StudentCourse course = mock(StudentCourse.class);
		when(course.getCourseType()).thenReturn(courseType);
		lenient().when(course.getTotalSessions()).thenReturn(totalSessions);
		lenient().when(course.getAbsentCount()).thenReturn(absentCount);
		return course;
	}

	// score가 null이면 성취율 계산에서 weightPercent·maxScore를 읽지 않는다 (achievementRate 참고)
	private Assessment assessment(int weightPercent, Integer score, int maxScore) {
		Assessment assessment = mock(Assessment.class);
		when(assessment.getScore()).thenReturn(score);
		lenient().when(assessment.getWeightPercent()).thenReturn(weightPercent);
		lenient().when(assessment.getMaxScore()).thenReturn(maxScore);
		return assessment;
	}

	@Test
	void 공통과목_수행평가1건_30점이면_성취율30_이수위험() {
		setUp();
		givenRule(CourseType.COMMON, 66.67, 40.0, 50.0);
		StudentCourse course = course(CourseType.COMMON, null, 0);

		CourseEvaluation result = service.evaluate(course, List.of(assessment(20, 30, 100)));

		assertThat(result.achievementRate()).isCloseTo(30.0, within(0.01));
		assertThat(result.riskLevel()).isEqualTo(RiskLevel.RISK);
	}

	@Test
	void 수행평가_추가하면_성취율45_주의() {
		setUp();
		givenRule(CourseType.COMMON, 66.67, 40.0, 50.0);
		StudentCourse course = course(CourseType.COMMON, null, 0);

		CourseEvaluation result =
				service.evaluate(course, List.of(assessment(20, 30, 100), assessment(20, 60, 100)));

		assertThat(result.achievementRate()).isCloseTo(45.0, within(0.01));
		assertThat(result.riskLevel()).isEqualTo(RiskLevel.CAUTION);
	}

	@Test
	void 평가입력없음_성취율미입력() {
		setUp();
		givenRule(CourseType.COMMON, 66.67, 40.0, 50.0);
		StudentCourse course = course(CourseType.COMMON, null, 0);

		CourseEvaluation result = service.evaluate(course, List.of());

		assertThat(result.achievementRate()).isNull();
		assertThat(result.riskLevel()).isEqualTo(RiskLevel.NO_DATA);
	}

	@Test
	void 평가2건중1건_득점NULL이면_제외하고_계산() {
		setUp();
		givenRule(CourseType.COMMON, 66.67, 40.0, 50.0);
		StudentCourse course = course(CourseType.COMMON, null, 0);

		CourseEvaluation result =
				service.evaluate(course, List.of(assessment(30, 80, 100), assessment(20, null, 100)));

		assertThat(result.achievementRate()).isCloseTo(80.0, within(0.01));
	}

	@Test
	void 이수위험_공통과목에는_필요평균이_붙는다() {
		setUp();
		givenRule(CourseType.COMMON, 66.67, 40.0, 50.0);
		StudentCourse course = course(CourseType.COMMON, null, 0);

		CourseEvaluation result =
				service.evaluate(course, List.of(assessment(30, 25, 100), assessment(20, 50, 100)));

		assertThat(result.achievementRate()).isCloseTo(35.0, within(0.01));
		assertThat(result.riskLevel()).isEqualTo(RiskLevel.RISK);
		assertThat(result.requiredAverage()).isCloseTo(45.0, within(0.01));
	}

	@Test
	void 주의단계_과목은_필요평균이_없다() {
		setUp();
		givenRule(CourseType.COMMON, 66.67, 40.0, 50.0);
		StudentCourse course = course(CourseType.COMMON, null, 0);

		CourseEvaluation result = service.evaluate(course, List.of(assessment(30, 46, 100)));

		assertThat(result.riskLevel()).isEqualTo(RiskLevel.CAUTION);
		assertThat(result.requiredAverage()).isNull();
	}

	@Test
	void 선택과목은_출석률60이면_성취율이_높아도_이수위험() {
		setUp();
		givenRule(CourseType.GENERAL, 66.67, null, null);
		StudentCourse course = course(CourseType.GENERAL, 50, 20);

		CourseEvaluation result = service.evaluate(course, List.of(assessment(100, 90, 100)));

		assertThat(result.attendanceRate()).isCloseTo(60.0, within(0.01));
		assertThat(result.riskLevel()).isEqualTo(RiskLevel.RISK);
	}

	@Test
	void 선택과목은_성취율이_낮아도_출석률이_기준이상이면_정상() {
		setUp();
		givenRule(CourseType.GENERAL, 66.67, null, null);
		StudentCourse course = course(CourseType.GENERAL, null, 0);

		CourseEvaluation result = service.evaluate(course, List.of(assessment(100, 20, 100)));

		assertThat(result.riskLevel()).isEqualTo(RiskLevel.NORMAL);
	}

	@Test
	void 수행100퍼센트_과목도_정상_계산된다() {
		setUp();
		givenRule(CourseType.COMMON, 66.67, 40.0, 50.0);
		StudentCourse course = course(CourseType.COMMON, null, 0);

		CourseEvaluation result =
				service.evaluate(course, List.of(assessment(50, 90, 100), assessment(50, 43, 50)));

		assertThat(result.achievementRate()).isCloseTo(88.0, within(0.01));
		assertThat(result.riskLevel()).isEqualTo(RiskLevel.NORMAL);
	}

	@Test
	void 창체는_평가없이_출석률로만_판정한다() {
		setUp();
		givenRule(CourseType.CREATIVE, 66.67, null, null);
		StudentCourse course = course(CourseType.CREATIVE, null, 0);

		CourseEvaluation result = service.evaluate(course, List.of());

		assertThat(result.achievementRate()).isNull();
		assertThat(result.riskLevel()).isEqualTo(RiskLevel.NORMAL);
	}

	// 소수 오차 처리(PROGRESS.md "미정"): frontend와 같이 별도 허용 오차 없이 double 값을 그대로 경계와 비교한다.
	// 39.6%는 반올림하면 40%로 보이지만 반올림 전 값(미달)으로 이수 위험 판정한다
	@Test
	void 반올림전_값으로_경계를_판정한다() {
		setUp();
		givenRule(CourseType.COMMON, 66.67, 40.0, 50.0);
		StudentCourse course = course(CourseType.COMMON, null, 0);

		CourseEvaluation risk = service.evaluate(course, List.of(assessment(100, 396, 1000)));
		CourseEvaluation caution = service.evaluate(course, List.of(assessment(100, 499, 1000)));
		CourseEvaluation normal = service.evaluate(course, List.of(assessment(100, 500, 1000)));

		assertThat(risk.riskLevel()).isEqualTo(RiskLevel.RISK);
		assertThat(caution.riskLevel()).isEqualTo(RiskLevel.CAUTION);
		assertThat(normal.riskLevel()).isEqualTo(RiskLevel.NORMAL);
	}

	@Test
	void 총수업수를_모르면_출석률100으로_가정한다() {
		setUp();
		givenRule(CourseType.COMMON, 66.67, 40.0, 50.0);
		StudentCourse course = course(CourseType.COMMON, null, 3);

		CourseEvaluation result = service.evaluate(course, List.of(assessment(100, 90, 100)));

		assertThat(result.attendanceRate()).isEqualTo(100.0);
	}

}
