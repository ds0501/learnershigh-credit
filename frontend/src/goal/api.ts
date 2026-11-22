import { apiGet, apiRequest } from '../api/client';
import type { SaveStudentGoalInput, StudentGoal } from './types';

// 학점 모듈 밖의 공통 API라 /student/credit 아래가 아니다. mock 없이 항상 실제 백엔드로 보낸다
const BASE = '/student/goal';

// 저장된 값이 없으면 null
export function getGoal() {
  return apiGet<StudentGoal | null>(BASE, {}, true);
}

export function saveGoal(input: SaveStudentGoalInput) {
  return apiRequest<StudentGoal>('PUT', BASE, { body: input, forceReal: true });
}
