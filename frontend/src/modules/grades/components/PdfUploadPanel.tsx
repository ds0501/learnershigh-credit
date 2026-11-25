import { useRef, useState } from 'react';

import { ApiError } from '../../../api/errors';
import { uploadGradePdf } from '../api';
import { UNMATCHED_REASON_LABEL } from '../labels';
import type { GradeUploadResult } from '../types';
import styles from './PdfUploadPanel.module.css';

interface PdfUploadPanelProps {
  // 업로드가 끝나면(실패해도 호출하지 않음) 성적 표를 다시 불러온다
  onUploaded: () => void;
}

// 나이스 성적표 PDF 업로드. 실제 샘플로 파싱 규칙을 검증하지 못해(docs/PROGRESS.md 참고) 인식이 안 되면 수동 입력으로 이어간다
export function PdfUploadPanel({ onUploaded }: PdfUploadPanelProps) {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<GradeUploadResult | null>(null);

  const upload = async () => {
    const file = fileInputRef.current?.files?.[0];
    if (!file) {
      setError('PDF 파일을 선택하세요.');
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const response = await uploadGradePdf(file);
      setResult(response);
      if (fileInputRef.current) fileInputRef.current.value = '';
      onUploaded();
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : '업로드하지 못했습니다. 잠시 후 다시 시도하세요.');
    } finally {
      setLoading(false);
    }
  };

  const appliedCount = result?.records.filter((record) => record.appliedToCredit).length ?? 0;
  const unmatched = result?.records.filter((record) => !record.appliedToCredit) ?? [];

  return (
    <div className={styles.panel}>
      <div className={styles.row}>
        <input ref={fileInputRef} type="file" accept="application/pdf" className={styles.fileInput} />
        <button type="button" className={styles.uploadButton} disabled={loading} onClick={upload}>
          {loading ? '업로드 중...' : '성적표 PDF 업로드'}
        </button>
      </div>
      <p className={styles.hint}>나이스 성적증명서(전자 PDF) 형식을 인식합니다. 인식이 안 되면 아래에서 직접 입력하세요.</p>

      {error && <p className={styles.error} role="alert">{error}</p>}

      {result && !error && (
        <div>
          <p className={styles.summary} role="status">
            인식 {result.recognizedCount}건 중 학점제 반영 {appliedCount}건, 실패 {result.failedCount}건
          </p>
          {unmatched.length > 0 && (
            <ul className={styles.unmatchedList}>
              {unmatched.map((record) => (
                <li key={record.id}>
                  {record.subjectName}: {record.unmatchedReason ? UNMATCHED_REASON_LABEL[record.unmatchedReason] : '학점제에 반영하지 않았습니다.'}
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
}
