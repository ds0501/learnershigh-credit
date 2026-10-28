import { findMockHandler, type MockParams } from '../mocks';
import { ApiError } from './errors';

export { ApiError };

// VITE_USE_MOCK이 'false'일 때만 실제 API를 쓴다 (기본값 true)
export const USE_MOCK = import.meta.env.VITE_USE_MOCK !== 'false';

const API_BASE = '/api';

// mock 응답에도 약간의 지연을 줘서 로딩 상태가 보이게 한다
const MOCK_DELAY_MS = 300;

export type QueryParams = MockParams;
export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';

interface RequestOptions {
  params?: QueryParams;
  body?: unknown;
  // true면 VITE_USE_MOCK과 관계없이 항상 실제 백엔드로 보낸다. 백엔드 API를 하나씩 붙이는 동안 임시로 쓴다
  forceReal?: boolean;
}

export async function apiRequest<T>(
  method: HttpMethod,
  path: string,
  { params = {}, body, forceReal = false }: RequestOptions = {},
): Promise<T> {
  if (USE_MOCK && !forceReal) {
    const match = findMockHandler(method, path);
    if (!match) {
      throw new Error(`mock 데이터가 없습니다: ${method} ${path}`);
    }
    await new Promise((resolve) => setTimeout(resolve, MOCK_DELAY_MS));
    // 실제 네트워크처럼 응답을 복사해서 넘긴다 (mock 저장소의 데이터를 화면이 직접 잡지 않게)
    return structuredClone(match.handler({ params, body, pathParams: match.pathParams })) as T;
  }

  const query = new URLSearchParams(Object.entries(params).map(([key, value]) => [key, String(value)])).toString();
  const response = await fetch(`${API_BASE}${path}${query ? `?${query}` : ''}`, {
    method,
    headers: body === undefined ? { Accept: 'application/json' } : { Accept: 'application/json', 'Content-Type': 'application/json' },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  if (!response.ok) {
    const detail = (await response.json().catch(() => null)) as { message?: string; fieldErrors?: Record<string, string> } | null;
    throw new ApiError(response.status, detail?.message ?? `API 요청 실패 (${response.status}): ${path}`, detail?.fieldErrors);
  }
  // 204(본문 없음)는 undefined로 돌려준다
  if (response.status === 204) return undefined as T;
  return (await response.json()) as T;
}

export function apiGet<T>(path: string, params: QueryParams = {}, forceReal = false) {
  return apiRequest<T>('GET', path, { params, forceReal });
}
