import { useCallback, useState } from 'react';
import { useSearchParams } from 'react-router-dom';

import { useAsync } from '../../api/useAsync';
import { PageHeader } from '../../layout/PageHeader';
import { CURRENT_TERM } from '../credit/labels';
import { getDrafts } from './api';
import { DraftEditor } from './DraftEditor';
import { CARD_LABEL, cardKind, SEMESTERS } from './labels';
import { PastRecordModal } from './PastRecordModal';
import type { RecordDraftCard } from './types';
import styles from './Records.module.css';

// 데모 학생의 현재 학기(학점 현황과 같다)
const DEFAULT_SEMESTER = `${CURRENT_TERM.gradeYear}-${CURRENT_TERM.term}`;

// 생기부 관리(학생): 학기별 세특 카드. 카드를 누르면 4칸 작성 화면
export function StudentRecordPage() {
  // 선택한 학기는 주소(?semester=1-2)에 둬서 새로고침해도 유지한다
  const [searchParams, setSearchParams] = useSearchParams();
  const semester = SEMESTERS.find((option) => option.key === searchParams.get('semester'))?.key ?? DEFAULT_SEMESTER;
  const list = useAsync(`record-drafts-${semester}`, () => getDrafts(semester));

  const [openCard, setOpenCard] = useState<RecordDraftCard | null>(null);
  const [pastOpen, setPastOpen] = useState(false);
  const closePast = useCallback(() => setPastOpen(false), []);
  const { reload } = list;

  const selectSemester = (key: string) => setSearchParams({ semester: key }, { replace: true });

  if (openCard) {
    return (
      <DraftEditor
        card={openCard}
        onBack={() => {
          setOpenCard(null);
          reload();
        }}
      />
    );
  }

  return (
    <>
      <PageHeader
        title="생기부 관리"
        subtitle="학기별 세특 초안"
        actions={
          <button type="button" className={styles.primaryButton} data-action="open-past" onClick={() => setPastOpen(true)}>
            + 과거 기록 추가
          </button>
        }
      />

      <div className={styles.tabs} role="tablist" aria-label="학기 선택">
        {SEMESTERS.map((option) => (
          <button
            key={option.key}
            type="button"
            role="tab"
            aria-selected={option.key === semester}
            data-semester={option.key}
            className={option.key === semester ? `${styles.tab} ${styles.tabSelected}` : styles.tab}
            onClick={() => selectSemester(option.key)}
          >
            {option.label}
          </button>
        ))}
      </div>

      {list.state.status === 'loading' && <p className={styles.note}>불러오는 중입니다...</p>}
      {list.state.status === 'error' && (
        <p className={styles.error} role="alert">
          세특 목록을 불러오지 못했습니다.{' '}
          <button type="button" className={styles.linkButton} onClick={reload}>
            다시 시도
          </button>
        </p>
      )}
      {list.state.status === 'success' &&
        (list.state.data.items.length === 0 ? (
          <p className={styles.empty}>이 학기에 등록한 과목과 세특이 없습니다. 학점 현황에서 과목을 등록하거나 과거 기록을 추가하세요.</p>
        ) : (
          <ul className={styles.cards}>
            {list.state.data.items.map((card) => {
              const kind = cardKind(card);
              return (
                <li key={card.id ?? `course-${card.courseId}`}>
                  <button
                    type="button"
                    className={`${styles.card} ${styles[`card_${kind}`] ?? ''}`}
                    data-card-subject={card.subjectName}
                    onClick={() => setOpenCard(card)}
                  >
                    <span className={styles.cardSubject}>{card.subjectName}</span>
                    <span className={`${styles.badge} ${styles[`badge_${kind}`] ?? ''}`}>{CARD_LABEL[kind]}</span>
                    <span className={styles.cardMeta}>{card.updatedAt ? `${card.updatedAt.slice(0, 10)} 수정` : '아직 쓰지 않음'}</span>
                  </button>
                </li>
              );
            })}
          </ul>
        ))}

      {pastOpen && (
        <PastRecordModal
          initialSemester={semester}
          onClose={closePast}
          onSaved={(savedSemester) => {
            setPastOpen(false);
            if (savedSemester === semester) reload();
            else selectSemester(savedSemester);
          }}
        />
      )}
    </>
  );
}
