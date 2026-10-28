import { apiGet, apiRequest } from '../../api/client';
import type {
  Assessment,
  AssessmentInput,
  CatalogItem,
  CourseDetail,
  CourseItem,
  CourseRegistrationInput,
  CreditSummary,
  RequiredCourses,
  SemesterCourses,
} from './types';

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

export function searchCatalog(query: string) {
  return apiGet<CatalogItem[]>(`${BASE}/catalog`, { query });
}

export function registerCourse(input: CourseRegistrationInput) {
  return apiRequest<CourseItem>('POST', `${BASE}/courses`, { body: input });
}

export function getCourseDetail(courseId: string) {
  return apiGet<CourseDetail>(`${BASE}/courses/${encodeURIComponent(courseId)}`);
}

export function addAssessment(courseId: string, input: AssessmentInput) {
  return apiRequest<Assessment>('POST', `${BASE}/courses/${encodeURIComponent(courseId)}/assessments`, { body: input });
}

export function deleteAssessment(courseId: string, assessmentId: string) {
  return apiRequest<void>(
    'DELETE',
    `${BASE}/courses/${encodeURIComponent(courseId)}/assessments/${encodeURIComponent(assessmentId)}`,
  );
}

export function updateAssessment(assessmentId: string, input: AssessmentInput) {
  return apiRequest<Assessment>('PUT', `${BASE}/assessments/${encodeURIComponent(assessmentId)}`, { body: input });
}
