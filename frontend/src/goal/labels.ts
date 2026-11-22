import type { StudentGoal } from './types';

// 상단 칩 문구. 목표 대학이 없으면 학과만 보여 준다
export function goalChipLabel(goal: StudentGoal | null): string {
  if (!goal) return '희망 학과 설정';
  const parts = [`희망 학과: ${goal.department.name}`];
  if (goal.targetUniversity) parts.push(`목표 대학: ${goal.targetUniversity}`);
  return parts.join(' · ');
}
