import { describe, expect, it } from 'vitest';

import { checkSummary, groupSubjects } from './subjectGroups';
import type { DepartmentSubject } from './types';

const subject = (name: string, ratioPercent: number, options: { fullCommon?: boolean; alreadyRegistered?: boolean } = {}): DepartmentSubject => ({
  catalogId: name,
  name,
  courseType: 'GENERAL',
  requiredUniversityCount: Math.round(ratioPercent / 10),
  totalUniversityCount: 10,
  ratioPercent,
  fullCommon: options.fullCommon ?? false,
  designationType: 'RECOMMENDED',
  eduofficeListed: false,
  requiredUniversities: '',
  alreadyRegistered: options.alreadyRegistered ?? false,
});

describe('학과 로드맵 점검 요약', () => {
  it('다수 대학 공통 과목(50% 이상) 중 등록한 과목 수를 센다. 50% 미만 과목은 세지 않는다', () => {
    const groups = groupSubjects([
      subject('미적분Ⅰ', 80, { alreadyRegistered: true }),
      subject('확률과 통계', 50),
      subject('물리학', 60, { alreadyRegistered: true }),
      subject('기하', 49.9, { alreadyRegistered: true }),
    ]);
    expect(groups.majority.map((item) => item.name)).toEqual(['미적분Ⅰ', '확률과 통계', '물리학']);
    expect(checkSummary(groups)).toEqual({ total: 3, registered: 2 });
  });

  it('전체 대학 공통 과목도 N과 M에 포함한다', () => {
    const groups = groupSubjects([
      subject('화학', 100, { fullCommon: true, alreadyRegistered: true }),
      subject('생명과학', 100, { fullCommon: true }),
      subject('대수', 70),
    ]);
    expect(groups.fullCommon).toHaveLength(2);
    expect(groups.majority).toHaveLength(1);
    expect(checkSummary(groups)).toEqual({ total: 3, registered: 1 });
  });

  it('다수 대학 공통 과목이 없으면 N은 0 (화면에서 요약을 숨김)', () => {
    const groups = groupSubjects([subject('기하', 30, { alreadyRegistered: true }), subject('경제', 10)]);
    expect(checkSummary(groups)).toEqual({ total: 0, registered: 0 });
  });
});
