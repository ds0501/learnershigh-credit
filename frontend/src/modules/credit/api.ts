import { apiGet, apiRequest } from '../../api/client';
import type {
  Assessment,
  AssessmentInput,
  CatalogItem,
  CourseDetail,
  CourseItem,
  CourseRegistrationInput,
  CourseStatus,
  CreditSummary,
  RequiredCourses,
  SemesterCourses,
} from './types';

const BASE = '/student/credit';

// 학점 현황 요약은 실제 백엔드로 보낸다 (GET /summary, 계산 서비스 구현됨)
export function getSummary() {
  return apiGet<CreditSummary>(`${BASE}/summary`, {}, true);
}

export function getCourses(gradeYear: number, term: number) {
  return apiGet<SemesterCourses>(`${BASE}/courses`, { gradeYear, term }, true);
}

export function getRequiredCourses() {
  return apiGet<RequiredCourses>(`${BASE}/required-courses`, {}, true);
}

// 요약, 조회, 과목 검색+등록은 실제 백엔드로 보낸다. 나머지(상태 변경·삭제·평가)는 아직 없어 mock을 그대로 쓴다
export function searchCatalog(query: string) {
  return apiGet<CatalogItem[]>(`${BASE}/catalog`, { query }, true);
}

export function registerCourse(input: CourseRegistrationInput) {
  return apiRequest<CourseItem>('POST', `${BASE}/courses`, { body: input, forceReal: true });
}

export function changeCourseStatus(courseId: string, status: CourseStatus) {
  return apiRequest<CourseItem>('PATCH', `${BASE}/courses/${encodeURIComponent(courseId)}`, { body: { status } });
}

// 성공하면 본문 없음(204)
export function deleteCourse(courseId: string) {
  return apiRequest<void>('DELETE', `${BASE}/courses/${encodeURIComponent(courseId)}`);
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

export function updateAssessment(courseId: string, assessmentId: string, input: AssessmentInput) {
  return apiRequest<Assessment>(
    'PUT',
    `${BASE}/courses/${encodeURIComponent(courseId)}/assessments/${encodeURIComponent(assessmentId)}`,
    { body: input },
  );
}
