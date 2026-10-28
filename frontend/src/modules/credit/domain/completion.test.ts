import { describe, expect, it } from 'vitest';

import { achievementRate, attendanceRate, enteredWeight, evaluateCourse, requiredAverage, riskLevel } from './completion';
import type { ScoredAssessment } from './types';
import { validateAssessment } from './validation';

const performance = (weightPercent: number, score: number | null, maxScore = 100): ScoredAssessment => ({
  kind: 'PERFORMANCE',
  weightPercent,
  maxScore,
  score,
});
const written = (weightPercent: number, score: number | null, maxScore = 100): ScoredAssessment => ({
  kind: 'WRITTEN',
  weightPercent,
  maxScore,
  score,
});
const fullAttendance = { totalSessions: null, absentCount: 0 };

// PRD 6장 테스트 케이스
describe('PRD 6장 테스트 케이스', () => {
  it('1. 공통수학2(지필 60%, 수행 40%), 수행평가 1건(반영 20%, 100점 만점) 30점 → 성취율 30%, 이수 위험', () => {
    const result = evaluateCourse({ courseType: 'COMMON', assessments: [performance(20, 30)], attendance: fullAttendance });
    expect(result.achievementRate).toBeCloseTo(30);
    expect(result.riskLevel).toBe('RISK');
  });

  it('2. 위 상태에서 수행평가 1건 추가(반영 20%, 100점 만점) 60점 → 성취율 45%, 주의', () => {
    const result = evaluateCourse({
      courseType: 'COMMON',
      assessments: [performance(20, 30), performance(20, 60)],
      attendance: fullAttendance,
    });
    expect(result.achievementRate).toBeCloseTo(45);
    expect(result.riskLevel).toBe('CAUTION');
  });

  it('3. 평가 입력 없음 → 성취율 계산 안 함, 미입력', () => {
    const result = evaluateCourse({ courseType: 'COMMON', assessments: [], attendance: fullAttendance });
    expect(result.achievementRate).toBeNull();
    expect(result.riskLevel).toBe('NO_DATA');
  });

  it('4. 평가 2건 중 1건 득점 NULL → NULL 건 제외하고 계산', () => {
    const assessments = [written(30, 80), performance(20, null)];
    expect(achievementRate(assessments)).toBeCloseTo(80);
    expect(enteredWeight(assessments)).toBe(30);
  });

  it('5. 입력된 반영비율 합 50%, 현재 성취율 35% → 필요 평균 45%', () => {
    expect(requiredAverage(35, 50, 40)).toBeCloseTo(45);
  });

  it('6. 선택과목, 출석률 60% → 이수 위험', () => {
    const attendance = attendanceRate({ totalSessions: 50, absentCount: 20 });
    expect(attendance).toBeCloseTo(60);
    expect(riskLevel({ courseType: 'GENERAL', achievementRate: 90, attendanceRate: attendance })).toBe('RISK');
  });

  it('7. 수행 100% 과목(지필 0%), 수행 평가 점수 입력 → 정상 계산', () => {
    const result = evaluateCourse({
      courseType: 'COMMON',
      assessments: [performance(50, 90), performance(50, 43, 50)],
      attendance: fullAttendance,
    });
    expect(result.achievementRate).toBeCloseTo(88);
    expect(result.riskLevel).toBe('NORMAL');
  });
});

describe('추가 규칙', () => {
  it('반올림 전 값으로 판정한다 (39.6%는 표시가 40%여도 이수 위험)', () => {
    expect(riskLevel({ courseType: 'COMMON', achievementRate: 39.6, attendanceRate: 100 })).toBe('RISK');
    expect(riskLevel({ courseType: 'COMMON', achievementRate: 49.9, attendanceRate: 100 })).toBe('CAUTION');
    expect(riskLevel({ courseType: 'COMMON', achievementRate: 50, attendanceRate: 100 })).toBe('NORMAL');
  });

  it('선택과목은 성취율이 낮아도 출석률이 기준 이상이면 이수 위험이 아니다', () => {
    expect(riskLevel({ courseType: 'GENERAL', achievementRate: 20, attendanceRate: 100 })).toBe('NORMAL');
  });

  it('창체는 평가 없이 출석률로만 판정한다', () => {
    expect(evaluateCourse({ courseType: 'CREATIVE', assessments: [], attendance: fullAttendance }).riskLevel).toBe('NORMAL');
  });

  it('총 수업 수를 모르면 출석률 100%로 가정한다', () => {
    expect(attendanceRate({ totalSessions: null, absentCount: 3 })).toBe(100);
  });

  it('남은 반영비율이 0이면 필요 평균을 계산하지 않는다', () => {
    expect(requiredAverage(30, 100, 40)).toBeNull();
  });

  it('필요 평균이 100을 넘으면 그대로 돌려준다', () => {
    expect(requiredAverage(10, 80, 40)).toBeCloseTo(160);
  });

  it('이수 위험 과목에만 필요 평균을 붙인다', () => {
    const risk = evaluateCourse({
      courseType: 'COMMON',
      assessments: [written(30, 25), performance(20, 50)],
      attendance: fullAttendance,
    });
    expect(risk.achievementRate).toBeCloseTo(35);
    expect(risk.requiredAverage).toBeCloseTo(45);

    const caution = evaluateCourse({ courseType: 'COMMON', assessments: [written(30, 46)], attendance: fullAttendance });
    expect(caution.riskLevel).toBe('CAUTION');
    expect(caution.requiredAverage).toBeNull();
  });
});

describe('평가 입력 검증 (PRD F3)', () => {
  const weights = { writtenWeight: 60, performanceWeight: 40 };
  const existing = [
    { id: '1', kind: 'WRITTEN' as const, weightPercent: 30 },
    { id: '2', kind: 'PERFORMANCE' as const, weightPercent: 20 },
  ];
  const draft = {
    kind: 'PERFORMANCE' as const,
    title: '수행평가 2',
    weightPercent: 20,
    maxScore: 100,
    score: 80,
    evaluatedOn: '2026-10-28',
  };

  it('올바른 입력은 오류가 없다', () => {
    expect(validateAssessment(draft, weights, existing)).toEqual({});
  });

  it('득점이 만점보다 크면 오류', () => {
    expect(validateAssessment({ ...draft, score: 120 }, weights, existing).score).toBeDefined();
  });

  it('득점이 음수면 오류', () => {
    expect(validateAssessment({ ...draft, score: -1 }, weights, existing).score).toBeDefined();
  });

  it('득점을 비우면(미입력) 통과', () => {
    expect(validateAssessment({ ...draft, score: null }, weights, existing)).toEqual({});
  });

  it('같은 종류의 반영 비율 합이 그 종류의 반영 비율을 넘으면 오류', () => {
    expect(validateAssessment({ ...draft, weightPercent: 21 }, weights, existing).weightPercent).toBeDefined();
  });

  it('수정할 때는 자기 자신의 반영 비율을 빼고 센다', () => {
    expect(validateAssessment({ ...draft, weightPercent: 40 }, weights, existing, '2')).toEqual({});
  });

  it('수행 100% 과목은 지필 평가를 넣을 수 없다', () => {
    const errors = validateAssessment({ ...draft, kind: 'WRITTEN' }, { writtenWeight: 0, performanceWeight: 100 }, []);
    expect(errors.weightPercent).toBeDefined();
  });
});
