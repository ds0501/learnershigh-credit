package com.learnershigh.credit.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learnershigh.credit.entity.GraduationRule;

public interface GraduationRuleRepository extends JpaRepository<GraduationRule, Long> {

	Optional<GraduationRule> findFirstByOrderByEffectiveYearDesc();

}
