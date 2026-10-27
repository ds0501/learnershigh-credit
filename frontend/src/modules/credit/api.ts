import { apiGet } from '../../api/client';
import type { CreditSummary, RequiredCourses, SemesterCourses } from './types';

const BASE = '/student/credit';

export function getSummary() {
  return apiGet<CreditSummary>(`${BASE}/summary`);
}

export function getCourses(gradeYear: number, term: number) {
  return apiGet<SemesterCourses>(`${BASE}/courses`, { gradeYear, term });
}

export function getRequiredCourses() {
  return apiGet<RequiredCourses>(`${BASE}/required-courses`);
}
