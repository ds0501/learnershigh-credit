package com.learnershigh.credit.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// 평가 한 건. score가 NULL이면 미입력이라 계산에서 제외한다(CLAUDE.md 이수 기준).
@Entity
@Table(name = "credit_assessment")
public class Assessment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "student_course_id", nullable = false)
	private Long studentCourseId;

	@Column(nullable = false, length = 20)
	private String kind;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(name = "weight_percent", nullable = false)
	private Integer weightPercent;

	@Column(name = "max_score", nullable = false)
	private Integer maxScore;

	@Column
	private Integer score;

	@Column(name = "task_id", length = 50)
	private String taskId;

	@Column(name = "evaluated_on", nullable = false)
	private LocalDate evaluatedOn;

	protected Assessment() {
	}

	public Assessment(
			Long studentCourseId,
			String kind,
			String title,
			Integer weightPercent,
			Integer maxScore,
			Integer score,
			String taskId,
			LocalDate evaluatedOn) {
		this.studentCourseId = studentCourseId;
		this.kind = kind;
		this.title = title;
		this.weightPercent = weightPercent;
		this.maxScore = maxScore;
		this.score = score;
		this.taskId = taskId;
		this.evaluatedOn = evaluatedOn;
	}

	public Long getId() {
		return id;
	}

	public Long getStudentCourseId() {
		return studentCourseId;
	}

	public String getKind() {
		return kind;
	}

	public String getTitle() {
		return title;
	}

	public Integer getWeightPercent() {
		return weightPercent;
	}

	public Integer getMaxScore() {
		return maxScore;
	}

	public Integer getScore() {
		return score;
	}

	public String getTaskId() {
		return taskId;
	}

	public LocalDate getEvaluatedOn() {
		return evaluatedOn;
	}

	// 평가 수정 (api-contract.md 4.10). taskId는 바꾸지 않는다
	public void update(String kind, String title, Integer weightPercent, Integer maxScore, Integer score, LocalDate evaluatedOn) {
		this.kind = kind;
		this.title = title;
		this.weightPercent = weightPercent;
		this.maxScore = maxScore;
		this.score = score;
		this.evaluatedOn = evaluatedOn;
	}

}
