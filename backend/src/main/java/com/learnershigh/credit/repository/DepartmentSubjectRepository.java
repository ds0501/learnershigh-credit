package com.learnershigh.credit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learnershigh.credit.entity.DepartmentSubject;

public interface DepartmentSubjectRepository extends JpaRepository<DepartmentSubject, Long> {

	List<DepartmentSubject> findByDepartmentIdOrderByRatioPercentDesc(Long departmentId);

}
