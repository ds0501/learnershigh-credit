import { useCallback, useEffect, useRef, useState } from 'react';

export type AsyncState<T> =
  | { status: 'loading' }
  | { status: 'success'; data: T }
  | { status: 'error'; error: Error };

const LOADING = { status: 'loading' } as const;

// 비동기 요청 하나의 로딩/성공/오류 상태를 관리한다.
// key가 바뀌면 다시 요청하고, 그 결과가 오기 전까지는 loading이다.
// reload는 같은 key로 다시 요청한다. 이전에 성공한 데이터가 있으면 새 결과가 올 때까지 그대로 보여준다.
export function useAsync<T>(key: string, load: () => Promise<T>) {
  const loadRef = useRef(load);
  useEffect(() => {
    loadRef.current = load;
  });

  const [attempt, setAttempt] = useState(0);
  const requestKey = `${key}#${attempt}`;
  const [result, setResult] = useState<{ key: string; requestKey: string; state: AsyncState<T> } | null>(null);

  useEffect(() => {
    let cancelled = false;
    loadRef.current().then(
      (data) => {
        if (!cancelled) setResult({ key, requestKey, state: { status: 'success', data } });
      },
      (error: unknown) => {
        if (!cancelled) {
          setResult({
            key,
            requestKey,
            state: { status: 'error', error: error instanceof Error ? error : new Error(String(error)) },
          });
        }
      },
    );
    return () => {
      cancelled = true;
    };
  }, [key, requestKey]);

  const reload = useCallback(() => setAttempt((n) => n + 1), []);
  const current = result?.requestKey === requestKey;
  const refreshing = result?.key === key && result.state.status === 'success';
  const state: AsyncState<T> = result && (current || refreshing) ? result.state : LOADING;

  return { state, reload };
}
