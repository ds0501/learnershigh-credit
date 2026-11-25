export interface GradeRecord {
  id: string;
  gradeYear: number;
  term: number;
  subjectName: string;
  rawScore: number | null;
  subjectAverage: number | null;
  stdDev: number | null;
  gradeLevel: number | null;
  source: 'PDF' | 'MANUAL';
  appliedToCredit: boolean;
  unmatchedReason: 'NOT_FOUND' | 'AMBIGUOUS' | 'NO_ASSESSMENT' | null;
  matchedCourseId: string | null;
}

export interface GradeUploadResult {
  uploadId: string;
  recognizedCount: number;
  failedCount: number;
  records: GradeRecord[];
}

// 수동 추가/수정 입력. 비우면 null(등급만 있고 원점수가 없는 과목 등)
export interface GradeRecordInput {
  gradeYear: number;
  term: number;
  subjectName: string;
  rawScore: number | null;
  subjectAverage: number | null;
  stdDev: number | null;
  gradeLevel: number | null;
}

// GET /api/student/credit/catalog?query= 의 항목(표준 과목 목록). credit 모듈의 같은 모양을 그대로 쓴다(코드는 import하지 않음, API만 재사용)
export interface CatalogItem {
  id: string;
  name: string;
  subjectGroup: string;
  courseType: string;
}
