import type { CourseStatus, CourseType, RiskLevel } from './types';

// 로그인 전 단계라 데모 학생의 현재 학기를 고정한다
export const CURRENT_TERM = { gradeYear: 1, term: 2 };

// 공통과목 최소 성취율(%). 안내 문구 표시용이며 판정은 서버(mock)가 한다
export const COMMON_MIN_ACHIEVEMENT = 40;

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

export function formatRate(rate: number | null) {
  return rate === null ? '—' : `${Number(rate.toFixed(1))}%`;
}
