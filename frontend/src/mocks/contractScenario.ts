// docs/api-contract.md 예시를 만드는 mock 호출 순서.
// contract.test.ts가 이 순서대로 mock을 호출해 문서의 요청·응답 예시와 같은지 확인한다.
// 순서가 상태를 바꾸므로(등록, 상태 변경, 평가 추가 등) 문서 예시도 이 순서를 기준으로 쓴다.
import { ApiError } from '../api/errors';
import { findMockHandler } from './index';

export interface ContractStep {
  // 문서의 <!-- contract:id 상태코드 --> 표시와 맞춘다
  id: string;
  method: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';
  // /api/student/credit 뒤의 경로
  path: string;
  query?: Record<string, string | number>;
  body?: unknown;
}

export interface ContractResult {
  status: number;
  // 204면 null
  body: unknown;
}

// 성공 상태 코드: 생성(POST) 201, 삭제 204, 그 밖 200
const successStatus = (method: ContractStep['method']) => (method === 'POST' ? 201 : method === 'DELETE' ? 204 : 200);

export const CONTRACT_STEPS: ContractStep[] = [
  { id: 'summary', method: 'GET', path: '/summary' },
  { id: 'courses', method: 'GET', path: '/courses', query: { gradeYear: 1, term: 2 } },
  { id: 'required-courses', method: 'GET', path: '/required-courses' },
  { id: 'catalog', method: 'GET', path: '/catalog', query: { query: '공통국어' } },
  { id: 'course-detail', method: 'GET', path: '/courses/16' },
  { id: 'course-detail-not-found', method: 'GET', path: '/courses/9999' },
  {
    id: 'register-catalog',
    method: 'POST',
    path: '/courses',
    body: {
      gradeYear: 1,
      term: 2,
      catalogId: '17',
      credits: 4,
      status: 'IN_PROGRESS',
      writtenWeight: 60,
      performanceWeight: 40,
      gradeDisplay: 'ACHIEVEMENT_AND_RANK',
    },
  },
  {
    id: 'register-custom',
    method: 'POST',
    path: '/courses',
    body: {
      gradeYear: 2,
      term: 1,
      catalogId: null,
      customName: 'AI 기초',
      courseType: 'CAREER',
      credits: 2,
      status: 'IN_PROGRESS',
      writtenWeight: 0,
      performanceWeight: 100,
      gradeDisplay: 'THREE_LEVEL',
    },
  },
  {
    id: 'register-duplicate',
    method: 'POST',
    path: '/courses',
    body: {
      gradeYear: 1,
      term: 2,
      catalogId: '2',
      credits: 4,
      status: 'IN_PROGRESS',
      writtenWeight: 60,
      performanceWeight: 40,
      gradeDisplay: 'ACHIEVEMENT_AND_RANK',
    },
  },
  {
    id: 'register-invalid',
    method: 'POST',
    path: '/courses',
    body: {
      gradeYear: 1,
      term: 2,
      catalogId: null,
      customName: '',
      credits: 0,
      status: 'IN_PROGRESS',
      writtenWeight: 60,
      performanceWeight: 50,
      gradeDisplay: 'ACHIEVEMENT_AND_RANK',
    },
  },
  { id: 'change-status', method: 'PATCH', path: '/courses/16', body: { status: 'COMPLETED' } },
  { id: 'change-status-invalid', method: 'PATCH', path: '/courses/16', body: { status: 'DONE' } },
  { id: 'change-status-back', method: 'PATCH', path: '/courses/16', body: { status: 'IN_PROGRESS' } },
  {
    id: 'add-assessment',
    method: 'POST',
    path: '/courses/16/assessments',
    body: { kind: 'PERFORMANCE', title: '수행평가 2', weightPercent: 20, maxScore: 100, score: 80, evaluatedOn: '2026-10-28' },
  },
  { id: 'course-detail-after-add', method: 'GET', path: '/courses/16' },
  {
    id: 'add-assessment-invalid-score',
    method: 'POST',
    path: '/courses/16/assessments',
    body: { kind: 'WRITTEN', title: '기말고사', weightPercent: 30, maxScore: 100, score: 120, evaluatedOn: '2026-12-16' },
  },
  {
    id: 'add-assessment-invalid-weight',
    method: 'POST',
    path: '/courses/16/assessments',
    body: { kind: 'PERFORMANCE', title: '수행평가 3', weightPercent: 10, maxScore: 100, score: null, evaluatedOn: '2026-11-20' },
  },
  {
    id: 'update-assessment',
    method: 'PUT',
    path: '/courses/16/assessments/45',
    body: { kind: 'PERFORMANCE', title: '수행평가 2', weightPercent: 20, maxScore: 100, score: 60, evaluatedOn: '2026-10-28' },
  },
  {
    id: 'update-assessment-not-found',
    method: 'PUT',
    path: '/courses/13/assessments/45',
    body: { kind: 'PERFORMANCE', title: '수행평가 2', weightPercent: 20, maxScore: 100, score: 60, evaluatedOn: '2026-10-28' },
  },
  { id: 'delete-assessment', method: 'DELETE', path: '/courses/16/assessments/45' },
  { id: 'delete-course', method: 'DELETE', path: '/courses/22' },
  { id: 'delete-course-not-found', method: 'DELETE', path: '/courses/22' },
];

export function runContractStep(step: ContractStep): ContractResult {
  const match = findMockHandler(step.method, `/student/credit${step.path}`);
  if (!match) throw new Error(`mock 핸들러가 없습니다: ${step.method} ${step.path}`);
  try {
    const data = match.handler({ params: step.query ?? {}, body: step.body, pathParams: match.pathParams });
    // 실제 응답처럼 JSON으로 한 번 거친다
    return { status: successStatus(step.method), body: data === null ? null : JSON.parse(JSON.stringify(data)) };
  } catch (error) {
    if (error instanceof ApiError) {
      return { status: error.status, body: { message: error.message, fieldErrors: error.fieldErrors } };
    }
    throw error;
  }
}
