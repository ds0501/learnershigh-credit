// PRD 8장 응답 모양을 따른다. ID는 문자열, 날짜는 YYYY-MM-DD다.
import type { AssessmentKind, CourseStatus, CourseType, GradeDisplay, RiskLevel } from './domain/types';

export type { AssessmentKind, CourseStatus, CourseType, GradeDisplay, RiskLevel };

export interface CreditProgress {
  earned: number;
  required: number;
}

// GET /api/student/credit/summary
export interface CreditSummary {
  graduationCredits: number;
  completedCredits: number;
  inProgressCredits: number;
  remainingCredits: number;
  subjectCredits: CreditProgress;
  creativeCredits: CreditProgress;
  alert: {
    riskCount: number;
    cautionCount: number;
  };
}

export interface CourseItem {
  id: string;
  name: string;
  courseType: CourseType;
  credits: number;
  status: CourseStatus;
  // 미입력이면 null. 반올림하지 않은 값이며 화면에서 정수로 반올림해 표시한다
  achievementRate: number | null;
  riskLevel: RiskLevel;
  // 남은 평가에서 이수 기준을 넘기 위해 필요한 평균(%). 이수 위험 과목에만 있다
  requiredAverage?: number;
}

// GET /api/student/credit/courses?gradeYear=&term=
export interface SemesterCourses {
  gradeYear: number;
  term: number;
  totalCredits: number;
  courses: CourseItem[];
}

// GET /api/student/credit/required-courses (아직 이수하지 않은 공통과목)
export interface RequiredCourses {
  remainingCount: number;
  courses: CourseItem[];
}

export interface Assessment {
  id: string;
  kind: AssessmentKind;
  title: string;
  weightPercent: number;
  maxScore: number;
  // 미입력이면 null
  score: number | null;
  evaluatedOn: string;
  // 기존 수행평가 연결용 (이번 단계는 비워 둠)
  taskId: string | null;
}

// POST /courses/{courseId}/assessments, PUT /courses/{courseId}/assessments/{assessmentId} 요청 본문
export type AssessmentInput = Omit<Assessment, 'id' | 'taskId'>;

// GET /api/student/credit/catalog?query= 의 항목 (표준 과목 목록)
export interface CatalogItem {
  id: string;
  name: string;
  // 교과(군)
  subjectGroup: string;
  courseType: CourseType;
  credits: number;
}

// POST /api/student/credit/courses 요청 본문
// 목록 과목이면 catalogId를 보내고, 목록에 없는 과목이면 catalogId는 null이고 customName과 courseType을 보낸다
export interface CourseRegistrationInput {
  gradeYear: number;
  term: number;
  catalogId: string | null;
  customName?: string;
  courseType?: CourseType;
  // 목록 과목도 학교 편성에 맞춰 바꿀 수 있다
  credits: number;
  status: CourseStatus;
  writtenWeight: number;
  performanceWeight: number;
  // 창체는 null
  gradeDisplay: GradeDisplay | null;
}

// GET /api/student/credit/courses/{courseId} (PRD 8장에 없음, 점수 입력 패널용으로 추가)
export interface CourseDetail extends CourseItem {
  gradeYear: number;
  term: number;
  writtenWeight: number;
  performanceWeight: number;
  assessments: Assessment[];
}
