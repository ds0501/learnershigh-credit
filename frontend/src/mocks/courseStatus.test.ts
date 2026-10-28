import { describe, expect, it } from 'vitest';

import type { CourseDetail, CourseItem, CreditSummary, RequiredCourses, SemesterCourses } from '../modules/credit/types';
import { creditMockHandlers } from './credit';
import { findAssessment } from './creditStore';

// 이 파일은 mock 저장소의 과목 상태를 바꾸고 과목을 지운다. 테스트 순서대로 상태가 쌓인다
const call = <T>(key: string, request: Partial<Parameters<(typeof creditMockHandlers)[string]>[0]> = {}) =>
  creditMockHandlers[key]({ params: {}, body: undefined, pathParams: {}, ...request }) as T;

const summary = () => call<CreditSummary>('GET /student/credit/summary');
const semester = (gradeYear: number, term: number) =>
  call<SemesterCourses>('GET /student/credit/courses', { params: { gradeYear, term } });
const required = () => call<RequiredCourses>('GET /student/credit/required-courses');
const courseOf = (gradeYear: number, term: number, name: string) =>
  semester(gradeYear, term).courses.find((course) => course.name === name);
const changeStatus = (courseId: string, status: unknown) =>
  call<CourseItem>('PATCH /student/credit/courses/:courseId', { pathParams: { courseId }, body: { status } });
const deleteCourse = (courseId: string) => call<null>('DELETE /student/credit/courses/:courseId', { pathParams: { courseId } });
const errorOf = (run: () => unknown) => {
  try {
    run();
  } catch (error) {
    return error as { status: number; message: string; fieldErrors: Record<string, string> };
  }
  throw new Error('오류가 나지 않음');
};

const INITIAL = {
  completedCredits: 32,
  inProgressCredits: 32,
  remainingCredits: 128,
  subjectCredits: { earned: 29, required: 174 },
  creativeCredits: { earned: 3, required: 18 },
  alert: { riskCount: 1, cautionCount: 1 },
};

describe('과목 이수 상태 변경 (PATCH /courses/{courseId})', () => {
  it('통합과학2 수강 중 → 이수 완료: 이수 완료 +4, 수강 중 −4, 교과 +4, 필수 과목 7 → 6, 배너 위험 0', () => {
    const science = courseOf(1, 2, '통합과학2')!;
    const updated = changeStatus(science.id, 'COMPLETED');
    expect(updated).toMatchObject({ id: science.id, status: 'COMPLETED', riskLevel: 'RISK' });

    expect(summary()).toMatchObject({
      completedCredits: 36,
      inProgressCredits: 28,
      remainingCredits: 128,
      subjectCredits: { earned: 33, required: 174 },
      creativeCredits: { earned: 3, required: 18 },
      // 경고 배너는 수강 중인 과목만 센다
      alert: { riskCount: 0, cautionCount: 1 },
    });
    expect(required().remainingCount).toBe(6);
    expect(required().courses.map((course) => course.name)).not.toContain('통합과학2');
  });

  it('되돌리기(이수 완료 → 수강 중) → 처음 숫자로 돌아온다', () => {
    changeStatus(courseOf(1, 2, '통합과학2')!.id, 'IN_PROGRESS');
    expect(summary()).toMatchObject(INITIAL);
    expect(required().remainingCount).toBe(7);
  });

  it('창체 상태 변경 → 창체 학점이 바뀐다', () => {
    const creative = courseOf(1, 2, '창의적 체험활동')!;
    changeStatus(creative.id, 'COMPLETED');
    expect(summary().creativeCredits).toEqual({ earned: 6, required: 18 });
    changeStatus(creative.id, 'IN_PROGRESS');
    expect(summary().creativeCredits).toEqual({ earned: 3, required: 18 });
  });

  it('잘못된 상태 값은 400, 바뀌지 않음', () => {
    const korean = courseOf(1, 2, '공통국어2')!;
    expect(errorOf(() => changeStatus(korean.id, 'DONE'))).toMatchObject({
      status: 400,
      message: '입력값을 확인하세요.',
      fieldErrors: { status: '이수 상태를 선택하세요.' },
    });
    expect(courseOf(1, 2, '공통국어2')!.status).toBe('IN_PROGRESS');
  });
});

describe('과목 삭제 (DELETE /courses/{courseId})', () => {
  it('등록한 과목을 삭제하면 학점과 필수 과목이 등록 전으로 돌아온다', () => {
    call<CourseItem>('POST /student/credit/courses', {
      body: {
        gradeYear: 2,
        term: 1,
        catalogId: '1',
        credits: 4,
        status: 'IN_PROGRESS',
        writtenWeight: 60,
        performanceWeight: 40,
        gradeDisplay: 'ACHIEVEMENT_AND_RANK',
      },
    });
    const created = courseOf(2, 1, '공통국어1')!;
    expect(summary()).toMatchObject({ inProgressCredits: 36, remainingCredits: 124 });
    expect(required().remainingCount).toBe(8);

    expect(deleteCourse(created.id)).toBeNull();
    expect(semester(2, 1).courses).toEqual([]);
    expect(summary()).toMatchObject(INITIAL);
    expect(required().remainingCount).toBe(7);
  });

  it('과목을 삭제하면 그 과목의 평가도 지워지고 학점·필수 과목·배너가 다시 계산된다', () => {
    const science = courseOf(1, 2, '통합과학2')!;
    const assessmentIds = call<CourseDetail>('GET /student/credit/courses/:courseId', { pathParams: { courseId: science.id } })
      .assessments.map((assessment) => assessment.id);
    expect(assessmentIds.length).toBeGreaterThan(0);

    deleteCourse(science.id);
    expect(courseOf(1, 2, '통합과학2')).toBeUndefined();
    expect(summary()).toMatchObject({ inProgressCredits: 28, remainingCredits: 132, alert: { riskCount: 0, cautionCount: 1 } });
    expect(required().remainingCount).toBe(6);
    // 그 과목의 평가도 저장소에서 지워졌다
    expect(assessmentIds.every((id) => findAssessment(id) === undefined)).toBe(true);
  });

  it('이수 완료 과목 삭제 → 이수 완료·교과 학점이 줄어든다', () => {
    deleteCourse(courseOf(1, 1, '미술')!.id);
    expect(summary()).toMatchObject({ completedCredits: 29, subjectCredits: { earned: 26, required: 174 } });
  });
});

describe('없는 과목은 404', () => {
  it('PATCH', () => {
    expect(errorOf(() => changeStatus('9999', 'COMPLETED'))).toMatchObject({ status: 404, message: '과목을 찾을 수 없습니다.' });
  });

  it('DELETE (이미 삭제한 과목 포함)', () => {
    expect(errorOf(() => deleteCourse('9999')).status).toBe(404);
    const music = courseOf(1, 2, '음악')!;
    deleteCourse(music.id);
    expect(errorOf(() => deleteCourse(music.id)).status).toBe(404);
  });
});
