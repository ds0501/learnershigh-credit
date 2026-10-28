package com.learnershigh.credit.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learnershigh.credit.entity.CompletionRule;
import com.learnershigh.credit.entity.CourseType;

public interface CompletionRuleRepository extends JpaRepository<CompletionRule, Long> {

	Optional<CompletionRule> findFirstByCourseTypeOrderByEffectiveYearDesc(CourseType courseType);

}
