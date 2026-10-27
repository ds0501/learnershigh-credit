import { badgeOf, COURSE_TYPE_LABEL, formatRate } from '../labels';
import type { SemesterCourses } from '../types';
import { StatusBadge } from './StatusBadge';
import styles from './CourseTable.module.css';

// 선택한 학기의 수강 과목 표
export function CourseTable({ data }: { data: SemesterCourses }) {
  return (
    <div className={styles.wrapper}>
      <table className={styles.table}>
        <thead>
          <tr>
            <th scope="col">과목명</th>
            <th scope="col">유형</th>
            <th scope="col" className={styles.number}>
              학점
            </th>
            <th scope="col" className={styles.number}>
              현재 성취율
            </th>
            <th scope="col" className={styles.status}>
              상태
            </th>
          </tr>
        </thead>
        <tbody>
          {data.courses.map((course) => {
            const badge = badgeOf(course.status, course.riskLevel);
            return (
              <tr key={course.id} className={badge === 'risk' ? styles.riskRow : undefined}>
                <td className={styles.name}>{course.name}</td>
                <td className={styles.muted}>{COURSE_TYPE_LABEL[course.courseType]}</td>
                <td className={styles.number}>{course.credits}</td>
                <td className={`${styles.number} ${styles[badge] ?? ''}`}>{formatRate(course.achievementRate)}</td>
                <td className={styles.status}>
                  <StatusBadge kind={badge} />
                </td>
              </tr>
            );
          })}
        </tbody>
        <tfoot>
          <tr>
            <td colSpan={4} className={styles.muted}>
              학기 합계
            </td>
            <td className={styles.total}>{data.totalCredits}학점</td>
          </tr>
        </tfoot>
      </table>
    </div>
  );
}
