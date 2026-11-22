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

// POST /drafts 요청 본문
export interface CreateRecordDraftInput extends DraftContent {
  semester: string;
  subjectName: string;
  courseId: string | null;
  source: DraftSource;
}
