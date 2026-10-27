import type { CreditSummary } from '../types';
import styles from './SummaryCard.module.css';

// 졸업 학점 진행바와 교과/창체 학점
export function SummaryCard({ summary }: { summary: CreditSummary }) {
  const { graduationCredits, completedCredits, inProgressCredits, remainingCredits, subjectCredits, creativeCredits } =
    summary;
  const percent = (credits: number) => `${(credits / graduationCredits) * 100}%`;

  return (
    <section className={styles.card} aria-label="졸업 학점 현황">
      <p className={styles.caption}>졸업 기준 {graduationCredits}학점</p>
      <p className={styles.headline}>
        <strong className={styles.big}>{completedCredits}</strong> / {graduationCredits}학점 이수
      </p>

      <div
        className={styles.bar}
        role="img"
        aria-label={`이수 완료 ${completedCredits}학점, 수강 중 ${inProgressCredits}학점, 남은 학점 ${remainingCredits}학점`}
      >
        <span className={styles.barCompleted} style={{ width: percent(completedCredits) }} />
        <span className={styles.barInProgress} style={{ width: percent(inProgressCredits) }} />
      </div>

      <dl className={styles.stats}>
        <div className={styles.stat}>
          <dt>
            <span className={`${styles.swatch} ${styles.swatchCompleted}`} />
            이수 완료
          </dt>
          <dd>{completedCredits}학점</dd>
        </div>
        <div className={styles.stat}>
          <dt>
            <span className={`${styles.swatch} ${styles.swatchInProgress}`} />
            수강 중
          </dt>
          <dd>{inProgressCredits}학점</dd>
        </div>
        <div className={styles.stat}>
          <dt>
            <span className={`${styles.swatch} ${styles.swatchRemaining}`} />
            남은 학점
          </dt>
          <dd>{remainingCredits}학점</dd>
        </div>
      </dl>
      <p className={styles.note}>남은 학점은 수강 중인 과목을 모두 이수한다고 가정한 숫자입니다.</p>

      <dl className={styles.breakdown}>
        <div className={styles.row}>
          <dt>교과</dt>
          <dd>
            {subjectCredits.earned} / {subjectCredits.required}학점
          </dd>
        </div>
        <div className={styles.row}>
          <dt>창의적 체험활동</dt>
          <dd>
            {creativeCredits.earned} / {creativeCredits.required}학점
          </dd>
        </div>
      </dl>
    </section>
  );
}
