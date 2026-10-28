// PRD F2 과목 등록 규칙. 화면의 모달과 mock 저장소가 같은 함수를 쓴다.
import type { CourseStatus, CourseType } from './types';

export interface CourseDraft {
  gradeYear: number;
  term: number;
  name: string;
  courseType: CourseType;
  credits: number;
  status: CourseStatus;
  writtenWeight: number;
  performanceWeight: number;
}

export type CourseErrors = Partial<Record<keyof CourseDraft, string>>;

// 지필 비율을 바꾸면 수행 비율은 100에서 뺀 값이 된다. 0~100 사이 정수로 맞춘다
export function setWrittenWeight(value: number) {
  const written = Number.isFinite(value) ? Math.min(100, Math.max(0, Math.round(value))) : 0;
  return { writtenWeight: written, performanceWeight: 100 - written };
}

// 중복 비교용 과목명 (공백과 대소문자 무시)
export function normalizeCourseName(name: string) {
  return name.replace(/\s+/g, '').toLowerCase();
}

const isWeight = (value: number) => Number.isInteger(value) && value >= 0 && value <= 100;

// 오류가 없으면 빈 객체. sameTermNames는 등록하려는 학기에 이미 있는 과목 이름들
export function validateCourseDraft(draft: CourseDraft, sameTermNames: string[]): CourseErrors {
  const errors: CourseErrors = {};

  if (!Number.isInteger(draft.gradeYear) || draft.gradeYear < 1 || draft.gradeYear > 3 || ![1, 2].includes(draft.term)) {
    errors.term = '학기를 선택하세요.';
  }

  const name = draft.name.trim();
  if (name === '') {
    errors.name = '과목명을 입력하세요.';
  } else if (sameTermNames.some((existing) => normalizeCourseName(existing) === normalizeCourseName(name))) {
    errors.name = '같은 학기에 이미 등록된 과목입니다.';
  }

  if (!Number.isInteger(draft.credits) || draft.credits < 1) {
    errors.credits = '학점은 1 이상의 정수여야 합니다.';
  }

  // 창체는 평가가 없어 반영 비율을 쓰지 않는다
  if (draft.courseType !== 'CREATIVE') {
    const valid = isWeight(draft.writtenWeight) && isWeight(draft.performanceWeight);
    if (!valid || draft.writtenWeight + draft.performanceWeight !== 100) {
      errors.writtenWeight = '지필과 수행 반영 비율의 합은 100%여야 합니다.';
    }
  }

  return errors;
}
