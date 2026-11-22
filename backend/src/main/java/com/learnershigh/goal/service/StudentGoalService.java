package com.learnershigh.goal.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.learnershigh.common.DemoStudent;
import com.learnershigh.common.exception.NotFoundException;
import com.learnershigh.common.exception.ValidationException;
import com.learnershigh.credit.entity.Department;
import com.learnershigh.credit.repository.DepartmentRepository;
import com.learnershigh.goal.dto.SaveStudentGoalRequest;
import com.learnershigh.goal.dto.StudentGoalResponse;
import com.learnershigh.goal.entity.StudentGoal;
import com.learnershigh.goal.repository.StudentGoalRepository;

// 희망 학과와 목표 대학 조회·저장 (학생당 1개, 데모 학생 고정)
@Service
public class StudentGoalService {

	// student_goal.target_university 길이
	static final int TARGET_UNIVERSITY_MAX_LENGTH = 100;

	private final StudentGoalRepository goalRepository;
	private final DepartmentRepository departmentRepository;

	public StudentGoalService(StudentGoalRepository goalRepository, DepartmentRepository departmentRepository) {
		this.goalRepository = goalRepository;
		this.departmentRepository = departmentRepository;
	}

	// 저장된 희망 학과가 없으면 null
	@Transactional(readOnly = true)
	public StudentGoalResponse getGoal() {
		return goalRepository.findByStudentId(DemoStudent.ID)
				.map(goal -> StudentGoalResponse.of(goal, findDepartment(String.valueOf(goal.getDepartmentId()))))
				.orElse(null);
	}

	// 없으면 만들고, 있으면 바꾼다
	@Transactional
	public StudentGoalResponse saveGoal(SaveStudentGoalRequest request) {
		String departmentId = request == null || request.departmentId() == null ? "" : request.departmentId().trim();
		String targetUniversity = normalizeTargetUniversity(request == null ? null : request.targetUniversity());

		Map<String, String> errors = new LinkedHashMap<>();
		if (departmentId.isEmpty()) {
			errors.put("departmentId", "희망 학과를 선택하세요.");
		}
		if (targetUniversity != null && targetUniversity.length() > TARGET_UNIVERSITY_MAX_LENGTH) {
			errors.put("targetUniversity", "목표 대학은 " + TARGET_UNIVERSITY_MAX_LENGTH + "자 이하로 입력하세요.");
		}
		if (!errors.isEmpty()) {
			throw new ValidationException("입력값을 확인하세요.", errors);
		}

		Department department = findDepartment(departmentId);
		LocalDateTime now = LocalDateTime.now();
		StudentGoal goal = goalRepository.findByStudentId(DemoStudent.ID)
				.map(existing -> {
					existing.change(department.getId(), targetUniversity, now);
					return existing;
				})
				.orElseGet(() -> goalRepository.save(StudentGoal.create(DemoStudent.ID, department.getId(), targetUniversity, now)));
		return StudentGoalResponse.of(goal, department);
	}

	// 앞뒤 공백을 빼고, 비어 있으면 null
	private static String normalizeTargetUniversity(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private Department findDepartment(String departmentId) {
		Long id;
		try {
			id = Long.valueOf(departmentId);
		} catch (NumberFormatException exception) {
			throw new NotFoundException("학과를 찾을 수 없습니다.");
		}
		return departmentRepository.findById(id).orElseThrow(() -> new NotFoundException("학과를 찾을 수 없습니다."));
	}

}
