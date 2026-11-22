package com.learnershigh.goal.dto;

import com.learnershigh.credit.entity.Department;
import com.learnershigh.goal.entity.StudentGoal;

// GET·PUT /api/student/goal 응답. 희망 학과(id, 이름, 계열)와 목표 대학(없으면 null)
public record StudentGoalResponse(GoalDepartment department, String targetUniversity) {

	public record GoalDepartment(String id, String name, String field) {
	}

	public static StudentGoalResponse of(StudentGoal goal, Department department) {
		return new StudentGoalResponse(
				new GoalDepartment(String.valueOf(department.getId()), department.getName(), department.getField()),
				goal.getTargetUniversity());
	}

}
