import { badgeOf, COURSE_TYPE_LABEL, courseRateText } from '../labels';
import type { SemesterCourses } from '../types';
import { StatusBadge } from './StatusBadge';
import styles from './CourseTable.module.css';

interface CourseTableProps {
  data: SemesterCourses;
  // 행을 누르면 그 과목의 패널(평가 입력, 이수 상태 변경, 과목 삭제)을 연다
  onSelect: (courseId: string) => void;
}

// 선택한 학기의 수강 과목 표
export function CourseTable({ data, onSelect }: CourseTableProps) {
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
            // 모든 행(창체 포함)을 누르면 패널이 열린다. 창체 패널에는 상태 변경과 삭제만 있다
            return (
              <tr
                key={course.id}
                data-course-id={course.id}
                className={badge === 'risk' ? `${styles.riskRow} ${styles.selectable}` : styles.selectable}
                tabIndex={0}
                aria-label={`${course.name} 과목 관리 열기`}
                onClick={() => onSelect(course.id)}
                onKeyDown={(event) => {
                  if (event.key === 'Enter' || event.key === ' ') {
                    event.preventDefault();
                    onSelect(course.id);
                  }
                }}
              >
                <td className={styles.name}>{course.name}</td>
                <td className={styles.muted}>{COURSE_TYPE_LABEL[course.courseType]}</td>
                <td className={styles.number}>{course.credits}</td>
                <td className={`${styles.number} ${styles[badge] ?? ''}`}>{courseRateText(course)}</td>
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
