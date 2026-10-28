/// <reference types="node" />
// docs/api-contract.md의 요청·응답 예시가 mock의 실제 동작과 같은지 확인한다.
// 문서의 <!-- contract-request:id --> 요청 예시를 그대로 보내고, <!-- contract:id 상태코드 --> 응답 예시와 상태 코드·키·값을 비교한다.
import { readFileSync } from 'node:fs';
import { describe, expect, it } from 'vitest';

import { CONTRACT_STEPS, runContractStep } from './contractScenario';

const doc = readFileSync(new URL('../../../docs/api-contract.md', import.meta.url), 'utf-8').replace(/\r\n/g, '\n');

interface DocResponse {
  status: number;
  // 문서에 JSON 예시가 없으면 undefined (204, 또는 상태만 되돌리는 단계)
  body?: unknown;
}

const responses = new Map<string, DocResponse[]>();
for (const match of doc.matchAll(/<!-- contract:([\w-]+) (\d+) -->(\s*```json\n([\s\S]*?)\n```)?/g)) {
  const list = responses.get(match[1]) ?? [];
  list.push({ status: Number(match[2]), body: match[4] === undefined ? undefined : JSON.parse(match[4]) });
  responses.set(match[1], list);
}

const requests = new Map<string, unknown[]>();
for (const match of doc.matchAll(/<!-- contract-request:([\w-]+) -->\s*```json\n([\s\S]*?)\n```/g)) {
  requests.set(match[1], [...(requests.get(match[1]) ?? []), JSON.parse(match[2])]);
}

// 문서에서 요청 본문을 생략한 단계 (상태만 되돌려 다음 예시의 전제를 맞춘다)
const UNDOCUMENTED_REQUESTS = new Set(['change-status-back']);

describe('API 명세 문서(docs/api-contract.md)와 mock 일치', () => {
  it('문서의 예시 표시와 호출 순서가 1:1로 맞는다', () => {
    const stepIds = CONTRACT_STEPS.map((step) => step.id);
    expect([...responses.keys()].sort()).toEqual([...stepIds].sort());
    for (const [id, list] of responses) expect(list, id).toHaveLength(1);
    for (const [id, list] of requests) {
      expect(stepIds, id).toContain(id);
      expect(list, id).toHaveLength(1);
    }
  });

  // 순서대로 호출해야 상태(등록, 상태 변경, 평가 추가 등)가 문서와 같아진다
  it('순서대로 호출한 응답이 문서 예시와 같다', () => {
    for (const step of CONTRACT_STEPS) {
      const documentedRequest = requests.get(step.id)?.[0];
      if (step.body !== undefined && !UNDOCUMENTED_REQUESTS.has(step.id)) {
        expect(documentedRequest, `${step.id}: 요청 예시가 문서에 없음`).toBeDefined();
        expect(documentedRequest, `${step.id}: 요청 예시`).toEqual(step.body);
      }

      // 문서의 요청 본문을 그대로 보낸다
      const result = runContractStep(documentedRequest === undefined ? step : { ...step, body: documentedRequest });
      const documented = responses.get(step.id)![0];
      expect(result.status, `${step.id}: 상태 코드`).toBe(documented.status);
      if (documented.status === 204) {
        expect(result.body, `${step.id}: 204는 본문 없음`).toBeNull();
        expect(documented.body, `${step.id}: 204 예시에는 JSON이 없어야 함`).toBeUndefined();
      } else if (documented.body !== undefined) {
        expect(result.body, `${step.id}: 응답 예시`).toEqual(documented.body);
      } else {
        expect(UNDOCUMENTED_REQUESTS.has(step.id), `${step.id}: 응답 예시가 문서에 없음`).toBe(true);
      }
    }
  });
});
