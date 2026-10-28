package com.learnershigh.credit.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.learnershigh.common.DemoStudent;
import com.learnershigh.credit.dto.CourseItemResponse;
import com.learnershigh.credit.dto.RequiredCoursesResponse;
import com.learnershigh.credit.dto.SemesterCoursesResponse;
import com.learnershigh.credit.entity.Assessment;
import com.learnershigh.credit.entity.CourseStatus;
import com.learnershigh.credit.entity.CourseType;
import com.learnershigh.credit.entity.StudentCourse;
import com.learnershigh.credit.repository.AssessmentRepository;
import com.learnershigh.credit.repository.StudentCourseRepository;

// 학기별 수강 과목·남은 필수 이수 과목 조회 (api-contract.md 4.2, 4.3)
@Service
public class CourseQueryService {

	private final StudentCourseRepository studentCourseRepository;
	private final AssessmentRepository assessmentRepository;
	private final CourseItemAssembler assembler;

	public CourseQueryService(
			StudentCourseRepository studentCourseRepository,
			AssessmentRepository assessmentRepository,
			CourseItemAssembler assembler) {
		this.studentCourseRepository = studentCourseRepository;
		this.assessmentRepository = assessmentRepository;
		this.assembler = assembler;
	}

	// 그 학기에 과목이 없으면 courses: [], totalCredits: 0
	public SemesterCoursesResponse getCourses(Integer gradeYear, Integer term) {
		List<StudentCourse> courses = studentCourseRepository
				.findByStudentIdAndGradeYearAndTermOrderByIdAsc(DemoStudent.ID, gradeYear, term);
		List<CourseItemResponse> items = toItems(courses);
		int totalCredits = courses.stream().mapToInt(StudentCourse::getCredits).sum();
		return new SemesterCoursesResponse(gradeYear, term, totalCredits, items);
	}

	// 아직 이수하지 않은(이수 완료가 아닌) 공통과목
	public RequiredCoursesResponse getRequiredCourses() {
		List<StudentCourse> courses = studentCourseRepository.findByStudentIdOrderByIdAsc(DemoStudent.ID).stream()
				.filter(course -> course.getCourseType() == CourseType.COMMON && course.getStatus() != CourseStatus.COMPLETED)
				.toList();
		List<CourseItemResponse> items = toItems(courses);
		return new RequiredCoursesResponse(items.size(), items);
	}

	private List<CourseItemResponse> toItems(List<StudentCourse> courses) {
		Map<Long, List<Assessment>> assessmentsByCourse = loadAssessments(courses);
		return courses.stream()
				.map(course -> assembler.toItem(course, assessmentsByCourse.getOrDefault(course.getId(), List.of())))
				.toList();
	}

	private Map<Long, List<Assessment>> loadAssessments(List<StudentCourse> courses) {
		if (courses.isEmpty()) {
			return Map.of();
		}
		List<Long> ids = courses.stream().map(StudentCourse::getId).toList();
		return assessmentRepository.findByStudentCourseIdIn(ids).stream()
				.collect(Collectors.groupingBy(Assessment::getStudentCourseId));
	}

}
