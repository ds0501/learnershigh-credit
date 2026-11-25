package com.learnershigh.grade.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// 성적표 PDF 업로드 한 건의 이력. 파일 자체는 저장하지 않고 파싱 결과 요약만 남긴다.
@Entity
@Table(name = "grade_upload")
public class GradeUpload {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "student_id", nullable = false, length = 100)
	private String studentId;

	@Column(name = "original_file_name", nullable = false, length = 255)
	private String originalFileName;

	@Column(name = "uploaded_at", nullable = false)
	private LocalDateTime uploadedAt;

	@Column(name = "recognized_count", nullable = false)
	private Integer recognizedCount;

	@Column(name = "failed_count", nullable = false)
	private Integer failedCount;

	protected GradeUpload() {
	}

	public GradeUpload(String studentId, String originalFileName, LocalDateTime uploadedAt, Integer recognizedCount, Integer failedCount) {
		this.studentId = studentId;
		this.originalFileName = originalFileName;
		this.uploadedAt = uploadedAt;
		this.recognizedCount = recognizedCount;
		this.failedCount = failedCount;
	}

	public Long getId() {
		return id;
	}

	public String getStudentId() {
		return studentId;
	}

	public String getOriginalFileName() {
		return originalFileName;
	}

	public LocalDateTime getUploadedAt() {
		return uploadedAt;
	}

	public Integer getRecognizedCount() {
		return recognizedCount;
	}

	public Integer getFailedCount() {
		return failedCount;
	}

}
