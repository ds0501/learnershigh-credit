import { useState } from 'react';

import { ApiError } from '../../../api/errors';
import { deleteGradeRecord } from '../api';
import { UNMATCHED_REASON_LABEL } from '../labels';
import type { GradeRecord } from '../types';
import styles from './GradeTable.module.css';

interface GradeTableProps {
  records: GradeRecord[];
  onEdit: (record: GradeRecord) => void;
  onDeleted: () => void;
}

function unmatchedText(record: GradeRecord) {
  if (record.appliedToCredit) return null;
  if (record.unmatchedReason) return UNMATCHED_REASON_LABEL[record.unmatchedReason];
  return '학점제에 반영하지 않았습니다.';
}

// 학기별 성적 표. 과목별 원점수/과목평균/표준편차/등급 + 학점제 반영 여부
export function GradeTable({ records, onEdit, onDeleted }: GradeTableProps) {
  const [confirmingId, setConfirmingId] = useState<string | null>(null);
  const [deletingId, setDeletingId] = useState<string | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  const confirmDelete = async (recordId: string) => {
    setDeletingId(recordId);
    setDeleteError(null);
    try {
      await deleteGradeRecord(recordId);
      setConfirmingId(null);
      onDeleted();
    } catch (error) {
      setDeleteError(error instanceof ApiError ? error.message : '삭제하지 못했습니다. 잠시 후 다시 시도하세요.');
    } finally {
      setDeletingId(null);
    }
  };

  if (records.length === 0) {
    return <p className={styles.empty}>등록된 성적이 없습니다. 위에서 PDF를 업로드하거나 직접 입력하세요.</p>;
  }

  return (
    <>
      <table className={styles.table}>
        <thead>
          <tr>
            <th>과목명</th>
            <th>원점수</th>
            <th>과목평균</th>
            <th>표준편차</th>
            <th>등급</th>
            <th>학점제 반영</th>
            <th aria-hidden="true" />
          </tr>
        </thead>
        <tbody>
          {records.map((record) => {
            const unmatched = unmatchedText(record);
            return (
              <tr key={record.id}>
                <td>{record.subjectName}</td>
                <td>{record.rawScore ?? '—'}</td>
                <td>{record.subjectAverage ?? '—'}</td>
                <td>{record.stdDev ?? '—'}</td>
                <td>{record.gradeLevel ?? '—'}</td>
                <td>
                  {record.appliedToCredit ? (
                    <span className={`${styles.badge} ${styles.badgeApplied}`}>반영됨</span>
                  ) : (
                    <span className={`${styles.badge} ${styles.badgeUnmatched}`} title={unmatched ?? undefined}>
                      미반영
                    </span>
                  )}
                </td>
                <td>
                  {confirmingId === record.id ? (
                    <div className={styles.confirmRow}>
                      <span>정말 삭제할까요?</span>
                      <button
                        type="button"
                        className={styles.dangerButton}
                        disabled={deletingId === record.id}
                        onClick={() => confirmDelete(record.id)}
                      >
                        {deletingId === record.id ? '삭제 중...' : '삭제'}
                      </button>
                      <button type="button" className={styles.linkButton} onClick={() => setConfirmingId(null)}>
                        취소
                      </button>
                    </div>
                  ) : (
                    <div className={styles.actions}>
                      <button type="button" className={styles.linkButton} onClick={() => onEdit(record)}>
                        수정
                      </button>
                      <button type="button" className={styles.linkButton} onClick={() => setConfirmingId(record.id)}>
                        삭제
                      </button>
                    </div>
                  )}
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
      {deleteError && (
        <p role="alert" className={styles.confirmRow}>
          {deleteError}
        </p>
      )}
    </>
  );
}
