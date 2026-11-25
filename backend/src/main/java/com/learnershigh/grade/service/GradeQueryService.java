package com.learnershigh.grade.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.learnershigh.common.DemoStudent;
import com.learnershigh.grade.dto.GradeRecordResponse;
import com.learnershigh.grade.repository.GradeRecordRepository;

@Service
public class GradeQueryService {

	private final GradeRecordRepository gradeRecordRepository;

	public GradeQueryService(GradeRecordRepository gradeRecordRepository) {
		this.gradeRecordRepository = gradeRecordRepository;
	}

	public List<GradeRecordResponse> list(Integer gradeYear, Integer term) {
		return gradeRecordRepository.findByStudentIdAndGradeYearAndTermOrderByIdAsc(DemoStudent.ID, gradeYear, term)
				.stream()
				.map(GradeRecordResponse::from)
				.toList();
	}

}
