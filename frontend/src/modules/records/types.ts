// 생기부 관리 세특 API 모양. 기준: backend RecordDraftResponse/RecordDraftCardResponse, docs/api-contract.md 8장

export type DraftStatus = 'DRAFT' | 'SUBMITTED' | 'REVISION_REQUESTED' | 'CONFIRMED';
export type DraftSource = 'NEW' | 'PAST';

// 학기별 목록의 카드. 초안이 없는 등록 과목은 id·source·updatedAt이 null이고 status가 NONE
export interface RecordDraftCard {
  id: string | null;
  // '학년-학기' (예: '1-2')
  semester: string;
  subjectName: string;
  courseId: string | null;
  source: DraftSource | null;
  status: DraftStatus | 'NONE';
  updatedAt: string | null;
}

// GET /api/student/record/drafts?semester=
export interface RecordDraftList {
  semester: string;
  items: RecordDraftCard[];
}

// 세특 4칸과 가이드라인
export interface DraftContent {
  motive: string;
  process: string;
  result: string;
  careerLink: string;
  guidelineTitle: string | null;
  guidelineDetail: string | null;
}

// GET·PUT /drafts/{id}, POST /drafts, POST /drafts/{id}/submit 응답
export interface RecordDraft extends DraftContent {
  id: string;
  semester: string;
  subjectName: string;
  courseId: string | null;
  source: DraftSource;
  status: DraftStatus;
  teacherComment: string | null;
  submittedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

// 세특 방향 1안. 완성된 세특 문장이 아니라 방향만 담는다
export interface GuidelineSuggestion {
  title: string;
  reason: string;
  // 탐구·활동 예시 2~3개
  examples: string[];
  // 4칸에 적을 때의 포인트 2~3개
  points: string[];
}

// POST /drafts/{id}/guidelines, POST /api/student/record/guidelines 응답(항상 3안)
export interface GuidelineSuggestions {
  // AI | FALLBACK. 화면에는 표시하지 않는다
  generatedBy: string;
  suggestions: GuidelineSuggestion[];
}

// POST /drafts 요청 본문
export interface CreateRecordDraftInput extends DraftContent {
  semester: string;
  subjectName: string;
  courseId: string | null;
  source: DraftSource;
}
