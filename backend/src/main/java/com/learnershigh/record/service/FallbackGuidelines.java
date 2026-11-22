package com.learnershigh.record.service;

import java.util.List;

import com.learnershigh.record.dto.GuidelineSuggestion;

// AI 키가 없거나 호출이 실패했을 때의 고정 안내 3안. 희망 학과 로드맵 데이터(핵심/권장, 지정 대학 수)로 이유를 채운다.
// 완성된 세특 문장이 아니라 방향만 담는다
final class FallbackGuidelines {

	private FallbackGuidelines() {
	}

	static List<GuidelineSuggestion> build(GuidelineContext context) {
		String subject = context.subjectName();
		String career = context.hasDepartment() ? context.departmentName() : "희망 진로";
		String roadmap = roadmapReason(context);
		return List.of(
				new GuidelineSuggestion(
						subject + " 핵심 개념 하나를 깊게 파고들기",
						roadmap + " 수업에서 가장 궁금했던 개념 하나를 교과서 밖까지 확장해 보는 방향이에요.",
						List.of("수업 개념 하나를 골라 관련 책·자료로 넓혀 보기", "그 개념이 실제로 쓰이는 사례 찾아 정리하기"),
						List.of("탐구 동기: 그 개념이 왜 궁금했는지 수업 장면을 떠올려 보기", "활동 과정: 어떤 자료를 어떤 순서로 찾았는지 기록해 두기")),
				new GuidelineSuggestion(
						subject + josa(subject, "과", "와") + " " + career + josa(career, "을", "를") + " 잇는 탐구 주제 정하기",
						context.hasDepartment()
								? career + "에서 " + subject + " 내용이 어떻게 쓰이는지 연결하면 진로와 과목이 함께 드러나요."
								: "희망 학과를 정하면 학과에 맞춘 방향을 받을 수 있어요. 지금은 관심 있는 분야와 과목을 이어 보는 방향이에요.",
						List.of(career + " 분야에서 " + subject + " 내용이 필요한 문제 찾아보기", "관련 기사나 자료를 읽고 스스로 질문 만들기"),
						List.of("결과와 배운 점: 새로 알게 된 것과 아직 남은 질문을 나눠 보기", "진로 연결: " + career + "에서 이 지식이 쓰이는 장면 하나로 좁혀 보기")),
				new GuidelineSuggestion(
						"수업 활동을 과정 중심으로 돌아보기",
						"이미 한 수행평가나 수업 활동에서 출발하면 사실에 근거한 기록이 돼요.",
						List.of("발표·실험·보고서 등 실제로 한 수행평가 돌아보기", "모둠 활동에서 맡은 역할과 해결한 문제 정리하기"),
						List.of("활동 과정: 시도했다가 바꾼 방법이 있다면 그 이유 떠올려 보기", "결과와 배운 점: 활동 전후로 달라진 생각 적어 보기")));
	}

	// 로드맵 근거 한 문장
	static String roadmapReason(GuidelineContext context) {
		if (!context.hasDepartment()) {
			return "희망 학과가 아직 없어 과목 자체에 집중했어요.";
		}
		if (!context.inRoadmap()) {
			return context.departmentName() + " 로드맵에서 " + context.subjectName() + josa(context.subjectName(), "을", "를")
					+ " 지정한 대학은 없지만, 과목에서 키운 역량을 진로와 이어 볼 수 있어요.";
		}
		return context.departmentName() + " 조사 대학 " + context.totalUniversityCount() + "곳 중 " + context.requiredUniversityCount()
				+ "곳이 " + context.subjectName() + josa(context.subjectName(), "을", "를") + " " + context.designationLabel() + " 과목으로 둬요.";
	}

	// 받침 유무로 조사를 고른다(을/를, 과/와). 마지막 글자가 한글이 아니면(예: 통합과학2) "을(를)"처럼 둘 다 쓴다
	static String josa(String word, String withFinal, String withoutFinal) {
		if (word == null || word.isEmpty()) {
			return withFinal + "(" + withoutFinal + ")";
		}
		char last = word.charAt(word.length() - 1);
		if (last < 0xAC00 || last > 0xD7A3) {
			return withFinal + "(" + withoutFinal + ")";
		}
		return (last - 0xAC00) % 28 == 0 ? withoutFinal : withFinal;
	}

}
