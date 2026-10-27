import { creditMockHandlers } from './credit';

export type MockParams = Record<string, string | number>;
export type MockHandler = (params: MockParams) => unknown;

// mock 모드에서 API 경로별로 돌려줄 데이터. 키는 /api 뒤의 경로(예: '/student/credit/summary').
export const mockHandlers: Record<string, MockHandler> = {
  ...creditMockHandlers,
};
