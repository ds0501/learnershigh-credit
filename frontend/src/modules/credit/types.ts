// PRD 8장 응답 모양을 따른다. ID는 문자열이다.

export type CourseType = 'COMMON' | 'GENERAL' | 'CAREER' | 'FUSION' | 'CREATIVE';
export type CourseStatus = 'IN_PROGRESS' | 'COMPLETED';
export type RiskLevel = 'NORMAL' | 'CAUTION' | 'RISK' | 'NO_DATA';

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
  // 미입력이면 null
  achievementRate: number | null;
  riskLevel: RiskLevel;
  // 남은 평가에서 이수 기준을 넘기 위해 필요한 평균(%). 계산할 수 없으면 없음
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
