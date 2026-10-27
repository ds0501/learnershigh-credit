import type { ReactNode } from 'react';

import styles from './PageHeader.module.css';

interface PageHeaderProps {
  title: string;
  subtitle?: string;
  actions?: ReactNode;
}

// 모듈 페이지 상단 제목 (청록 밑줄)
export function PageHeader({ title, subtitle, actions }: PageHeaderProps) {
  return (
    <div className={styles.header}>
      <h1 className={styles.title}>{title}</h1>
      {subtitle && <span className={styles.subtitle}>{subtitle}</span>}
      {actions && <div className={styles.actions}>{actions}</div>}
    </div>
  );
}
