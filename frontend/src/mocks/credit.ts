// 고교학점제 mock API. 응답은 메모리 저장소(creditStore)의 과목·평가에서 계산 모듈로 만든다.
import { ApiError } from '../api/errors';
import { evaluateCourse } from '../modules/credit/domain/completion';
import { validateCourseDraft } from '../modules/credit/domain/courseRegistration';
import { GRADUATION_RULES } from '../modules/credit/domain/rules';
import { validateAssessment } from '../modules/credit/domain/validation';
import type {
  AssessmentInput,
  CourseDetail,
  CourseStatus,
  CourseItem,
  CourseRegistrationInput,
  CourseType,
  CreditSummary,
  GradeDisplay,
  RequiredCourses,
  SemesterCourses,
} from '../modules/credit/types';
import { findCatalogItem, searchCatalog } from './catalog';
import {
  assessmentsOf,
  findAssessment,
  findCourse,
  insertAssessment,
  insertCourse,
  listCourses,
  removeAssessment,
  removeCourse,
  replaceAssessment,
  updateCourseStatus,
  type StoredAssessment,
  type StoredCourse,
} from './creditStore';
import type { MockHandler } from './index';

function toAssessment({ courseId: _courseId, ...assessment }: StoredAssessment) {
  return assessment;
}

function toCourseItem(course: StoredCourse): CourseItem {
  const evaluation = evaluateCourse({
    courseType: course.courseType,
    assessments: assessmentsOf(course.id),
    attendance: { totalSessions: course.totalSessions, absentCount: course.absentCount },
  });
  const item: CourseItem = {
    id: course.id,
    name: course.name,
    courseType: course.courseType,
    credits: course.credits,
    status: course.status,
    achievementRate: evaluation.achievementRate,
    riskLevel: evaluation.riskLevel,
  };
  if (evaluation.requiredAverage !== null) item.requiredAverage = evaluation.requiredAverage;
  return item;
}

const sumCredits = (courses: { credits: number }[]) => courses.reduce((total, course) => total + course.credits, 0);

function summary(): CreditSummary {
  const courses = listCourses();
  const completed = courses.filter((course) => course.status === 'COMPLETED');
  const inProgress = courses.filter((course) => course.status === 'IN_PROGRESS');
  const completedCredits = sumCredits(completed);
  const inProgressCredits = sumCredits(inProgress);
  // 경고 배너는 수강 중인 과목만 센다 (이수 완료 과목은 이미 이수함)
  const levels = inProgress.map((course) => toCourseItem(course).riskLevel);
  return {
    graduationCredits: GRADUATION_RULES.totalCredits,
    completedCredits,
    inProgressCredits,
    remainingCredits: GRADUATION_RULES.totalCredits - completedCredits - inProgressCredits,
    subjectCredits: {
      earned: sumCredits(completed.filter((course) => course.courseType !== 'CREATIVE')),
      required: GRADUATION_RULES.subjectCredits,
    },
    creativeCredits: {
      earned: sumCredits(completed.filter((course) => course.courseType === 'CREATIVE')),
      required: GRADUATION_RULES.creativeCredits,
    },
    alert: {
      riskCount: levels.filter((level) => level === 'RISK').length,
      cautionCount: levels.filter((level) => level === 'CAUTION').length,
    },
  };
}

function semesterCourses(gradeYear: number, term: number): SemesterCourses {
  const courses = listCourses().filter((course) => course.gradeYear === gradeYear && course.term === term);
  return { gradeYear, term, totalCredits: sumCredits(courses), courses: courses.map(toCourseItem) };
}

function requiredCourses(): RequiredCourses {
  const courses = listCourses().filter((course) => course.courseType === 'COMMON' && course.status !== 'COMPLETED');
  return { remainingCount: courses.length, courses: courses.map(toCourseItem) };
}

function requireCourse(courseId: string) {
  const course = findCourse(courseId);
  if (!course) throw new ApiError(404, '과목을 찾을 수 없습니다.');
  return course;
}

function courseDetail(courseId: string): CourseDetail {
  const course = requireCourse(courseId);
  return {
    ...toCourseItem(course),
    gradeYear: course.gradeYear,
    term: course.term,
    writtenWeight: course.writtenWeight,
    performanceWeight: course.performanceWeight,
    assessments: assessmentsOf(course.id).map(toAssessment),
  };
}

// 서버처럼 요청 본문을 다시 검증한다
function checkInput(course: StoredCourse, body: unknown, editingId?: string): AssessmentInput {
  const input = body as AssessmentInput;
  const errors = validateAssessment(input, course, assessmentsOf(course.id), editingId);
  if (Object.keys(errors).length > 0) throw new ApiError(400, '입력값을 확인하세요.', errors);
  return {
    kind: input.kind,
    title: input.title.trim(),
    weightPercent: input.weightPercent,
    maxScore: input.maxScore,
    score: input.score,
    evaluatedOn: input.evaluatedOn,
  };
}

function addAssessment(courseId: string, body: unknown) {
  const course = requireCourse(courseId);
  return toAssessment(insertAssessment(course.id, checkInput(course, body)));
}

function updateAssessment(assessmentId: string, body: unknown) {
  const existing = findAssessment(assessmentId);
  if (!existing) throw new ApiError(404, '평가를 찾을 수 없습니다.');
  const course = requireCourse(existing.courseId);
  return toAssessment(replaceAssessment(assessmentId, checkInput(course, body, assessmentId))!);
}

const COURSE_TYPES: CourseType[] = ['COMMON', 'GENERAL', 'CAREER', 'FUSION', 'CREATIVE'];
const GRADE_DISPLAYS: GradeDisplay[] = ['ACHIEVEMENT_ONLY', 'ACHIEVEMENT_AND_RANK', 'THREE_LEVEL'];

// 과목 등록 (PRD F2). 목록 과목은 이름과 유형을 목록에서 가져오고, 학점은 요청 값을 쓴다
function registerCourse(body: unknown): CourseItem {
  const input = body as CourseRegistrationInput;
  const errors: Record<string, string> = {};

  let name = '';
  let courseType: CourseType = 'COMMON';
  let catalogId: string | null = null;
  if (input.catalogId) {
    const item = findCatalogItem(input.catalogId);
    if (item) {
      name = item.name;
      courseType = item.courseType;
      catalogId = item.id;
    } else {
      errors.name = '과목 목록에서 찾을 수 없는 과목입니다.';
    }
  } else {
    name = (input.customName ?? '').trim();
    if (input.courseType && COURSE_TYPES.includes(input.courseType)) courseType = input.courseType;
    else errors.courseType = '과목 유형을 선택하세요.';
  }

  // 창체는 평가가 없어 반영 비율과 표기 방식을 쓰지 않는다
  const creative = courseType === 'CREATIVE';
  const draft = {
    gradeYear: input.gradeYear,
    term: input.term,
    name,
    courseType,
    credits: input.credits,
    status: input.status,
    writtenWeight: creative ? 0 : input.writtenWeight,
    performanceWeight: creative ? 0 : input.performanceWeight,
  };
  const sameTermNames = listCourses()
    .filter((course) => course.gradeYear === draft.gradeYear && course.term === draft.term)
    .map((course) => course.name);
  Object.assign(errors, { ...validateCourseDraft(draft, sameTermNames), ...errors });

  if (input.status !== 'IN_PROGRESS' && input.status !== 'COMPLETED') errors.status = '이수 상태를 선택하세요.';
  if (!creative && !GRADE_DISPLAYS.includes(input.gradeDisplay as GradeDisplay)) {
    errors.gradeDisplay = '성적 표기 방식을 선택하세요.';
  }
  if (Object.keys(errors).length > 0) throw new ApiError(400, '입력값을 확인하세요.', errors);

  const created = insertCourse({
    ...draft,
    catalogId,
    absentCount: 0,
    totalSessions: null,
    gradeDisplay: creative ? null : input.gradeDisplay,
  });
  return toCourseItem(created);
}

// 이수 상태 변경. 이수 위험·주의 과목도 제한 없이 바꿀 수 있다
function changeCourseStatus(courseId: string, body: unknown): CourseItem {
  requireCourse(courseId);
  const status = (body as { status?: CourseStatus } | null)?.status;
  if (status !== 'IN_PROGRESS' && status !== 'COMPLETED') {
    throw new ApiError(400, '입력값을 확인하세요.', { status: '이수 상태를 선택하세요.' });
  }
  return toCourseItem(updateCourseStatus(courseId, status)!);
}

// 과목과 그 과목의 평가를 지운다. 응답 본문 없음(204, 평가 삭제와 같음)
function deleteCourse(courseId: string) {
  requireCourse(courseId);
  removeCourse(courseId);
  return null;
}

// 응답 본문 없음(204)
function deleteAssessment(courseId: string, assessmentId: string) {
  const course = requireCourse(courseId);
  const existing = findAssessment(assessmentId);
  if (!existing || existing.courseId !== course.id) throw new ApiError(404, '평가를 찾을 수 없습니다.');
  removeAssessment(assessmentId);
  return null;
}

export const creditMockHandlers: Record<string, MockHandler> = {
  'GET /student/credit/summary': () => summary(),
  'GET /student/credit/courses': ({ params }) => semesterCourses(Number(params.gradeYear), Number(params.term)),
  'GET /student/credit/courses/:courseId': ({ pathParams }) => courseDetail(pathParams.courseId),
  'GET /student/credit/catalog': ({ params }) => searchCatalog(String(params.query ?? '')),
  'POST /student/credit/courses': ({ body }) => registerCourse(body),
  'PATCH /student/credit/courses/:courseId': ({ pathParams, body }) => changeCourseStatus(pathParams.courseId, body),
  'DELETE /student/credit/courses/:courseId': ({ pathParams }) => deleteCourse(pathParams.courseId),
  'GET /student/credit/required-courses': () => requiredCourses(),
  'POST /student/credit/courses/:courseId/assessments': ({ pathParams, body }) => addAssessment(pathParams.courseId, body),
  'PUT /student/credit/assessments/:assessmentId': ({ pathParams, body }) => updateAssessment(pathParams.assessmentId, body),
  'DELETE /student/credit/courses/:courseId/assessments/:assessmentId': ({ pathParams }) =>
    deleteAssessment(pathParams.courseId, pathParams.assessmentId),
};
