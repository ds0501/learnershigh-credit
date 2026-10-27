import { PageHeader } from './PageHeader';
import styles from './ComingSoonPage.module.css';

interface ComingSoonPageProps {
  title: string;
}

// 아직 구현하지 않은 모듈의 빈 페이지
export function ComingSoonPage({ title }: ComingSoonPageProps) {
  return (
    <>
      <PageHeader title={title} />
      <section className={styles.panel}>
        <p className={styles.message}>준비 중입니다.</p>
      </section>
    </>
  );
}
