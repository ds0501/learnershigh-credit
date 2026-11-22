import { useRole } from '../../role/roleContext';
import { StudentRecordPage } from './StudentRecordPage';
import { TeacherRecordPage } from './TeacherRecordPage';

// 생기부 관리 탭. 상단 역할 전환에 따라 학생 화면(세특 작성)과 선생님 화면을 보여 준다
export function RecordsPage() {
  const { role } = useRole();
  return role === 'TEACHER' ? <TeacherRecordPage /> : <StudentRecordPage />;
}
