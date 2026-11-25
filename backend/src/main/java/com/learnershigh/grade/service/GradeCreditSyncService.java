package com.learnershigh.grade.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.learnershigh.credit.entity.Assessment;
import com.learnershigh.credit.entity.CourseCatalog;
import com.learnershigh.credit.entity.CourseStatus;
import com.learnershigh.credit.entity.CourseType;
import com.learnershigh.credit.entity.GradeDisplay;
import com.learnershigh.credit.entity.StudentCourse;
import com.learnershigh.credit.repository.AssessmentRepository;
import com.learnershigh.credit.repository.CourseCatalogRepository;
import com.learnershigh.credit.repository.StudentCourseRepository;
import com.learnershigh.grade.entity.GradeRecord;

// 성적표 원점수를 고교학점제(credit) 모듈의 과목 평가 점수에 반영한다 (설계 결정은 docs/PROGRESS.md 참고).
// credit 패키지 파일은 건드리지 않고, credit의 리포지토리·엔티티만 주입·생성해서 쓴다(모듈 분리 원칙 유지).
@Service
public class GradeCreditSyncService {

	// 학점제에 등록되지 않은 과목을 신규 등록할 때 쓰는 기본값. 성적표에는 학점·반영 비율 정보가 없어
	// CourseRegisterModal(frontend)의 기본값과 똑같이 맞춘다(docs/PROGRESS.md "미정" 참고 — 실제 학교 편성과 다를 수 있음)
	private static final int DEFAULT_CREDITS = 4;
	private static final int DEFAULT_WRITTEN_WEIGHT = 60;
	private static final int DEFAULT_PERFORMANCE_WEIGHT = 40;

	private final StudentCourseRepository studentCourseRepository;
	private final CourseCatalogRepository courseCatalogRepository;
	private final AssessmentRepository assessmentRepository;

	public GradeCreditSyncService(
			StudentCourseRepository studentCourseRepository,
			CourseCatalogRepository courseCatalogRepository,
			AssessmentRepository assessmentRepository) {
		this.studentCourseRepository = studentCourseRepository;
		this.courseCatalogRepository = courseCatalogRepository;
		this.assessmentRepository = assessmentRepository;
	}

	// 원점수가 없으면 반영 대상이 아니다(등급만 있는 성적 등). 매칭/반영 결과는 record에 기록된다
	@Transactional
	public void sync(GradeRecord record) {
		if (record.getRawScore() == null) {
			record.markSync(null, false, null);
			return;
		}

		List<StudentCourse> candidates = studentCourseRepository
				.findByStudentIdAndGradeYearAndTermOrderByIdAsc(record.getStudentId(), record.getGradeYear(), record.getTerm())
				.stream()
				.filter(course -> record.getSubjectName().equals(resolveName(course)))
				.toList();

		if (candidates.isEmpty()) {
			Optional<StudentCourse> registered = tryAutoRegister(record);
			if (registered.isPresent()) {
				record.markSync(registered.get().getId(), true, null);
				return;
			}
			record.markSync(null, false, "NOT_FOUND");
			return;
		}
		if (candidates.size() > 1) {
			record.markSync(null, false, "AMBIGUOUS");
			return;
		}

		StudentCourse course = candidates.get(0);
		List<Assessment> assessments = assessmentRepository.findByStudentCourseIdOrderByIdAsc(course.getId());
		if (assessments.isEmpty()) {
			record.markSync(course.getId(), false, "NO_ASSESSMENT");
			return;
		}

		double ratio = record.getRawScore() / 100.0;
		for (Assessment assessment : assessments) {
			int newScore = (int) Math.round(ratio * assessment.getMaxScore());
			assessment.update(
					assessment.getKind(), assessment.getTitle(), assessment.getWeightPercent(), assessment.getMaxScore(),
					newScore, assessment.getEvaluatedOn());
		}
		assessmentRepository.saveAll(assessments);
		record.markSync(course.getId(), true, null);
	}

	// 학점제에 등록된 과목에서 못 찾으면, 표준 과목 목록(credit_course_catalog)에서 같은 이름을 찾아 새로 등록하고
	// 그 성적표 값을 그대로 평가 1건으로 넣는다(학생이 매번 먼저 과목을 등록해야 하는 수고를 덜어 주기 위함).
	// 표준 과목 목록에도 없으면(비표준 과목명 등) 등록하지 않는다 — 그 경우는 호출한 쪽에서 NOT_FOUND로 남긴다.
	// SPECIALIZED(전문교과)는 credit_student_course의 course_type CHECK 제약에 없어 등록하면 저장이 깨진다(PROGRESS.md 미정) — 건너뛴다
	private Optional<StudentCourse> tryAutoRegister(GradeRecord record) {
		Optional<CourseCatalog> catalog = courseCatalogRepository.findAllByOrderByIdAsc().stream()
				.filter(entry -> record.getSubjectName().equals(entry.getName()) && entry.getCourseType() != CourseType.SPECIALIZED)
				.findFirst();
		if (catalog.isEmpty()) {
			return Optional.empty();
		}

		StudentCourse course = studentCourseRepository.save(new StudentCourse(
				record.getStudentId(),
				catalog.get().getId(),
				null,
				catalog.get().getCourseType(),
				DEFAULT_CREDITS,
				record.getGradeYear(),
				record.getTerm(),
				CourseStatus.COMPLETED,
				DEFAULT_WRITTEN_WEIGHT,
				DEFAULT_PERFORMANCE_WEIGHT,
				GradeDisplay.ACHIEVEMENT_AND_RANK,
				0,
				null));

		assessmentRepository.save(new Assessment(
				course.getId(), "WRITTEN", "성적표 반영", DEFAULT_WRITTEN_WEIGHT, 100, record.getRawScore(), null, LocalDate.now()));

		return Optional.of(course);
	}

	private String resolveName(StudentCourse course) {
		if (course.getCatalogId() != null) {
			return courseCatalogRepository.findById(course.getCatalogId())
					.map(catalog -> catalog.getName())
					.orElse(course.getCustomName());
		}
		return course.getCustomName();
	}

}
