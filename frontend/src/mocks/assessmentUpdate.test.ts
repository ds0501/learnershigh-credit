import { describe, expect, it } from 'vitest';

import type { Assessment, CourseDetail, SemesterCourses } from '../modules/credit/types';
import { creditMockHandlers } from './credit';

// 평가 수정 PUT /courses/{courseId}/assessments/{assessmentId}. 이 파일은 mock 저장소를 바꾼다
const call = <T>(key: string, request: Partial<Parameters<(typeof creditMockHandlers)[string]>[0]> = {}) =>
  creditMockHandlers[key]({ params: {}, body: undefined, pathParams: {}, ...request }) as T;

const PUT = 'PUT /student/credit/courses/:courseId/assessments/:assessmentId';
const courseOf = (name: string) =>
  call<SemesterCourses>('GET /student/credit/courses', { params: { gradeYear: 1, term: 2 } }).courses.find((course) => course.name === name)!;
const detailOf = (courseId: string) => call<CourseDetail>('GET /student/credit/courses/:courseId', { pathParams: { courseId } });
const errorOf = (run: () => unknown) => {
  try {
    run();
  } catch (error) {
    return error as { status: number; message: string; fieldErrors: Record<string, string> };
  }
  throw new Error('오류가 나지 않음');
};

describe('평가 수정 (PUT /courses/{courseId}/assessments/{assessmentId})', () => {
  it('통합과학2 실험 보고서 50점 → 100점: 성취율 35% → 55%, 이수 위험 → 정상', () => {
    const science = courseOf('통합과학2');
    const report = detailOf(science.id).assessments.find((assessment) => assessment.title === '실험 보고서')!;
    const { id: _id, taskId: _taskId, ...input } = report;

    const updated = call<Assessment>(PUT, { pathParams: { courseId: science.id, assessmentId: report.id }, body: { ...input, score: 100 } });
    expect(updated).toMatchObject({ id: report.id, score: 100, taskId: null });

    const after = courseOf('통합과학2');
    expect(after.achievementRate).toBeCloseTo(55);
    expect(after.riskLevel).toBe('NORMAL');
    expect(after.requiredAverage).toBeUndefined();
  });

  it('다른 과목의 평가 ID로 수정하면 404, 바뀌지 않음', () => {
    const english = courseOf('공통영어2');
    const scienceAssessment = detailOf(courseOf('통합과학2').id).assessments[0];
    const body = { kind: 'WRITTEN', title: '중간고사', weightPercent: 30, maxScore: 100, score: 0, evaluatedOn: '2026-10-20' };
    expect(errorOf(() => call(PUT, { pathParams: { courseId: english.id, assessmentId: scienceAssessment.id }, body }))).toMatchObject({
      status: 404,
      message: '평가를 찾을 수 없습니다.',
    });
    expect(detailOf(courseOf('통합과학2').id).assessments[0]).toEqual(scienceAssessment);
  });

  it('없는 과목이면 404', () => {
    expect(errorOf(() => call(PUT, { pathParams: { courseId: '9999', assessmentId: '1' }, body: {} }))).toMatchObject({
      status: 404,
      message: '과목을 찾을 수 없습니다.',
    });
  });

  it('검증 실패(득점 > 만점)는 400 { message, fieldErrors }', () => {
    const korean = courseOf('공통국어2');
    const midterm = detailOf(korean.id).assessments[0];
    const { id: _id, taskId: _taskId, ...input } = midterm;
    expect(errorOf(() => call(PUT, { pathParams: { courseId: korean.id, assessmentId: midterm.id }, body: { ...input, score: 120 } }))).toMatchObject({
      status: 400,
      message: '입력값을 확인하세요.',
      fieldErrors: { score: '득점은 만점(100점) 이하여야 합니다.' },
    });
  });
});
