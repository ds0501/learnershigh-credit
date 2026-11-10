// 학과 로드맵 API 응답 모양. 기준: backend DepartmentSummaryResponse/DepartmentDetailResponse/DepartmentSubjectResponse
import type { CourseType } from '../types';

// GET /api/student/credit/departments?query= 의 검색 결과 항목
export interface DepartmentSummary {
  id: string;
  name: string;
  field: string | null;
  totalUniversityCount: number;
  lowSample: boolean;
}

export type DesignationType = 'CORE' | 'RECOMMENDED' | 'UNSPECIFIED';

export interface DepartmentSubject {
  catalogId: string;
  name: string;
  courseType: CourseType;
  requiredUniversityCount: number;
  totalUniversityCount: number;
  ratioPercent: number;
  fullCommon: boolean;
  designationType: DesignationType;
  eduofficeListed: boolean;
  // 콤마로 구분된 대학명 목록 ("요구 대학 보기" 토글용)
  requiredUniversities: string;
  alreadyRegistered: boolean;
}

// GET /api/student/credit/departments/{departmentId}
export interface DepartmentDetail {
  id: string;
  name: string;
  field: string | null;
  totalUniversityCount: number;
  lowSample: boolean;
  // 서울시교육청교육연구정보원 자료. 이용 승인 전까지 화면에 노출하지 않는다(docs/PROGRESS.md 미정)
  generalElectiveText: string | null;
  careerElectiveText: string | null;
  fusionElectiveText: string | null;
  subjects: DepartmentSubject[];
}
