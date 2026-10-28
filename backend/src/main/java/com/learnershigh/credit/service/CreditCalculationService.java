package com.learnershigh.credit.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.learnershigh.credit.dto.RiskLevel;
import com.learnershigh.credit.entity.Assessment;
import com.learnershigh.credit.entity.CompletionRule;
import com.learnershigh.credit.entity.CourseType;
import com.learnershigh.credit.entity.StudentCourse;
import com.learnershigh.credit.repository.CompletionRuleRepository;

// 이수 위험 계산 (CLAUDE.md "이수 기준", PRD 6장). frontend domain/completion.ts와 같은 규칙이지만
// 기준값은 상수가 아니라 credit_completion_rule(effective_year가 가장 큰 행)에서 읽는다.
@Service
public class CreditCalculationService {

	private final CompletionRuleRepository completionRuleRepository;

	public CreditCalculationService(CompletionRuleRepository completionRuleRepository) {
		this.completionRuleRepository = completionRuleRepository;
	}

	// 과목 하나의 성취율, 출석률, 위험 단계, 필요 평균을 한 번에 계산한다
	public CourseEvaluation evaluate(StudentCourse course, List<Assessment> assessments) {
		CompletionRule rule = completionRuleRepository
				.findFirstByCourseTypeOrderByEffectiveYearDesc(course.getCourseType())
				.orElseThrow(() -> new IllegalStateException("이수 기준이 없습니다: " + course.getCourseType()));

		Double rate = course.getCourseType() == CourseType.CREATIVE ? null : achievementRate(assessments);
		double attendance = attendanceRate(course.getTotalSessions(), course.getAbsentCount());
		RiskLevel level = riskLevel(course.getCourseType(), rate, attendance, rule);

		Double minRate = rule.getMinAchievement() == null ? null : rule.getMinAchievement().doubleValue();
		// 필요 평균은 성취율 미달로 이수 위험인 과목에만 안내한다
		boolean needsGuide = level == RiskLevel.RISK && minRate != null && rate != null && rate < minRate;
		Double required = needsGuide ? requiredAverage(rate, enteredWeight(assessments), minRate) : null;

		return new CourseEvaluation(rate, attendance, level, required);
	}

	// 득점이 입력된 평가의 반영 비율 합(%)
	private double enteredWeight(List<Assessment> assessments) {
		return assessments.stream()
				.filter(assessment -> assessment.getScore() != null)
				.mapToDouble(Assessment::getWeightPercent)
				.sum();
	}

	// 성취율(%) = Σ(득점 / 만점 × 반영비율) ÷ Σ(입력된 평가의 반영비율) × 100. 입력된 평가가 없으면 null
	private Double achievementRate(List<Assessment> assessments) {
		List<Assessment> entered = assessments.stream().filter(assessment -> assessment.getScore() != null).toList();
		double weight = enteredWeight(entered);
		if (entered.isEmpty() || weight <= 0) {
			return null;
		}
		double weightedSum = entered.stream()
				.mapToDouble(assessment -> ((double) assessment.getScore() / assessment.getMaxScore()) * assessment.getWeightPercent())
				.sum();
		return (weightedSum / weight) * 100;
	}

	// 출석률(%) = (총 수업 수 − 결석 수) ÷ 총 수업 수 × 100. 총 수업 수를 모르면 100%로 가정한다
	private double attendanceRate(Integer totalSessions, Integer absentCount) {
		if (totalSessions == null || totalSessions <= 0) {
			return 100;
		}
		return ((double) (totalSessions - absentCount) / totalSessions) * 100;
	}

	// 필요 평균(%) = (기준 × 100 − 현재 성취율 × 입력된 반영비율 합) ÷ (100 − 입력된 반영비율 합)
	// 남은 반영비율이 없으면 null. 100을 넘으면 그대로 돌려준다
	private Double requiredAverage(Double currentRate, double weightEntered, double minRate) {
		double remaining = 100 - weightEntered;
		if (remaining <= 0) {
			return null;
		}
		double current = currentRate == null ? 0 : currentRate;
		return (minRate * 100 - current * weightEntered) / remaining;
	}

	// 위험 단계. 반올림하기 전 값으로 판정한다
	private RiskLevel riskLevel(CourseType courseType, Double achievementRate, double attendanceRate, CompletionRule rule) {
		// 출석률 미달은 성취율과 관계없이 모든 유형에서 이수 위험
		if (attendanceRate < rule.getMinAttendance().doubleValue()) {
			return RiskLevel.RISK;
		}
		// 창체는 평가가 없고 출석률로만 판정한다
		if (courseType == CourseType.CREATIVE) {
			return RiskLevel.NORMAL;
		}
		if (achievementRate == null) {
			return RiskLevel.NO_DATA;
		}
		if (rule.getMinAchievement() == null) {
			return RiskLevel.NORMAL;
		}
		double minRate = rule.getMinAchievement().doubleValue();
		if (achievementRate < minRate) {
			return RiskLevel.RISK;
		}
		double cautionBelow = rule.getCautionBelowRate() == null ? minRate : rule.getCautionBelowRate().doubleValue();
		if (achievementRate < cautionBelow) {
			return RiskLevel.CAUTION;
		}
		return RiskLevel.NORMAL;
	}

}
