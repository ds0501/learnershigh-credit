package com.learnershigh.goal.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// 학생의 희망 학과와 목표 대학 (학생당 1개). 학생부 관리의 모든 탭이 같은 값을 쓴다.
@Entity
@Table(name = "student_goal")
public class StudentGoal {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "student_id", nullable = false, length = 100, unique = true)
	private String studentId;

	@Column(name = "department_id", nullable = false)
	private Long departmentId;

	@Column(name = "target_university", length = 100)
	private String targetUniversity;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	protected StudentGoal() {
	}

	public static StudentGoal create(String studentId, Long departmentId, String targetUniversity, LocalDateTime now) {
		StudentGoal goal = new StudentGoal();
		goal.studentId = studentId;
		goal.change(departmentId, targetUniversity, now);
		return goal;
	}

	public void change(Long departmentId, String targetUniversity, LocalDateTime now) {
		this.departmentId = departmentId;
		this.targetUniversity = targetUniversity;
		this.updatedAt = now;
	}

	public Long getId() {
		return id;
	}

	public String getStudentId() {
		return studentId;
	}

	public Long getDepartmentId() {
		return departmentId;
	}

	public String getTargetUniversity() {
		return targetUniversity;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

}
