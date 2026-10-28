package com.learnershigh.credit.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// 이수 기준 설정값(CLAUDE.md "이수 기준"). minAchievement, cautionBelowRate가 없는 유형은 성취율로 판정하지 않는다.
@Entity
@Table(name = "credit_completion_rule")
public class CompletionRule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "course_type", nullable = false, length = 20)
	private CourseType courseType;

	@Column(name = "effective_year", nullable = false)
	private Integer effectiveYear;

	@Column(name = "min_attendance", nullable = false)
	private BigDecimal minAttendance;

	@Column(name = "min_achievement")
	private BigDecimal minAchievement;

	@Column(name = "caution_below_rate")
	private BigDecimal cautionBelowRate;

	protected CompletionRule() {
	}

	public Long getId() {
		return id;
	}

	public CourseType getCourseType() {
		return courseType;
	}

	public Integer getEffectiveYear() {
		return effectiveYear;
	}

	public BigDecimal getMinAttendance() {
		return minAttendance;
	}

	public BigDecimal getMinAchievement() {
		return minAchievement;
	}

	public BigDecimal getCautionBelowRate() {
		return cautionBelowRate;
	}

}
