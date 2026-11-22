package com.learnershigh.record.dto;

import java.util.List;

// 방향 추천 응답(항상 3안). generatedBy: AI = AI가 만든 안, FALLBACK = 키가 없거나 호출이 실패해 로드맵 데이터로 만든 고정 안내
public record GuidelineSuggestionsResponse(String generatedBy, List<GuidelineSuggestion> suggestions) {

	public static final String AI = "AI";
	public static final String FALLBACK = "FALLBACK";

}
