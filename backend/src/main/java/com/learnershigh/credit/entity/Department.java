package com.learnershigh.credit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// 학과 로드맵의 학과 (docs/dept_subject_2depth.xlsx 학과카드 시트). 참고 데이터라 변경 메서드가 없다.
@Entity
@Table(name = "credit_department")
public class Department {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 50)
	private String name;

	@Column(name = "effective_year", nullable = false)
	private Integer effectiveYear;

	@Column(length = 30)
	private String field;

	@Column(name = "total_university_count", nullable = false)
	private Integer totalUniversityCount;

	@Column(name = "low_sample", nullable = false)
	private Boolean lowSample;

	@Column(name = "general_elective_text", length = 300)
	private String generalElectiveText;

	@Column(name = "career_elective_text", length = 300)
	private String careerElectiveText;

	@Column(name = "fusion_elective_text", length = 300)
	private String fusionElectiveText;

	protected Department() {
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Integer getEffectiveYear() {
		return effectiveYear;
	}

	public String getField() {
		return field;
	}

	public Integer getTotalUniversityCount() {
		return totalUniversityCount;
	}

	public Boolean getLowSample() {
		return lowSample;
	}

	public String getGeneralElectiveText() {
		return generalElectiveText;
	}

	public String getCareerElectiveText() {
		return careerElectiveText;
	}

	public String getFusionElectiveText() {
		return fusionElectiveText;
	}

}
