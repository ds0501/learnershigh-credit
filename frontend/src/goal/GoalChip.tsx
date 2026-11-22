import { useCallback, useState } from 'react';

import { GoalEditorModal } from './GoalEditorModal';
import { useGoal } from './goalContext';
import { goalChipLabel } from './labels';
import styles from './Goal.module.css';

// 학생부 관리 상단의 희망 학과 칩. 누르면 설정 창이 열린다
export function GoalChip() {
  const { status, goal, reload } = useGoal();
  const [open, setOpen] = useState(false);
  const close = useCallback(() => setOpen(false), []);

  if (status === 'loading') {
    return <span className={styles.chipNote}>희망 학과 불러오는 중...</span>;
  }
  if (status === 'error') {
    return (
      <span className={styles.chipNote}>
        희망 학과를 불러오지 못했습니다.{' '}
        <button type="button" className={styles.linkButton} onClick={reload}>
          다시 시도
        </button>
      </span>
    );
  }

  return (
    <>
      <button
        type="button"
        className={goal ? styles.chip : `${styles.chip} ${styles.chipEmpty}`}
        data-action="open-goal"
        onClick={() => setOpen(true)}
        aria-label={goal ? `${goalChipLabel(goal)} (바꾸기)` : '희망 학과 설정'}
      >
        {goalChipLabel(goal)}
        {goal && (
          <span className={styles.chipEdit} aria-hidden="true">
            ✎
          </span>
        )}
      </button>
      {open && <GoalEditorModal onClose={close} />}
    </>
  );
}
