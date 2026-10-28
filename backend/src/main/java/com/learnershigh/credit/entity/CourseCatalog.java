package com.learnershigh.credit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// 표준 과목 목록(초기 데이터). 학점은 학교 편성에 따라 달라 여기 두지 않는다(credit_student_course.credits만 쓴다).
@Entity
@Table(name = "credit_course_catalog")
public class CourseCatalog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(name = "subject_group", nullable = false, length = 50)
	private String subjectGroup;

	@Enumerated(EnumType.STRING)
	@Column(name = "course_type", nullable = false, length = 20)
	private CourseType courseType;

	protected CourseCatalog() {
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getSubjectGroup() {
		return subjectGroup;
	}

	public CourseType getCourseType() {
		return courseType;
	}

}
