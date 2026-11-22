package com.learnershigh.record.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// 과목별 세특 초안 (학생·학기·과목명마다 하나). semester는 '학년-학기' 문자열('1-2')
@Entity
@Table(name = "record_draft")
public class RecordDraft {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "student_id", nullable = false, length = 100)
	private String studentId;

	@Column(nullable = false, length = 3)
	private String semester;

	@Column(name = "subject_name", nullable = false, length = 100)
	private String subjectName;

	// 학점 현황에 등록한 과목(credit_student_course). 과목을 지우면 DB가 NULL로 바꾼다
	@Column(name = "course_id")
	private Long courseId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private DraftSource source;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String motive;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String process;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String result;

	@Column(name = "career_link", nullable = false, columnDefinition = "TEXT")
	private String careerLink;

	@Column(name = "guideline_title", length = 200)
	private String guidelineTitle;

	@Column(name = "guideline_detail", columnDefinition = "TEXT")
	private String guidelineDetail;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private DraftStatus status;

	@Column(name = "teacher_comment", columnDefinition = "TEXT")
	private String teacherComment;

	@Column(name = "submitted_at")
	private LocalDateTime submittedAt;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	protected RecordDraft() {
	}

	// 새 초안은 DRAFT, 과거 기록은 CONFIRMED로 만든다
	public static RecordDraft create(
			String studentId, String semester, String subjectName, Long courseId, DraftSource source, DraftContent content, LocalDateTime now) {
		RecordDraft draft = new RecordDraft();
		draft.studentId = studentId;
		draft.semester = semester;
		draft.subjectName = subjectName;
		draft.courseId = courseId;
		draft.source = source;
		draft.status = source == DraftSource.PAST ? DraftStatus.CONFIRMED : DraftStatus.DRAFT;
		draft.createdAt = now;
		draft.applyContent(content, now);
		return draft;
	}

	// 서비스에서 상태(수정 가능 여부)를 확인한 뒤 부른다
	public void updateContent(DraftContent content, LocalDateTime now) {
		applyContent(content, now);
	}

	public void submit(LocalDateTime now) {
		this.status = DraftStatus.SUBMITTED;
		this.submittedAt = now;
		this.updatedAt = now;
	}

	private void applyContent(DraftContent content, LocalDateTime now) {
		this.motive = content.motive();
		this.process = content.process();
		this.result = content.result();
		this.careerLink = content.careerLink();
		this.guidelineTitle = content.guidelineTitle();
		this.guidelineDetail = content.guidelineDetail();
		this.updatedAt = now;
	}

	public Long getId() {
		return id;
	}

	public String getStudentId() {
		return studentId;
	}

	public String getSemester() {
		return semester;
	}

	public String getSubjectName() {
		return subjectName;
	}

	public Long getCourseId() {
		return courseId;
	}

	public DraftSource getSource() {
		return source;
	}

	public String getMotive() {
		return motive;
	}

	public String getProcess() {
		return process;
	}

	public String getResult() {
		return result;
	}

	public String getCareerLink() {
		return careerLink;
	}

	public String getGuidelineTitle() {
		return guidelineTitle;
	}

	public String getGuidelineDetail() {
		return guidelineDetail;
	}

	public DraftStatus getStatus() {
		return status;
	}

	public String getTeacherComment() {
		return teacherComment;
	}

	public LocalDateTime getSubmittedAt() {
		return submittedAt;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

}
