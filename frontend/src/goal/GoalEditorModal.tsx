import { useEffect, useRef, useState, type FormEvent } from 'react';

import { ApiError } from '../api/errors';
import { useAsync } from '../api/useAsync';
import { searchDepartments } from '../modules/credit/roadmap/api';
import { useGoal } from './goalContext';
import type { GoalDepartment } from './types';
import styles from './Goal.module.css';

// student_goal.target_university 길이와 같다
const TARGET_UNIVERSITY_MAX_LENGTH = 100;

// 희망 학과 설정: 학과 검색(GET /departments?query=)과 목표 대학(선택) 입력 후 PUT /api/student/goal
export function GoalEditorModal({ onClose }: { onClose: () => void }) {
  const { goal, saveGoal } = useGoal();
  const [selected, setSelected] = useState<GoalDepartment | null>(goal?.department ?? null);
  const [query, setQuery] = useState(goal?.department.name ?? '');
  const [targetUniversity, setTargetUniversity] = useState(goal?.targetUniversity ?? '');
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [formError, setFormError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const searchRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    searchRef.current?.focus();
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [onClose]);

  // 학과를 고르기 전, 검색어가 있을 때만 검색한다
  const keyword = selected ? '' : query.trim();
  const results = useAsync(`goal-dept-search:${keyword}`, () => (keyword ? searchDepartments(keyword) : Promise.resolve([])));

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    if (!selected) {
      setErrors({ departmentId: '희망 학과를 검색해서 선택하세요.' });
      setFormError('입력값을 확인하세요. 저장되지 않았습니다.');
      return;
    }
    setSaving(true);
    setErrors({});
    setFormError(null);
    try {
      const trimmed = targetUniversity.trim();
      await saveGoal(selected.id, trimmed === '' ? null : trimmed);
      onClose();
    } catch (error) {
      if (error instanceof ApiError) {
        setErrors(error.fieldErrors);
        setFormError(`${error.message} 저장되지 않았습니다.`);
      } else {
        setFormError('저장하지 못했습니다. 잠시 후 다시 시도하세요.');
      }
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className={styles.overlay} onClick={onClose}>
      <form
        className={styles.modal}
        role="dialog"
        aria-modal="true"
        aria-labelledby="goal-editor-title"
        onClick={(event) => event.stopPropagation()}
        onSubmit={submit}
        noValidate
      >
        <header className={styles.modalHeader}>
          <h2 id="goal-editor-title" className={styles.modalTitle}>
            희망 학과 설정
          </h2>
          <button type="button" className={styles.close} onClick={onClose} aria-label="닫기">
            ×
          </button>
        </header>

        <div className={styles.modalBody}>
          <div className={styles.field}>
            <label className={styles.label} htmlFor="goal-department">
              희망 학과
            </label>
            <input
              ref={searchRef}
              id="goal-department"
              name="goalDepartment"
              className={styles.input}
              value={query}
              placeholder="학과명을 검색하세요 (예: 간호학과)"
              autoComplete="off"
              aria-invalid={Boolean(errors.departmentId)}
              onChange={(event) => {
                setQuery(event.target.value);
                setSelected(null);
                setErrors((previous) => ({ ...previous, departmentId: '' }));
              }}
            />
            {keyword && (
              <div className={styles.results}>
                {results.state.status === 'loading' && <p className={styles.resultNote}>검색 중...</p>}
                {results.state.status === 'error' && <p className={styles.resultNote}>학과를 불러오지 못했습니다.</p>}
                {results.state.status === 'success' &&
                  (results.state.data.length === 0 ? (
                    <p className={styles.resultNote}>"{keyword}"와 일치하는 학과가 없어요.</p>
                  ) : (
                    <ul className={styles.resultList} aria-label="학과 검색 결과">
                      {results.state.data.map((item) => (
                        <li key={item.id}>
                          <button
                            type="button"
                            className={styles.resultItem}
                            data-department-id={item.id}
                            onClick={() => {
                              setSelected({ id: item.id, name: item.name, field: item.field });
                              setQuery(item.name);
                            }}
                          >
                            <span className={styles.resultName}>{item.name}</span>
                            <span className={styles.resultMeta}>{item.field ?? '분야 미분류'}</span>
                          </button>
                        </li>
                      ))}
                    </ul>
                  ))}
              </div>
            )}
            {selected && (
              <p className={styles.hint}>
                선택: <strong>{selected.name}</strong>
                {selected.field && ` · ${selected.field}`}
              </p>
            )}
            {errors.departmentId && (
              <p className={styles.fieldError} data-error-for="departmentId">
                {errors.departmentId}
              </p>
            )}
          </div>

          <div className={styles.field}>
            <label className={styles.label} htmlFor="goal-university">
              목표 대학 (선택)
            </label>
            <input
              id="goal-university"
              name="targetUniversity"
              className={styles.input}
              value={targetUniversity}
              maxLength={TARGET_UNIVERSITY_MAX_LENGTH}
              placeholder="예: 서울대학교 (비워 두어도 됩니다)"
              autoComplete="off"
              aria-invalid={Boolean(errors.targetUniversity)}
              onChange={(event) => setTargetUniversity(event.target.value)}
            />
            {errors.targetUniversity && (
              <p className={styles.fieldError} data-error-for="targetUniversity">
                {errors.targetUniversity}
              </p>
            )}
          </div>
        </div>

        <footer className={styles.modalFooter}>
          {formError && (
            <p className={styles.formError} role="alert">
              {formError}
            </p>
          )}
          <button type="button" className={styles.secondaryButton} onClick={onClose}>
            취소
          </button>
          <button type="submit" className={styles.primaryButton} disabled={saving} data-action="save-goal">
            {saving ? '저장 중...' : '저장'}
          </button>
        </footer>
      </form>
    </div>
  );
}
