// 학생부 관리 공통: 희망 학과와 목표 대학. 기준: backend StudentGoalResponse, docs/api-contract.md "희망 학과"

export interface GoalDepartment {
  id: string;
  name: string;
  // 계열 (예: 보건·의약학). 미분류면 null
  field: string | null;
}

// GET /api/student/goal 응답(없으면 null), PUT /api/student/goal 응답
export interface StudentGoal {
  department: GoalDepartment;
  // 선택 입력. 없으면 null
  targetUniversity: string | null;
}

// PUT /api/student/goal 요청 본문
export interface SaveStudentGoalInput {
  departmentId: string;
  targetUniversity: string | null;
}
