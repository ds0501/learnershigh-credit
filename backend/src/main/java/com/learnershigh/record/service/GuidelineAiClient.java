package com.learnershigh.record.service;

import java.util.List;
import java.util.Optional;

import com.learnershigh.record.dto.GuidelineSuggestion;

// 세특 방향 추천을 만드는 AI. 키가 없거나 호출·파싱에 실패하면 빈 값을 돌려주고, 호출하는 쪽이 고정 안내로 대체한다.
// 테스트에서는 가짜 구현으로 바꾼다
public interface GuidelineAiClient {

	Optional<List<GuidelineSuggestion>> suggest(GuidelineContext context);

}
