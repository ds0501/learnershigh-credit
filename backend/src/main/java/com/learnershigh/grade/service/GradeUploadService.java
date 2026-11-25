package com.learnershigh.grade.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.learnershigh.common.DemoStudent;
import com.learnershigh.common.exception.ValidationException;
import com.learnershigh.grade.dto.GradeRecordResponse;
import com.learnershigh.grade.dto.GradeUploadResponse;
import com.learnershigh.grade.entity.GradeRecord;
import com.learnershigh.grade.entity.GradeUpload;
import com.learnershigh.grade.repository.GradeRecordRepository;
import com.learnershigh.grade.repository.GradeUploadRepository;

// 나이스 성적표 PDF 업로드 처리. 파일은 파싱 후 버리고 메타만 grade_upload에 남긴다
@Service
public class GradeUploadService {

	private final GradePdfParsingService pdfParsingService;
	private final GradeRecordRepository gradeRecordRepository;
	private final GradeUploadRepository gradeUploadRepository;
	private final GradeCreditSyncService creditSyncService;

	public GradeUploadService(
			GradePdfParsingService pdfParsingService,
			GradeRecordRepository gradeRecordRepository,
			GradeUploadRepository gradeUploadRepository,
			GradeCreditSyncService creditSyncService) {
		this.pdfParsingService = pdfParsingService;
		this.gradeRecordRepository = gradeRecordRepository;
		this.gradeUploadRepository = gradeUploadRepository;
		this.creditSyncService = creditSyncService;
	}

	@Transactional
	public GradeUploadResponse upload(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new ValidationException("입력값을 확인하세요.", Map.of("file", "PDF 파일을 선택하세요."));
		}

		GradeParseResult parsed;
		try {
			parsed = pdfParsingService.parse(file.getBytes());
		} catch (IOException exception) {
			throw new ValidationException("입력값을 확인하세요.", Map.of("file", "PDF 파일을 읽을 수 없습니다."));
		}

		GradeUpload upload = gradeUploadRepository.save(new GradeUpload(
				DemoStudent.ID, file.getOriginalFilename(), LocalDateTime.now(), parsed.lines().size(), parsed.failedCount()));

		List<GradeRecordResponse> responses = new ArrayList<>();
		for (ParsedGradeLine line : parsed.lines()) {
			GradeRecord record = gradeRecordRepository
					.findByStudentIdAndGradeYearAndTermAndSubjectName(DemoStudent.ID, line.gradeYear(), line.term(), line.subjectName())
					.orElse(null);
			if (record == null) {
				record = new GradeRecord(
						DemoStudent.ID, line.gradeYear(), line.term(), line.subjectName(),
						line.rawScore(), line.subjectAverage(), line.stdDev(), line.gradeLevel(), "PDF", upload.getId());
			} else {
				record.upsertFromParsed(line.rawScore(), line.subjectAverage(), line.stdDev(), line.gradeLevel(), upload.getId());
			}
			creditSyncService.sync(record);
			responses.add(GradeRecordResponse.from(gradeRecordRepository.save(record)));
		}

		return new GradeUploadResponse(String.valueOf(upload.getId()), parsed.lines().size(), parsed.failedCount(), responses);
	}

}
