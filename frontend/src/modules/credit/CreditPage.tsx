import { useSearchParams } from 'react-router-dom';

import { useAsync } from '../../api/useAsync';
import { PageHeader } from '../../layout/PageHeader';
import { getCourses, getRequiredCourses, getSummary } from './api';
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

  return (
    <>
      <PageHeader title="학점 현황" subtitle={termLabel(CURRENT_TERM.gradeYear, CURRENT_TERM.term)} />

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
                <CourseTable data={courses.state.data} />
                {courses.state.data.courses.some((course) => course.riskLevel === 'RISK' || course.riskLevel === 'CAUTION') && (
                  <Disclaimer />
                )}
              </>
            ))}
        </section>
      </div>
    </>
  );
}
