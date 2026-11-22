package com.learnershigh.record.entity;

// 세특 4칸(탐구 동기, 활동 과정, 결과와 배운 점, 진로 연결)과 가이드라인. 4칸은 null 대신 빈 문자열
public record DraftContent(
		String motive, String process, String result, String careerLink, String guidelineTitle, String guidelineDetail) {

	public boolean isBlank() {
		return motive.isBlank() && process.isBlank() && result.isBlank() && careerLink.isBlank();
	}

}
