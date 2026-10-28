package com.learnershigh.credit.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

// 과목 응답 (api-contract.md 3장 CourseItem). achievementRate는 없으면 null로 내려가고,
// requiredAverage는 없으면 필드 자체를 뺀다(RISK가 아니면 항상 없음).
public class CourseItemResponse {

	private final String id;
	private final String name;
	private final String courseType;
	private final Integer credits;
	private final String status;
	private final Double achievementRate;
	private final String riskLevel;
	private final Double requiredAverage;

	public CourseItemResponse(
			String id,
			String name,
			String courseType,
			Integer credits,
			String status,
			Double achievementRate,
			String riskLevel,
			Double requiredAverage) {
		this.id = id;
		this.name = name;
		this.courseType = courseType;
		this.credits = credits;
		this.status = status;
		this.achievementRate = achievementRate;
		this.riskLevel = riskLevel;
		this.requiredAverage = requiredAverage;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getCourseType() {
		return courseType;
	}

	public Integer getCredits() {
		return credits;
	}

	public String getStatus() {
		return status;
	}

	public Double getAchievementRate() {
		return achievementRate;
	}

	public String getRiskLevel() {
		return riskLevel;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Double getRequiredAverage() {
		return requiredAverage;
	}

}
