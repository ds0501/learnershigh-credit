import { useState, type FormEvent } from 'react';

import { ApiError } from '../../../api/errors';
import { addAssessment, updateAssessment } from '../api';
import { kindWeight, usedKindWeight, validateAssessment, type AssessmentErrors } from '../domain/validation';
import { ASSESSMENT_KIND_LABEL } from '../labels';
import type { Assessment, AssessmentInput, AssessmentKind, CourseDetail } from '../types';
import styles from './AssessmentDrawer.module.css';

interface AssessmentFormProps {
  course: CourseDetail;
  // 수정할 평가. 없으면 새 평가 추가
  editing?: Assessment;
  onSaved: () => void;
  onCancelEdit: () => void;
}

interface FormValues {
  kind: AssessmentKind;
  title: string;
  weightPercent: string;
  maxScore: string;
  score: string;
  evaluatedOn: string;
}

function today() {
  const now = new Date();
  const pad = (value: number) => String(value).padStart(2, '0');
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`;
}

function initialValues(course: CourseDetail, editing?: Assessment): FormValues {
  if (editing) {
    return {
      kind: editing.kind,
      title: editing.title,
      weightPercent: String(editing.weightPercent),
      maxScore: String(editing.maxScore),
      score: editing.score === null ? '' : String(editing.score),
      evaluatedOn: editing.evaluatedOn,
    };
  }
  // 반영 비율이 남아 있는 종류를 기본으로 고른다 (수행 100% 과목이면 수행)
  const kind: AssessmentKind = course.writtenWeight > 0 ? 'WRITTEN' : 'PERFORMANCE';
  return { kind, title: '', weightPercent: '', maxScore: '100', score: '', evaluatedOn: today() };
}

// 빈 칸은 NaN으로 바꿔 검증에서 걸리게 한다. 득점만 빈 칸이 미입력(null)이다
const toNumber = (value: string) => (value.trim() === '' ? Number.NaN : Number(value));

function toInput(values: FormValues): AssessmentInput {
  return {
    kind: values.kind,
    title: values.title,
    weightPercent: toNumber(values.weightPercent),
    maxScore: toNumber(values.maxScore),
    score: values.score.trim() === '' ? null : Number(values.score),
    evaluatedOn: values.evaluatedOn,
  };
}

// 평가 추가/수정 폼 (PRD F3)
export function AssessmentForm({ course, editing, onSaved, onCancelEdit }: AssessmentFormProps) {
  const [values, setValues] = useState<FormValues>(() => initialValues(course, editing));
  const [errors, setErrors] = useState<AssessmentErrors>({});
  const [formError, setFormError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  const set = (field: keyof FormValues) => (event: { target: { value: string } }) =>
    setValues((previous) => ({ ...previous, [field]: event.target.value }));

  const limit = kindWeight(course, values.kind);
  const used = usedKindWeight(course.assessments, values.kind, editing?.id);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    const input = toInput(values);
    const found = validateAssessment(input, course, course.assessments, editing?.id);
    setErrors(found);
    setFormError(Object.keys(found).length > 0 ? '입력값을 확인하세요. 저장되지 않았습니다.' : null);
    if (Object.keys(found).length > 0) return;

    setSaving(true);
    try {
      if (editing) await updateAssessment(editing.id, input);
      else await addAssessment(course.id, input);
      onSaved();
    } catch (error) {
      if (error instanceof ApiError) {
        setErrors(error.fieldErrors as AssessmentErrors);
        setFormError(`${error.message} 저장되지 않았습니다.`);
      } else {
        setFormError('저장하지 못했습니다. 잠시 후 다시 시도하세요.');
      }
    } finally {
      setSaving(false);
    }
  };

  const fieldError = (field: keyof AssessmentErrors) =>
    errors[field] && (
      <span className={styles.fieldError} id={`assessment-${field}-error`}>
        {errors[field]}
      </span>
    );
  const describedBy = (field: keyof AssessmentErrors) => (errors[field] ? `assessment-${field}-error` : undefined);

  return (
    <form className={styles.form} onSubmit={submit} noValidate aria-labelledby="assessment-form-title">
      <h3 id="assessment-form-title" className={styles.sectionTitle}>
        {editing ? `평가 수정: ${editing.title}` : '평가 추가'}
      </h3>

      <div className={styles.fields}>
        <label className={styles.field}>
          <span className={styles.label}>종류</span>
          <select name="kind" value={values.kind} onChange={set('kind')} className={styles.input}>
            {(['WRITTEN', 'PERFORMANCE'] as const).map((kind) => (
              <option key={kind} value={kind}>
                {ASSESSMENT_KIND_LABEL[kind]} (반영 {kindWeight(course, kind)}%)
              </option>
            ))}
          </select>
        </label>

        <label className={`${styles.field} ${styles.wide}`}>
          <span className={styles.label}>평가 이름</span>
          <input
            name="title"
            value={values.title}
            onChange={set('title')}
            placeholder="예: 수행평가 2"
            className={styles.input}
            aria-invalid={Boolean(errors.title)}
            aria-describedby={describedBy('title')}
          />
          {fieldError('title')}
        </label>

        <label className={styles.field}>
          <span className={styles.label}>반영 비율(%)</span>
          <input
            name="weightPercent"
            type="number"
            inputMode="decimal"
            min="0"
            value={values.weightPercent}
            onChange={set('weightPercent')}
            className={styles.input}
            aria-invalid={Boolean(errors.weightPercent)}
            aria-describedby={describedBy('weightPercent') ?? 'assessment-weight-hint'}
          />
          <span className={styles.hint} id="assessment-weight-hint">
            {ASSESSMENT_KIND_LABEL[values.kind]} {used}% / {limit}% 사용 중
          </span>
          {fieldError('weightPercent')}
        </label>

        <label className={styles.field}>
          <span className={styles.label}>만점</span>
          <input
            name="maxScore"
            type="number"
            inputMode="decimal"
            min="0"
            value={values.maxScore}
            onChange={set('maxScore')}
            className={styles.input}
            aria-invalid={Boolean(errors.maxScore)}
            aria-describedby={describedBy('maxScore')}
          />
          {fieldError('maxScore')}
        </label>

        <label className={styles.field}>
          <span className={styles.label}>득점</span>
          <input
            name="score"
            type="number"
            inputMode="decimal"
            min="0"
            value={values.score}
            onChange={set('score')}
            placeholder="비우면 미입력"
            className={styles.input}
            aria-invalid={Boolean(errors.score)}
            aria-describedby={describedBy('score')}
          />
          {fieldError('score')}
        </label>

        <label className={styles.field}>
          <span className={styles.label}>날짜</span>
          <input
            name="evaluatedOn"
            type="date"
            value={values.evaluatedOn}
            onChange={set('evaluatedOn')}
            className={styles.input}
            aria-invalid={Boolean(errors.evaluatedOn)}
            aria-describedby={describedBy('evaluatedOn')}
          />
          {fieldError('evaluatedOn')}
        </label>
      </div>

      {formError && (
        <p className={styles.formError} role="alert">
          {formError}
        </p>
      )}

      <div className={styles.actions}>
        {editing && (
          <button type="button" className={styles.secondaryButton} onClick={onCancelEdit}>
            수정 취소
          </button>
        )}
        <button type="submit" className={styles.primaryButton} disabled={saving}>
          {saving ? '저장 중...' : '저장'}
        </button>
      </div>
    </form>
  );
}
