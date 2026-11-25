import { apiGet, apiRequest, apiUpload } from '../../api/client';
import type { CatalogItem, GradeRecord, GradeRecordInput, GradeUploadResult } from './types';

// mock 없이 항상 실제 백엔드로 보낸다 (records 모듈과 같은 방식)
const BASE = '/student/grade';

// 직접 입력할 때 과목명 검색용. credit 모듈의 표준 과목 목록 API를 그대로 호출한다(코드 import 없이 경로만 공유, 모듈 분리 유지)
export function searchCourseCatalog(query: string) {
  return apiGet<CatalogItem[]>('/student/credit/catalog', { query }, true);
}

export function uploadGradePdf(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return apiUpload<GradeUploadResult>(`${BASE}/uploads`, formData);
}

export function listGradeRecords(gradeYear: number, term: number) {
  return apiGet<GradeRecord[]>(`${BASE}/records`, { gradeYear, term }, true);
}

export function createGradeRecord(input: GradeRecordInput) {
  return apiRequest<GradeRecord>('POST', `${BASE}/records`, { body: input, forceReal: true });
}

export function updateGradeRecord(recordId: string, input: GradeRecordInput) {
  return apiRequest<GradeRecord>('PUT', `${BASE}/records/${encodeURIComponent(recordId)}`, { body: input, forceReal: true });
}

export function deleteGradeRecord(recordId: string) {
  return apiRequest<void>('DELETE', `${BASE}/records/${encodeURIComponent(recordId)}`, { forceReal: true });
}
