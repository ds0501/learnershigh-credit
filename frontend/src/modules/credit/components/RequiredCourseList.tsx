import { badgeOf } from '../labels';
import type { RequiredCourses } from '../types';
import { Disclaimer } from './Disclaimer';
import { StatusBadge } from './StatusBadge';
import styles from './RequiredCourseList.module.css';

// 남은 필수 이수 과목 체크리스트
export function RequiredCourseList({ data }: { data: RequiredCourses }) {
  if (data.courses.length === 0) {
    return <p className={styles.empty}>남은 필수 과목이 없습니다.</p>;
  }

  return (
    <>
      <ul className={styles.list}>
        {data.courses.map((course) => (
          <li key={course.id} className={styles.item}>
            <span className={styles.name}>{course.name}</span>
            <StatusBadge kind={badgeOf(course.status, course.riskLevel)} />
          </li>
        ))}
      </ul>
      <Disclaimer />
    </>
  );
}
