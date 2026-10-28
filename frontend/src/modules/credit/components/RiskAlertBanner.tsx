import { formatRate, requiredAverageMessage } from '../labels';
import type { CourseItem, CreditSummary } from '../types';
import { Disclaimer } from './Disclaimer';
import styles from './RiskAlertBanner.module.css';

interface RiskAlertBannerProps {
  alert: CreditSummary['alert'];
  // 과목별 안내 문구를 만들 과목 목록 (남은 필수 과목)
  courses: CourseItem[];
}

function WarningIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" aria-hidden="true">
      <path d="M12 3 2 20h20L12 3Z" strokeLinejoin="round" />
      <path d="M12 10v4" strokeLinecap="round" />
      <circle cx="12" cy="17" r="0.6" fill="currentColor" />
    </svg>
  );
}

// 이수 위험·주의 과목 수와 과목별 안내
export function RiskAlertBanner({ alert, courses }: RiskAlertBannerProps) {
  const { riskCount, cautionCount } = alert;
  if (riskCount === 0 && cautionCount === 0) return null;

  const riskCourses = courses.filter((course) => course.riskLevel === 'RISK');
  const cautionCourses = courses.filter((course) => course.riskLevel === 'CAUTION');

  return (
    <div className={styles.banner} role="alert">
      <WarningIcon />
      <div className={styles.body}>
        <p className={styles.title}>
          이수 위험 과목 {riskCount}개 · 주의 {cautionCount}개
        </p>
        {riskCourses.map((course) => (
          <p key={course.id} className={styles.detail}>
            {course.name} 현재 성취율 {formatRate(course.achievementRate)}.
            {course.requiredAverage !== undefined && ` ${requiredAverageMessage(course.requiredAverage)}`}
          </p>
        ))}
        {cautionCourses.length > 0 && (
          <p className={styles.detail}>
            주의: {cautionCourses.map((course) => `${course.name} ${formatRate(course.achievementRate)}`).join(', ')}
          </p>
        )}
        <Disclaimer />
      </div>
    </div>
  );
}
