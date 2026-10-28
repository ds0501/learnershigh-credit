import { describe, expect, it } from 'vitest';

import { setWrittenWeight, validateCourseDraft, type CourseDraft } from './courseRegistration';

const draft: CourseDraft = {
  gradeYear: 1,
  term: 2,
  name: '확률과 통계',
  courseType: 'GENERAL',
  credits: 4,
  status: 'IN_PROGRESS',
  writtenWeight: 60,
  performanceWeight: 40,
};

describe('지필/수행 비율 연동', () => {
  it('지필 70이면 수행 30', () => {
    expect(setWrittenWeight(70)).toEqual({ writtenWeight: 70, performanceWeight: 30 });
  });

  it('지필 0이면 수행 100 (수행 100% 과목)', () => {
    expect(setWrittenWeight(0)).toEqual({ writtenWeight: 0, performanceWeight: 100 });
  });

  it('범위를 넘으면 0~100으로 맞추고 정수로 반올림한다', () => {
    expect(setWrittenWeight(130)).toEqual({ writtenWeight: 100, performanceWeight: 0 });
    expect(setWrittenWeight(-10)).toEqual({ writtenWeight: 0, performanceWeight: 100 });
    expect(setWrittenWeight(62.6)).toEqual({ writtenWeight: 63, performanceWeight: 37 });
    expect(setWrittenWeight(Number.NaN)).toEqual({ writtenWeight: 0, performanceWeight: 100 });
  });

  it('합계는 항상 100', () => {
    for (const value of [-5, 0, 10, 35, 60, 99, 100, 250]) {
      const { writtenWeight, performanceWeight } = setWrittenWeight(value);
      expect(writtenWeight + performanceWeight).toBe(100);
    }
  });
});

describe('과목 등록 검증 (PRD F2)', () => {
  it('올바른 입력은 오류가 없다', () => {
    expect(validateCourseDraft(draft, ['공통국어2', '공통수학2'])).toEqual({});
  });

  it('과목명은 필수', () => {
    expect(validateCourseDraft({ ...draft, name: '   ' }, []).name).toBe('과목명을 입력하세요.');
  });

  it('같은 학기에 같은 과목은 중복 등록할 수 없다 (공백·대소문자 무시)', () => {
    expect(validateCourseDraft({ ...draft, name: '공통국어2' }, ['공통국어2']).name).toBe('같은 학기에 이미 등록된 과목입니다.');
    expect(validateCourseDraft({ ...draft, name: '확률과통계' }, ['확률과 통계']).name).toBeDefined();
    expect(validateCourseDraft({ ...draft, name: 'ai 기초' }, ['AI 기초']).name).toBeDefined();
  });

  it('학점은 1 이상 정수', () => {
    expect(validateCourseDraft({ ...draft, credits: 0 }, []).credits).toBeDefined();
    expect(validateCourseDraft({ ...draft, credits: 2.5 }, []).credits).toBeDefined();
    expect(validateCourseDraft({ ...draft, credits: 1 }, [])).toEqual({});
  });

  it('지필 + 수행 반영 비율 합은 100', () => {
    expect(validateCourseDraft({ ...draft, writtenWeight: 60, performanceWeight: 50 }, []).writtenWeight).toBeDefined();
    expect(validateCourseDraft({ ...draft, writtenWeight: 0, performanceWeight: 100 }, [])).toEqual({});
  });

  it('창체는 반영 비율을 보지 않는다', () => {
    const creative = { ...draft, name: '창의적 체험활동', courseType: 'CREATIVE' as const, writtenWeight: 0, performanceWeight: 0 };
    expect(validateCourseDraft(creative, [])).toEqual({});
  });

  it('학기는 1~3학년, 1~2학기', () => {
    expect(validateCourseDraft({ ...draft, gradeYear: 4 }, []).term).toBeDefined();
    expect(validateCourseDraft({ ...draft, term: 3 }, []).term).toBeDefined();
  });
});
