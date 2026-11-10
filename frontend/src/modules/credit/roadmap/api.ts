import { apiGet } from '../../../api/client';
import type { DepartmentDetail, DepartmentSummary } from './types';

const BASE = '/student/credit/departments';

export function searchDepartments(query: string) {
  return apiGet<DepartmentSummary[]>(BASE, { query }, true);
}

export function getDepartmentDetail(departmentId: string) {
  return apiGet<DepartmentDetail>(`${BASE}/${encodeURIComponent(departmentId)}`, {}, true);
}
