import { useCallback, useEffect, useRef, useState } from 'react';

export type AsyncState<T> =
  | { status: 'loading' }
  | { status: 'success'; data: T }
  | { status: 'error'; error: Error };

const LOADING = { status: 'loading' } as const;

// 비동기 요청 하나의 로딩/성공/오류 상태를 관리한다.
// key가 바뀌거나 reload를 부르면 다시 요청하고, 그 결과가 오기 전까지는 loading이다.
export function useAsync<T>(key: string, load: () => Promise<T>) {
  const loadRef = useRef(load);
  useEffect(() => {
    loadRef.current = load;
  });

  const [attempt, setAttempt] = useState(0);
  const requestKey = `${key}#${attempt}`;
  const [result, setResult] = useState<{ requestKey: string; state: AsyncState<T> } | null>(null);

  useEffect(() => {
    let cancelled = false;
    loadRef.current().then(
      (data) => {
        if (!cancelled) setResult({ requestKey, state: { status: 'success', data } });
      },
      (error: unknown) => {
        if (!cancelled) {
          setResult({
            requestKey,
            state: { status: 'error', error: error instanceof Error ? error : new Error(String(error)) },
          });
        }
      },
    );
    return () => {
      cancelled = true;
    };
  }, [requestKey]);

  const reload = useCallback(() => setAttempt((n) => n + 1), []);
  const state: AsyncState<T> = result?.requestKey === requestKey ? result.state : LOADING;

  return { state, reload };
}
