// 성적 수동 입력 검증. 화면 폼과 백엔드(GradeManagementService.validateScores)가 같은 규칙을 쓴다.
export interface GradeRecordDraft {
  gradeYear: number;
  term: number;
  subjectName: string;
  rawScore: number | null;
  subjectAverage: number | null;
  stdDev: number | null;
  gradeLevel: number | null;
}

export type GradeRecordErrors = Partial<Record<keyof GradeRecordDraft, string>>;

// 오류가 없으면 빈 객체를 돌려준다. isEdit이면 과목 정체(학년/학기/과목명)는 검사하지 않는다(수정 불가)
export function validateGradeRecord(draft: GradeRecordDraft, isEdit: boolean): GradeRecordErrors {
  const errors: GradeRecordErrors = {};

  if (!isEdit) {
    if (!Number.isFinite(draft.gradeYear) || draft.gradeYear < 1 || draft.gradeYear > 3) {
      errors.gradeYear = '학년은 1~3 중 하나여야 합니다.';
    }
    if (draft.term !== 1 && draft.term !== 2) {
      errors.term = '학기는 1 또는 2여야 합니다.';
    }
    if (draft.subjectName.trim() === '') {
      errors.subjectName = '과목명을 입력하세요.';
    }
  }

  if (draft.rawScore !== null && (!Number.isFinite(draft.rawScore) || draft.rawScore < 0 || draft.rawScore > 100)) {
    errors.rawScore = '원점수는 0~100 사이여야 합니다.';
  }

  if (draft.stdDev !== null && (!Number.isFinite(draft.stdDev) || draft.stdDev < 0)) {
    errors.stdDev = '표준편차는 0 이상이어야 합니다.';
  }

  if (draft.gradeLevel !== null && (!Number.isFinite(draft.gradeLevel) || draft.gradeLevel < 1 || draft.gradeLevel > 9)) {
    errors.gradeLevel = '등급은 1~9 중 하나여야 합니다.';
  }

  return errors;
}
