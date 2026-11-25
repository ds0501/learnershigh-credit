import { useState } from 'react';
import { useSearchParams } from 'react-router-dom';

import { useAsync } from '../../api/useAsync';
import { PageHeader } from '../../layout/PageHeader';
import { listGradeRecords } from './api';
import { GradeRecordForm } from './components/GradeRecordForm';
import { GradeTable } from './components/GradeTable';
import { PdfUploadPanel } from './components/PdfUploadPanel';
import { SemesterTabs } from './components/SemesterTabs';
import { StateMessage } from './components/StateMessage';
import { TERMS } from './labels';
import type { GradeRecord } from './types';
import styles from './GradesPage.module.css';

const DEFAULT_TERM_KEY = '1-1';

// 성적 관리: 나이스 성적표 PDF 업로드 또는 직접 입력으로 학기별 성적을 관리한다
export function GradesPage() {
  // 선택한 학기는 주소(?term=1-1)에 둬서 새로고침해도 유지한다
  const [searchParams, setSearchParams] = useSearchParams();
  const selectedTerm = TERMS.find((option) => option.key === searchParams.get('term')) ?? TERMS.find((option) => option.key === DEFAULT_TERM_KEY)!;
  const selectTerm = (key: string) => setSearchParams({ term: key }, { replace: true });

  const records = useAsync(`grade-records-${selectedTerm.key}`, () => listGradeRecords(selectedTerm.gradeYear, selectedTerm.term));

  const [editing, setEditing] = useState<GradeRecord | null>(null);
  const [addOpen, setAddOpen] = useState(false);

  return (
    <>
      <PageHeader title="성적 관리" subtitle="나이스 성적표 업로드 또는 직접 입력" />

      <PdfUploadPanel onUploaded={records.reload} />

      <section className={styles.panel} aria-labelledby="grades-title">
        <div className={styles.panelHeader}>
          <h2 id="grades-title" className={styles.panelTitle}>
            학기별 성적
          </h2>
          <button type="button" className={styles.addButton} onClick={() => setAddOpen(true)}>
            + 직접 입력
          </button>
        </div>

        <SemesterTabs selectedKey={selectedTerm.key} onSelect={selectTerm} />

        {records.state.status === 'loading' && <StateMessage kind="loading" />}
        {records.state.status === 'error' && <StateMessage kind="error" onRetry={records.reload} />}
        {records.state.status === 'success' && (
          <div className={styles.tableWrap}>
            <GradeTable records={records.state.data} onEdit={setEditing} onDeleted={records.reload} />
          </div>
        )}
      </section>

      {(addOpen || editing) && (
        <GradeRecordForm
          initialGradeYear={selectedTerm.gradeYear}
          initialTerm={selectedTerm.term}
          editing={editing ?? undefined}
          onClose={() => {
            setAddOpen(false);
            setEditing(null);
          }}
          onSaved={() => {
            setAddOpen(false);
            setEditing(null);
            records.reload();
          }}
        />
      )}
    </>
  );
}
