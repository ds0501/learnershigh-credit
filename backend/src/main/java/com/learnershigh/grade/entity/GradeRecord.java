package com.learnershigh.grade.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// 과목 한 학기 성적 한 건(PDF 업로드 또는 수동 입력). 나이스 성적표의 원점수/과목평균/표준편차/석차등급을 담는다.
// matchedStudentCourseId가 있으면 credit 모듈의 해당 과목 평가 점수에 반영됐다는 뜻(unmatchedReason이 null일 때만 appliedToCredit=true).
@Entity
@Table(name = "grade_record")
public class GradeRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "student_id", nullable = false, length = 100)
	private String studentId;

	@Column(name = "grade_year", nullable = false)
	private Integer gradeYear;

	@Column(nullable = false)
	private Integer term;

	@Column(name = "subject_name", nullable = false, length = 100)
	private String subjectName;

	@Column(name = "raw_score")
	private Integer rawScore;

	@Column(name = "subject_average")
	private BigDecimal subjectAverage;

	@Column(name = "std_dev")
	private BigDecimal stdDev;

	@Column(name = "grade_level")
	private Integer gradeLevel;

	@Column(nullable = false, length = 10)
	private String source;

	@Column(name = "upload_id")
	private Long uploadId;

	@Column(name = "matched_student_course_id")
	private Long matchedStudentCourseId;

	@Column(name = "applied_to_credit", nullable = false)
	private Boolean appliedToCredit;

	@Column(name = "unmatched_reason", length = 50)
	private String unmatchedReason;

	protected GradeRecord() {
	}

	public GradeRecord(
			String studentId,
			Integer gradeYear,
			Integer term,
			String subjectName,
			Integer rawScore,
			BigDecimal subjectAverage,
			BigDecimal stdDev,
			Integer gradeLevel,
			String source,
			Long uploadId) {
		this.studentId = studentId;
		this.gradeYear = gradeYear;
		this.term = term;
		this.subjectName = subjectName;
		this.rawScore = rawScore;
		this.subjectAverage = subjectAverage;
		this.stdDev = stdDev;
		this.gradeLevel = gradeLevel;
		this.source = source;
		this.uploadId = uploadId;
		this.appliedToCredit = false;
	}

	public Long getId() {
		return id;
	}

	public String getStudentId() {
		return studentId;
	}

	public Integer getGradeYear() {
		return gradeYear;
	}

	public Integer getTerm() {
		return term;
	}

	public String getSubjectName() {
		return subjectName;
	}

	public Integer getRawScore() {
		return rawScore;
	}

	public BigDecimal getSubjectAverage() {
		return subjectAverage;
	}

	public BigDecimal getStdDev() {
		return stdDev;
	}

	public Integer getGradeLevel() {
		return gradeLevel;
	}

	public String getSource() {
		return source;
	}

	public Long getUploadId() {
		return uploadId;
	}

	public Long getMatchedStudentCourseId() {
		return matchedStudentCourseId;
	}

	public Boolean getAppliedToCredit() {
		return appliedToCredit;
	}

	public String getUnmatchedReason() {
		return unmatchedReason;
	}

	// 수동 입력 수정 (PDF 업로드 값은 upsertFromParsed로 갱신한다)
	public void update(Integer rawScore, BigDecimal subjectAverage, BigDecimal stdDev, Integer gradeLevel) {
		this.rawScore = rawScore;
		this.subjectAverage = subjectAverage;
		this.stdDev = stdDev;
		this.gradeLevel = gradeLevel;
	}

	// PDF 재업로드로 같은 학기·과목이 다시 인식됐을 때 값을 덮어쓴다
	public void upsertFromParsed(Integer rawScore, BigDecimal subjectAverage, BigDecimal stdDev, Integer gradeLevel, Long uploadId) {
		this.rawScore = rawScore;
		this.subjectAverage = subjectAverage;
		this.stdDev = stdDev;
		this.gradeLevel = gradeLevel;
		this.source = "PDF";
		this.uploadId = uploadId;
	}

	// credit 모듈 반영 결과 기록 (GradeCreditSyncService가 호출)
	public void markSync(Long matchedStudentCourseId, boolean appliedToCredit, String unmatchedReason) {
		this.matchedStudentCourseId = matchedStudentCourseId;
		this.appliedToCredit = appliedToCredit;
		this.unmatchedReason = unmatchedReason;
	}

}
