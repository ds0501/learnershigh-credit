import { describe, expect, it } from 'vitest';

import { CARD_LABEL, cardKind, initialCareerLink, isContentBlank, isEditable } from './labels';

describe('세특 카드 상태 문구', () => {
  it('초안 없음, 작성 중, 제출, 확인 완료, 수정 요청', () => {
    expect(CARD_LABEL[cardKind({ status: 'NONE', source: null })]).toBe('초안 없음');
    expect(CARD_LABEL[cardKind({ status: 'DRAFT', source: 'NEW' })]).toBe('작성 중');
    expect(CARD_LABEL[cardKind({ status: 'SUBMITTED', source: 'NEW' })]).toBe('제출');
    expect(CARD_LABEL[cardKind({ status: 'CONFIRMED', source: 'NEW' })]).toBe('확인 완료');
    expect(CARD_LABEL[cardKind({ status: 'REVISION_REQUESTED', source: 'NEW' })]).toBe('수정 요청');
  });

  it('과거 기록은 "기존 기록"', () => {
    expect(CARD_LABEL[cardKind({ status: 'CONFIRMED', source: 'PAST' })]).toBe('기존 기록');
  });

  it('학생이 고칠 수 있는 상태는 초안 없음, 작성 중, 수정 요청뿐', () => {
    expect(isEditable('NONE')).toBe(true);
    expect(isEditable('DRAFT')).toBe(true);
    expect(isEditable('REVISION_REQUESTED')).toBe(true);
    expect(isEditable('SUBMITTED')).toBe(false);
    expect(isEditable('CONFIRMED')).toBe(false);
  });
});

describe('세특 4칸', () => {
  it('4칸이 모두 공백이면 비어 있음', () => {
    expect(isContentBlank({ motive: ' ', process: '', result: '\n', careerLink: '' })).toBe(true);
    expect(isContentBlank({ motive: '', process: '', result: '배운 점', careerLink: '' })).toBe(false);
  });

  it('진로 연결이 비어 있으면 희망 학과 이름을 기본으로', () => {
    expect(initialCareerLink('', '간호학과')).toBe('간호학과');
    expect(initialCareerLink('  ', '간호학과')).toBe('간호학과');
    expect(initialCareerLink('이미 쓴 내용', '간호학과')).toBe('이미 쓴 내용');
    expect(initialCareerLink('', null)).toBe('');
  });
});
