package com.learnershigh.credit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// 졸업 학점 기준(CLAUDE.md "이수 기준"). 연도별로 한 행.
@Entity
@Table(name = "credit_graduation_rule")
public class GraduationRule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "effective_year", nullable = false)
	private Integer effectiveYear;

	@Column(name = "total_credits", nullable = false)
	private Integer totalCredits;

	@Column(name = "subject_credits", nullable = false)
	private Integer subjectCredits;

	@Column(name = "creative_credits", nullable = false)
	private Integer creativeCredits;

	protected GraduationRule() {
	}

	public Long getId() {
		return id;
	}

	public Integer getEffectiveYear() {
		return effectiveYear;
	}

	public Integer getTotalCredits() {
		return totalCredits;
	}

	public Integer getSubjectCredits() {
		return subjectCredits;
	}

	public Integer getCreativeCredits() {
		return creativeCredits;
	}

}
