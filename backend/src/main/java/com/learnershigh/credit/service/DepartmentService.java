package com.learnershigh.credit.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.learnershigh.common.DemoStudent;
import com.learnershigh.common.exception.NotFoundException;
import com.learnershigh.credit.dto.DepartmentDetailResponse;
import com.learnershigh.credit.dto.DepartmentSubjectResponse;
import com.learnershigh.credit.dto.DepartmentSummaryResponse;
import com.learnershigh.credit.entity.CourseCatalog;
import com.learnershigh.credit.entity.Department;
import com.learnershigh.credit.entity.DepartmentSubject;
import com.learnershigh.credit.entity.StudentCourse;
import com.learnershigh.credit.repository.CourseCatalogRepository;
import com.learnershigh.credit.repository.DepartmentRepository;
import com.learnershigh.credit.repository.DepartmentSubjectRepository;
import com.learnershigh.credit.repository.StudentCourseRepository;

// 학과 로드맵: 학과 검색과 추천 과목 상세 (docs/dept_subject_2depth.xlsx 기반)
@Service
public class DepartmentService {

	private static final int SEARCH_LIMIT = 10;

	private final DepartmentRepository departmentRepository;
	private final DepartmentSubjectRepository departmentSubjectRepository;
	private final CourseCatalogRepository catalogRepository;
	private final StudentCourseRepository studentCourseRepository;

	public DepartmentService(
			DepartmentRepository departmentRepository,
			DepartmentSubjectRepository departmentSubjectRepository,
			CourseCatalogRepository catalogRepository,
			StudentCourseRepository studentCourseRepository) {
		this.departmentRepository = departmentRepository;
		this.departmentSubjectRepository = departmentSubjectRepository;
		this.catalogRepository = catalogRepository;
		this.studentCourseRepository = studentCourseRepository;
	}

	// 학과명 부분 일치(공백 무시). query가 비어 있으면 빈 목록. 최대 10개, 이름 순
	public List<DepartmentSummaryResponse> search(String query) {
		String keyword = query == null ? "" : query.replaceAll("\\s+", "");
		if (keyword.isEmpty()) {
			return List.of();
		}
		return departmentRepository.findByEffectiveYear(latestEffectiveYear()).stream()
				.filter(department -> department.getName().replaceAll("\\s+", "").contains(keyword))
				.sorted(Comparator.comparing(Department::getName))
				.limit(SEARCH_LIMIT)
				.map(DepartmentSummaryResponse::from)
				.toList();
	}

	public DepartmentDetailResponse getDetail(String departmentId) {
		Department department = findDepartment(departmentId);
		List<DepartmentSubject> subjects = departmentSubjectRepository.findByDepartmentIdOrderByRatioPercentDesc(department.getId());

		Map<Long, CourseCatalog> catalogById = catalogRepository.findAllById(subjects.stream().map(DepartmentSubject::getCatalogId).toList())
				.stream()
				.collect(Collectors.toMap(CourseCatalog::getId, catalog -> catalog));
		Set<Long> registeredCatalogIds = studentCourseRepository.findByStudentIdOrderByIdAsc(DemoStudent.ID).stream()
				.map(StudentCourse::getCatalogId)
				.filter(catalogId -> catalogId != null)
				.collect(Collectors.toSet());

		List<DepartmentSubjectResponse> subjectResponses = subjects.stream()
				.map(subject -> {
					CourseCatalog catalog = catalogById.get(subject.getCatalogId());
					return new DepartmentSubjectResponse(
							String.valueOf(subject.getCatalogId()),
							catalog == null ? "" : catalog.getName(),
							catalog == null ? "" : catalog.getCourseType().name(),
							subject.getRequiredUniversityCount(),
							subject.getTotalUniversityCount(),
							subject.getRatioPercent().doubleValue(),
							Boolean.TRUE.equals(subject.getFullCommon()),
							subject.getDesignationType(),
							Boolean.TRUE.equals(subject.getEduofficeListed()),
							subject.getRequiredUniversities(),
							registeredCatalogIds.contains(subject.getCatalogId()));
				})
				.toList();

		return new DepartmentDetailResponse(
				String.valueOf(department.getId()),
				department.getName(),
				department.getField(),
				department.getTotalUniversityCount(),
				Boolean.TRUE.equals(department.getLowSample()),
				department.getGeneralElectiveText(),
				department.getCareerElectiveText(),
				department.getFusionElectiveText(),
				subjectResponses);
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

	private Integer latestEffectiveYear() {
		return departmentRepository.findLatestEffectiveYear()
				.orElseThrow(() -> new IllegalStateException("학과 로드맵 자료가 없습니다."));
	}

}
