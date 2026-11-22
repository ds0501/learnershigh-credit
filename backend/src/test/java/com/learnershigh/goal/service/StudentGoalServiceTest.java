package com.learnershigh.goal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.learnershigh.common.DemoStudent;
import com.learnershigh.common.exception.NotFoundException;
import com.learnershigh.common.exception.ValidationException;
import com.learnershigh.credit.entity.Department;
import com.learnershigh.credit.repository.DepartmentRepository;
import com.learnershigh.goal.dto.SaveStudentGoalRequest;
import com.learnershigh.goal.dto.StudentGoalResponse;
import com.learnershigh.goal.entity.StudentGoal;
import com.learnershigh.goal.repository.StudentGoalRepository;

// 희망 학과·목표 대학 조회·저장 (학생당 1개, 데모 학생)
@ExtendWith(MockitoExtension.class)
class StudentGoalServiceTest {

	@Mock
	private StudentGoalRepository goalRepository;

	@Mock
	private DepartmentRepository departmentRepository;

	private StudentGoalService service;

	@BeforeEach
	void setUp() {
		service = new StudentGoalService(goalRepository, departmentRepository);
	}

	private Department department(long id, String name, String field) {
		Department department = mock(Department.class);
		lenient().when(department.getId()).thenReturn(id);
		lenient().when(department.getName()).thenReturn(name);
		lenient().when(department.getField()).thenReturn(field);
		when(departmentRepository.findById(id)).thenReturn(Optional.of(department));
		return department;
	}

	@Test
	void 저장된_희망_학과가_없으면_null() {
		when(goalRepository.findByStudentId(DemoStudent.ID)).thenReturn(Optional.empty());

		assertThat(service.getGoal()).isNull();
	}

	@Test
	void 저장된_희망_학과와_목표_대학을_돌려준다() {
		department(12L, "간호학과", "보건·의약학");
		StudentGoal goal = StudentGoal.create(DemoStudent.ID, 12L, "서울대학교", LocalDateTime.now());
		when(goalRepository.findByStudentId(DemoStudent.ID)).thenReturn(Optional.of(goal));

		StudentGoalResponse response = service.getGoal();

		assertThat(response.department()).isEqualTo(new StudentGoalResponse.GoalDepartment("12", "간호학과", "보건·의약학"));
		assertThat(response.targetUniversity()).isEqualTo("서울대학교");
	}

	@Test
	void 처음_저장하면_새로_만든다_목표_대학_앞뒤_공백은_뺀다() {
		department(12L, "간호학과", "보건·의약학");
		when(goalRepository.findByStudentId(DemoStudent.ID)).thenReturn(Optional.empty());
		when(goalRepository.save(any(StudentGoal.class))).thenAnswer(invocation -> invocation.getArgument(0));

		StudentGoalResponse response = service.saveGoal(new SaveStudentGoalRequest("12", "  연세대학교 "));

		ArgumentCaptor<StudentGoal> saved = ArgumentCaptor.forClass(StudentGoal.class);
		verify(goalRepository).save(saved.capture());
		assertThat(saved.getValue().getStudentId()).isEqualTo(DemoStudent.ID);
		assertThat(saved.getValue().getDepartmentId()).isEqualTo(12L);
		assertThat(saved.getValue().getTargetUniversity()).isEqualTo("연세대학교");
		assertThat(saved.getValue().getUpdatedAt()).isNotNull();
		assertThat(response.department().name()).isEqualTo("간호학과");
		assertThat(response.targetUniversity()).isEqualTo("연세대학교");
	}

	@Test
	void 이미_있으면_바꾼다_목표_대학을_비우면_null() {
		department(30L, "컴퓨터공학과", "공학");
		StudentGoal existing = StudentGoal.create(DemoStudent.ID, 12L, "서울대학교", LocalDateTime.of(2026, 11, 1, 9, 0));
		when(goalRepository.findByStudentId(DemoStudent.ID)).thenReturn(Optional.of(existing));

		StudentGoalResponse response = service.saveGoal(new SaveStudentGoalRequest("30", "   "));

		verify(goalRepository, never()).save(any());
		assertThat(existing.getDepartmentId()).isEqualTo(30L);
		assertThat(existing.getTargetUniversity()).isNull();
		assertThat(existing.getUpdatedAt()).isAfter(LocalDateTime.of(2026, 11, 1, 9, 0));
		assertThat(response).isEqualTo(new StudentGoalResponse(new StudentGoalResponse.GoalDepartment("30", "컴퓨터공학과", "공학"), null));
	}

	@Test
	void 없는_학과_id면_404() {
		when(departmentRepository.findById(9999L)).thenReturn(Optional.empty());

		NotFoundException exception = assertThrows(NotFoundException.class, () -> service.saveGoal(new SaveStudentGoalRequest("9999", null)));

		assertThat(exception.getMessage()).isEqualTo("학과를 찾을 수 없습니다.");
		verify(goalRepository, never()).save(any());
	}

	@Test
	void 숫자가_아닌_학과_id도_404() {
		assertThrows(NotFoundException.class, () -> service.saveGoal(new SaveStudentGoalRequest("abc", null)));
	}

	@Test
	void 학과_id가_비어_있거나_목표_대학이_너무_길면_400() {
		ValidationException exception = assertThrows(ValidationException.class,
				() -> service.saveGoal(new SaveStudentGoalRequest(" ", "가".repeat(StudentGoalService.TARGET_UNIVERSITY_MAX_LENGTH + 1))));

		assertThat(exception.getMessage()).isEqualTo("입력값을 확인하세요.");
		assertThat(exception.getFieldErrors()).isEqualTo(Map.of(
				"departmentId", "희망 학과를 선택하세요.",
				"targetUniversity", "목표 대학은 100자 이하로 입력하세요."));
		verify(goalRepository, never()).save(any());
	}

}
