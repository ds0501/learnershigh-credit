import { createContext, useContext } from 'react';

import type { StudentGoal } from './types';

export interface GoalContextValue {
  status: 'loading' | 'success' | 'error';
  // status가 success일 때만 의미가 있다. 저장된 값이 없으면 null
  goal: StudentGoal | null;
  // 저장(PUT) 후 모든 탭이 새 값을 쓴다. targetUniversity가 null이면 목표 대학 없음
  saveGoal: (departmentId: string, targetUniversity: string | null) => Promise<StudentGoal>;
  reload: () => void;
}

export const GoalContext = createContext<GoalContextValue | null>(null);

// 학생부 관리의 모든 탭이 같은 희망 학과를 쓴다 (AppLayout의 GoalProvider 안에서만 쓸 수 있음)
export function useGoal(): GoalContextValue {
  const value = useContext(GoalContext);
  if (!value) throw new Error('useGoal은 GoalProvider 안에서만 쓸 수 있습니다.');
  return value;
}
