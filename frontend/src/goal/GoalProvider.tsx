import { useCallback, useMemo, useState, type ReactNode } from 'react';

import { useAsync } from '../api/useAsync';
import { getGoal, saveGoal as putGoal } from './api';
import { GoalContext, type GoalContextValue } from './goalContext';
import type { StudentGoal } from './types';

// 희망 학과를 한 번 불러와 학생부 관리의 모든 탭에 제공한다
export function GoalProvider({ children }: { children: ReactNode }) {
  const fetched = useAsync('student-goal', getGoal);
  // 저장한 값. 다시 불러오지 않고 바로 모든 탭에 반영한다
  const [saved, setSaved] = useState<StudentGoal | undefined>(undefined);

  const saveGoal = useCallback(async (departmentId: string, targetUniversity: string | null) => {
    const result = await putGoal({ departmentId, targetUniversity });
    setSaved(result);
    return result;
  }, []);

  const { state, reload } = fetched;
  const value = useMemo<GoalContextValue>(() => {
    if (saved !== undefined) return { status: 'success', goal: saved, saveGoal, reload };
    if (state.status === 'success') return { status: 'success', goal: state.data, saveGoal, reload };
    return { status: state.status, goal: null, saveGoal, reload };
  }, [saved, state, saveGoal, reload]);

  return <GoalContext.Provider value={value}>{children}</GoalContext.Provider>;
}
