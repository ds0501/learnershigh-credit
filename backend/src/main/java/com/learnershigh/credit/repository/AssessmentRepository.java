package com.learnershigh.credit.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learnershigh.credit.entity.Assessment;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

	List<Assessment> findByStudentCourseIdIn(Collection<Long> studentCourseIds);

}
