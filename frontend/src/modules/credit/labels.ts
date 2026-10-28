import { COMPLETION_RULES } from './domain/rules';
import type { AssessmentKind, CourseStatus, CourseType, RiskLevel } from './types';

// 로그인 전 단계라 데모 학생의 현재 학기를 고정한다
export const CURRENT_TERM = { gradeYear: 1, term: 2 };

export const DISCLAIMER = '참고용 예측이며 학교의 공식 판정이 아닙니다';

export interface TermOption {
  key: string;
  gradeYear: number;
  term: number;
  label: string;
}

export const TERMS: TermOption[] = [1, 2, 3].flatMap((gradeYear) =>
  [1, 2].map((term) => ({ key: `${gradeYear}-${term}`, gradeYear, term, label: `${gradeYear}학년 ${term}학기` })),
);

export const termLabel = (gradeYear: number, term: number) => `${gradeYear}학년 ${term}학기`;

export const COURSE_TYPE_LABEL: Record<CourseType, string> = {
  COMMON: '공통',
  GENERAL: '일반선택',
  CAREER: '진로선택',
  FUSION: '융합선택',
  CREATIVE: '창체',
};

export type BadgeKind = 'inProgress' | 'caution' | 'risk' | 'noData' | 'completed';

export const BADGE_LABEL: Record<BadgeKind, string> = {
  inProgress: '수강 중',
  caution: '주의',
  risk: '이수 위험',
  noData: '미입력',
  completed: '이수 완료',
};

export function badgeOf(status: CourseStatus, riskLevel: RiskLevel): BadgeKind {
  if (status === 'COMPLETED') return 'completed';
  if (riskLevel === 'RISK') return 'risk';
  if (riskLevel === 'CAUTION') return 'caution';
  if (riskLevel === 'NO_DATA') return 'noData';
  return 'inProgress';
}

export const ASSESSMENT_KIND_LABEL: Record<AssessmentKind, string> = {
  WRITTEN: '지필',
  PERFORMANCE: '수행',
};

// 성취율은 정수로 반올림해 표시한다 (판정은 반올림 전 값으로 이미 끝난 상태)
export function formatRate(rate: number | null, emptyText = '미입력') {
  return rate === null ? emptyText : `${Math.round(rate)}%`;
}

// 창체는 성취율이 없어 '—'로 표시한다
export function courseRateText(course: { courseType: CourseType; achievementRate: number | null }) {
  return formatRate(course.achievementRate, course.courseType === 'CREATIVE' ? '—' : '미입력');
}

// 남은 평가에서 필요한 평균 안내 (PRD F4, P1). "이상 받아야"라서 소수점은 올림한다
export function requiredAverageMessage(requiredAverage: number) {
  if (requiredAverage > 100) return '남은 평가로는 기준 도달이 어렵습니다.';
  const score = Math.ceil(requiredAverage - 1e-9);
  return `남은 평가에서 평균 ${score}점 이상 받아야 이수 기준(${COMPLETION_RULES.minAchievementRate.COMMON}%)을 넘어요.`;
}
