// 학과 로드맵의 과목 분류와 점검 요약. 화면과 API에 의존하지 않는 순수 함수만 둔다.
import type { DepartmentSubject } from './types';

// "다수 대학 공통" 기준: 조사 대학의 이 비율(%) 이상이 지정한 과목
export const MAJORITY_RATIO_PERCENT = 50;

export interface SubjectGroups {
  // 조사 대학 전원이 지정한 과목
  fullCommon: DepartmentSubject[];
  // 전원은 아니지만 50% 이상이 지정한 과목
  majority: DepartmentSubject[];
  // 50% 미만의 대학만 지정한 과목
  split: DepartmentSubject[];
}

export function groupSubjects(subjects: DepartmentSubject[]): SubjectGroups {
  return {
    fullCommon: subjects.filter((subject) => subject.fullCommon),
    majority: subjects.filter((subject) => !subject.fullCommon && subject.ratioPercent >= MAJORITY_RATIO_PERCENT),
    split: subjects.filter((subject) => subject.ratioPercent < MAJORITY_RATIO_PERCENT),
  };
}

// 이미 등록한 과목이면 화면에서 테두리·막대 색을 강조한다
export function isHighlighted(subject: DepartmentSubject): boolean {
  return subject.alreadyRegistered;
}

export interface CheckSummary {
  // 다수 대학 공통 과목 수 (전체 대학 공통 과목 포함)
  total: number;
  // 그중 강조 표시되는(이미 등록한) 과목 수
  registered: number;
}

// 점검 요약 "다수 대학 공통 과목 N개 중 M개 등록". 전체 대학 공통 과목도 N, M에 포함한다
export function checkSummary({ fullCommon, majority }: SubjectGroups): CheckSummary {
  const subjects = [...fullCommon, ...majority];
  return { total: subjects.length, registered: subjects.filter(isHighlighted).length };
}
