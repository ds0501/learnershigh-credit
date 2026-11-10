package com.learnershigh.credit.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// 학과가 추천하는 과목 하나 (docs/dept_subject_2depth.xlsx 1depth_공통과목·2depth_학교별 시트).
// ratioPercent로 전체 대학 공통(100)·다수 대학 공통(50~99)·대학별로 다른 과목(50 미만)을 구분한다.
@Entity
@Table(name = "credit_department_subject")
public class DepartmentSubject {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "department_id", nullable = false)
	private Long departmentId;

	@Column(name = "catalog_id", nullable = false)
	private Long catalogId;

	@Column(name = "required_university_count", nullable = false)
	private Integer requiredUniversityCount;

	@Column(name = "total_university_count", nullable = false)
	private Integer totalUniversityCount;

	@Column(name = "ratio_percent", nullable = false)
	private BigDecimal ratioPercent;

	@Column(name = "full_common", nullable = false)
	private Boolean fullCommon;

	@Column(name = "designation_type", nullable = false, length = 20)
	private String designationType;

	@Column(name = "eduoffice_listed", nullable = false)
	private Boolean eduofficeListed;

	@Column(name = "required_universities", nullable = false, length = 255)
	private String requiredUniversities;

	protected DepartmentSubject() {
	}

	public Long getId() {
		return id;
	}

	public Long getDepartmentId() {
		return departmentId;
	}

	public Long getCatalogId() {
		return catalogId;
	}

	public Integer getRequiredUniversityCount() {
		return requiredUniversityCount;
	}

	public Integer getTotalUniversityCount() {
		return totalUniversityCount;
	}

	public BigDecimal getRatioPercent() {
		return ratioPercent;
	}

	public Boolean getFullCommon() {
		return fullCommon;
	}

	public String getDesignationType() {
		return designationType;
	}

	public Boolean getEduofficeListed() {
		return eduofficeListed;
	}

	public String getRequiredUniversities() {
		return requiredUniversities;
	}

}
