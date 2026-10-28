import { useEffect, useRef, useState, type FormEvent } from 'react';

import { ApiError } from '../../../api/errors';
import { useAsync } from '../../../api/useAsync';
import { registerCourse, searchCatalog } from '../api';
import { setWrittenWeight, validateCourseDraft } from '../domain/courseRegistration';
import { COURSE_STATUS_LABEL, COURSE_TYPE_LABEL, GRADE_DISPLAY_LABEL, TERMS } from '../labels';
import type { CatalogItem, CourseStatus, CourseType, GradeDisplay } from '../types';
import styles from './CourseRegisterModal.module.css';

interface CourseRegisterModalProps {
  // 처음에 선택해 둘 학기 (지금 보고 있는 학기 탭)
  initialTermKey: string;
  onClose: () => void;
  // 등록에 성공하면 그 학기로 이동하고 화면을 다시 계산하게 알린다
  onRegistered: (gradeYear: number, term: number) => void;
}

// 직접 입력할 때 고를 수 있는 유형 (목업과 같이 4개. 창체는 목록에서만 고른다)
const DIRECT_TYPES: CourseType[] = ['COMMON', 'GENERAL', 'CAREER', 'FUSION'];
const STATUSES: CourseStatus[] = ['IN_PROGRESS', 'COMPLETED'];
const GRADE_DISPLAYS: GradeDisplay[] = ['ACHIEVEMENT_ONLY', 'ACHIEVEMENT_AND_RANK', 'THREE_LEVEL'];
const WEIGHT_STEP = 10;

function InfoIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
      <circle cx="12" cy="12" r="9" />
      <path d="M12 11v5" strokeLinecap="round" />
      <circle cx="12" cy="7.5" r="0.6" fill="currentColor" />
    </svg>
  );
}

function SearchIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
      <circle cx="11" cy="11" r="6.5" />
      <path d="m16 16 4 4" strokeLinecap="round" />
    </svg>
  );
}

// 신규 과목 등록 모달 (PRD F2)
export function CourseRegisterModal({ initialTermKey, onClose, onRegistered }: CourseRegisterModalProps) {
  const [termKey, setTermKey] = useState(initialTermKey);
  const [mode, setMode] = useState<'catalog' | 'custom'>('catalog');
  const [query, setQuery] = useState('');
  const [selected, setSelected] = useState<CatalogItem | null>(null);
  const [customName, setCustomName] = useState('');
  const [customType, setCustomType] = useState<CourseType | null>(null);
  const [credits, setCredits] = useState(4);
  const [status, setStatus] = useState<CourseStatus>('IN_PROGRESS');
  const [weights, setWeights] = useState(setWrittenWeight(60));
  const [gradeDisplay, setGradeDisplay] = useState<GradeDisplay>('ACHIEVEMENT_AND_RANK');
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

  // 목록 검색은 과목을 고르기 전, 검색어가 있을 때만 한다
  const keyword = mode === 'catalog' && !selected ? query.trim() : '';
  const results = useAsync(`catalog:${keyword}`, () => (keyword ? searchCatalog(keyword) : Promise.resolve([])));

  const courseType = mode === 'catalog' ? (selected?.courseType ?? null) : customType;
  const creative = courseType === 'CREATIVE';
  const term = TERMS.find((option) => option.key === termKey)!;

  const clearError = (field: string) =>
    setErrors((previous) => {
      if (!(field in previous)) return previous;
      const next = { ...previous };
      delete next[field];
      return next;
    });

  const selectCatalogItem = (item: CatalogItem) => {
    setSelected(item);
    clearError('name');
    clearError('courseType');
  };

  const switchMode = (next: 'catalog' | 'custom') => {
    if (next === 'custom') setCustomName(selected?.name ?? query.trim());
    else setQuery(customName);
    setSelected(null);
    setMode(next);
    setErrors({});
    setFormError(null);
  };

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    const name = mode === 'catalog' ? (selected?.name ?? '') : customName;
    const found: Record<string, string> = {
      ...validateCourseDraft(
        {
          gradeYear: term.gradeYear,
          term: term.term,
          name,
          courseType: courseType ?? 'COMMON',
          credits,
          status,
          writtenWeight: creative ? 0 : weights.writtenWeight,
          performanceWeight: creative ? 0 : weights.performanceWeight,
        },
        // 같은 학기 중복은 저장할 때 서버(mock)가 확인한다
        [],
      ),
    };
    if (mode === 'catalog' && !selected) {
      found.name = query.trim()
        ? '목록에서 과목을 고르거나 "목록에 없는 과목 직접 입력"을 누르세요.'
        : '과목명을 입력하세요.';
    }
    if (mode === 'custom' && !customType) found.courseType = '과목 유형을 선택하세요.';

    setErrors(found);
    if (Object.keys(found).length > 0) {
      setFormError('입력값을 확인하세요. 등록되지 않았습니다.');
      return;
    }

    setSaving(true);
    setFormError(null);
    try {
      await registerCourse({
        gradeYear: term.gradeYear,
        term: term.term,
        catalogId: mode === 'catalog' ? selected!.id : null,
        ...(mode === 'custom' ? { customName: customName.trim(), courseType: customType! } : {}),
        credits,
        status,
        writtenWeight: creative ? 0 : weights.writtenWeight,
        performanceWeight: creative ? 0 : weights.performanceWeight,
        gradeDisplay: creative ? null : gradeDisplay,
      });
      onRegistered(term.gradeYear, term.term);
    } catch (error) {
      if (error instanceof ApiError) {
        setErrors(error.fieldErrors);
        setFormError(`${error.message} 등록되지 않았습니다.`);
      } else {
        setFormError('등록하지 못했습니다. 잠시 후 다시 시도하세요.');
      }
    } finally {
      setSaving(false);
    }
  };

  const fieldError = (field: string) =>
    errors[field] && (
      <p className={styles.fieldError} id={`register-${field}-error`} data-error-for={field}>
        {errors[field]}
      </p>
    );

  const chip = (selectedChip: boolean) => (selectedChip ? `${styles.chip} ${styles.chipSelected}` : styles.chip);
  const typeOptions: CourseType[] = mode === 'catalog' && creative ? [...DIRECT_TYPES, 'CREATIVE'] : DIRECT_TYPES;
  const showResults = mode === 'catalog' && !selected && keyword !== '';

  return (
    <div className={styles.overlay} onClick={onClose}>
      <form
        className={styles.modal}
        role="dialog"
        aria-modal="true"
        aria-labelledby="register-title"
        onClick={(event) => event.stopPropagation()}
        onSubmit={submit}
        noValidate
      >
        <header className={styles.header}>
          <h2 id="register-title" className={styles.title}>
            신규 과목 등록
          </h2>
          <button type="button" className={styles.close} onClick={onClose} aria-label="닫기">
            ×
          </button>
        </header>

        <div className={styles.body}>
          {/* 학기 */}
          <fieldset className={styles.group}>
            <legend className={styles.label}>학기</legend>
            <div className={styles.chips}>
              {TERMS.map((option) => (
                <button
                  key={option.key}
                  type="button"
                  className={chip(option.key === termKey)}
                  aria-pressed={option.key === termKey}
                  data-register-term={option.key}
                  onClick={() => {
                    setTermKey(option.key);
                    clearError('term');
                    clearError('name');
                  }}
                >
                  {option.label}
                </button>
              ))}
            </div>
            {fieldError('term')}
          </fieldset>

          {/* 과목명 */}
          <div className={styles.group}>
            <label className={styles.label} htmlFor="register-name">
              과목명
            </label>
            {mode === 'catalog' ? (
              <div className={styles.searchBox}>
                <span className={styles.searchIcon}>
                  <SearchIcon />
                </span>
                <input
                  ref={searchRef}
                  id="register-name"
                  name="courseSearch"
                  className={`${styles.input} ${styles.searchInput}`}
                  value={selected ? selected.name : query}
                  placeholder="과목명을 검색하세요 (예: 공통국어)"
                  autoComplete="off"
                  role="combobox"
                  aria-expanded={showResults}
                  aria-controls="register-results"
                  aria-invalid={Boolean(errors.name)}
                  aria-describedby={errors.name ? 'register-name-error' : undefined}
                  onChange={(event) => {
                    setSelected(null);
                    setQuery(event.target.value);
                    clearError('name');
                  }}
                />
              </div>
            ) : (
              <input
                id="register-name"
                name="customName"
                className={styles.input}
                value={customName}
                placeholder="과목명을 직접 입력하세요"
                autoComplete="off"
                aria-invalid={Boolean(errors.name)}
                aria-describedby={errors.name ? 'register-name-error' : undefined}
                onChange={(event) => {
                  setCustomName(event.target.value);
                  clearError('name');
                }}
              />
            )}

            {showResults && (
              <div className={styles.results} id="register-results">
                {results.state.status === 'loading' && <p className={styles.resultNote}>검색 중...</p>}
                {results.state.status === 'error' && <p className={styles.resultNote}>과목 목록을 불러오지 못했습니다.</p>}
                {results.state.status === 'success' &&
                  (results.state.data.length === 0 ? (
                    <p className={styles.resultNote}>검색 결과가 없습니다.</p>
                  ) : (
                    <ul className={styles.resultList} role="listbox" aria-label="과목 검색 결과">
                      {results.state.data.map((item) => (
                        <li key={item.id} role="option" aria-selected={false}>
                          <button
                            type="button"
                            className={styles.resultItem}
                            data-catalog-id={item.id}
                            onClick={() => selectCatalogItem(item)}
                          >
                            <span className={styles.resultName}>{item.name}</span>
                            <span className={styles.resultMeta}>
                              {item.subjectGroup} · {COURSE_TYPE_LABEL[item.courseType]}
                            </span>
                          </button>
                        </li>
                      ))}
                    </ul>
                  ))}
              </div>
            )}

            {selected && (
              <p className={styles.info} data-testid="autofill-info">
                <InfoIcon />
                <span>
                  표준 과목 정보로 교과(군)·유형을 자동 입력했어요 · 교과(군) <strong>{selected.subjectGroup}</strong>
                </span>
              </p>
            )}
            {fieldError('name')}

            <button
              type="button"
              className={styles.linkButton}
              data-action={mode === 'catalog' ? 'custom-mode' : 'catalog-mode'}
              onClick={() => switchMode(mode === 'catalog' ? 'custom' : 'catalog')}
            >
              {mode === 'catalog' ? '목록에 없는 과목 직접 입력' : '과목 목록에서 검색'}
            </button>
          </div>

          {/* 과목 유형, 학점 */}
          <div className={styles.typeRow}>
            <fieldset className={styles.group}>
              <legend className={styles.label}>과목 유형</legend>
              <div className={styles.chips}>
                {typeOptions.map((type) => (
                  <button
                    key={type}
                    type="button"
                    className={chip(courseType === type)}
                    aria-pressed={courseType === type}
                    data-course-type={type}
                    // 목록 과목은 유형을 바꿀 수 없다
                    disabled={mode === 'catalog'}
                    onClick={() => {
                      setCustomType(type);
                      clearError('courseType');
                    }}
                  >
                    {COURSE_TYPE_LABEL[type]}
                  </button>
                ))}
              </div>
              {mode === 'catalog' && <p className={styles.hint}>목록 과목의 유형은 과목 정보를 따릅니다.</p>}
              {fieldError('courseType')}
            </fieldset>

            <div className={styles.group}>
              <label className={styles.label} htmlFor="register-credits">
                학점
              </label>
              <div className={styles.stepper}>
                <button
                  type="button"
                  className={styles.stepButton}
                  aria-label="학점 줄이기"
                  data-action="credits-down"
                  onClick={() => setCredits((value) => Math.max(1, value - 1))}
                >
                  −
                </button>
                <input
                  id="register-credits"
                  name="credits"
                  type="number"
                  min="1"
                  step="1"
                  className={styles.stepValue}
                  value={Number.isNaN(credits) ? '' : credits}
                  aria-invalid={Boolean(errors.credits)}
                  onChange={(event) => {
                    setCredits(event.target.value === '' ? Number.NaN : Number(event.target.value));
                    clearError('credits');
                  }}
                />
                <button
                  type="button"
                  className={styles.stepButton}
                  aria-label="학점 늘리기"
                  data-action="credits-up"
                  onClick={() => setCredits((value) => (Number.isNaN(value) ? 1 : value + 1))}
                >
                  +
                </button>
              </div>
              {fieldError('credits')}
            </div>
          </div>

          {/* 이수 상태 */}
          <fieldset className={styles.group}>
            <legend className={styles.label}>이수 상태</legend>
            <div className={styles.chips}>
              {STATUSES.map((option) => (
                <button
                  key={option}
                  type="button"
                  className={chip(status === option)}
                  aria-pressed={status === option}
                  data-status={option}
                  onClick={() => setStatus(option)}
                >
                  {COURSE_STATUS_LABEL[option]}
                </button>
              ))}
            </div>
            {fieldError('status')}
          </fieldset>

          {/* 성적 산출 방식 (창체는 평가가 없어 숨긴다) */}
          {creative ? (
            <p className={styles.hint}>창의적 체험활동은 평가가 없어 성적 산출 방식을 입력하지 않습니다.</p>
          ) : (
            <section className={styles.grading} aria-labelledby="register-grading-title">
              <div className={styles.gradingHeader}>
                <h3 id="register-grading-title" className={styles.gradingTitle}>
                  성적 산출 방식
                </h3>
                <span className={styles.hint}>반영 비율 합계 100%</span>
              </div>

              <div className={styles.weightRow}>
                <label className={styles.weightLabel} htmlFor="register-written">
                  지필평가
                </label>
                <div className={styles.stepper}>
                  <button
                    type="button"
                    className={styles.stepButton}
                    aria-label={`지필 비율 ${WEIGHT_STEP}% 줄이기`}
                    data-action="written-down"
                    onClick={() => setWeights(setWrittenWeight(weights.writtenWeight - WEIGHT_STEP))}
                  >
                    −
                  </button>
                  <input
                    id="register-written"
                    name="writtenWeight"
                    type="number"
                    min="0"
                    max="100"
                    step="1"
                    className={styles.stepValue}
                    value={weights.writtenWeight}
                    onChange={(event) => setWeights(setWrittenWeight(Number(event.target.value)))}
                  />
                  <span className={styles.percent}>%</span>
                  <button
                    type="button"
                    className={styles.stepButton}
                    aria-label={`지필 비율 ${WEIGHT_STEP}% 늘리기`}
                    data-action="written-up"
                    onClick={() => setWeights(setWrittenWeight(weights.writtenWeight + WEIGHT_STEP))}
                  >
                    +
                  </button>
                </div>
                <span className={styles.weightLabel}>수행평가</span>
                <output className={styles.performance} htmlFor="register-written" data-testid="performance-weight">
                  {weights.performanceWeight}%
                </output>
              </div>
              <div className={styles.weightBar} aria-hidden="true">
                <span className={styles.weightBarWritten} style={{ width: `${weights.writtenWeight}%` }} />
              </div>
              {fieldError('writtenWeight')}

              <fieldset className={styles.group}>
                <legend className={styles.label}>성적 표기 방식</legend>
                <div className={styles.chips}>
                  {GRADE_DISPLAYS.map((option) => (
                    <button
                      key={option}
                      type="button"
                      className={chip(gradeDisplay === option)}
                      aria-pressed={gradeDisplay === option}
                      data-grade-display={option}
                      onClick={() => setGradeDisplay(option)}
                    >
                      {GRADE_DISPLAY_LABEL[option]}
                    </button>
                  ))}
                </div>
                <p className={styles.hint}>표기 방식은 이수 위험 계산에 영향을 주지 않습니다.</p>
                {fieldError('gradeDisplay')}
              </fieldset>
            </section>
          )}
        </div>

        <footer className={styles.footer}>
          {formError && (
            <p className={styles.formError} role="alert">
              {formError}
            </p>
          )}
          <button type="button" className={styles.secondaryButton} onClick={onClose}>
            취소
          </button>
          <button type="submit" className={styles.primaryButton} disabled={saving}>
            {saving ? '등록 중...' : '등록'}
          </button>
        </footer>
      </form>
    </div>
  );
}
