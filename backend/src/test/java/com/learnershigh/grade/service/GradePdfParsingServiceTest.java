package com.learnershigh.grade.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Test;

// 나이스 성적증명서 형식(텍스트 PDF 가정)을 흉내 낸 줄 패턴으로 파싱 규칙을 검증한다.
// 실제 나이스 PDF 샘플로는 검증하지 못했다(docs/PROGRESS.md 미정 참고) - "텍스트 PDF면 이렇게 동작한다"만 보장한다.
// 정규식 파싱은 PDF 텍스트 추출과 분리된 parseText()로 검증하고(한글 폰트 임베딩 없이),
// extractsTextFromRealPdf()에서만 PDFBox의 바이트[] -> 텍스트 추출 경로 자체가 동작하는지 ASCII로 확인한다.
class GradePdfParsingServiceTest {

	private final GradePdfParsingService service = new GradePdfParsingService();

	@Test
	void parsesSubjectLinesUnderSemesterHeader() {
		String text = """
				1학년 1학기
				공통국어1 90/75.5(10.2) A(300) 2
				공통수학1 60/66.7(8.0) B(300) 5
				""";

		GradeParseResult result = service.parseText(text);

		assertThat(result.lines()).hasSize(2);
		ParsedGradeLine first = result.lines().get(0);
		assertThat(first.gradeYear()).isEqualTo(1);
		assertThat(first.term()).isEqualTo(1);
		assertThat(first.subjectName()).isEqualTo("공통국어1");
		assertThat(first.rawScore()).isEqualTo(90);
		assertThat(first.subjectAverage()).isEqualTo(new BigDecimal("75.5"));
		assertThat(first.stdDev()).isEqualTo(new BigDecimal("10.2"));
		assertThat(first.gradeLevel()).isEqualTo(2);
	}

	@Test
	void linesWithoutSemesterHeaderAreSkipped() {
		GradeParseResult result = service.parseText("공통국어1 90/75.5(10.2) A(300) 2");

		assertThat(result.lines()).isEmpty();
	}

	@Test
	void stripsLeadingSubjectGroupLabel() {
		// 실제 나이스 교과학습발달상황 표는 "교과"(국어 등)와 "과목"(공통국어1 등)이 별도 칸이다
		String text = """
				1학년 1학기
				국어 공통국어1 4 90/75.5(10.2) A(300) 2
				수학 공통수학1 4 66/66.7(8.0) B(300) 5
				""";

		GradeParseResult result = service.parseText(text);

		assertThat(result.lines()).hasSize(2);
		assertThat(result.lines().get(0).subjectName()).isEqualTo("공통국어1");
		assertThat(result.lines().get(0).rawScore()).isEqualTo(90);
		assertThat(result.lines().get(1).subjectName()).isEqualTo("공통수학1");
	}

	@Test
	void parsesRealExportedTableLayout() {
		// docs/references의 실제 발급본 일부를 PDFBox로 추출한 텍스트 그대로(표 칸이 줄 단위로 흩어지는 실제 모양).
		// 학년은 "[2학년]"처럼 괄호로, 학기는 숫자 한 줄(1, 2)로 따로 온다. 교과명이 길어서 줄바꿈된 과목(한국지리, 중국어Ⅰ, 한문Ⅰ)은
		// 그 줄에 교과명이 안 붙어 있다
		String text = """
				[2학년]
				학기 교과 과목 단위수
				원점수/과목평균
				(표준편차)
				성취도
				(수강자수)
				석차등급 비
				1
				국어 독서 5 84/71.2(14.6) B(251) 3
				수학 수학Ⅰ 4 93/56.4(23.8) A(251) 2
				영어 영어Ⅰ 5 78/72.5(18.1) C(251) 4
				사회(역사/도덕포
				함)
				한국지리 3 88/61.7(22.9) B(251) 3
				과학 화학Ⅰ 3 95/58.3(20.4) A(124) 1
				과학 생명과학Ⅰ 3 81/66.9(16.2) B(251) 3
				기술 · 가정/제2외
				국어/한문/교양
				중국어Ⅰ 3 90/60.4(24.7) A(91) 2
				기술 · 가정/제2외
				국어/한문/교양
				한문Ⅰ 2 86/54.1(21.5) B(251) 3
				2
				국어 문학 5 89/73.6(16.8) B(251) 3
				수학 수학Ⅱ 4 94/59.8(22.1) A(251) 2
				영어 영어 독해와 작문 5 82/69.4(20.3) B(251) 4
				사회(역사/도덕포
				함)
				한국지리 3 91/66.2(19.8) A(251) 2
				과학 화학Ⅰ 3 87/60.5(18.9) B(124) 3
				과학 생명과학Ⅰ 3 92/63.1(17.2) A(251) 2
				기술 · 가정/제2외
				국어/한문/교양
				중국어Ⅰ 3 79/61.9(19.6) C(91) 4
				기술 · 가정/제2외
				국어/한문/교양
				한문Ⅰ 2 95/57.3(18.8) A(251) 1
				이수단위 합계 56
				""";

		GradeParseResult result = service.parseText(text);

		assertThat(result.lines()).hasSize(16);
		assertThat(result.lines()).allSatisfy(line -> assertThat(line.gradeYear()).isEqualTo(2));
		assertThat(result.lines().stream().filter(line -> line.term() == 1)).hasSize(8);
		assertThat(result.lines().stream().filter(line -> line.term() == 2)).hasSize(8);

		ParsedGradeLine firstTermSubject = result.lines().get(0);
		assertThat(firstTermSubject.subjectName()).isEqualTo("독서");
		assertThat(firstTermSubject.rawScore()).isEqualTo(84);

		// 교과명이 줄바꿈된 과목도 과목명만 정확히 잡힌다(앞줄에 붙어 있던 교과명이 없음)
		ParsedGradeLine wrappedGroupLabel = result.lines().stream()
				.filter(line -> line.subjectName().equals("한국지리") && line.term() == 1)
				.findFirst().orElseThrow();
		assertThat(wrappedGroupLabel.rawScore()).isEqualTo(88);
		assertThat(wrappedGroupLabel.gradeLevel()).isEqualTo(3);

		// 과목명에 공백이 들어간 경우도 통째로 잡힌다("영어"는 교과, "영어 독해와 작문"이 과목명)
		ParsedGradeLine multiWordSubject = result.lines().stream()
				.filter(line -> line.term() == 2 && line.rawScore() == 82)
				.findFirst().orElseThrow();
		assertThat(multiWordSubject.subjectName()).isEqualTo("영어 독해와 작문");
	}

	@Test
	void doesNotStripDigitsThatAreAttachedToTheSubjectName() {
		// "공통국어1"의 "1"은 단위수가 아니라 과목명 일부라서(공백 없이 붙어 있음) 떼어내면 안 된다
		GradeParseResult result = service.parseText("""
				1학년 1학기
				공통국어1 4 90/75.5(10.2) A(300) 2
				""");

		assertThat(result.lines()).hasSize(1);
		assertThat(result.lines().get(0).subjectName()).isEqualTo("공통국어1");
	}

	@Test
	void switchesSemesterWhenNewHeaderAppears() {
		String text = """
				1학년 1학기
				공통국어1 90/75.5(10.2) A(300) 2
				2학년 1학기
				수학I 80/70.0(9.0) B(300) 3
				""";

		GradeParseResult result = service.parseText(text);

		assertThat(result.lines()).hasSize(2);
		assertThat(result.lines().get(1).gradeYear()).isEqualTo(2);
		assertThat(result.lines().get(1).subjectName()).isEqualTo("수학I");
	}

	// PDFBox의 byte[] -> 텍스트 추출 경로 자체가 깨지지 않았는지만 ASCII로 확인(한글 폰트 임베딩은 테스트 환경 제약으로 생략)
	@Test
	void extractsTextFromRealPdf() throws IOException {
		byte[] pdf = buildAsciiPdf("hello grade module");

		GradeParseResult result = service.parse(pdf);

		assertThat(result.lines()).isEmpty();
		assertThat(result.failedCount()).isEqualTo(0);
	}

	private byte[] buildAsciiPdf(String line) throws IOException {
		try (PDDocument document = new PDDocument()) {
			PDPage page = new PDPage();
			document.addPage(page);
			try (PDPageContentStream content = new PDPageContentStream(document, page)) {
				content.beginText();
				content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
				content.newLineAtOffset(50, 700);
				content.showText(line);
				content.endText();
			}
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			document.save(out);
			return out.toByteArray();
		}
	}

}
