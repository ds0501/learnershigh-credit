import { describe, expect, it } from 'vitest';

import { goalChipLabel } from './labels';

describe('희망 학과 칩 문구', () => {
  it('미설정이면 "희망 학과 설정"', () => {
    expect(goalChipLabel(null)).toBe('희망 학과 설정');
  });

  it('학과와 목표 대학', () => {
    expect(goalChipLabel({ department: { id: '12', name: '간호학과', field: '보건·의약학' }, targetUniversity: '서울대학교' })).toBe(
      '희망 학과: 간호학과 · 목표 대학: 서울대학교',
    );
  });

  it('목표 대학이 없으면 학과만', () => {
    expect(goalChipLabel({ department: { id: '12', name: '간호학과', field: null }, targetUniversity: null })).toBe('희망 학과: 간호학과');
  });
});
