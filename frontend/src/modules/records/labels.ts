import type { DraftContent, DraftStatus, RecordDraftCard } from './types';

export interface SemesterOption {
  // '학년-학기'
  key: string;
  label: string;
}

export const SEMESTERS: SemesterOption[] = [1, 2, 3].flatMap((gradeYear) =>
  [1, 2].map((term) => ({ key: `${gradeYear}-${term}`, label: `${gradeYear}학년 ${term}학기` })),
);

export const semesterLabel = (key: string) => SEMESTERS.find((option) => option.key === key)?.label ?? key;

// 카드 상태. 과거 기록(PAST)은 상태와 관계없이 "기존 기록"
export type CardKind = 'none' | 'draft' | 'submitted' | 'confirmed' | 'revision' | 'past';

export const CARD_LABEL: Record<CardKind, string> = {
  none: '초안 없음',
  draft: '작성 중',
  submitted: '제출',
  confirmed: '확인 완료',
  revision: '수정 요청',
  past: '기존 기록',
};

const STATUS_KIND: Record<DraftStatus, CardKind> = {
  DRAFT: 'draft',
  SUBMITTED: 'submitted',
  CONFIRMED: 'confirmed',
  REVISION_REQUESTED: 'revision',
};

export function cardKind(card: Pick<RecordDraftCard, 'status' | 'source'>): CardKind {
  if (card.status === 'NONE') return 'none';
  if (card.source === 'PAST') return 'past';
  return STATUS_KIND[card.status];
}

// 학생이 고칠 수 있는 상태(백엔드 DraftStatus.isEditableByStudent와 같다). 초안이 없으면 새로 쓸 수 있다
export function isEditable(status: DraftStatus | 'NONE'): boolean {
  return status === 'NONE' || status === 'DRAFT' || status === 'REVISION_REQUESTED';
}

// 제출 취소는 제출(SUBMITTED) 상태에서만 할 수 있다(백엔드 POST /drafts/{id}/withdraw와 같다). 확인 완료·기존 기록은 안 된다
export function canWithdraw(status: DraftStatus | 'NONE'): boolean {
  return status === 'SUBMITTED';
}

// 4칸 (화면 순서)
export const CONTENT_FIELDS: { key: 'motive' | 'process' | 'result' | 'careerLink'; label: string; placeholder: string }[] = [
  { key: 'motive', label: '탐구 동기', placeholder: '이 활동을 시작하게 된 계기나 궁금했던 점' },
  { key: 'process', label: '활동 과정', placeholder: '무엇을 조사하고, 어떻게 탐구했는지' },
  { key: 'result', label: '결과와 배운 점', placeholder: '알게 된 것, 달라진 생각' },
  { key: 'careerLink', label: '진로 연결', placeholder: '희망 진로와 어떻게 이어지는지' },
];

export function isContentBlank(content: Pick<DraftContent, 'motive' | 'process' | 'result' | 'careerLink'>): boolean {
  return CONTENT_FIELDS.every(({ key }) => content[key].trim() === '');
}

// 진로 연결이 비어 있으면 희망 학과 이름을 기본값으로 채운다
export function initialCareerLink(careerLink: string, goalDepartmentName: string | null | undefined): string {
  return careerLink.trim() === '' && goalDepartmentName ? goalDepartmentName : careerLink;
}
