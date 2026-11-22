package com.learnershigh.goal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learnershigh.goal.entity.StudentGoal;

public interface StudentGoalRepository extends JpaRepository<StudentGoal, Long> {

	Optional<StudentGoal> findByStudentId(String studentId);

}
