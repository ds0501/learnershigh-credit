package com.learnershigh.record.service;

// 방향 추천에 쓰는 입력. 희망 학과가 없으면 학과 관련 값이 null, 로드맵에 과목이 없으면 designation 관련 값이 null
public record GuidelineContext(
		String subjectName,
		String departmentName,
		String departmentField,
		String targetUniversity,
		// CORE(핵심), RECOMMENDED(권장), UNSPECIFIED(구분없음). 희망 학과 로드맵에 과목이 없으면 null
		String designationType,
		Integer requiredUniversityCount,
		Integer totalUniversityCount,
		Double ratioPercent) {

	public boolean hasDepartment() {
		return departmentName != null;
	}

	public boolean inRoadmap() {
		return designationType != null;
	}

	public String designationLabel() {
		if (designationType == null) {
			return null;
		}
		return switch (designationType) {
			case "CORE" -> "핵심";
			case "RECOMMENDED" -> "권장";
			default -> "지정(구분없음)";
		};
	}

}
