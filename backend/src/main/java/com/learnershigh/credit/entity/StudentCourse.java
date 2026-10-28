package com.learnershigh.credit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// 학생이 등록한 과목. 과목명은 저장하지 않는다(catalogId가 있으면 catalog.name, 없으면 customName에서 구한다).
@Entity
@Table(name = "credit_student_course")
public class StudentCourse {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "student_id", nullable = false, length = 100)
	private String studentId;

	@Column(name = "catalog_id")
	private Long catalogId;

	@Column(name = "custom_name", length = 100)
	private String customName;

	@Enumerated(EnumType.STRING)
	@Column(name = "course_type", nullable = false, length = 20)
	private CourseType courseType;

	@Column(nullable = false)
	private Integer credits;

	@Column(name = "grade_year", nullable = false)
	private Integer gradeYear;

	@Column(nullable = false)
	private Integer term;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private CourseStatus status;

	@Column(name = "written_weight", nullable = false)
	private Integer writtenWeight;

	@Column(name = "performance_weight", nullable = false)
	private Integer performanceWeight;

	@Enumerated(EnumType.STRING)
	@Column(name = "grade_display", length = 30)
	private GradeDisplay gradeDisplay;

	@Column(name = "absent_count", nullable = false)
	private Integer absentCount;

	@Column(name = "total_sessions")
	private Integer totalSessions;

	protected StudentCourse() {
	}

	public StudentCourse(
			String studentId,
			Long catalogId,
			String customName,
			CourseType courseType,
			Integer credits,
			Integer gradeYear,
			Integer term,
			CourseStatus status,
			Integer writtenWeight,
			Integer performanceWeight,
			GradeDisplay gradeDisplay,
			Integer absentCount,
			Integer totalSessions) {
		this.studentId = studentId;
		this.catalogId = catalogId;
		this.customName = customName;
		this.courseType = courseType;
		this.credits = credits;
		this.gradeYear = gradeYear;
		this.term = term;
		this.status = status;
		this.writtenWeight = writtenWeight;
		this.performanceWeight = performanceWeight;
		this.gradeDisplay = gradeDisplay;
		this.absentCount = absentCount;
		this.totalSessions = totalSessions;
	}

	public Long getId() {
		return id;
	}

	public String getStudentId() {
		return studentId;
	}

	public Long getCatalogId() {
		return catalogId;
	}

	public String getCustomName() {
		return customName;
	}

	public CourseType getCourseType() {
		return courseType;
	}

	public Integer getCredits() {
		return credits;
	}

	public Integer getGradeYear() {
		return gradeYear;
	}

	public Integer getTerm() {
		return term;
	}

	public CourseStatus getStatus() {
		return status;
	}

	public Integer getWrittenWeight() {
		return writtenWeight;
	}

	public Integer getPerformanceWeight() {
		return performanceWeight;
	}

	public GradeDisplay getGradeDisplay() {
		return gradeDisplay;
	}

	public Integer getAbsentCount() {
		return absentCount;
	}

	public Integer getTotalSessions() {
		return totalSessions;
	}

}
