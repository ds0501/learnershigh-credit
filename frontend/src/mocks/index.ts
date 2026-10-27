// mock 모드에서 API 경로별로 돌려줄 데이터. 키는 /api 뒤의 경로(예: '/student/credit/summary').
// 실제 시연 데이터는 화면을 만들 때 추가한다.
export const mockHandlers: Record<string, () => unknown> = {};
