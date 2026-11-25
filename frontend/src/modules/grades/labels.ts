export interface TermOption {
  key: string;
  gradeYear: number;
  term: number;
  label: string;
}

export const TERMS: TermOption[] = [1, 2, 3].flatMap((gradeYear) =>
  [1, 2].map((term) => ({ key: `${gradeYear}-${term}`, gradeYear, term, label: `${gradeYear}학년 ${term}학기` })),
);

export const UNMATCHED_REASON_LABEL: Record<string, string> = {
  NOT_FOUND: '학점제에도, 표준 과목 목록에도 없는 과목입니다.',
  AMBIGUOUS: '같은 과목이 여러 건이라 자동 반영하지 않았습니다.',
  NO_ASSESSMENT: '학점제에 등록은 됐지만 반영할 평가가 없습니다.',
};

// 과목명 검색 결과 표시용(credit 모듈 COURSE_TYPE_LABEL과 같은 값, import 없이 그대로 둠)
export const CATALOG_COURSE_TYPE_LABEL: Record<string, string> = {
  COMMON: '공통',
  GENERAL: '일반선택',
  CAREER: '진로선택',
  FUSION: '융합선택',
  CREATIVE: '창체',
  SPECIALIZED: '전문교과',
};
