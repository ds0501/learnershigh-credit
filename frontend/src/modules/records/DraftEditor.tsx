import { useState } from 'react';

import { ApiError } from '../../api/errors';
import { useAsync } from '../../api/useAsync';
import { useGoal } from '../../goal/goalContext';
import { PageHeader } from '../../layout/PageHeader';
import { createDraft, getDraft, saveDraft, submitDraft, withdrawDraft } from './api';
import { canWithdraw, CARD_LABEL, cardKind, CONTENT_FIELDS, initialCareerLink, isContentBlank, isEditable, semesterLabel } from './labels';
import type { DraftContent, RecordDraft, RecordDraftCard } from './types';
import styles from './Records.module.css';

type ContentValues = Pick<DraftContent, 'motive' | 'process' | 'result' | 'careerLink'>;

interface DraftEditorProps {
  card: RecordDraftCard;
  onBack: () => void;
}

// 세특 4칸 화면. 초안이 없는 카드는 빈 칸으로 시작하고 처음 저장할 때 만든다(POST)
export function DraftEditor({ card, onBack }: DraftEditorProps) {
  const loaded = useAsync(`record-draft-${card.id ?? 'new'}`, () => (card.id ? getDraft(card.id) : Promise.resolve(null)));
  // 저장·제출 응답. 다시 불러오지 않고 바로 반영한다
  const [latest, setLatest] = useState<RecordDraft | null>(null);
  // 저장·제출 안내. 처음 저장(만들기)이나 제출로 폼을 다시 그려도 남도록 여기서 들고 있는다
  const [notice, setNotice] = useState<string | null>(null);

  const draft = latest ?? (loaded.state.status === 'success' ? loaded.state.data : null);
  const waiting = card.id !== null && !latest && loaded.state.status !== 'success';

  return (
    <>
      <PageHeader
        title={card.subjectName}
        subtitle={`${semesterLabel(card.semester)} 세특`}
        actions={
          <button type="button" className={styles.secondaryButton} data-action="back-to-list" onClick={onBack}>
            ← 목록으로
          </button>
        }
      />
      {waiting && loaded.state.status === 'loading' && <p className={styles.note}>불러오는 중입니다...</p>}
      {waiting && loaded.state.status === 'error' && (
        <p className={styles.error} role="alert">
          세특을 불러오지 못했습니다.{' '}
          <button type="button" className={styles.linkButton} onClick={loaded.reload}>
            다시 시도
          </button>
        </p>
      )}
      {!waiting && (
        // 제출하면 상태가 바뀌어 읽기 전용 화면으로 다시 그린다
        <DraftForm
          key={draft ? `${draft.id}:${draft.status}` : 'new'}
          card={card}
          draft={draft}
          notice={notice}
          onNotice={setNotice}
          onSaved={setLatest}
        />
      )}
    </>
  );
}

interface DraftFormProps {
  card: RecordDraftCard;
  draft: RecordDraft | null;
  notice: string | null;
  onNotice: (notice: string | null) => void;
  onSaved: (draft: RecordDraft) => void;
}

function DraftForm({ card, draft, notice, onNotice, onSaved }: DraftFormProps) {
  const { goal } = useGoal();
  const status = draft?.status ?? 'NONE';
  const editable = isEditable(status);
  const kind = cardKind({ status, source: draft?.source ?? card.source });

  const [values, setValues] = useState<ContentValues>(() => ({
    motive: draft?.motive ?? '',
    process: draft?.process ?? '',
    result: draft?.result ?? '',
    // 진로 연결이 비어 있으면 희망 학과 이름을 기본으로 채운다(고칠 수 있을 때만)
    careerLink: editable ? initialCareerLink(draft?.careerLink ?? '', goal?.department.name) : (draft?.careerLink ?? ''),
  }));
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  // 제출은 되돌릴 수 없어 브라우저 확인 창 대신 버튼 자리를 확인 문구로 바꾼다
  const [confirmingSubmit, setConfirmingSubmit] = useState(false);
  // 제출 취소도 같은 2단계 확인
  const [confirmingWithdraw, setConfirmingWithdraw] = useState(false);

  const content = (): DraftContent => ({
    ...values,
    guidelineTitle: draft?.guidelineTitle ?? null,
    guidelineDetail: draft?.guidelineDetail ?? null,
  });

  // 초안이 없으면 만들고(POST), 있으면 저장(PUT)한다
  const persist = () =>
    draft
      ? saveDraft(draft.id, content())
      : createDraft({ ...content(), semester: card.semester, subjectName: card.subjectName, courseId: card.courseId, source: 'NEW' });

  const run = async (action: () => Promise<RecordDraft>, done: string) => {
    setBusy(true);
    setError(null);
    onNotice(null);
    try {
      const result = await action();
      onSaved(result);
      onNotice(done);
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : '저장하지 못했습니다. 잠시 후 다시 시도하세요.');
    } finally {
      setBusy(false);
      setConfirmingSubmit(false);
      setConfirmingWithdraw(false);
    }
  };

  const save = () => run(persist, '저장했어요.');

  const askSubmit = () => {
    onNotice(null);
    if (isContentBlank(values)) {
      setError('내용을 한 칸 이상 입력한 뒤 제출하세요.');
      return;
    }
    setError(null);
    setConfirmingSubmit(true);
  };

  // 제출 취소 → 작성 중(DRAFT). 상태가 바뀌어 편집 화면으로 다시 그린다
  const withdraw = () => {
    if (!draft) return;
    run(() => withdrawDraft(draft.id), '제출을 취소했어요. 다시 고칠 수 있어요.');
  };

  const submit = () =>
    run(async () => {
      const saved = await persist();
      return submitDraft(saved.id);
    }, '제출했어요.');

  return (
    <section className={styles.editor} aria-label="세특 작성">
      <div className={styles.editorStatus}>
        <span className={`${styles.badge} ${styles[`badge_${kind}`] ?? ''}`} data-testid="draft-status">
          {CARD_LABEL[kind]}
        </span>
        {draft?.submittedAt && <span className={styles.cardMeta}>{draft.submittedAt.slice(0, 10)} 제출</span>}
        {!editable && <span className={styles.cardMeta}>읽기 전용</span>}
      </div>

      {draft?.teacherComment && (
        <div className={styles.comment} data-testid="teacher-comment">
          <span className={styles.commentLabel}>선생님 코멘트</span>
          <p className={styles.commentText}>{draft.teacherComment}</p>
        </div>
      )}

      {CONTENT_FIELDS.map((field) => (
        <div key={field.key} className={styles.field}>
          <label className={styles.label} htmlFor={`draft-${field.key}`}>
            {field.label}
          </label>
          {editable ? (
            <textarea
              id={`draft-${field.key}`}
              name={field.key}
              className={styles.textarea}
              rows={4}
              placeholder={field.placeholder}
              value={values[field.key]}
              onChange={(event) => {
                const value = event.target.value;
                setValues((previous) => ({ ...previous, [field.key]: value }));
              }}
            />
          ) : (
            <p className={styles.readonlyText} id={`draft-${field.key}`} data-readonly-field={field.key}>
              {values[field.key].trim() === '' ? <span className={styles.cardMeta}>(비어 있음)</span> : values[field.key]}
            </p>
          )}
        </div>
      ))}

      {notice && (
        <p className={styles.notice} role="status">
          {notice}
        </p>
      )}
      {error && (
        <p className={styles.error} role="alert">
          {error}
        </p>
      )}

      {editable && (
        <div className={styles.actions}>
          {confirmingSubmit ? (
            <div className={styles.confirm}>
              <span>제출한 뒤에도 "제출 취소"로 다시 고칠 수 있어요. 제출할까요?</span>
              <button type="button" className={styles.primaryButton} data-action="confirm-submit" disabled={busy} onClick={submit}>
                {busy ? '제출 중...' : '제출'}
              </button>
              <button
                type="button"
                className={styles.secondaryButton}
                data-action="cancel-submit"
                disabled={busy}
                onClick={() => setConfirmingSubmit(false)}
              >
                취소
              </button>
            </div>
          ) : (
            <>
              <button type="button" className={styles.secondaryButton} data-action="save-draft" disabled={busy} onClick={save}>
                {busy ? '저장 중...' : '저장'}
              </button>
              <button type="button" className={styles.primaryButton} data-action="submit-draft" disabled={busy} onClick={askSubmit}>
                제출
              </button>
            </>
          )}
        </div>
      )}

      {/* 제출 취소: 참고용 프로그램이라 제출한 뒤에도 다시 고칠 수 있다(확인 완료·기존 기록은 제외) */}
      {draft && canWithdraw(status) && (
        <div className={styles.actions}>
          {confirmingWithdraw ? (
            <div className={styles.confirm}>
              <span>제출을 취소할까요? 취소하면 "작성 중"으로 돌아가 다시 고칠 수 있어요.</span>
              <button type="button" className={styles.primaryButton} data-action="confirm-withdraw" disabled={busy} onClick={withdraw}>
                {busy ? '취소 중...' : '제출 취소'}
              </button>
              <button
                type="button"
                className={styles.secondaryButton}
                data-action="close-withdraw"
                disabled={busy}
                onClick={() => setConfirmingWithdraw(false)}
              >
                닫기
              </button>
            </div>
          ) : (
            <button
              type="button"
              className={styles.secondaryButton}
              data-action="withdraw-draft"
              onClick={() => {
                onNotice(null);
                setError(null);
                setConfirmingWithdraw(true);
              }}
            >
              제출 취소
            </button>
          )}
        </div>
      )}
    </section>
  );
}
