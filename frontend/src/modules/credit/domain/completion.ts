// PRD 6장 이수 위험 계산 규칙. 화면과 API에 의존하지 않는 순수 함수만 둔다.
import { COMPLETION_RULES, type CompletionRules } from './rules';
import type { Attendance, CourseEvaluation, CourseType, RiskLevel, ScoredAssessment } from './types';

// 득점이 입력된 평가만 고른다
function scoredOnly(assessments: ScoredAssessment[]) {
  return assessments.filter((assessment) => assessment.score !== null);
}

// 득점이 입력된 평가의 반영 비율 합(%)
export function enteredWeight(assessments: ScoredAssessment[]): number {
  return scoredOnly(assessments).reduce((total, assessment) => total + assessment.weightPercent, 0);
}

// 성취율(%) = Σ(득점 / 만점 × 반영비율) ÷ Σ(입력된 평가의 반영비율) × 100. 입력된 평가가 없으면 null
export function achievementRate(assessments: ScoredAssessment[]): number | null {
  const entered = scoredOnly(assessments);
  const weight = enteredWeight(entered);
  if (entered.length === 0 || weight <= 0) return null;
  const weightedSum = entered.reduce(
    (total, assessment) => total + ((assessment.score as number) / assessment.maxScore) * assessment.weightPercent,
    0,
  );
  return (weightedSum / weight) * 100;
}

// 출석률(%) = (총 수업 수 − 결석 수) ÷ 총 수업 수 × 100. 총 수업 수를 모르면 100%로 가정한다
export function attendanceRate({ totalSessions, absentCount }: Attendance): number {
  if (totalSessions === null || totalSessions <= 0) return 100;
  return ((totalSessions - absentCount) / totalSessions) * 100;
}

// 필요 평균(%) = (기준 × 100 − 현재 성취율 × 입력된 반영비율 합) ÷ (100 − 입력된 반영비율 합)
// 남은 반영비율이 없으면 null. 100을 넘으면 그대로 돌려준다(화면에서 "도달이 어렵습니다"로 표시)
export function requiredAverage(currentRate: number | null, weightEntered: number, minRate: number): number | null {
  const remaining = 100 - weightEntered;
  if (remaining <= 0) return null;
  return (minRate * 100 - (currentRate ?? 0) * weightEntered) / remaining;
}

export interface RiskInput {
  courseType: CourseType;
  achievementRate: number | null;
  attendanceRate: number;
}

// 위험 단계. 반올림하기 전 값으로 판정한다
export function riskLevel(input: RiskInput, rules: CompletionRules = COMPLETION_RULES): RiskLevel {
  // 출석률 미달은 성취율과 관계없이 모든 유형에서 이수 위험
  if (input.attendanceRate < rules.minAttendanceRate) return 'RISK';

  // 창체는 평가가 없고 출석률로만 판정한다
  if (input.courseType === 'CREATIVE') return 'NORMAL';

  if (input.achievementRate === null) return 'NO_DATA';

  const minRate = rules.minAchievementRate[input.courseType];
  if (minRate === undefined) return 'NORMAL';
  if (input.achievementRate < minRate) return 'RISK';
  if (input.achievementRate < rules.cautionBelowRate) return 'CAUTION';
  return 'NORMAL';
}

export interface CourseInput {
  courseType: CourseType;
  assessments: ScoredAssessment[];
  attendance: Attendance;
}

// 과목 하나의 성취율, 출석률, 위험 단계, 필요 평균을 한 번에 계산한다
export function evaluateCourse(course: CourseInput, rules: CompletionRules = COMPLETION_RULES): CourseEvaluation {
  const rate = course.courseType === 'CREATIVE' ? null : achievementRate(course.assessments);
  const attendance = attendanceRate(course.attendance);
  const level = riskLevel({ courseType: course.courseType, achievementRate: rate, attendanceRate: attendance }, rules);
  const minRate = rules.minAchievementRate[course.courseType];

  // 필요 평균은 성취율 미달로 이수 위험인 과목에만 안내한다
  const needsGuide = level === 'RISK' && minRate !== undefined && rate !== null && rate < minRate;
  return {
    achievementRate: rate,
    attendanceRate: attendance,
    riskLevel: level,
    requiredAverage: needsGuide ? requiredAverage(rate, enteredWeight(course.assessments), minRate) : null,
  };
}
