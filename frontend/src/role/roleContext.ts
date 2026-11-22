import { createContext, useContext } from 'react';

// 학생부 관리를 보는 역할. 로그인이 없어 상단 토글로 바꾼다(데모용, 저장하지 않음)
export type Role = 'STUDENT' | 'TEACHER';

export const ROLE_LABEL: Record<Role, string> = {
  STUDENT: '학생',
  TEACHER: '선생님',
};

export interface RoleContextValue {
  role: Role;
  setRole: (role: Role) => void;
}

export const RoleContext = createContext<RoleContextValue | null>(null);

// 학생부 관리의 모든 탭이 같은 역할을 쓴다 (AppLayout의 RoleProvider 안에서만 쓸 수 있음)
export function useRole(): RoleContextValue {
  const value = useContext(RoleContext);
  if (!value) throw new Error('useRole은 RoleProvider 안에서만 쓸 수 있습니다.');
  return value;
}
