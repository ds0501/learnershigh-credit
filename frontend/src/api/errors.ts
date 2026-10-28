// API 오류. 입력 검증 오류면 fieldErrors에 항목별 문구가 들어 있다
// (실제 서버의 오류 응답 형식은 아직 정하지 않았다)
export class ApiError extends Error {
  readonly status: number;
  readonly fieldErrors: Record<string, string>;

  constructor(status: number, message: string, fieldErrors: Record<string, string> = {}) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}
