// 고교학점제 mock 메모리 저장소. 과목과 평가 원본만 들고 있고 성취율·상태는 저장하지 않는다.
// 새로고침하면 처음 상태(시연 데이터)로 돌아간다.
import type { AssessmentInput, AssessmentKind, CourseStatus, CourseType } from '../modules/credit/types';

export interface StoredCourse {
  id: string;
  name: string;
  courseType: CourseType;
  credits: number;
  gradeYear: number;
  term: number;
  status: CourseStatus;
  writtenWeight: number;
  performanceWeight: number;
  absentCount: number;
  totalSessions: number | null;
}

export interface StoredAssessment extends AssessmentInput {
  id: string;
  courseId: string;
  taskId: string | null;
}

// 시연 데이터 (PRD 9장). 데모 학생은 1학년 2학기다.
// 성취율은 아래 평가 점수에서 계산되어 1학년 2학기가 PRD 9장 표의 숫자가 되도록 맞췄다.
// 1학년 1학기 과목 이름과 점수는 시연용 임의 값이다 (교과 29 + 창체 3학점).
type CourseSeed = [name: string, courseType: CourseType, credits: number, writtenWeight: number];
// 평가: [종류, 이름, 반영 비율, 득점(null이면 미입력), 날짜]. 만점은 모두 100점
type AssessmentSeed = [kind: AssessmentKind, title: string, weightPercent: number, score: number | null, evaluatedOn: string];

const W = 'WRITTEN';
const P = 'PERFORMANCE';

const SEMESTER_1: [CourseSeed, AssessmentSeed[]][] = [
  [['공통국어1', 'COMMON', 4, 60], [[W, '중간고사', 30, 82, '2026-04-24'], [W, '기말고사', 30, 76, '2026-07-03'], [P, '독서 감상문', 40, 85, '2026-06-12']]],
  [['공통수학1', 'COMMON', 4, 60], [[W, '중간고사', 30, 68, '2026-04-23'], [W, '기말고사', 30, 61, '2026-07-02'], [P, '수학 탐구 보고서', 40, 70, '2026-06-05']]],
  [['공통영어1', 'COMMON', 4, 60], [[W, '중간고사', 30, 74, '2026-04-27'], [W, '기말고사', 30, 79, '2026-07-06'], [P, '영어 말하기', 40, 80, '2026-05-29']]],
  [['한국사1', 'COMMON', 3, 60], [[W, '중간고사', 30, 88, '2026-04-28'], [W, '기말고사', 30, 84, '2026-07-07'], [P, '역사 신문 만들기', 40, 90, '2026-06-19']]],
  [['통합사회1', 'COMMON', 4, 60], [[W, '중간고사', 30, 71, '2026-04-29'], [W, '기말고사', 30, 66, '2026-07-08'], [P, '모둠 토론', 40, 75, '2026-06-10']]],
  [['통합과학1', 'COMMON', 4, 60], [[W, '중간고사', 30, 58, '2026-04-30'], [W, '기말고사', 30, 63, '2026-07-09'], [P, '실험 보고서', 40, 72, '2026-06-17']]],
  [['과학탐구실험1', 'COMMON', 1, 0], [[P, '탐구 계획서', 50, 90, '2026-05-15'], [P, '탐구 발표', 50, 86, '2026-07-01']]],
  [['체육', 'GENERAL', 2, 0], [[P, '체력 측정', 50, 88, '2026-05-08'], [P, '구기 실기', 50, 92, '2026-06-26']]],
  [['미술', 'GENERAL', 3, 0], [[P, '드로잉', 50, 80, '2026-05-22'], [P, '작품 감상문', 50, 84, '2026-06-30']]],
  [['창의적 체험활동', 'CREATIVE', 3, 0], []],
];

const SEMESTER_2: [CourseSeed, AssessmentSeed[]][] = [
  // 78%
  [['공통국어2', 'COMMON', 4, 60], [[W, '중간고사', 30, 80, '2026-10-14'], [P, '논설문 쓰기', 20, 75, '2026-09-25'], [W, '기말고사', 30, null, '2026-12-16']]],
  // 46% (반영 비율 합 50%)
  [['공통수학2', 'COMMON', 4, 60], [[W, '중간고사', 30, 50, '2026-10-13'], [P, '수행평가 1', 20, 40, '2026-09-30'], [W, '기말고사', 30, null, '2026-12-15']]],
  // 72%
  [['공통영어2', 'COMMON', 4, 60], [[W, '중간고사', 30, 70, '2026-10-15'], [P, '영어 에세이', 20, 75, '2026-09-24']]],
  // 81%
  [['한국사2', 'COMMON', 3, 60], [[W, '중간고사', 30, 85, '2026-10-16'], [P, '역사 인물 탐구', 20, 75, '2026-10-02']]],
  // 69%
  [['통합사회2', 'COMMON', 4, 60], [[W, '중간고사', 30, 65, '2026-10-19'], [P, '사회 문제 보고서', 20, 75, '2026-09-29']]],
  // 35% (반영 비율 합 50%). 수행은 20%만 써서 20%가 남아 있다
  [['통합과학2', 'COMMON', 4, 60], [[W, '중간고사', 30, 25, '2026-10-20'], [P, '실험 보고서', 20, 50, '2026-10-06']]],
  // 88% (수행 100%)
  [['과학탐구실험2', 'COMMON', 1, 0], [[P, '탐구 계획서', 50, 90, '2026-09-18'], [P, '탐구 발표', 50, 86, '2026-10-23']]],
  // 85%
  [['운동과 건강', 'GENERAL', 2, 0], [[P, '체력 측정', 40, 80, '2026-09-11'], [P, '건강 계획서', 40, 90, '2026-10-08']]],
  // 79%
  [['음악', 'GENERAL', 3, 0], [[P, '가창', 40, 76, '2026-09-17'], [P, '감상문', 40, 82, '2026-10-21']]],
  [['창의적 체험활동', 'CREATIVE', 3, 0], []],
];

let nextAssessmentId = 1;

function buildSeed() {
  const courses: StoredCourse[] = [];
  const assessments: StoredAssessment[] = [];
  const semesters = [
    { term: 1, status: 'COMPLETED' as const, rows: SEMESTER_1 },
    { term: 2, status: 'IN_PROGRESS' as const, rows: SEMESTER_2 },
  ];
  for (const { term, status, rows } of semesters) {
    for (const [[name, courseType, credits, writtenWeight], seeds] of rows) {
      const id = String(courses.length + 1);
      courses.push({
        id,
        name,
        courseType,
        credits,
        gradeYear: 1,
        term,
        status,
        // 창체는 평가가 없어 반영 비율이 0이다
        writtenWeight: courseType === 'CREATIVE' ? 0 : writtenWeight,
        performanceWeight: courseType === 'CREATIVE' ? 0 : 100 - writtenWeight,
        absentCount: 0,
        totalSessions: null,
      });
      for (const [kind, title, weightPercent, score, evaluatedOn] of seeds) {
        assessments.push({ id: String(nextAssessmentId++), courseId: id, kind, title, weightPercent, maxScore: 100, score, evaluatedOn, taskId: null });
      }
    }
  }
  return { courses, assessments };
}

const store = buildSeed();

export function listCourses(): StoredCourse[] {
  return store.courses;
}

export function findCourse(courseId: string): StoredCourse | undefined {
  return store.courses.find((course) => course.id === courseId);
}

export function findAssessment(assessmentId: string): StoredAssessment | undefined {
  return store.assessments.find((assessment) => assessment.id === assessmentId);
}

export function assessmentsOf(courseId: string): StoredAssessment[] {
  return store.assessments.filter((assessment) => assessment.courseId === courseId);
}

// 검증은 호출하는 쪽(mock API)에서 끝낸 뒤 부른다
export function insertAssessment(courseId: string, input: AssessmentInput): StoredAssessment {
  const assessment: StoredAssessment = { ...input, id: String(nextAssessmentId++), courseId, taskId: null };
  store.assessments.push(assessment);
  return assessment;
}

export function replaceAssessment(assessmentId: string, input: AssessmentInput): StoredAssessment | undefined {
  const assessment = findAssessment(assessmentId);
  if (!assessment) return undefined;
  Object.assign(assessment, input);
  return assessment;
}
