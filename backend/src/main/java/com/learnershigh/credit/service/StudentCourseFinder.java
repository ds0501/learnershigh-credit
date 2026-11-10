package com.learnershigh.credit.service;

import org.springframework.stereotype.Service;

import com.learnershigh.common.DemoStudent;
import com.learnershigh.common.exception.NotFoundException;
import com.learnershigh.credit.entity.StudentCourse;
import com.learnershigh.credit.repository.StudentCourseRepository;

// 과목 상세·상태변경·삭제·평가 API에서 공용으로 쓰는 "경로의 courseId로 과목 찾기" (api-contract.md 4.6~4.11, 없는 과목은 404)
@Service
public class StudentCourseFinder {

	private final StudentCourseRepository studentCourseRepository;

	public StudentCourseFinder(StudentCourseRepository studentCourseRepository) {
		this.studentCourseRepository = studentCourseRepository;
	}

	public StudentCourse findOrThrow(String courseId) {
		Long id = parse(courseId);
		return studentCourseRepository.findById(id)
				.filter(course -> course.getStudentId().equals(DemoStudent.ID))
				.orElseThrow(() -> new NotFoundException("과목을 찾을 수 없습니다."));
	}

	private Long parse(String courseId) {
		try {
			return Long.valueOf(courseId);
		} catch (NumberFormatException exception) {
			throw new NotFoundException("과목을 찾을 수 없습니다.");
		}
	}

}
