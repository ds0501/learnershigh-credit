import { BADGE_LABEL, type BadgeKind } from '../labels';
import styles from './StatusBadge.module.css';

export function StatusBadge({ kind }: { kind: BadgeKind }) {
  return <span className={`${styles.badge} ${styles[kind]}`}>{BADGE_LABEL[kind]}</span>;
}
