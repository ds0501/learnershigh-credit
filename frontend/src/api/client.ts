import { mockHandlers, type MockParams } from '../mocks';

// VITE_USE_MOCK이 'false'일 때만 실제 API를 쓴다 (기본값 true)
export const USE_MOCK = import.meta.env.VITE_USE_MOCK !== 'false';

const API_BASE = '/api';

// mock 응답에도 약간의 지연을 줘서 로딩 상태가 보이게 한다
const MOCK_DELAY_MS = 300;

export type QueryParams = MockParams;

export async function apiGet<T>(path: string, params: QueryParams = {}): Promise<T> {
  if (USE_MOCK) {
    const handler = mockHandlers[path];
    if (!handler) {
      throw new Error(`mock 데이터가 없습니다: ${path}`);
    }
    await new Promise((resolve) => setTimeout(resolve, MOCK_DELAY_MS));
    return handler(params) as T;
  }

  const query = new URLSearchParams(Object.entries(params).map(([key, value]) => [key, String(value)])).toString();
  const response = await fetch(`${API_BASE}${path}${query ? `?${query}` : ''}`, {
    headers: { Accept: 'application/json' },
  });
  if (!response.ok) {
    throw new Error(`API 요청 실패 (${response.status}): ${path}`);
  }
  return (await response.json()) as T;
}
