import { DISCLAIMER } from '../labels';
import styles from './Disclaimer.module.css';

// 위험 표시 근처에 항상 두는 안내 문구
export function Disclaimer() {
  return <p className={styles.disclaimer}>※ {DISCLAIMER}</p>;
}
