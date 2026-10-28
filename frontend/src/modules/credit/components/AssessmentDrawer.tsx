import { useEffect, useRef, useState } from 'react';

import { useAsync } from '../../../api/useAsync';
import { getCourseDetail } from '../api';
import { ASSESSMENT_KIND_LABEL, badgeOf, COURSE_TYPE_LABEL, formatRate, requiredAverageMessage } from '../labels';
import type { CourseDetail } from '../types';
import { AssessmentForm } from './AssessmentForm';
import { Disclaimer } from './Disclaimer';
import { StateMessage } from './StateMessage';
import { StatusBadge } from './StatusBadge';
import styles from './AssessmentDrawer.module.css';

interface AssessmentDrawerProps {
  courseId: string;
  onClose: () => void;
  // 평가를 저장한 뒤 학점 현황 화면을 다시 계산하게 알린다
  onSaved: () => void;
}

function CourseStatus({ course }: { course: CourseDetail }) {
  const badge = badgeOf(course.status, course.riskLevel);
  const warned = course.riskLevel === 'RISK' || course.riskLevel === 'CAUTION';
  return (
    <div className={`${styles.status} ${badge === 'risk' ? styles.statusRisk : ''}`}>
      <div className={styles.statusRow}>
        <span className={styles.label}>현재 성취율</span>
        <strong className={`${styles.rate} ${styles[badge] ?? ''}`} data-testid="drawer-rate">
          {formatRate(course.achievementRate)}
        </strong>
        <StatusBadge kind={badge} />
      </div>
      {course.riskLevel === 'RISK' && course.requiredAverage !== undefined && (
        <p className={styles.guide}>{requiredAverageMessage(course.requiredAverage)}</p>
      )}
      {course.achievementRate === null && <p className={styles.hint}>입력된 평가가 없어 성취율을 계산하지 않습니다.</p>}
      {warned && <Disclaimer />}
    </div>
  );
}

function AssessmentList({ course, editingId, onEdit }: { course: CourseDetail; editingId?: string; onEdit: (id: string) => void }) {
  if (course.assessments.length === 0) {
    return <p className={styles.empty}>등록된 평가가 없습니다.</p>;
  }
  return (
    <div className={styles.tableWrapper}>
      <table className={styles.table}>
        <thead>
          <tr>
            <th scope="col">종류</th>
            <th scope="col">평가 이름</th>
            <th scope="col" className={styles.number}>
              반영
            </th>
            <th scope="col" className={styles.number}>
              점수
            </th>
            <th scope="col">날짜</th>
            <th scope="col">
              <span className={styles.visuallyHidden}>수정</span>
            </th>
          </tr>
        </thead>
        <tbody>
          {course.assessments.map((assessment) => (
            <tr key={assessment.id} className={assessment.id === editingId ? styles.editingRow : undefined}>
              <td className={styles.muted}>{ASSESSMENT_KIND_LABEL[assessment.kind]}</td>
              <td>{assessment.title}</td>
              <td className={styles.number}>{assessment.weightPercent}%</td>
              <td className={styles.number}>
                {assessment.score === null ? (
                  <span className={styles.noScore}>미입력</span>
                ) : (
                  `${assessment.score} / ${assessment.maxScore}`
                )}
              </td>
              <td className={styles.muted}>{assessment.evaluatedOn}</td>
              <td>
                <button type="button" className={styles.linkButton} onClick={() => onEdit(assessment.id)}>
                  수정
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

// 과목의 평가 목록과 평가 입력 폼을 보여주는 오른쪽 패널
export function AssessmentDrawer({ courseId, onClose, onSaved }: AssessmentDrawerProps) {
  const detail = useAsync(`course-${courseId}`, () => getCourseDetail(courseId));
  const [editingId, setEditingId] = useState<string | undefined>();
  // 저장할 때마다 바꿔서 폼을 새로 그린다 (입력 칸 초기화)
  const [formVersion, setFormVersion] = useState(0);
  const closeRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    closeRef.current?.focus();
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [onClose]);

  const course = detail.state.status === 'success' ? detail.state.data : null;
  const editing = course?.assessments.find((assessment) => assessment.id === editingId);

  const handleSaved = () => {
    setEditingId(undefined);
    setFormVersion((version) => version + 1);
    detail.reload();
    onSaved();
  };

  return (
    <div className={styles.overlay} onClick={onClose}>
      <aside
        className={styles.drawer}
        role="dialog"
        aria-modal="true"
        aria-labelledby="assessment-drawer-title"
        onClick={(event) => event.stopPropagation()}
      >
        <header className={styles.header}>
          <div>
            <h2 id="assessment-drawer-title" className={styles.title}>
              {course?.name ?? '평가 입력'}
            </h2>
            {course && (
              <p className={styles.meta}>
                {COURSE_TYPE_LABEL[course.courseType]} · {course.credits}학점 · 지필 {course.writtenWeight}% / 수행{' '}
                {course.performanceWeight}%
              </p>
            )}
          </div>
          <button ref={closeRef} type="button" className={styles.close} onClick={onClose} aria-label="닫기">
            ×
          </button>
        </header>

        {detail.state.status === 'loading' && <StateMessage kind="loading" />}
        {detail.state.status === 'error' && <StateMessage kind="error" onRetry={detail.reload} />}
        {course && (
          <div className={styles.content}>
            <CourseStatus course={course} />

            <section aria-labelledby="assessment-list-title">
              <h3 id="assessment-list-title" className={styles.sectionTitle}>
                평가 목록
              </h3>
              <AssessmentList course={course} editingId={editingId} onEdit={setEditingId} />
            </section>

            <AssessmentForm
              key={`${editing?.id ?? 'new'}#${formVersion}`}
              course={course}
              editing={editing}
              onSaved={handleSaved}
              onCancelEdit={() => setEditingId(undefined)}
            />
          </div>
        )}
      </aside>
    </div>
  );
}
