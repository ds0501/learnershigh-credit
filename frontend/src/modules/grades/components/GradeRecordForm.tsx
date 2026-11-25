import { useState, type FormEvent } from 'react';

import { ApiError } from '../../../api/errors';
import { useAsync } from '../../../api/useAsync';
import { createGradeRecord, searchCourseCatalog, updateGradeRecord } from '../api';
import { validateGradeRecord, type GradeRecordErrors } from '../domain/validation';
import { CATALOG_COURSE_TYPE_LABEL } from '../labels';
import type { CatalogItem, GradeRecord, GradeRecordInput } from '../types';
import styles from './GradeRecordForm.module.css';

interface GradeRecordFormProps {
  // 처음에 선택해 둘 학기 (지금 보고 있는 학기 탭). 수정일 때는 안 쓴다
  initialGradeYear: number;
  initialTerm: number;
  // 수정할 성적. 없으면 새로 추가
  editing?: GradeRecord;
  onClose: () => void;
  onSaved: () => void;
}

interface FormValues {
  gradeYear: string;
  term: string;
  subjectName: string;
  rawScore: string;
  subjectAverage: string;
  stdDev: string;
  gradeLevel: string;
}

function initialValues(initialGradeYear: number, initialTerm: number, editing?: GradeRecord): FormValues {
  if (editing) {
    return {
      gradeYear: String(editing.gradeYear),
      term: String(editing.term),
      subjectName: editing.subjectName,
      rawScore: editing.rawScore === null ? '' : String(editing.rawScore),
      subjectAverage: editing.subjectAverage === null ? '' : String(editing.subjectAverage),
      stdDev: editing.stdDev === null ? '' : String(editing.stdDev),
      gradeLevel: editing.gradeLevel === null ? '' : String(editing.gradeLevel),
    };
  }
  return { gradeYear: String(initialGradeYear), term: String(initialTerm), subjectName: '', rawScore: '', subjectAverage: '', stdDev: '', gradeLevel: '' };
}

// 비우면 null, 아니면 숫자(잘못된 값이면 NaN으로 둬서 검증에서 걸리게 한다)
const toNullableNumber = (value: string) => (value.trim() === '' ? null : Number(value));

function toInput(values: FormValues): GradeRecordInput {
  return {
    gradeYear: Number(values.gradeYear),
    term: Number(values.term),
    subjectName: values.subjectName,
    rawScore: toNullableNumber(values.rawScore),
    subjectAverage: toNullableNumber(values.subjectAverage),
    stdDev: toNullableNumber(values.stdDev),
    gradeLevel: toNullableNumber(values.gradeLevel),
  };
}

// 성적 수동 추가/수정 모달
export function GradeRecordForm({ initialGradeYear, initialTerm, editing, onClose, onSaved }: GradeRecordFormProps) {
  const [values, setValues] = useState<FormValues>(() => initialValues(initialGradeYear, initialTerm, editing));
  const [errors, setErrors] = useState<GradeRecordErrors>({});
  const [formError, setFormError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  // 과목명을 고르면 검색 결과를 닫는다. 다시 타이핑하면 그 과목명으로 다시 검색한다(새로 추가할 때만, 수정 중엔 과목명을 못 바꾸니 검색 안 함)
  const [pickedFromCatalog, setPickedFromCatalog] = useState(false);

  const set = (field: keyof FormValues) => (event: { target: { value: string } }) => {
    if (field === 'subjectName') setPickedFromCatalog(false);
    setValues((previous) => ({ ...previous, [field]: event.target.value }));
  };

  const keyword = !editing && !pickedFromCatalog ? values.subjectName.trim() : '';
  const catalogResults = useAsync(`grade-catalog:${keyword}`, () => (keyword ? searchCourseCatalog(keyword) : Promise.resolve([])));
  const showCatalogResults = keyword !== '';

  const pickCatalogItem = (item: CatalogItem) => {
    setValues((previous) => ({ ...previous, subjectName: item.name }));
    setPickedFromCatalog(true);
    setErrors((previous) => {
      if (!('subjectName' in previous)) return previous;
      const next = { ...previous };
      delete next.subjectName;
      return next;
    });
  };

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    const input = toInput(values);
    const found = validateGradeRecord(input, Boolean(editing));
    setErrors(found);
    setFormError(Object.keys(found).length > 0 ? '입력값을 확인하세요. 저장되지 않았습니다.' : null);
    if (Object.keys(found).length > 0) return;

    setSaving(true);
    try {
      if (editing) await updateGradeRecord(editing.id, input);
      else await createGradeRecord(input);
      onSaved();
    } catch (error) {
      if (error instanceof ApiError) {
        setErrors(error.fieldErrors as GradeRecordErrors);
        setFormError(`${error.message} 저장되지 않았습니다.`);
      } else {
        setFormError('저장하지 못했습니다. 잠시 후 다시 시도하세요.');
      }
    } finally {
      setSaving(false);
    }
  };

  const fieldError = (field: keyof GradeRecordErrors) =>
    errors[field] && <span className={styles.fieldError}>{errors[field]}</span>;

  return (
    <div className={styles.overlay} role="presentation" onClick={onClose}>
      <div
        className={styles.modal}
        role="dialog"
        aria-modal="true"
        aria-labelledby="grade-record-form-title"
        onClick={(event) => event.stopPropagation()}
      >
        <div className={styles.header}>
          <h2 id="grade-record-form-title" className={styles.title}>
            {editing ? `성적 수정: ${editing.subjectName}` : '성적 직접 입력'}
          </h2>
          <button type="button" className={styles.close} aria-label="닫기" onClick={onClose}>
            ×
          </button>
        </div>

        <form onSubmit={submit} noValidate>
          <div className={styles.fields}>
            <label className={styles.field}>
              <span className={styles.label}>학년</span>
              <select className={styles.input} value={values.gradeYear} onChange={set('gradeYear')} disabled={Boolean(editing)}>
                {[1, 2, 3].map((year) => (
                  <option key={year} value={year}>
                    {year}학년
                  </option>
                ))}
              </select>
              {fieldError('gradeYear')}
            </label>

            <label className={styles.field}>
              <span className={styles.label}>학기</span>
              <select className={styles.input} value={values.term} onChange={set('term')} disabled={Boolean(editing)}>
                {[1, 2].map((term) => (
                  <option key={term} value={term}>
                    {term}학기
                  </option>
                ))}
              </select>
              {fieldError('term')}
            </label>

            <label className={`${styles.field} ${styles.wide}`}>
              <span className={styles.label}>과목명</span>
              <div className={styles.searchBox}>
                <input
                  className={styles.input}
                  value={values.subjectName}
                  onChange={set('subjectName')}
                  disabled={Boolean(editing)}
                  placeholder="과목명을 입력하거나 표준 과목 목록에서 검색하세요 (예: 공통국어)"
                  autoComplete="off"
                  role="combobox"
                  aria-expanded={showCatalogResults}
                  aria-controls="grade-subject-results"
                  aria-invalid={Boolean(errors.subjectName)}
                />
              </div>
              {!editing && showCatalogResults && (
                <div className={styles.results} id="grade-subject-results">
                  {catalogResults.state.status === 'loading' && <p className={styles.resultNote}>검색 중...</p>}
                  {catalogResults.state.status === 'error' && <p className={styles.resultNote}>과목 목록을 불러오지 못했습니다.</p>}
                  {catalogResults.state.status === 'success' &&
                    (catalogResults.state.data.length === 0 ? (
                      <p className={styles.resultNote}>검색 결과가 없습니다. 직접 입력한 과목명으로 저장할 수 있습니다.</p>
                    ) : (
                      <ul className={styles.resultList} role="listbox" aria-label="과목 검색 결과">
                        {catalogResults.state.data.map((item) => (
                          <li key={item.id} role="option" aria-selected={false}>
                            <button type="button" className={styles.resultItem} onClick={() => pickCatalogItem(item)}>
                              <span className={styles.resultName}>{item.name}</span>
                              <span className={styles.resultMeta}>
                                {item.subjectGroup} · {CATALOG_COURSE_TYPE_LABEL[item.courseType] ?? item.courseType}
                              </span>
                            </button>
                          </li>
                        ))}
                      </ul>
                    ))}
                </div>
              )}
              {fieldError('subjectName')}
            </label>

            <label className={styles.field}>
              <span className={styles.label}>원점수</span>
              <input
                className={styles.input}
                type="number"
                inputMode="decimal"
                min="0"
                max="100"
                value={values.rawScore}
                onChange={set('rawScore')}
                placeholder="비우면 미입력"
              />
              {fieldError('rawScore')}
            </label>

            <label className={styles.field}>
              <span className={styles.label}>등급(1~9)</span>
              <input
                className={styles.input}
                type="number"
                inputMode="numeric"
                min="1"
                max="9"
                value={values.gradeLevel}
                onChange={set('gradeLevel')}
                placeholder="비우면 미입력"
              />
              {fieldError('gradeLevel')}
            </label>

            <label className={styles.field}>
              <span className={styles.label}>과목평균</span>
              <input
                className={styles.input}
                type="number"
                inputMode="decimal"
                step="0.1"
                value={values.subjectAverage}
                onChange={set('subjectAverage')}
                placeholder="비우면 미입력"
              />
              {fieldError('subjectAverage')}
            </label>

            <label className={styles.field}>
              <span className={styles.label}>표준편차</span>
              <input
                className={styles.input}
                type="number"
                inputMode="decimal"
                step="0.1"
                min="0"
                value={values.stdDev}
                onChange={set('stdDev')}
                placeholder="비우면 미입력"
              />
              {fieldError('stdDev')}
            </label>
          </div>

          {formError && (
            <p className={styles.formError} role="alert">
              {formError}
            </p>
          )}

          <div className={styles.actions}>
            <button type="button" className={styles.secondaryButton} onClick={onClose}>
              취소
            </button>
            <button type="submit" className={styles.primaryButton} disabled={saving}>
              {saving ? '저장 중...' : '저장'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
