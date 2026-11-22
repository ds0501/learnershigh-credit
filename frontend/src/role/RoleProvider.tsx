import { useMemo, useState, type ReactNode } from 'react';

import { RoleContext, type Role } from './roleContext';

// 처음에는 학생 모드. 새로고침하면 학생으로 돌아간다
export function RoleProvider({ children }: { children: ReactNode }) {
  const [role, setRole] = useState<Role>('STUDENT');
  const value = useMemo(() => ({ role, setRole }), [role]);
  return <RoleContext.Provider value={value}>{children}</RoleContext.Provider>;
}
