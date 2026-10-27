import { PageHeader } from '../../layout/PageHeader';
import styles from './CreditPage.module.css';

// 고교학점제 관리 > 학점 현황. 화면 내용은 다음 단계에서 채운다.
export function CreditPage() {
  return (
    <>
      <PageHeader title="학점 현황" subtitle="1학년 2학기" />
      <section className={styles.placeholder} aria-label="학점 현황">
        <p className={styles.message}>학점 현황 화면이 들어올 자리입니다.</p>
      </section>
    </>
  );
}
