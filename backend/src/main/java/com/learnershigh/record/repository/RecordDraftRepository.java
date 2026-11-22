package com.learnershigh.record.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learnershigh.record.entity.RecordDraft;

public interface RecordDraftRepository extends JpaRepository<RecordDraft, Long> {

	List<RecordDraft> findByStudentIdAndSemesterOrderByIdAsc(String studentId, String semester);

	boolean existsByStudentIdAndSemesterAndSubjectName(String studentId, String semester, String subjectName);

}
