import styles from './StateMessage.module.css';

interface StateMessageProps {
  kind: 'loading' | 'empty' | 'error';
  message?: string;
  onRetry?: () => void;
}

const DEFAULT_MESSAGE = {
  loading: '불러오는 중입니다...',
  empty: '데이터가 없습니다.',
  error: '데이터를 불러오지 못했습니다.',
};

// 로딩, 비어 있음, 오류 상태 표시
export function StateMessage({ kind, message, onRetry }: StateMessageProps) {
  return (
    <div className={`${styles.state} ${kind === 'error' ? styles.error : ''}`} role={kind === 'error' ? 'alert' : 'status'}>
      <p className={styles.message}>{message ?? DEFAULT_MESSAGE[kind]}</p>
      {onRetry && (
        <button type="button" className={styles.retry} onClick={onRetry}>
          다시 시도
        </button>
      )}
    </div>
  );
}
