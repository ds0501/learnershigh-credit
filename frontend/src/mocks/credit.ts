import type { CourseItem, CreditSummary, RequiredCourses, SemesterCourses } from '../modules/credit/types';
import type { MockHandler } from './index';

// PRD 9장 시연 데이터. 데모 학생은 1학년 2학기다.
// 1학년 1학기 이수 완료 과목(교과 29 + 창체 3)은 과목 구성이 아직 정해지지 않아 요약 숫자에만 반영한다.
const CURRENT = { gradeYear: 1, term: 2 };

const currentCourses: CourseItem[] = [
  { id: '101', name: '공통국어2', courseType: 'COMMON', credits: 4, status: 'IN_PROGRESS', achievementRate: 78, riskLevel: 'NORMAL' },
  { id: '102', name: '공통수학2', courseType: 'COMMON', credits: 4, status: 'IN_PROGRESS', achievementRate: 46, riskLevel: 'CAUTION' },
  { id: '103', name: '공통영어2', courseType: 'COMMON', credits: 4, status: 'IN_PROGRESS', achievementRate: 72, riskLevel: 'NORMAL' },
  { id: '104', name: '한국사2', courseType: 'COMMON', credits: 3, status: 'IN_PROGRESS', achievementRate: 81, riskLevel: 'NORMAL' },
  { id: '105', name: '통합사회2', courseType: 'COMMON', credits: 4, status: 'IN_PROGRESS', achievementRate: 69, riskLevel: 'NORMAL' },
  {
    id: '106',
    name: '통합과학2',
    courseType: 'COMMON',
    credits: 4,
    status: 'IN_PROGRESS',
    achievementRate: 35,
    riskLevel: 'RISK',
    requiredAverage: 45,
  },
  { id: '107', name: '과학탐구실험2', courseType: 'COMMON', credits: 1, status: 'IN_PROGRESS', achievementRate: 88, riskLevel: 'NORMAL' },
  { id: '108', name: '운동과 건강', courseType: 'GENERAL', credits: 2, status: 'IN_PROGRESS', achievementRate: 85, riskLevel: 'NORMAL' },
  { id: '109', name: '음악', courseType: 'GENERAL', credits: 3, status: 'IN_PROGRESS', achievementRate: 79, riskLevel: 'NORMAL' },
  { id: '110', name: '창의적 체험활동', courseType: 'CREATIVE', credits: 3, status: 'IN_PROGRESS', achievementRate: null, riskLevel: 'NORMAL' },
];

const sumCredits = (courses: CourseItem[]) => courses.reduce((total, course) => total + course.credits, 0);

function summary(): CreditSummary {
  const graduationCredits = 192;
  const completedCredits = 32;
  const inProgressCredits = sumCredits(currentCourses);
  return {
    graduationCredits,
    completedCredits,
    inProgressCredits,
    remainingCredits: graduationCredits - completedCredits - inProgressCredits,
    subjectCredits: { earned: 29, required: 174 },
    creativeCredits: { earned: 3, required: 18 },
    alert: {
      riskCount: currentCourses.filter((course) => course.riskLevel === 'RISK').length,
      cautionCount: currentCourses.filter((course) => course.riskLevel === 'CAUTION').length,
    },
  };
}

function semesterCourses(gradeYear: number, term: number): SemesterCourses {
  const courses = gradeYear === CURRENT.gradeYear && term === CURRENT.term ? currentCourses : [];
  return { gradeYear, term, totalCredits: sumCredits(courses), courses };
}

function requiredCourses(): RequiredCourses {
  const courses = currentCourses.filter((course) => course.courseType === 'COMMON' && course.status !== 'COMPLETED');
  return { remainingCount: courses.length, courses };
}

export const creditMockHandlers: Record<string, MockHandler> = {
  '/student/credit/summary': () => summary(),
  '/student/credit/courses': (params) => semesterCourses(Number(params.gradeYear), Number(params.term)),
  '/student/credit/required-courses': () => requiredCourses(),
};
