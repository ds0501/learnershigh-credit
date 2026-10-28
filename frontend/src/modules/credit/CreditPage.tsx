import { useCallback, useState } from 'react';
import { useSearchParams } from 'react-router-dom';

import { useAsync } from '../../api/useAsync';
import { PageHeader } from '../../layout/PageHeader';
import { getCourses, getRequiredCourses, getSummary } from './api';
import { AssessmentDrawer } from './components/AssessmentDrawer';
import { CourseRegisterModal } from './components/CourseRegisterModal';
import { CourseTable } from './components/CourseTable';
import { Disclaimer } from './components/Disclaimer';
import { RequiredCourseList } from './components/RequiredCourseList';
import { RiskAlertBanner } from './components/RiskAlertBanner';
import { SemesterTabs } from './components/SemesterTabs';
import { StateMessage } from './components/StateMessage';
import { SummaryCard } from './components/SummaryCard';
import { CURRENT_TERM, TERMS, termLabel } from './labels';
import styles from './CreditPage.module.css';

const CURRENT_TERM_KEY = `${CURRENT_TERM.gradeYear}-${CURRENT_TERM.term}`;

// 고교학점제 관리 > 학점 현황
export function CreditPage() {
  // 선택한 학기는 주소(?term=1-2)에 둬서 새로고침해도 유지한다
  const [searchParams, setSearchParams] = useSearchParams();
  const selectedTerm = TERMS.find((option) => option.key === searchParams.get('term')) ?? TERMS.find((option) => option.key === CURRENT_TERM_KEY)!;

  const summary = useAsync('summary', getSummary);
  const required = useAsync('required-courses', getRequiredCourses);
  const courses = useAsync(`courses-${selectedTerm.key}`, () => getCourses(selectedTerm.gradeYear, selectedTerm.term));

  const selectTerm = (key: string) => setSearchParams({ term: key }, { replace: true });

  // 점수 입력 패널을 연 과목
  const [selectedCourseId, setSelectedCourseId] = useState<string | null>(null);
  const closeDrawer = useCallback(() => setSelectedCourseId(null), []);
  // 평가를 저장하면 요약, 경고 배너, 필수 과목, 과목 표를 모두 다시 받아 계산 결과를 반영한다
  const { reload: reloadSummary } = summary;
  const { reload: reloadRequired } = required;
  const { reload: reloadCourses } = courses;
  const refreshAll = useCallback(() => {
    reloadSummary();
    reloadRequired();
    reloadCourses();
  }, [reloadSummary, reloadRequired, reloadCourses]);

  // 신규 과목 등록 모달
  const [registerOpen, setRegisterOpen] = useState(false);
  const closeRegister = useCallback(() => setRegisterOpen(false), []);
  const handleRegistered = (gradeYear: number, term: number) => {
    setRegisterOpen(false);
    // 등록한 학기 탭으로 이동하고 요약, 배너, 필수 과목, 과목 표를 다시 계산한다
    selectTerm(`${gradeYear}-${term}`);
    refreshAll();
  };

  return (
    <>
      <PageHeader
        title="학점 현황"
        subtitle={termLabel(CURRENT_TERM.gradeYear, CURRENT_TERM.term)}
        actions={
          <button type="button" className={styles.registerButton} data-action="open-register" onClick={() => setRegisterOpen(true)}>
            + 신규 과목 등록
          </button>
        }
      />

      <div className={styles.layout}>
        <div className={styles.column}>
          {summary.state.status === 'success' ? (
            <SummaryCard summary={summary.state.data} />
          ) : (
            <section className={styles.panel}>
              <StateMessage kind={summary.state.status} onRetry={summary.state.status === 'error' ? summary.reload : undefined} />
            </section>
          )}

          <section className={styles.panel} aria-labelledby="required-title">
            <div className={styles.panelHeader}>
              <h2 id="required-title" className={styles.panelTitle}>
                남은 필수 이수 과목
              </h2>
              {required.state.status === 'success' && (
                <span className={styles.count}>{required.state.data.remainingCount}과목</span>
              )}
            </div>
            {required.state.status === 'success' ? (
              <RequiredCourseList data={required.state.data} />
            ) : (
              <StateMessage kind={required.state.status} onRetry={required.state.status === 'error' ? required.reload : undefined} />
            )}
          </section>
        </div>

        <section className={`${styles.panel} ${styles.coursesPanel}`} aria-labelledby="courses-title">
          <h2 id="courses-title" className={styles.panelTitle}>
            학기별 수강 과목
          </h2>

          {summary.state.status === 'success' && required.state.status === 'success' && (
            <RiskAlertBanner alert={summary.state.data.alert} courses={required.state.data.courses} />
          )}

          <SemesterTabs selectedKey={selectedTerm.key} onSelect={selectTerm} />

          {courses.state.status === 'loading' && <StateMessage kind="loading" />}
          {courses.state.status === 'error' && <StateMessage kind="error" onRetry={courses.reload} />}
          {courses.state.status === 'success' &&
            (courses.state.data.courses.length === 0 ? (
              <StateMessage kind="empty" message="등록된 과목이 없습니다." />
            ) : (
              <>
                <CourseTable data={courses.state.data} onSelect={setSelectedCourseId} />
                <p className={styles.hint}>과목을 누르면 평가 점수 입력, 이수 상태 변경, 과목 삭제를 할 수 있습니다.</p>
                {courses.state.data.courses.some((course) => course.riskLevel === 'RISK' || course.riskLevel === 'CAUTION') && (
                  <Disclaimer />
                )}
              </>
            ))}
        </section>
      </div>

      {registerOpen && (
        <CourseRegisterModal initialTermKey={selectedTerm.key} onClose={closeRegister} onRegistered={handleRegistered} />
      )}
      {selectedCourseId && (
        <AssessmentDrawer
          courseId={selectedCourseId}
          onClose={closeDrawer}
          onSaved={refreshAll}
          onCourseDeleted={() => {
            // 과목을 지우면 패널을 닫고 요약, 배너, 필수 과목, 과목 표를 다시 계산한다
            setSelectedCourseId(null);
            refreshAll();
          }}
        />
      )}
    </>
  );
}
