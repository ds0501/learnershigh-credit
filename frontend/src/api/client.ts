import { mockHandlers } from '../mocks';

// VITE_USE_MOCK이 'false'일 때만 실제 API를 쓴다 (기본값 true)
export const USE_MOCK = import.meta.env.VITE_USE_MOCK !== 'false';

const API_BASE = '/api';

export async function apiGet<T>(path: string): Promise<T> {
  if (USE_MOCK) {
    const handler = mockHandlers[path];
    if (!handler) {
      throw new Error(`mock 데이터가 없습니다: ${path}`);
    }
    return handler() as T;
  }

  const response = await fetch(`${API_BASE}${path}`, {
    headers: { Accept: 'application/json' },
  });
  if (!response.ok) {
    throw new Error(`API 요청 실패 (${response.status}): ${path}`);
  }
  return (await response.json()) as T;
}
