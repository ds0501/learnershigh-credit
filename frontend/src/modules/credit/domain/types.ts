// 계산 모듈이 쓰는 값의 모양. 화면과 API 타입은 이 파일을 가져다 쓴다.

export type CourseType = 'COMMON' | 'GENERAL' | 'CAREER' | 'FUSION' | 'CREATIVE';
export type RiskLevel = 'NORMAL' | 'CAUTION' | 'RISK' | 'NO_DATA';
export type AssessmentKind = 'WRITTEN' | 'PERFORMANCE';

// 성취율 계산에 필요한 평가 한 건
export interface ScoredAssessment {
  kind: AssessmentKind;
  weightPercent: number;
  maxScore: number;
  // 미입력이면 null
  score: number | null;
}

export interface Attendance {
  // 모르면 null (100%로 가정)
  totalSessions: number | null;
  absentCount: number;
}

export interface CourseEvaluation {
  // 미입력이면 null. 반올림하지 않은 값
  achievementRate: number | null;
  attendanceRate: number;
  riskLevel: RiskLevel;
  // 남은 평가에서 이수 기준을 넘기 위해 필요한 평균(%). 계산하지 않는 경우 null
  requiredAverage: number | null;
}
