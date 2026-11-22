package com.learnershigh.record.service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessage;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.learnershigh.record.dto.GuidelineSuggestion;

// Claude API로 세특 방향 3안을 만든다. API 키는 환경변수 ANTHROPIC_API_KEY로만 읽는다(코드·설정 파일에 두지 않음).
// 키가 없거나 호출·파싱이 실패하면 빈 값을 돌려주고 RecordGuidelineService가 고정 안내로 대체한다
@Component
public class ClaudeGuidelineAiClient implements GuidelineAiClient {

	static final String API_KEY_ENV = "ANTHROPIC_API_KEY";

	private static final Logger log = LoggerFactory.getLogger(ClaudeGuidelineAiClient.class);

	// 완성된 세특 문장을 쓰지 않고 방향만 제안하도록 한다
	static final String SYSTEM_PROMPT = """
			너는 고등학생이 과목별 세특(세부능력 및 특기사항) 활동의 방향을 정하도록 돕는 진로 상담 도우미다.
			학생이 직접 활동하고 직접 글을 쓴다. 너는 방향만 제안한다.

			반드시 지킬 것
			- 완성된 세특 문장을 절대 쓰지 마라. 학생부에 그대로 옮겨 적을 수 있는 서술형 문장(예: "~함.", "~을 보임.", "~하는 역량이 돋보임.")이나 문단을 만들지 마라.
			- 학생이 하지 않은 활동을 한 것처럼 꾸미지 마라. 예시는 "해 볼 수 있는 활동"으로만 제안한다.
			- 서로 다른 접근의 방향 3안을 제안한다.
			- 각 안: title은 방향을 한 줄로(30자 안팎), reason은 이 방향이 이 학생에게 맞는 이유 1~2문장(희망 학과·로드맵 정보를 근거로),
			  examples는 해 볼 수 있는 탐구·활동 예시 2~3개(짧은 명사구), points는 4칸(탐구 동기, 활동 과정, 결과와 배운 점, 진로 연결)에 적을 때 떠올려 볼 포인트 2~3개(어느 칸인지 앞에 밝히고, 질문이나 안내 형태로).
			- 희망 학과가 없으면 과목 자체에 집중한 방향을 제안한다.
			- 한국어로 쓴다.
			""";

	private final String model;
	private final long maxTokens;
	// 키가 없으면 null (호출하지 않고 바로 대체)
	private final AnthropicClient client;

	public ClaudeGuidelineAiClient(
			@Value("${record.guideline.ai.model:claude-opus-5-5}") String model,
			@Value("${record.guideline.ai.timeout-seconds:60}") long timeoutSeconds,
			@Value("${record.guideline.ai.max-tokens:16000}") long maxTokens) {
		this.model = model;
		this.maxTokens = maxTokens;
		String apiKey = System.getenv(API_KEY_ENV);
		if (apiKey == null || apiKey.isBlank()) {
			log.info("{}가 없어 세특 방향 추천은 로드맵 기반 고정 안내로 대체합니다.", API_KEY_ENV);
			this.client = null;
		} else {
			this.client = AnthropicOkHttpClient.builder()
					.apiKey(apiKey)
					.timeout(Duration.ofSeconds(timeoutSeconds))
					.maxRetries(1)
					.build();
		}
	}

	// 구조화 출력(JSON 스키마)으로 받는 모양
	public record AiOutput(@JsonPropertyDescription("방향 3안") List<AiSuggestion> suggestions) {
	}

	public record AiSuggestion(
			@JsonPropertyDescription("방향 한 줄") String title,
			@JsonPropertyDescription("이 방향이 맞는 이유 1~2문장") String reason,
			@JsonPropertyDescription("해 볼 수 있는 탐구·활동 예시 2~3개, 짧은 명사구") List<String> examples,
			@JsonPropertyDescription("4칸에 적을 때 떠올려 볼 포인트 2~3개") List<String> points) {
	}

	@Override
	public Optional<List<GuidelineSuggestion>> suggest(GuidelineContext context) {
		if (client == null) {
			return Optional.empty();
		}
		try {
			StructuredMessageCreateParams<AiOutput> params = MessageCreateParams.builder()
					.model(model)
					.maxTokens(maxTokens)
					.system(SYSTEM_PROMPT)
					.outputConfig(AiOutput.class)
					.addUserMessage(userMessage(context))
					.build();
			StructuredMessage<AiOutput> response = client.messages().create(params);
			return response.content().stream()
					.flatMap(block -> block.text().stream())
					.map(typed -> typed.text())
					.findFirst()
					.map(output -> output.suggestions() == null ? List.<GuidelineSuggestion>of() : output.suggestions().stream()
							.map(item -> new GuidelineSuggestion(item.title(), item.reason(), item.examples(), item.points()))
							.toList());
		} catch (RuntimeException exception) {
			// 키 오류, 시간 초과, 응답 파싱 실패 등 모든 실패는 고정 안내로 대체한다
			log.warn("세특 방향 추천 AI 호출 실패, 고정 안내로 대체: {}", exception.toString());
			return Optional.empty();
		}
	}

	// AI에 보내는 입력: 희망 학과, 목표 대학, 과목명, 로드맵에서의 지정(핵심/권장)
	static String userMessage(GuidelineContext context) {
		StringBuilder message = new StringBuilder();
		message.append("과목: ").append(context.subjectName()).append('\n');
		if (context.hasDepartment()) {
			message.append("희망 학과: ").append(context.departmentName());
			if (context.departmentField() != null) {
				message.append(" (계열: ").append(context.departmentField()).append(')');
			}
			message.append('\n');
		} else {
			message.append("희망 학과: 아직 정하지 않음\n");
		}
		message.append("목표 대학: ").append(context.targetUniversity() == null ? "정하지 않음" : context.targetUniversity()).append('\n');
		if (context.inRoadmap()) {
			message.append("희망 학과 로드맵에서 이 과목: ").append(context.designationLabel())
					.append(" 과목 (조사 대학 ").append(context.totalUniversityCount()).append("곳 중 ")
					.append(context.requiredUniversityCount()).append("곳 지정)\n");
		} else if (context.hasDepartment()) {
			message.append("희망 학과 로드맵에서 이 과목: 지정한 대학 없음\n");
		}
		message.append("\n이 학생이 이 과목에서 해 볼 만한 세특 활동 방향 3안을 제안해 줘. 완성된 세특 문장은 쓰지 마.");
		return message.toString();
	}

}
