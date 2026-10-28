import { describe, expect, it } from 'vitest';

import type { CatalogItem, CourseItem, CourseRegistrationInput, CreditSummary, RequiredCourses, SemesterCourses } from '../modules/credit/types';
import { creditMockHandlers } from './credit';

// 이 파일은 mock 저장소에 과목을 등록한다. 테스트 순서대로 상태가 쌓인다
const call = <T>(key: string, request: Partial<Parameters<(typeof creditMockHandlers)[string]>[0]> = {}) =>
  creditMockHandlers[key]({ params: {}, body: undefined, pathParams: {}, ...request }) as T;

const summary = () => call<CreditSummary>('GET /student/credit/summary');
const semester = (gradeYear: number, term: number) =>
  call<SemesterCourses>('GET /student/credit/courses', { params: { gradeYear, term } });
const required = () => call<RequiredCourses>('GET /student/credit/required-courses');
const catalog = (query: string) => call<CatalogItem[]>('GET /student/credit/catalog', { params: { query } });
const register = (input: Partial<CourseRegistrationInput>) =>
  call<CourseItem>('POST /student/credit/courses', {
    body: {
      gradeYear: 1,
      term: 2,
      catalogId: null,
      credits: 4,
      status: 'IN_PROGRESS',
      writtenWeight: 60,
      performanceWeight: 40,
      gradeDisplay: 'ACHIEVEMENT_AND_RANK',
      ...input,
    },
  });
const catalogId = (name: string) => catalog(name).find((item) => item.name === name)!.id;

describe('과목 목록 검색', () => {
  it('"공통국어"로 공통국어1, 공통국어2가 나온다', () => {
    expect(catalog('공통국어').map((item) => item.name)).toEqual(['공통국어1', '공통국어2']);
  });

  it('부분 일치, 공백 무시', () => {
    expect(catalog('통계').map((item) => item.name)).toEqual(['확률과 통계']);
    expect(catalog('확률과통계').map((item) => item.name)).toEqual(['확률과 통계']);
  });

  it('빈 검색어는 빈 목록', () => {
    expect(catalog('  ')).toEqual([]);
  });
});

describe('과목 등록 후 다시 계산', () => {
  it('1학년 2학기에 목록 과목(수강 중) 등록 → 수강 중 +4, 남은 학점 -4, 표에 미입력으로 추가', () => {
    const created = register({ catalogId: catalogId('확률과 통계') });
    expect(created).toMatchObject({ name: '확률과 통계', courseType: 'GENERAL', credits: 4, status: 'IN_PROGRESS' });
    expect(created.achievementRate).toBeNull();
    expect(created.riskLevel).toBe('NO_DATA');

    expect(summary()).toMatchObject({ completedCredits: 32, inProgressCredits: 36, remainingCredits: 124 });
    const { courses, totalCredits } = semester(1, 2);
    expect(totalCredits).toBe(36);
    expect(courses.map((course) => course.name)).toContain('확률과 통계');
  });

  it('목록 과목도 학점은 요청 값을 쓴다', () => {
    const created = register({ catalogId: catalogId('기하'), credits: 3 });
    expect(created.credits).toBe(3);
    expect(summary()).toMatchObject({ inProgressCredits: 39, remainingCredits: 121 });
  });

  it('공통과목(수강 중) 등록 → 남은 필수 과목에 포함', () => {
    register({ gradeYear: 2, term: 1, catalogId: catalogId('공통국어1') });
    const names = required().courses.map((course) => course.name);
    expect(required().remainingCount).toBe(8);
    expect(names).toContain('공통국어1');
  });

  it('공통과목(이수 완료) 등록 → 이수 완료·교과 학점 증가, 남은 필수 과목에서 제외', () => {
    register({ gradeYear: 2, term: 2, catalogId: catalogId('공통영어1'), status: 'COMPLETED' });
    expect(summary()).toMatchObject({
      completedCredits: 36,
      inProgressCredits: 43,
      remainingCredits: 113,
      subjectCredits: { earned: 33, required: 174 },
    });
    expect(required().remainingCount).toBe(8);
  });

  it('창체(이수 완료) 등록 → 창체 학점 증가, 반영 비율은 0/0', () => {
    register({ gradeYear: 2, term: 2, catalogId: catalogId('창의적 체험활동'), credits: 3, status: 'COMPLETED', gradeDisplay: null });
    expect(summary().creativeCredits).toEqual({ earned: 6, required: 18 });
    const created = semester(2, 2).courses.find((course) => course.name === '창의적 체험활동')!;
    const detail = call<{ writtenWeight: number; performanceWeight: number }>('GET /student/credit/courses/:courseId', {
      pathParams: { courseId: created.id },
    });
    expect(detail).toMatchObject({ writtenWeight: 0, performanceWeight: 0 });
  });

  it('직접 입력 과목 등록 (수행 100%)', () => {
    const created = register({ catalogId: null, customName: 'AI 기초', courseType: 'CAREER', credits: 2, writtenWeight: 0, performanceWeight: 100 });
    expect(created).toMatchObject({ name: 'AI 기초', courseType: 'CAREER', credits: 2 });
  });
});

describe('과목 등록 검증 (400 { message, fieldErrors })', () => {
  const fieldErrorsOf = (input: Partial<CourseRegistrationInput>) => {
    try {
      register(input);
    } catch (error) {
      return { message: (error as Error).message, status: (error as { status: number }).status, fieldErrors: (error as { fieldErrors: Record<string, string> }).fieldErrors };
    }
    throw new Error('오류가 나지 않음');
  };

  it('같은 학기에 같은 과목 중복 등록 불가 (목록 과목, 직접 입력 모두)', () => {
    expect(fieldErrorsOf({ catalogId: catalogId('공통국어2') })).toEqual({
      message: '입력값을 확인하세요.',
      status: 400,
      fieldErrors: { name: '같은 학기에 이미 등록된 과목입니다.' },
    });
    expect(fieldErrorsOf({ customName: '공통 국어2', courseType: 'COMMON' }).fieldErrors.name).toBe('같은 학기에 이미 등록된 과목입니다.');
  });

  it('같은 과목이라도 다른 학기에는 등록할 수 있다', () => {
    expect(register({ gradeYear: 3, term: 1, catalogId: catalogId('공통국어2') }).name).toBe('공통국어2');
  });

  it('과목명 필수, 학점 1 이상, 유형 필수', () => {
    const { fieldErrors } = fieldErrorsOf({ customName: '', credits: 0 });
    expect(fieldErrors).toMatchObject({
      name: '과목명을 입력하세요.',
      credits: '학점은 1 이상의 정수여야 합니다.',
      courseType: '과목 유형을 선택하세요.',
    });
  });

  it('실패하면 저장하지 않는다', () => {
    const before = semester(1, 2).courses.length;
    fieldErrorsOf({ catalogId: catalogId('공통국어2') });
    expect(semester(1, 2).courses.length).toBe(before);
  });
});
