import { useState } from 'react';

import { ApiError } from '../../api/errors';
import { suggestGuidelinesForDraft, suggestGuidelinesForSubject } from './api';
import type { GuidelineSuggestion, RecordDraft, RecordDraftCard } from './types';
import styles from './Records.module.css';

interface GuidelinePanelProps {
  card: RecordDraftCard;
  // 초안이 아직 없으면 null (과목 정보로 추천받는다)
  draft: RecordDraft | null;
  busy: boolean;
  // 채택. 성공하면 true
  onAdopt: (suggestion: GuidelineSuggestion) => Promise<boolean>;
}

// "방향 추천받기": 3안을 카드로 보여 주고 "이 방향으로 채택"을 누르면 저장한다. 4칸에는 아무것도 채우지 않는다
export function GuidelinePanel({ card, draft, busy, onAdopt }: GuidelinePanelProps) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [suggestions, setSuggestions] = useState<GuidelineSuggestion[] | null>(null);

  const request = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = draft
        ? await suggestGuidelinesForDraft(draft.id)
        : await suggestGuidelinesForSubject({ semester: card.semester, subjectName: card.subjectName, courseId: card.courseId });
      setSuggestions(response.suggestions);
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : '방향을 불러오지 못했습니다. 잠시 후 다시 시도하세요.');
    } finally {
      setLoading(false);
    }
  };

  const adopt = async (suggestion: GuidelineSuggestion) => {
    if (await onAdopt(suggestion)) setSuggestions(null);
  };

  return (
    <div className={styles.guidelinePanel}>
      <div className={styles.guidelineHeader}>
        <span className={styles.guidelineHint}>무엇을 탐구할지 막막하면 방향을 먼저 골라 보세요. 4칸은 직접 써요.</span>
        <button type="button" className={styles.secondaryButton} data-action="suggest-guidelines" disabled={loading || busy} onClick={request}>
          {loading ? '방향을 찾는 중...' : suggestions ? '다시 추천받기' : '방향 추천받기'}
        </button>
      </div>
      {loading && (
        <p className={styles.note} role="status" data-testid="guideline-loading">
          희망 학과와 로드맵을 보고 방향을 찾는 중이에요...
        </p>
      )}
      {error && (
        <p className={styles.error} role="alert">
          {error}
        </p>
      )}
      {suggestions && !loading && (
        <ul className={styles.suggestions} aria-label="방향 추천">
          {suggestions.map((suggestion, index) => (
            <li key={`${index}-${suggestion.title}`} className={styles.suggestion} data-suggestion-index={index}>
              <h3 className={styles.suggestionTitle}>{suggestion.title}</h3>
              <p className={styles.suggestionReason}>{suggestion.reason}</p>
              <span className={styles.suggestionLabel}>탐구·활동 예시</span>
              <ul className={styles.suggestionList}>
                {suggestion.examples.map((example) => (
                  <li key={example}>{example}</li>
                ))}
              </ul>
              <span className={styles.suggestionLabel}>4칸에 적을 때의 포인트</span>
              <ul className={styles.suggestionList}>
                {suggestion.points.map((point) => (
                  <li key={point}>{point}</li>
                ))}
              </ul>
              <button
                type="button"
                className={styles.primaryButton}
                data-action="adopt-guideline"
                disabled={busy}
                onClick={() => adopt(suggestion)}
              >
                이 방향으로 채택
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
