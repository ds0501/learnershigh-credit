package com.learnershigh.goal.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.learnershigh.common.exception.NotFoundException;
import com.learnershigh.goal.dto.StudentGoalResponse;
import com.learnershigh.goal.service.StudentGoalService;

// DB 없이 웹 계층만 띄워 응답 본문 모양(null, 404 오류 형식)을 확인한다
@WebMvcTest(StudentGoalController.class)
class StudentGoalControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private StudentGoalService goalService;

	@Test
	void 희망_학과가_없으면_200과_JSON_null() throws Exception {
		when(goalService.getGoal()).thenReturn(null);

		mockMvc.perform(get("/api/student/goal"))
				.andExpect(status().isOk())
				.andExpect(content().string("null"));
	}

	@Test
	void 희망_학과가_있으면_학과와_목표_대학() throws Exception {
		when(goalService.getGoal()).thenReturn(
				new StudentGoalResponse(new StudentGoalResponse.GoalDepartment("12", "간호학과", "보건·의약학"), "서울대학교"));

		mockMvc.perform(get("/api/student/goal"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.department.id").value("12"))
				.andExpect(jsonPath("$.department.name").value("간호학과"))
				.andExpect(jsonPath("$.department.field").value("보건·의약학"))
				.andExpect(jsonPath("$.targetUniversity").value("서울대학교"));
	}

	@Test
	void 없는_학과로_저장하면_404_오류_형식() throws Exception {
		when(goalService.saveGoal(any())).thenThrow(new NotFoundException("학과를 찾을 수 없습니다."));

		mockMvc.perform(put("/api/student/goal").contentType(MediaType.APPLICATION_JSON).content("{\"departmentId\":\"9999\"}"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("학과를 찾을 수 없습니다."))
				.andExpect(jsonPath("$.fieldErrors").isEmpty());
	}

}
