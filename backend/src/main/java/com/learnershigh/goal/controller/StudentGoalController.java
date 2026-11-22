package com.learnershigh.goal.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tools.jackson.databind.node.NullNode;

import com.learnershigh.goal.dto.SaveStudentGoalRequest;
import com.learnershigh.goal.dto.StudentGoalResponse;
import com.learnershigh.goal.service.StudentGoalService;

// 학생부 관리 공통: 희망 학과와 목표 대학
@RestController
@RequestMapping("/api/student/goal")
public class StudentGoalController {

	private final StudentGoalService goalService;

	public StudentGoalController(StudentGoalService goalService) {
		this.goalService = goalService;
	}

	// 저장된 값이 없으면 본문이 JSON null이다. 그냥 null을 돌려주면 Spring이 빈 본문을 보내므로 NullNode로 null을 쓴다
	@GetMapping
	public Object getGoal() {
		StudentGoalResponse goal = goalService.getGoal();
		return goal != null ? goal : NullNode.getInstance();
	}

	@PutMapping
	public StudentGoalResponse saveGoal(@RequestBody SaveStudentGoalRequest request) {
		return goalService.saveGoal(request);
	}

}
