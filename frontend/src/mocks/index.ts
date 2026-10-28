import { creditMockHandlers } from './credit';

export type MockParams = Record<string, string | number>;

export interface MockRequest {
  params: MockParams;
  body: unknown;
  // 경로의 :이름 자리에 들어온 값
  pathParams: Record<string, string>;
}

export type MockHandler = (request: MockRequest) => unknown;

// mock 모드에서 API별로 돌려줄 응답. 키는 "메서드 /api 뒤의 경로"이고 :이름은 경로 변수다.
// 예: 'GET /student/credit/summary', 'PUT /student/credit/courses/:courseId/assessments/:assessmentId'
export const mockHandlers: Record<string, MockHandler> = {
  ...creditMockHandlers,
};

export function findMockHandler(method: string, path: string) {
  const segments = path.split('/');
  for (const [key, handler] of Object.entries(mockHandlers)) {
    const [keyMethod, keyPath] = key.split(' ');
    const keySegments = keyPath.split('/');
    if (keyMethod !== method || keySegments.length !== segments.length) continue;

    const pathParams: Record<string, string> = {};
    const matched = keySegments.every((segment, index) => {
      if (segment.startsWith(':')) {
        pathParams[segment.slice(1)] = decodeURIComponent(segments[index]);
        return true;
      }
      return segment === segments[index];
    });
    if (matched) return { handler, pathParams };
  }
  return null;
}
