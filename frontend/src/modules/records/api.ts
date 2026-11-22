import { apiGet, apiRequest } from '../../api/client';
import type { CreateRecordDraftInput, DraftContent, GuidelineSuggestions, RecordDraft, RecordDraftList } from './types';

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

// 방향 추천(3안): 이미 있는 초안
export function suggestGuidelinesForDraft(draftId: string) {
  return apiRequest<GuidelineSuggestions>('POST', `${BASE}/${encodeURIComponent(draftId)}/guidelines`, { forceReal: true });
}

// 방향 추천(3안): 초안이 아직 없는 카드. DB에는 아무것도 만들지 않는다
export function suggestGuidelinesForSubject(input: { semester: string; subjectName: string; courseId: string | null }) {
  return apiRequest<GuidelineSuggestions>('POST', '/student/record/guidelines', { body: input, forceReal: true });
}

// 채택한 방향 저장(4칸은 그대로)
export function adoptGuideline(draftId: string, guideline: { title: string; detail: string | null }) {
  return apiRequest<RecordDraft>('PUT', `${BASE}/${encodeURIComponent(draftId)}/guideline`, { body: guideline, forceReal: true });
}

// 제출 취소: SUBMITTED → DRAFT
export function withdrawDraft(draftId: string) {
  return apiRequest<RecordDraft>('POST', `${BASE}/${encodeURIComponent(draftId)}/withdraw`, { forceReal: true });
}
