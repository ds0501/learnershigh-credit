// 시연용 표준 과목 목록 (약 30개).
// 2022 개정 교육과정 과목 이름을 참고해 시연용으로 임의 구성한 것이라 공식 과목 목록·교과(군)과 다를 수 있다.
// 실제 목록(약 290개)의 출처와 이용 조건은 아직 정해지지 않았다(PROGRESS.md "미정").
// 학점은 학교마다 편성이 달라 목록에 두지 않는다. 과목 등록 시 항상 직접 입력한다.
import type { CatalogItem, CourseType } from '../modules/credit/types';

type CatalogSeed = [name: string, subjectGroup: string, courseType: CourseType];

const SEEDS: CatalogSeed[] = [
  // 공통과목
  ['공통국어1', '국어', 'COMMON'],
  ['공통국어2', '국어', 'COMMON'],
  ['공통수학1', '수학', 'COMMON'],
  ['공통수학2', '수학', 'COMMON'],
  ['공통영어1', '영어', 'COMMON'],
  ['공통영어2', '영어', 'COMMON'],
  ['한국사1', '사회', 'COMMON'],
  ['한국사2', '사회', 'COMMON'],
  ['통합사회1', '사회', 'COMMON'],
  ['통합사회2', '사회', 'COMMON'],
  ['통합과학1', '과학', 'COMMON'],
  ['통합과학2', '과학', 'COMMON'],
  ['과학탐구실험1', '과학', 'COMMON'],
  ['과학탐구실험2', '과학', 'COMMON'],
  // 일반선택
  ['화법과 언어', '국어', 'GENERAL'],
  ['대수', '수학', 'GENERAL'],
  ['확률과 통계', '수학', 'GENERAL'],
  ['영어Ⅰ', '영어', 'GENERAL'],
  ['물리학', '과학', 'GENERAL'],
  ['생명과학', '과학', 'GENERAL'],
  ['체육', '체육', 'GENERAL'],
  ['운동과 건강', '체육', 'GENERAL'],
  ['음악', '예술', 'GENERAL'],
  ['미술', '예술', 'GENERAL'],
  // 진로선택
  ['기하', '수학', 'CAREER'],
  ['영미 문학 읽기', '영어', 'CAREER'],
  ['역학과 에너지', '과학', 'CAREER'],
  // 융합선택
  ['사회문제 탐구', '사회', 'FUSION'],
  ['기후변화와 환경생태', '과학', 'FUSION'],
  // 창체
  ['창의적 체험활동', '창의적 체험활동', 'CREATIVE'],
];

export const CATALOG: CatalogItem[] = SEEDS.map(([name, subjectGroup, courseType], index) => ({
  id: String(index + 1),
  name,
  subjectGroup,
  courseType,
}));

const SEARCH_LIMIT = 10;

// 과목명 부분 일치 검색 (공백 무시). 빈 검색어면 빈 목록
export function searchCatalog(query: string): CatalogItem[] {
  const keyword = query.replace(/\s+/g, '');
  if (keyword === '') return [];
  return CATALOG.filter((item) => item.name.replace(/\s+/g, '').includes(keyword)).slice(0, SEARCH_LIMIT);
}

export function findCatalogItem(id: string): CatalogItem | undefined {
  return CATALOG.find((item) => item.id === id);
}
