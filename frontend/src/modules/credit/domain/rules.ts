// 이수 기준과 졸업 학점 기준 설정값. 판정에 쓰는 숫자는 이 객체에만 둔다.
// 나중에 credit_completion_rule, credit_graduation_rule 테이블 값으로 바뀐다.
import type { CourseType } from './types';

export interface CompletionRules {
  // 최소 출석률(%). 모든 과목 유형에 같다
  minAttendanceRate: number;
  // 과목 유형별 최소 성취율(%). 값이 없는 유형은 성취율로 판정하지 않는다
  minAchievementRate: Partial<Record<CourseType, number>>;
  // "주의" 구간의 상한(%). 최소 성취율 이상 이 값 미만이면 주의
  cautionBelowRate: number;
}

export const COMPLETION_RULES: CompletionRules = {
  minAttendanceRate: 66.67,
  minAchievementRate: { COMMON: 40 },
  cautionBelowRate: 50,
};

export const GRADUATION_RULES = {
  totalCredits: 192,
  subjectCredits: 174,
  creativeCredits: 18,
};
