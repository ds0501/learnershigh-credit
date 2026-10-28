package com.learnershigh.credit.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.learnershigh.common.DemoStudent;
import com.learnershigh.credit.dto.CreditAlertResponse;
import com.learnershigh.credit.dto.CreditProgressResponse;
import com.learnershigh.credit.dto.CreditSummaryResponse;
import com.learnershigh.credit.dto.RiskLevel;
import com.learnershigh.credit.entity.Assessment;
import com.learnershigh.credit.entity.CourseStatus;
import com.learnershigh.credit.entity.CourseType;
import com.learnershigh.credit.entity.GraduationRule;
import com.learnershigh.credit.entity.StudentCourse;
import com.learnershigh.credit.repository.AssessmentRepository;
import com.learnershigh.credit.repository.GraduationRuleRepository;
import com.learnershigh.credit.repository.StudentCourseRepository;

// 학점 현황 요약 (api-contract.md 4.1)
@Service
public class CreditSummaryService {

	private final StudentCourseRepository studentCourseRepository;
	private final AssessmentRepository assessmentRepository;
	private final GraduationRuleRepository graduationRuleRepository;
	private final CreditCalculationService calculationService;

	public CreditSummaryService(
			StudentCourseRepository studentCourseRepository,
			AssessmentRepository assessmentRepository,
			GraduationRuleRepository graduationRuleRepository,
			CreditCalculationService calculationService) {
		this.studentCourseRepository = studentCourseRepository;
		this.assessmentRepository = assessmentRepository;
		this.graduationRuleRepository = graduationRuleRepository;
		this.calculationService = calculationService;
	}

	public CreditSummaryResponse getSummary() {
		List<StudentCourse> courses = studentCourseRepository.findByStudentIdOrderByIdAsc(DemoStudent.ID);
		List<StudentCourse> completed = courses.stream().filter(course -> course.getStatus() == CourseStatus.COMPLETED).toList();
		List<StudentCourse> inProgress = courses.stream().filter(course -> course.getStatus() == CourseStatus.IN_PROGRESS).toList();

		int completedCredits = sumCredits(completed);
		int inProgressCredits = sumCredits(inProgress);

		GraduationRule graduationRule = graduationRuleRepository.findFirstByOrderByEffectiveYearDesc()
				.orElseThrow(() -> new IllegalStateException("졸업 학점 기준이 없습니다."));

		int subjectEarned = sumCredits(completed.stream().filter(course -> course.getCourseType() != CourseType.CREATIVE).toList());
		int creativeEarned = sumCredits(completed.stream().filter(course -> course.getCourseType() == CourseType.CREATIVE).toList());

		// 경고 배너는 수강 중인 과목만 센다 (이수 완료 과목은 이미 이수함)
		List<Long> inProgressIds = inProgress.stream().map(StudentCourse::getId).toList();
		Map<Long, List<Assessment>> assessmentsByCourse = assessmentRepository.findByStudentCourseIdIn(inProgressIds).stream()
				.collect(Collectors.groupingBy(Assessment::getStudentCourseId));

		int riskCount = 0;
		int cautionCount = 0;
		for (StudentCourse course : inProgress) {
			List<Assessment> assessments = assessmentsByCourse.getOrDefault(course.getId(), List.of());
			RiskLevel level = calculationService.evaluate(course, assessments).riskLevel();
			if (level == RiskLevel.RISK) {
				riskCount++;
			} else if (level == RiskLevel.CAUTION) {
				cautionCount++;
			}
		}

		return new CreditSummaryResponse(
				graduationRule.getTotalCredits(),
				completedCredits,
				inProgressCredits,
				graduationRule.getTotalCredits() - completedCredits - inProgressCredits,
				new CreditProgressResponse(subjectEarned, graduationRule.getSubjectCredits()),
				new CreditProgressResponse(creativeEarned, graduationRule.getCreativeCredits()),
				new CreditAlertResponse(riskCount, cautionCount));
	}

	private int sumCredits(List<StudentCourse> courses) {
		return courses.stream().mapToInt(StudentCourse::getCredits).sum();
	}

}
