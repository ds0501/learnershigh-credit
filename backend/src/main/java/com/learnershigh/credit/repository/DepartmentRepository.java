package com.learnershigh.credit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.learnershigh.credit.entity.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

	List<Department> findByEffectiveYear(Integer effectiveYear);

	// 자료가 여러 연도로 쌓여도 검색은 가장 최근 연도만 보여준다
	@Query("SELECT MAX(d.effectiveYear) FROM Department d")
	Optional<Integer> findLatestEffectiveYear();

}
