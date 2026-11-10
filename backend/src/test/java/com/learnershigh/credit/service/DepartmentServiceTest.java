package com.learnershigh.credit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.learnershigh.common.DemoStudent;
import com.learnershigh.common.exception.NotFoundException;
import com.learnershigh.credit.dto.DepartmentDetailResponse;
import com.learnershigh.credit.dto.DepartmentSummaryResponse;
import com.learnershigh.credit.entity.CourseCatalog;
import com.learnershigh.credit.entity.CourseType;
import com.learnershigh.credit.entity.Department;
import com.learnershigh.credit.entity.DepartmentSubject;
import com.learnershigh.credit.entity.StudentCourse;
import com.learnershigh.credit.repository.CourseCatalogRepository;
import com.learnershigh.credit.repository.DepartmentRepository;
import com.learnershigh.credit.repository.DepartmentSubjectRepository;
import com.learnershigh.credit.repository.StudentCourseRepository;

// 학과 검색·상세 (docs/dept_subject_2depth.xlsx 기반). 간호학과 실제 값을 그대로 쓴다
@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

	@Mock
	private DepartmentRepository departmentRepository;

	@Mock
	private DepartmentSubjectRepository departmentSubjectRepository;

	@Mock
	private CourseCatalogRepository catalogRepository;

	@Mock
	private StudentCourseRepository studentCourseRepository;

	private DepartmentService service;

	@BeforeEach
	void setUp() {
		service = new DepartmentService(departmentRepository, departmentSubjectRepository, catalogRepository, studentCourseRepository);
	}

	private Department department(long id, String name, String field, int total, boolean lowSample) {
		Department department = mock(Department.class);
		lenient().when(department.getId()).thenReturn(id);
		lenient().when(department.getName()).thenReturn(name);
		lenient().when(department.getField()).thenReturn(field);
		lenient().when(department.getTotalUniversityCount()).thenReturn(total);
		lenient().when(department.getLowSample()).thenReturn(lowSample);
		return department;
	}

	private CourseCatalog catalog(long id, String name, CourseType courseType) {
		CourseCatalog catalog = mock(CourseCatalog.class);
		when(catalog.getId()).thenReturn(id);
		when(catalog.getName()).thenReturn(name);
		when(catalog.getCourseType()).thenReturn(courseType);
		return catalog;
	}

	private DepartmentSubject subject(
			long catalogId, int required, int total, String ratio, boolean fullCommon, String designation, boolean eduoffice, String universities) {
		DepartmentSubject subject = mock(DepartmentSubject.class);
		when(subject.getCatalogId()).thenReturn(catalogId);
		when(subject.getRequiredUniversityCount()).thenReturn(required);
		when(subject.getTotalUniversityCount()).thenReturn(total);
		when(subject.getRatioPercent()).thenReturn(new BigDecimal(ratio));
		when(subject.getFullCommon()).thenReturn(fullCommon);
		when(subject.getDesignationType()).thenReturn(designation);
		when(subject.getEduofficeListed()).thenReturn(eduoffice);
		when(subject.getRequiredUniversities()).thenReturn(universities);
		return subject;
	}

	@Test
	void 학과명_부분일치로_검색한다() {
		when(departmentRepository.findLatestEffectiveYear()).thenReturn(Optional.of(2028));
		Department nursing = department(1L, "간호학과", "보건·의약학", 13, false);
		Department pathology = department(2L, "임상병리학과", "보건·의약학", 4, false);
		when(departmentRepository.findByEffectiveYear(2028)).thenReturn(List.of(nursing, pathology));

		List<DepartmentSummaryResponse> response = service.search("간호");

		assertThat(response).hasSize(1);
		assertThat(response.get(0).id()).isEqualTo("1");
		assertThat(response.get(0).name()).isEqualTo("간호학과");
		assertThat(response.get(0).field()).isEqualTo("보건·의약학");
		assertThat(response.get(0).totalUniversityCount()).isEqualTo(13);
		assertThat(response.get(0).lowSample()).isFalse();
	}

	@Test
	void 검색어가_없으면_빈목록() {
		assertThat(service.search(null)).isEmpty();
		assertThat(service.search("")).isEmpty();
	}

	@Test
	void 학과상세는_과목을_비율_내림차순으로_돌려주고_등록여부를_계산한다() {
		Department nursing = department(16L, "간호학과", "보건·의약학", 13, false);
		when(departmentRepository.findById(16L)).thenReturn(Optional.of(nursing));

		DepartmentSubject lifeScience = subject(1L, 10, 13, "76.9", false, "CORE", true, "가톨릭대, 경희대");
		DepartmentSubject probability = subject(2L, 4, 13, "30.8", false, "CORE", true, "경희대, 아주대, 원광대, 충남대");
		when(departmentSubjectRepository.findByDepartmentIdOrderByRatioPercentDesc(16L)).thenReturn(List.of(lifeScience, probability));

		CourseCatalog lifeScienceCatalog = catalog(1L, "생명과학", CourseType.GENERAL);
		CourseCatalog probabilityCatalog = catalog(2L, "확률과 통계", CourseType.GENERAL);
		when(catalogRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(lifeScienceCatalog, probabilityCatalog));

		StudentCourse registeredProbability = mock(StudentCourse.class);
		when(registeredProbability.getCatalogId()).thenReturn(2L);
		when(studentCourseRepository.findByStudentIdOrderByIdAsc(DemoStudent.ID)).thenReturn(List.of(registeredProbability));

		DepartmentDetailResponse response = service.getDetail("16");

		assertThat(response.id()).isEqualTo("16");
		assertThat(response.name()).isEqualTo("간호학과");
		assertThat(response.subjects()).hasSize(2);
		assertThat(response.subjects().get(0).name()).isEqualTo("생명과학");
		assertThat(response.subjects().get(0).courseType()).isEqualTo("GENERAL");
		assertThat(response.subjects().get(0).ratioPercent()).isEqualTo(76.9);
		assertThat(response.subjects().get(0).alreadyRegistered()).isFalse();
		assertThat(response.subjects().get(1).name()).isEqualTo("확률과 통계");
		assertThat(response.subjects().get(1).alreadyRegistered()).isTrue();
	}

	@Test
	void 없는_학과는_404() {
		when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

		NotFoundException exception = assertThrows(NotFoundException.class, () -> service.getDetail("999"));

		assertThat(exception.getMessage()).isEqualTo("학과를 찾을 수 없습니다.");
	}

	@Test
	void 숫자가_아닌_departmentId도_404() {
		assertThrows(NotFoundException.class, () -> service.getDetail("abc"));
	}

}
