package com.learnershigh.grade.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learnershigh.grade.entity.GradeUpload;

public interface GradeUploadRepository extends JpaRepository<GradeUpload, Long> {
}
