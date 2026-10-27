import { TERMS } from '../labels';
import styles from './SemesterTabs.module.css';

interface SemesterTabsProps {
  selectedKey: string;
  onSelect: (key: string) => void;
}

// 학기 탭 (1-1 ~ 3-2)
export function SemesterTabs({ selectedKey, onSelect }: SemesterTabsProps) {
  return (
    <div className={styles.tabs} role="tablist" aria-label="학기 선택">
      {TERMS.map((option) => {
        const selected = option.key === selectedKey;
        return (
          <button
            key={option.key}
            type="button"
            role="tab"
            aria-selected={selected}
            data-term={option.key}
            className={selected ? `${styles.tab} ${styles.selected}` : styles.tab}
            onClick={() => onSelect(option.key)}
          >
            {option.label}
          </button>
        );
      })}
    </div>
  );
}
