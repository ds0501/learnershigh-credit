import { describe, expect, it } from 'vitest';

import type { CourseDetail, CourseItem, CreditSummary, RequiredCourses, SemesterCourses } from '../modules/credit/types';
import { creditMockHandlers } from './credit';

const call = <T>(key: string, request: Partial<Parameters<(typeof creditMockHandlers)[string]>[0]> = {}) =>
  creditMockHandlers[key]({ params: {}, body: undefined, pathParams: {}, ...request }) as T;

const semester = (gradeYear: number, term: number) =>
  call<SemesterCourses>('GET /student/credit/courses', { params: { gradeYear, term } });
const byName = (courses: CourseItem[], name: string) => courses.find((course) => course.name === name)!;

// mock 시연 데이터가 PRD 9장 숫자를 평가 점수에서 계산해 내는지 확인한다
describe('mock 시연 데이터', () => {
  it('요약: 32 / 192, 교과 29 / 174, 창체 3 / 18, 남은 128, 위험 1 · 주의 1', () => {
    const summary = call<CreditSummary>('GET /student/credit/summary');
    expect(summary).toEqual({
      graduationCredits: 192,
      completedCredits: 32,
      inProgressCredits: 32,
      remainingCredits: 128,
      subjectCredits: { earned: 29, required: 174 },
      creativeCredits: { earned: 3, required: 18 },
      alert: { riskCount: 1, cautionCount: 1 },
    });
  });

  it('1학년 2학기 과목별 성취율과 상태', () => {
    const { courses, totalCredits } = semester(1, 2);
    expect(totalCredits).toBe(32);
    const expected: [string, number, string][] = [
      ['공통국어2', 78, 'NORMAL'],
      ['공통수학2', 46, 'CAUTION'],
      ['공통영어2', 72, 'NORMAL'],
      ['한국사2', 81, 'NORMAL'],
      ['통합사회2', 69, 'NORMAL'],
      ['통합과학2', 35, 'RISK'],
      ['과학탐구실험2', 88, 'NORMAL'],
      ['운동과 건강', 85, 'NORMAL'],
      ['음악', 79, 'NORMAL'],
    ];
    for (const [name, rate, level] of expected) {
      const course = byName(courses, name);
      expect(course.achievementRate, name).toBeCloseTo(rate);
      expect(course.riskLevel, name).toBe(level);
    }
    expect(byName(courses, '통합과학2').requiredAverage).toBeCloseTo(45);
    expect(byName(courses, '창의적 체험활동').achievementRate).toBeNull();
  });

  it('1학년 1학기: 모두 이수 완료, 합계 32학점, 교과 29 + 창체 3, 일반선택 2과목', () => {
    const { courses, totalCredits } = semester(1, 1);
    expect(totalCredits).toBe(32);
    expect(courses.every((course) => course.status === 'COMPLETED')).toBe(true);
    expect(courses.filter((course) => course.courseType === 'GENERAL')).toHaveLength(2);
    const creative = courses.filter((course) => course.courseType === 'CREATIVE');
    expect(creative.reduce((total, course) => total + course.credits, 0)).toBe(3);
  });

  it('남은 필수 과목은 1학년 2학기 공통과목 7개', () => {
    expect(call<RequiredCourses>('GET /student/credit/required-courses').remainingCount).toBe(7);
  });

  // 저장소를 바꾸므로 맨 마지막에 둔다
  it('통합과학2에 수행 20% 80점을 추가하면 48% 주의, 배너 위험 0 · 주의 2', () => {
    const scienceId = byName(semester(1, 2).courses, '통합과학2').id;
    const input = { kind: 'PERFORMANCE', title: '수행평가 2', weightPercent: 20, maxScore: 100, evaluatedOn: '2026-10-28' };

    expect(() =>
      call('POST /student/credit/courses/:courseId/assessments', { pathParams: { courseId: scienceId }, body: { ...input, score: 120 } }),
    ).toThrow('입력값을 확인하세요.');

    call('POST /student/credit/courses/:courseId/assessments', { pathParams: { courseId: scienceId }, body: { ...input, score: 80 } });
    const science = byName(semester(1, 2).courses, '통합과학2');
    expect(science.achievementRate).toBeCloseTo(47.857, 2);
    expect(Math.round(science.achievementRate!)).toBe(48);
    expect(science.riskLevel).toBe('CAUTION');
    expect(call<CreditSummary>('GET /student/credit/summary').alert).toEqual({ riskCount: 0, cautionCount: 2 });
  });

  // 바로 위 테스트에서 추가한 평가를 지운다
  it('통합과학2에서 수행평가 2를 삭제하면 48% → 35% 이수 위험, 필요 평균 45로 돌아온다', () => {
    const science = byName(semester(1, 2).courses, '통합과학2');
    const detail = call<CourseDetail>('GET /student/credit/courses/:courseId', { pathParams: { courseId: science.id } });
    const added = detail.assessments.find((assessment) => assessment.title === '수행평가 2')!;

    call('DELETE /student/credit/courses/:courseId/assessments/:assessmentId', {
      pathParams: { courseId: science.id, assessmentId: added.id },
    });
    const after = byName(semester(1, 2).courses, '통합과학2');
    expect(after.achievementRate).toBeCloseTo(35);
    expect(after.riskLevel).toBe('RISK');
    expect(after.requiredAverage).toBeCloseTo(45);
    expect(call<CreditSummary>('GET /student/credit/summary').alert).toEqual({ riskCount: 1, cautionCount: 1 });
  });

  it('다른 과목의 평가 ID로는 삭제할 수 없다', () => {
    const [english, science] = ['공통영어2', '통합과학2'].map((name) => byName(semester(1, 2).courses, name));
    const scienceAssessment = call<CourseDetail>('GET /student/credit/courses/:courseId', { pathParams: { courseId: science.id } })
      .assessments[0];
    expect(() =>
      call('DELETE /student/credit/courses/:courseId/assessments/:assessmentId', {
        pathParams: { courseId: english.id, assessmentId: scienceAssessment.id },
      }),
    ).toThrow('평가를 찾을 수 없습니다.');
  });

  it('마지막 평가까지 삭제하면 성취율은 미입력(null), 상태는 NO_DATA', () => {
    const english = byName(semester(1, 2).courses, '공통영어2');
    const detail = call<CourseDetail>('GET /student/credit/courses/:courseId', { pathParams: { courseId: english.id } });
    for (const assessment of detail.assessments) {
      call('DELETE /student/credit/courses/:courseId/assessments/:assessmentId', {
        pathParams: { courseId: english.id, assessmentId: assessment.id },
      });
    }
    const after = byName(semester(1, 2).courses, '공통영어2');
    expect(after.achievementRate).toBeNull();
    expect(after.riskLevel).toBe('NO_DATA');
  });
});
