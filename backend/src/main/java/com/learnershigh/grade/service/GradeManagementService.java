package com.learnershigh.grade.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.learnershigh.common.DemoStudent;
import com.learnershigh.common.exception.NotFoundException;
import com.learnershigh.common.exception.ValidationException;
import com.learnershigh.grade.dto.GradeRecordRequest;
import com.learnershigh.grade.dto.GradeRecordResponse;
import com.learnershigh.grade.entity.GradeRecord;
import com.learnershigh.grade.repository.GradeRecordRepository;

// 성적 수동 추가·수정·삭제. 저장할 때마다 credit 모듈 반영을 다시 시도한다
@Service
public class GradeManagementService {

	private final GradeRecordRepository gradeRecordRepository;
	private final GradeCreditSyncService creditSyncService;

	public GradeManagementService(GradeRecordRepository gradeRecordRepository, GradeCreditSyncService creditSyncService) {
		this.gradeRecordRepository = gradeRecordRepository;
		this.creditSyncService = creditSyncService;
	}

	@Transactional
	public GradeRecordResponse add(GradeRecordRequest request) {
		validateIdentity(request);
		validateScores(request);
		if (gradeRecordRepository
				.findByStudentIdAndGradeYearAndTermAndSubjectName(DemoStudent.ID, request.gradeYear(), request.term(), request.subjectName().trim())
				.isPresent()) {
			throw new ValidationException("입력값을 확인하세요.", Map.of("subjectName", "이미 등록된 과목입니다."));
		}

		GradeRecord record = new GradeRecord(
				DemoStudent.ID, request.gradeYear(), request.term(), request.subjectName().trim(),
				request.rawScore(), request.subjectAverage(), request.stdDev(), request.gradeLevel(), "MANUAL", null);
		creditSyncService.sync(record);
		return GradeRecordResponse.from(gradeRecordRepository.save(record));
	}

	@Transactional
	public GradeRecordResponse update(String recordId, GradeRecordRequest request) {
		GradeRecord record = findOwnedOrThrow(recordId);
		validateScores(request);
		record.update(request.rawScore(), request.subjectAverage(), request.stdDev(), request.gradeLevel());
		creditSyncService.sync(record);
		return GradeRecordResponse.from(gradeRecordRepository.save(record));
	}

	public void delete(String recordId) {
		gradeRecordRepository.delete(findOwnedOrThrow(recordId));
	}

	private GradeRecord findOwnedOrThrow(String recordId) {
		Long id;
		try {
			id = Long.valueOf(recordId);
		} catch (NumberFormatException exception) {
			throw new NotFoundException("성적을 찾을 수 없습니다.");
		}
		return gradeRecordRepository.findById(id)
				.filter(record -> record.getStudentId().equals(DemoStudent.ID))
				.orElseThrow(() -> new NotFoundException("성적을 찾을 수 없습니다."));
	}

	private void validateIdentity(GradeRecordRequest request) {
		Map<String, String> errors = new LinkedHashMap<>();
		if (request.gradeYear() == null || request.gradeYear() < 1 || request.gradeYear() > 3) {
			errors.put("gradeYear", "학년은 1~3 중 하나여야 합니다.");
		}
		if (request.term() == null || (request.term() != 1 && request.term() != 2)) {
			errors.put("term", "학기는 1 또는 2여야 합니다.");
		}
		if (request.subjectName() == null || request.subjectName().trim().isEmpty()) {
			errors.put("subjectName", "과목명을 입력하세요.");
		}
		if (!errors.isEmpty()) {
			throw new ValidationException("입력값을 확인하세요.", errors);
		}
	}

	private void validateScores(GradeRecordRequest request) {
		Map<String, String> errors = new LinkedHashMap<>();
		Integer rawScore = request.rawScore();
		if (rawScore != null && (rawScore < 0 || rawScore > 100)) {
			errors.put("rawScore", "원점수는 0~100 사이여야 합니다.");
		}
		BigDecimal stdDev = request.stdDev();
		if (stdDev != null && stdDev.signum() < 0) {
			errors.put("stdDev", "표준편차는 0 이상이어야 합니다.");
		}
		Integer gradeLevel = request.gradeLevel();
		if (gradeLevel != null && (gradeLevel < 1 || gradeLevel > 9)) {
			errors.put("gradeLevel", "등급은 1~9 중 하나여야 합니다.");
		}
		if (!errors.isEmpty()) {
			throw new ValidationException("입력값을 확인하세요.", errors);
		}
	}

}
