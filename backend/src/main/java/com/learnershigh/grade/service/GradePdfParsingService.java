package com.learnershigh.grade.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

// 나이스 성적증명서(전자 텍스트 PDF 가정) 파싱. docs/references의 실제 발급본 일부로 맞춰 봤다(PROGRESS.md 참고).
// PDF 표를 텍스트로 뽑으면 칸이 줄 단위로 흩어진다. 그래서 학년·학기와 과목 데이터를 서로 다른 모양의 줄로 각각 인식한다:
//   - 학년: "[2학년]" 또는 "2학년"처럼 그 줄에 학년만 있음(GRADE_YEAR_ONLY). 예전 "N학년 N학기"가 한 줄에 같이 오는 형식도 지원(SEMESTER_HEADER)
//   - 학기: 그 줄에 숫자 하나(1 또는 2)만 있음(TERM_ONLY) — 표의 학기 칸이 세로로 합쳐진 칸이라 값이 한 번만 나온다
//   - 과목 데이터: "(교과) 과목명 (단위수) 원점수/과목평균(표준편차) 성취도(수강자수) 석차등급". 교과(국어·수학 등)와 과목(공통국어1 등)은 별도 칸이라
//     같은 줄에 같이 오면 교과명을 걷어낸다(GROUP_LABEL). 교과명이 길어서 줄바꿈되면 그 줄엔 숫자가 없어 자연히 건너뛰고, 다음 줄(과목부터 시작)은 그대로 인식한다.
//     과목명에 공백이 들어간 경우("영어 독해와 작문" 등)도 지원한다 — 글자 종류로 과목명을 제한하지 않고 원점수/평균(편차) 숫자 패턴을 기준으로
//     앞부분을 통째로 잡은 뒤(SUBJECT_LINE), 그 끝에 공백+숫자로 붙은 단위수만 따로 떼어낸다(TRAILING_UNIT_COUNT)
// 학년·학기를 못 찾았거나 줄이 패턴에 안 맞으면 건너뛰고 failedCount에 센다(파일이 스캔 이미지면 전부 실패로 처리되고, 화면에서는 수동 입력으로 이어간다).
@Service
public class GradePdfParsingService {

	private static final Pattern SEMESTER_HEADER = Pattern.compile("([1-3])\\s*학년\\s*([1-2])\\s*학기");
	private static final Pattern GRADE_YEAR_ONLY = Pattern.compile("^\\[?([1-3])\\s*학년\\]?$");
	private static final Pattern TERM_ONLY = Pattern.compile("^[12]$");

	// 2015/2022 개정 교육과정의 보통교과(군) 이름. 줄 맨 앞에 이 이름이 단독 토큰으로 오면 교과명이지 과목명이 아니다
	private static final Pattern GROUP_LABEL = Pattern.compile(
			"^(국어|수학|영어|한국사|사회|과학|체육|예술|음악|미술|기술\\s*[·ㆍ]\\s*가정|제2외국어|한문|교양|생활\\s*[·ㆍ]\\s*교양)\\s+");

	// prefix는 "(단위수까지 포함한) 과목명"을 통째로 잡는다 — "영어 독해와 작문"처럼 과목명에 공백이 들어간 경우를 지원하려면
	// 과목명 글자 종류를 미리 제한할 수 없어서(공백 허용), 대신 뒤에 오는 원점수/평균(편차) 숫자 패턴으로 경계를 찾는다(lazy)
	private static final Pattern SUBJECT_LINE = Pattern.compile(
			"(?<prefix>.+?)\\s*"
					+ "(?<raw>\\d{1,3})\\s*/\\s*(?<avg>\\d{1,3}(?:\\.\\d{1,2})?)\\s*\\(\\s*(?<std>\\d{1,3}(?:\\.\\d{1,2})?)\\s*\\)"
					+ ".*?(?<grade>[1-9])\\s*$");

	// prefix 끝에 단위수(1~2자리 숫자)가 공백을 사이에 두고 붙어 있으면 걷어낸다. "공통국어1"처럼 과목명 자체에 붙은 숫자(공백 없음)는 그대로 둔다
	private static final Pattern TRAILING_UNIT_COUNT = Pattern.compile("\\s+\\d{1,2}$");

	public GradeParseResult parse(byte[] pdfBytes) throws IOException {
		String text;
		try (PDDocument document = Loader.loadPDF(pdfBytes)) {
			text = new PDFTextStripper().getText(document);
		}
		return parseText(text);
	}

	// PDF 텍스트 추출과 줄 단위 정규식 파싱을 분리해 둔다(파싱 규칙만 떼어 테스트하기 위함)
	GradeParseResult parseText(String text) {
		List<ParsedGradeLine> lines = new ArrayList<>();
		int failedCount = 0;
		Integer currentYear = null;
		Integer currentTerm = null;

		for (String rawLine : text.split("\\r?\\n")) {
			String line = rawLine.trim();
			if (line.isEmpty()) {
				continue;
			}

			Matcher header = SEMESTER_HEADER.matcher(line);
			if (header.find()) {
				currentYear = Integer.valueOf(header.group(1));
				currentTerm = Integer.valueOf(header.group(2));
				continue;
			}

			Matcher yearOnly = GRADE_YEAR_ONLY.matcher(line);
			if (yearOnly.matches()) {
				currentYear = Integer.valueOf(yearOnly.group(1));
				currentTerm = null;
				continue;
			}

			// 학년을 먼저 찾은 뒤에만 학기 단독 숫자 줄을 인식한다(엉뚱한 한 글자 줄을 학기로 잘못 읽지 않게)
			if (currentYear != null && TERM_ONLY.matcher(line).matches()) {
				currentTerm = Integer.valueOf(line);
				continue;
			}

			String withoutGroupLabel = GROUP_LABEL.matcher(line).replaceFirst("");
			Matcher subject = SUBJECT_LINE.matcher(withoutGroupLabel);
			if (!subject.matches() || currentYear == null || currentTerm == null) {
				continue;
			}

			String prefix = subject.group("prefix").trim();
			String title = TRAILING_UNIT_COUNT.matcher(prefix).replaceFirst("").trim();
			if (title.isEmpty()) {
				failedCount++;
				continue;
			}

			try {
				lines.add(new ParsedGradeLine(
						currentYear,
						currentTerm,
						title,
						Integer.valueOf(subject.group("raw")),
						new BigDecimal(subject.group("avg")),
						new BigDecimal(subject.group("std")),
						Integer.valueOf(subject.group("grade"))));
			} catch (NumberFormatException exception) {
				failedCount++;
			}
		}

		return new GradeParseResult(lines, failedCount);
	}

}
