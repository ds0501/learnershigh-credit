import { apiGet, apiRequest } from '../../api/client';
import type { CreateRecordDraftInput, DraftContent, RecordDraft, RecordDraftList } from './types';

// mock 없이 항상 실제 백엔드로 보낸다
const BASE = '/student/record/drafts';

export function getDrafts(semester: string) {
  return apiGet<RecordDraftList>(BASE, { semester }, true);
}

export function getDraft(draftId: string) {
  return apiGet<RecordDraft>(`${BASE}/${encodeURIComponent(draftId)}`, {}, true);
}

export function createDraft(input: CreateRecordDraftInput) {
  return apiRequest<RecordDraft>('POST', BASE, { body: input, forceReal: true });
}

export function saveDraft(draftId: string, content: DraftContent) {
  return apiRequest<RecordDraft>('PUT', `${BASE}/${encodeURIComponent(draftId)}`, { body: content, forceReal: true });
}

export function submitDraft(draftId: string) {
  return apiRequest<RecordDraft>('POST', `${BASE}/${encodeURIComponent(draftId)}/submit`, { forceReal: true });
}

// 제출 취소: SUBMITTED → DRAFT
export function withdrawDraft(draftId: string) {
  return apiRequest<RecordDraft>('POST', `${BASE}/${encodeURIComponent(draftId)}/withdraw`, { forceReal: true });
}
