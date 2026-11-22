import { ROLE_LABEL, useRole, type Role } from './roleContext';
import styles from './RoleToggle.module.css';

const ROLES: Role[] = ['STUDENT', 'TEACHER'];

// 학생부 관리 상단의 역할 전환(학생/선생님)
export function RoleToggle() {
  const { role, setRole } = useRole();
  return (
    <div className={styles.toggle} role="group" aria-label="보기 역할">
      {ROLES.map((option) => (
        <button
          key={option}
          type="button"
          className={option === role ? `${styles.option} ${styles.selected}` : styles.option}
          aria-pressed={option === role}
          data-role={option}
          onClick={() => setRole(option)}
        >
          {ROLE_LABEL[option]}
        </button>
      ))}
    </div>
  );
}
