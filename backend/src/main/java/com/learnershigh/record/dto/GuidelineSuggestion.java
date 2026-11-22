package com.learnershigh.record.dto;

import java.util.List;

// 세특 방향 1안. 완성된 세특 문장이 아니라 방향(제목, 이유, 탐구·활동 예시, 4칸에 적을 때의 포인트)만 담는다
public record GuidelineSuggestion(String title, String reason, List<String> examples, List<String> points) {
}
