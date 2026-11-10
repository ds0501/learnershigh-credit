package com.learnershigh.credit.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.learnershigh.common.exception.NotFoundException;
import com.learnershigh.common.exception.ValidationException;
import com.learnershigh.credit.dto.AssessmentRequest;
import com.learnershigh.credit.dto.AssessmentResponse;
import com.learnershigh.credit.entity.Assessment;
import com.learnershigh.credit.entity.StudentCourse;
import com.learnershigh.credit.repository.AssessmentRepository;

// 평가 추가·수정·삭제 (api-contract.md 4.9~4.11). frontend domain/validation.ts와 같은 검증 규칙을 따른다.
@Service
public class AssessmentService {

	private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
	private static final Map<String, String> KIND_LABEL = Map.of("WRITTEN", "지필", "PERFORMANCE", "수행");

	private final StudentCourseFinder courseFinder;
	private final AssessmentRepository assessmentRepository;

	public AssessmentService(StudentCourseFinder courseFinder, AssessmentRepository assessmentRepository) {
		this.courseFinder = courseFinder;
		this.assessmentRepository = assessmentRepository;
	}

	public AssessmentResponse add(String courseId, AssessmentRequest request) {
		StudentCourse course = courseFinder.findOrThrow(courseId);
		List<Assessment> existing = assessmentRepository.findByStudentCourseIdOrderByIdAsc(course.getId());
		Checked checked = validate(course, request, existing, null);
		Assessment created = assessmentRepository.save(new Assessment(
				course.getId(), checked.kind(), checked.title(), request.weightPercent(), request.maxScore(),
				request.score(), null, checked.evaluatedOn()));
		return AssessmentResponse.from(created);
	}

	public AssessmentResponse update(String courseId, String assessmentId, AssessmentRequest request) {
		StudentCourse course = courseFinder.findOrThrow(courseId);
		Assessment assessment = findOwnedAssessment(course, assessmentId);
		List<Assessment> existing = assessmentRepository.findByStudentCourseIdOrderByIdAsc(course.getId());
		Checked checked = validate(course, request, existing, assessment.getId());
		assessment.update(checked.kind(), checked.title(), request.weightPercent(), request.maxScore(), request.score(), checked.evaluatedOn());
		return AssessmentResponse.from(assessmentRepository.save(assessment));
	}

	public void delete(String courseId, String assessmentId) {
		StudentCourse course = courseFinder.findOrThrow(courseId);
		Assessment assessment = findOwnedAssessment(course, assessmentId);
		assessmentRepository.delete(assessment);
	}

	// 그 과목의 평가가 아니면 404 "평가를 찾을 수 없습니다." (api-contract.md 4.10, 4.11)
	private Assessment findOwnedAssessment(StudentCourse course, String assessmentId) {
		Long id;
		try {
			id = Long.valueOf(assessmentId);
		} catch (NumberFormatException exception) {
			throw new NotFoundException("평가를 찾을 수 없습니다.");
		}
		return assessmentRepository.findById(id)
				.filter(found -> found.getStudentCourseId().equals(course.getId()))
				.orElseThrow(() -> new NotFoundException("평가를 찾을 수 없습니다."));
	}

	private record Checked(String kind, String title, LocalDate evaluatedOn) {
	}

	// api-contract.md 4.9 검증표. 모든 필드를 함께 검사해 한 번에 fieldErrors로 돌려준다
	private Checked validate(StudentCourse course, AssessmentRequest request, List<Assessment> existing, Long excludeId) {
		Map<String, String> errors = new LinkedHashMap<>();

		String title = request.title() == null ? "" : request.title().trim();
		if (title.isEmpty()) {
			errors.put("title", "평가 이름을 입력하세요.");
		}

		LocalDate evaluatedOn = null;
		String rawDate = request.evaluatedOn();
		if (rawDate == null || !DATE_PATTERN.matcher(rawDate).matches()) {
			errors.put("evaluatedOn", "날짜를 입력하세요.");
		} else {
			try {
				evaluatedOn = LocalDate.parse(rawDate);
			} catch (DateTimeParseException exception) {
				errors.put("evaluatedOn", "날짜를 입력하세요.");
			}
		}

		Integer maxScore = request.maxScore();
		if (maxScore == null || maxScore <= 0) {
			errors.put("maxScore", "만점은 0보다 커야 합니다.");
		}

		Integer score = request.score();
		if (score != null) {
			if (score < 0) {
				errors.put("score", "득점은 0 이상이어야 합니다.");
			} else if (maxScore != null && maxScore > 0 && score > maxScore) {
				errors.put("score", "득점은 만점(" + maxScore + "점) 이하여야 합니다.");
			}
		}

		String kind = request.kind();
		if (!KIND_LABEL.containsKey(kind)) {
			errors.put("kind", "평가 종류를 선택하세요.");
		} else {
			Integer weightPercent = request.weightPercent();
			if (weightPercent == null || weightPercent <= 0) {
				errors.put("weightPercent", "반영 비율은 0보다 커야 합니다.");
			} else {
				int limit = "WRITTEN".equals(kind) ? course.getWrittenWeight() : course.getPerformanceWeight();
				int used = existing.stream()
						.filter(assessment -> assessment.getKind().equals(kind) && !assessment.getId().equals(excludeId))
						.mapToInt(Assessment::getWeightPercent)
						.sum();
				if (used + weightPercent > limit) {
					errors.put("weightPercent", KIND_LABEL.get(kind) + " 반영 비율 합이 " + limit + "%를 넘습니다. (이미 "
							+ used + "% 사용, 남은 " + Math.max(limit - used, 0) + "%)");
				}
			}
		}

		if (!errors.isEmpty()) {
			throw new ValidationException("입력값을 확인하세요.", errors);
		}

		return new Checked(kind, title, evaluatedOn);
	}

}
