import { useEffect, useRef, useState, type FormEvent } from 'react';

import { ApiError } from '../../api/errors';
import { createDraft } from './api';
import { CONTENT_FIELDS, isContentBlank, SEMESTERS } from './labels';
import styles from './Records.module.css';

interface PastRecordModalProps {
  initialSemester: string;
  onClose: () => void;
  // 저장한 학기로 목록을 옮긴다
  onSaved: (semester: string) => void;
}

// 과거 기록 추가: 이미 확정된 세특을 학기, 과목명, 4칸으로 옮겨 적는다(source PAST, 확인 완료로 저장)
export function PastRecordModal({ initialSemester, onClose, onSaved }: PastRecordModalProps) {
  const [semester, setSemester] = useState(initialSemester);
  const [subjectName, setSubjectName] = useState('');
  const [values, setValues] = useState({ motive: '', process: '', result: '', careerLink: '' });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [formError, setFormError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const subjectRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    subjectRef.current?.focus();
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [onClose]);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    const found: Record<string, string> = {};
    if (subjectName.trim() === '') found.subjectName = '과목명을 입력하세요.';
    setErrors(found);
    if (Object.keys(found).length > 0 || isContentBlank(values)) {
      setFormError(isContentBlank(values) ? '과거 기록은 내용을 한 칸 이상 입력하세요.' : '입력값을 확인하세요. 저장되지 않았습니다.');
      return;
    }
    setSaving(true);
    setFormError(null);
    try {
      await createDraft({
        ...values,
        guidelineTitle: null,
        guidelineDetail: null,
        semester,
        subjectName: subjectName.trim(),
        courseId: null,
        source: 'PAST',
      });
      onSaved(semester);
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
        aria-labelledby="past-record-title"
        onClick={(event) => event.stopPropagation()}
        onSubmit={submit}
        noValidate
      >
        <header className={styles.modalHeader}>
          <h2 id="past-record-title" className={styles.modalTitle}>
            과거 기록 추가
          </h2>
          <button type="button" className={styles.close} onClick={onClose} aria-label="닫기">
            ×
          </button>
        </header>

        <div className={styles.modalBody}>
          <p className={styles.note}>이미 확정된 세특을 옮겨 적어요. 저장하면 "기존 기록"으로 보이고 고칠 수 없어요.</p>
          <div className={styles.fieldRow}>
            <div className={styles.field}>
              <label className={styles.label} htmlFor="past-semester">
                학기
              </label>
              <select id="past-semester" name="semester" className={styles.input} value={semester} onChange={(event) => setSemester(event.target.value)}>
                {SEMESTERS.map((option) => (
                  <option key={option.key} value={option.key}>
                    {option.label}
                  </option>
                ))}
              </select>
              {errors.semester && <p className={styles.fieldError}>{errors.semester}</p>}
            </div>
            <div className={styles.field}>
              <label className={styles.label} htmlFor="past-subject">
                과목명
              </label>
              <input
                ref={subjectRef}
                id="past-subject"
                name="subjectName"
                className={styles.input}
                value={subjectName}
                maxLength={100}
                placeholder="예: 통합과학1"
                autoComplete="off"
                aria-invalid={Boolean(errors.subjectName)}
                onChange={(event) => setSubjectName(event.target.value)}
              />
              {errors.subjectName && (
                <p className={styles.fieldError} data-error-for="subjectName">
                  {errors.subjectName}
                </p>
              )}
            </div>
          </div>
          {CONTENT_FIELDS.map((field) => (
            <div key={field.key} className={styles.field}>
              <label className={styles.label} htmlFor={`past-${field.key}`}>
                {field.label}
              </label>
              <textarea
                id={`past-${field.key}`}
                name={field.key}
                className={styles.textarea}
                rows={3}
                value={values[field.key]}
                onChange={(event) => {
                  const value = event.target.value;
                  setValues((previous) => ({ ...previous, [field.key]: value }));
                }}
              />
            </div>
          ))}
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
          <button type="submit" className={styles.primaryButton} disabled={saving} data-action="save-past">
            {saving ? '저장 중...' : '저장'}
          </button>
        </footer>
      </form>
    </div>
  );
}
