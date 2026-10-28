// PRD F3 평가 입력 검증. 화면의 폼과 mock 저장소가 같은 함수를 쓴다.
import type { AssessmentKind } from './types';

export interface AssessmentDraft {
  kind: AssessmentKind;
  title: string;
  weightPercent: number;
  maxScore: number;
  // 비우면 미입력(null)
  score: number | null;
  // YYYY-MM-DD
  evaluatedOn: string;
}

export interface CourseWeights {
  writtenWeight: number;
  performanceWeight: number;
}

export interface ExistingAssessment {
  id: string;
  kind: AssessmentKind;
  weightPercent: number;
}

export type AssessmentErrors = Partial<Record<keyof AssessmentDraft, string>>;

const KIND_LABEL: Record<AssessmentKind, string> = { WRITTEN: '지필', PERFORMANCE: '수행' };
const DATE_PATTERN = /^\d{4}-\d{2}-\d{2}$/;

export function kindWeight(weights: CourseWeights, kind: AssessmentKind) {
  return kind === 'WRITTEN' ? weights.writtenWeight : weights.performanceWeight;
}

// 같은 종류 평가의 반영 비율 합(%). 수정 중인 평가는 빼고 센다
export function usedKindWeight(existing: ExistingAssessment[], kind: AssessmentKind, excludeId?: string) {
  return existing
    .filter((assessment) => assessment.kind === kind && assessment.id !== excludeId)
    .reduce((total, assessment) => total + assessment.weightPercent, 0);
}

// 오류가 없으면 빈 객체를 돌려준다
export function validateAssessment(
  draft: AssessmentDraft,
  weights: CourseWeights,
  existing: ExistingAssessment[],
  editingId?: string,
): AssessmentErrors {
  const errors: AssessmentErrors = {};

  if (draft.title.trim() === '') errors.title = '평가 이름을 입력하세요.';

  if (!DATE_PATTERN.test(draft.evaluatedOn)) errors.evaluatedOn = '날짜를 입력하세요.';

  if (!Number.isFinite(draft.maxScore) || draft.maxScore <= 0) {
    errors.maxScore = '만점은 0보다 커야 합니다.';
  }

  if (draft.score !== null) {
    if (!Number.isFinite(draft.score) || draft.score < 0) {
      errors.score = '득점은 0 이상이어야 합니다.';
    } else if (Number.isFinite(draft.maxScore) && draft.score > draft.maxScore) {
      errors.score = `득점은 만점(${draft.maxScore}점) 이하여야 합니다.`;
    }
  }

  const limit = kindWeight(weights, draft.kind);
  if (!Number.isFinite(draft.weightPercent) || draft.weightPercent <= 0) {
    errors.weightPercent = '반영 비율은 0보다 커야 합니다.';
  } else {
    const used = usedKindWeight(existing, draft.kind, editingId);
    if (used + draft.weightPercent > limit) {
      errors.weightPercent = `${KIND_LABEL[draft.kind]} 반영 비율 합이 ${limit}%를 넘습니다. (이미 ${used}% 사용, 남은 ${Math.max(limit - used, 0)}%)`;
    }
  }

  return errors;
}
