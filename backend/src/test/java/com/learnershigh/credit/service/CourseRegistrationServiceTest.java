package com.learnershigh.credit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.learnershigh.common.DemoStudent;
import com.learnershigh.common.exception.ValidationException;
import com.learnershigh.credit.dto.CourseItemResponse;
import com.learnershigh.credit.dto.CourseRegistrationRequest;
import com.learnershigh.credit.entity.CourseCatalog;
import com.learnershigh.credit.entity.CourseStatus;
import com.learnershigh.credit.entity.CourseType;
import com.learnershigh.credit.entity.StudentCourse;
import com.learnershigh.credit.repository.StudentCourseRepository;

// api-contract.md 4.5의 검증 규칙과 응답 모양을 확인한다. frontend courseRegistration.test.ts와 같은 케이스를 쓴다
@ExtendWith(MockitoExtension.class)
class CourseRegistrationServiceTest {

	@Mock
	private StudentCourseRepository studentCourseRepository;

	@Mock
	private CourseCatalogService catalogService;

	private CourseRegistrationService service;

	@BeforeEach
	void setUp() {
		service = new CourseRegistrationService(studentCourseRepository, catalogService);
		when(studentCourseRepository.findByStudentIdAndGradeYearAndTerm(any(), any(), any()))
			.thenReturn(List.of());
	}

	private CourseCatalog catalog(long id, String name, CourseType courseType) {
		CourseCatalog catalog = mock(CourseCatalog.class);
		when(catalog.getId()).thenReturn(id);
		when(catalog.getName()).thenReturn(name);
		when(catalog.getCourseType()).thenReturn(courseType);
		return catalog;
	}

	private StudentCourse savedAs(long id, Integer credits, CourseStatus status) {
		StudentCourse saved = mock(StudentCourse.class);
		when(saved.getId()).thenReturn(id);
		when(saved.getCredits()).thenReturn(credits);
		when(saved.getStatus()).thenReturn(status);
		return saved;
	}

	private CourseRegistrationRequest catalogRequest(String catalogId, Integer credits) {
		return new CourseRegistrationRequest(1, 2, catalogId, null, null, credits, "IN_PROGRESS", 60, 40, "ACHIEVEMENT_AND_RANK");
	}

	private CourseRegistrationRequest directRequest(String customName, String courseType, Integer credits) {
		return new CourseRegistrationRequest(1, 2, null, customName, courseType, credits, "IN_PROGRESS", 60, 40, "ACHIEVEMENT_AND_RANK");
	}

	@Test
	void 목록과목등록_성공() {
		CourseCatalog catalogEntity = catalog(17L, "확률과 통계", CourseType.GENERAL);
		when(catalogService.findById(17L)).thenReturn(Optional.of(catalogEntity));
		StudentCourse saved = savedAs(21L, 4, CourseStatus.IN_PROGRESS);
		when(studentCourseRepository.save(any())).thenReturn(saved);

		CourseItemResponse response = service.register(catalogRequest("17", 4));

		assertThat(response.getId()).isEqualTo("21");
		assertThat(response.getName()).isEqualTo("확률과 통계");
		assertThat(response.getCourseType()).isEqualTo("GENERAL");
		assertThat(response.getCredits()).isEqualTo(4);
		assertThat(response.getAchievementRate()).isNull();
		assertThat(response.getRiskLevel()).isEqualTo("NO_DATA");
		assertThat(response.getRequiredAverage()).isNull();
	}

	@Test
	void 창체는_반영비율을_0으로_저장하고_NORMAL이다() {
		CourseCatalog catalogEntity = catalog(20L, "창의적 체험활동", CourseType.CREATIVE);
		when(catalogService.findById(20L)).thenReturn(Optional.of(catalogEntity));
		when(studentCourseRepository.save(any())).thenAnswer(invocation -> {
			StudentCourse arg = invocation.getArgument(0);
			assertThat(arg.getWrittenWeight()).isEqualTo(0);
			assertThat(arg.getPerformanceWeight()).isEqualTo(0);
			assertThat(arg.getGradeDisplay()).isNull();
			return savedAs(22L, 3, CourseStatus.IN_PROGRESS);
		});

		CourseItemResponse response = service.register(catalogRequest("20", 3));

		assertThat(response.getRiskLevel()).isEqualTo("NORMAL");
	}

	@Test
	void 직접입력_등록_성공() {
		StudentCourse saved = savedAs(23L, 2, CourseStatus.COMPLETED);
		when(studentCourseRepository.save(any())).thenReturn(saved);
		CourseRegistrationRequest request = new CourseRegistrationRequest(1, 2, null, "AI 기초", "CAREER", 2, "COMPLETED", 0, 100, "THREE_LEVEL");

		CourseItemResponse response = service.register(request);

		assertThat(response.getName()).isEqualTo("AI 기초");
		assertThat(response.getCourseType()).isEqualTo("CAREER");
		assertThat(response.getStatus()).isEqualTo("COMPLETED");
	}

	@Test
	void 목록에_없는_catalogId() {
		when(catalogService.findById(999L)).thenReturn(Optional.empty());

		ValidationException exception = assertThrows(
			ValidationException.class, () -> service.register(catalogRequest("999", 4)));

		assertThat(exception.getFieldErrors()).containsEntry("name", "과목 목록에서 찾을 수 없는 과목입니다.");
	}

	@Test
	void 직접입력인데_과목명이_비어있음() {
		ValidationException exception = assertThrows(
			ValidationException.class, () -> service.register(directRequest("   ", "CAREER", 2)));

		assertThat(exception.getFieldErrors()).containsEntry("name", "과목명을 입력하세요.");
	}

	@Test
	void 같은_학기에_이미_등록된_과목명() {
		StudentCourse existing = mock(StudentCourse.class);
		when(existing.getCatalogId()).thenReturn(null);
		when(existing.getCustomName()).thenReturn("AI 기초");
		when(studentCourseRepository.findByStudentIdAndGradeYearAndTerm(DemoStudent.ID, 1, 2)).thenReturn(List.of(existing));

		ValidationException exception = assertThrows(
			ValidationException.class, () -> service.register(directRequest("ai 기초", "CAREER", 2)));

		assertThat(exception.getFieldErrors()).containsEntry("name", "같은 학기에 이미 등록된 과목입니다.");
	}

	@Test
	void 직접입력인데_유형이_없음() {
		ValidationException exception = assertThrows(
			ValidationException.class, () -> service.register(directRequest("AI 기초", null, 2)));

		assertThat(exception.getFieldErrors()).containsEntry("courseType", "과목 유형을 선택하세요.");
	}

	@Test
	void 학점이_1보다_작음() {
		ValidationException exception = assertThrows(
			ValidationException.class, () -> service.register(directRequest("AI 기초", "CAREER", 0)));

		assertThat(exception.getFieldErrors()).containsEntry("credits", "학점은 1 이상의 정수여야 합니다.");
	}

	@Test
	void 반영비율_합이_100이_아님() {
		CourseRegistrationRequest request = new CourseRegistrationRequest(1, 2, null, "AI 기초", "CAREER", 2, "IN_PROGRESS", 60, 50, "THREE_LEVEL");

		ValidationException exception = assertThrows(
			ValidationException.class, () -> service.register(request));

		assertThat(exception.getFieldErrors()).containsEntry("writtenWeight", "지필과 수행 반영 비율의 합은 100%여야 합니다.");
	}

	@Test
	void 이수상태가_잘못됨() {
		CourseRegistrationRequest request = new CourseRegistrationRequest(1, 2, null, "AI 기초", "CAREER", 2, "DONE", 60, 40, "THREE_LEVEL");

		ValidationException exception = assertThrows(
			ValidationException.class, () -> service.register(request));

		assertThat(exception.getFieldErrors()).containsEntry("status", "이수 상태를 선택하세요.");
	}

	@Test
	void 성적표기방식이_잘못됨() {
		CourseRegistrationRequest request = new CourseRegistrationRequest(1, 2, null, "AI 기초", "CAREER", 2, "IN_PROGRESS", 60, 40, "INVALID");

		ValidationException exception = assertThrows(
			ValidationException.class, () -> service.register(request));

		assertThat(exception.getFieldErrors()).containsEntry("gradeDisplay", "성적 표기 방식을 선택하세요.");
	}

	@Test
	void 학기가_잘못됨() {
		CourseRegistrationRequest request = new CourseRegistrationRequest(4, 3, null, "AI 기초", "CAREER", 2, "IN_PROGRESS", 60, 40, "THREE_LEVEL");

		ValidationException exception = assertThrows(
			ValidationException.class, () -> service.register(request));

		assertThat(exception.getFieldErrors()).containsEntry("term", "학기를 선택하세요.");
	}

	@Test
	void 여러_필드가_동시에_잘못됨() {
		CourseRegistrationRequest request = new CourseRegistrationRequest(1, 2, null, "", null, 0, "IN_PROGRESS", 60, 50, "THREE_LEVEL");

		ValidationException exception = assertThrows(
			ValidationException.class, () -> service.register(request));

		assertThat(exception.getFieldErrors())
			.containsEntry("courseType", "과목 유형을 선택하세요.")
			.containsEntry("name", "과목명을 입력하세요.")
			.containsEntry("credits", "학점은 1 이상의 정수여야 합니다.")
			.containsEntry("writtenWeight", "지필과 수행 반영 비율의 합은 100%여야 합니다.");
	}

}
