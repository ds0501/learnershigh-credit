import { describe, expect, it } from 'vitest';

import { canWithdraw, CARD_LABEL, cardKind, guidelineDetailText, initialCareerLink, isContentBlank, isEditable } from './labels';

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

describe('제출 취소', () => {
  it('제출 상태에서만 제출 취소할 수 있다(확인 완료·기존 기록·작성 중·수정 요청은 안 됨)', () => {
    expect(canWithdraw('SUBMITTED')).toBe(true);
    expect(canWithdraw('CONFIRMED')).toBe(false);
    expect(canWithdraw('DRAFT')).toBe(false);
    expect(canWithdraw('REVISION_REQUESTED')).toBe(false);
    expect(canWithdraw('NONE')).toBe(false);
  });
});

describe('채택한 방향', () => {
  it('이유, 탐구·활동 예시, 4칸 포인트를 참고용 글로 만든다', () => {
    expect(
      guidelineDetailText({
        title: '실험 설계 깊게 파기',
        reason: '핵심 과목이에요.',
        examples: ['변인 통제 실험', '오차 분석'],
        points: ['탐구 동기: 궁금했던 장면', '활동 과정: 바꾼 방법'],
      }),
    ).toBe('핵심 과목이에요.\n\n탐구·활동 예시\n- 변인 통제 실험\n- 오차 분석\n\n4칸에 적을 때의 포인트\n- 탐구 동기: 궁금했던 장면\n- 활동 과정: 바꾼 방법');
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
