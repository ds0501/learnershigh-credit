package com.learnershigh.credit.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.learnershigh.credit.dto.CatalogItemResponse;
import com.learnershigh.credit.entity.CourseCatalog;
import com.learnershigh.credit.entity.StudentCourse;
import com.learnershigh.credit.repository.CourseCatalogRepository;

// 표준 과목 검색 (api-contract.md 4.4)
@Service
public class CourseCatalogService {

	private static final int SEARCH_LIMIT = 10;

	private final CourseCatalogRepository catalogRepository;

	public CourseCatalogService(CourseCatalogRepository catalogRepository) {
		this.catalogRepository = catalogRepository;
	}

	// 과목명 부분 일치(공백 무시). query가 비어 있거나 없으면 빈 목록. 최대 10개, id 순서대로
	public List<CatalogItemResponse> search(String query) {
		String keyword = query == null ? "" : query.replaceAll("\\s+", "");
		if (keyword.isEmpty()) {
			return List.of();
		}
		return catalogRepository.findAllByOrderByIdAsc().stream()
				.filter(catalog -> catalog.getName().replaceAll("\\s+", "").contains(keyword))
				.limit(SEARCH_LIMIT)
				.map(CatalogItemResponse::from)
				.toList();
	}

	// 등록 처리에서 쓰는 단건 조회. 없으면 Optional.empty()를 그대로 돌려준다(검증은 호출하는 쪽에서 한다)
	public Optional<CourseCatalog> findById(Long id) {
		return catalogRepository.findById(id);
	}

	// 학생 과목의 과목명. catalogId가 있으면 catalog.name, 없으면 customName (StudentCourse는 과목명을 저장하지 않음)
	public String resolveCourseName(StudentCourse course) {
		if (course.getCatalogId() != null) {
			return findById(course.getCatalogId()).map(CourseCatalog::getName).orElse("");
		}
		return course.getCustomName() == null ? "" : course.getCustomName();
	}

}
