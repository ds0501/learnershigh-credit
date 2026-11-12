import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';

import { useAsync } from '../../../api/useAsync';
import { PageHeader } from '../../../layout/PageHeader';
import { StateMessage } from '../components/StateMessage';
import { COURSE_TYPE_LABEL } from '../labels';
import { getDepartmentDetail, searchDepartments } from './api';
import styles from './DepartmentRoadmapPage.module.css';
import { checkSummary, groupSubjects, isHighlighted } from './subjectGroups';
import type { DepartmentSubject, DepartmentSummary, DesignationType } from './types';

// "참고: 시도교육청 예시" 섹션 표시 여부. 자료 이용 승인 전까지 화면에서 숨긴다(데이터와 API는 그대로)
const SHOW_EDU_OFFICE_EXAMPLE = false;

const DESIGNATION_LABEL: Record<DesignationType, string> = {
  CORE: '핵심',
  RECOMMENDED: '권장',
  UNSPECIFIED: '구분없음',
};

function RatioBar({ ratioPercent, emphasized }: { ratioPercent: number; emphasized: boolean }) {
  return (
    <span className={styles.ratioTrack}>
      <span className={emphasized ? styles.ratioFillEmphasized : styles.ratioFill} style={{ width: `${ratioPercent}%` }} />
    </span>
  );
}

function SubjectRow({ subject, showUniversityToggle }: { subject: DepartmentSubject; showUniversityToggle: boolean }) {
  const [showUniversities, setShowUniversities] = useState(false);
  const highlighted = isHighlighted(subject);
  return (
    <div className={`${styles.subjectRow} ${highlighted ? styles.registered : ''}`}>
      <div className={styles.subjectRowTop}>
        <span className={styles.subjectName}>{subject.name}</span>
        <span className={styles.subjectTag}>{COURSE_TYPE_LABEL[subject.courseType]}</span>
        {highlighted && <span className={styles.registeredTag}>등록함</span>}
        <span className={`${styles.subjectTag} ${subject.designationType === 'CORE' ? styles.subjectTagCore : ''}`}>
          {DESIGNATION_LABEL[subject.designationType]}
        </span>
        <RatioBar ratioPercent={subject.ratioPercent} emphasized={highlighted} />
        <span className={styles.ratioText}>
          {subject.requiredUniversityCount} / {subject.totalUniversityCount}개 대학 · {Math.round(subject.ratioPercent)}%
        </span>
        {showUniversityToggle && (
          <button type="button" className={styles.universityToggle} onClick={() => setShowUniversities((value) => !value)}>
            요구 대학 보기
          </button>
        )}
      </div>
      {showUniversityToggle && showUniversities && (
        <div className={styles.universityPanel}>
          <span className={styles.universityLabel}>요구하는 대학 ({subject.requiredUniversityCount}곳)</span>
          <span>{subject.requiredUniversities}</span>
        </div>
      )}
    </div>
  );
}

// 고교학점제 관리 > 학과 로드맵. 학과를 검색하면 추천 선택과목을 보여준다
export function DepartmentRoadmapPage() {
  const [query, setQuery] = useState('');
  const [selected, setSelected] = useState<DepartmentSummary | null>(null);
  const [splitOpen, setSplitOpen] = useState(false);

  const changeQuery = (value: string) => {
    setQuery(value);
    setSelected(null);
  };

  // 학과를 고르기 전, 검색어가 있을 때만 검색한다 (CourseRegisterModal의 catalog 검색과 같은 방식)
  const keyword = selected ? '' : query.trim();
  const results = useAsync(`dept-search:${keyword}`, () => (keyword ? searchDepartments(keyword) : Promise.resolve([])));

  const detail = useAsync(`dept-detail:${selected?.id ?? ''}`, () =>
    selected ? getDepartmentDetail(selected.id) : Promise.resolve(null),
  );

  const selectDepartment = (item: DepartmentSummary) => {
    setQuery(item.name);
    setSelected(item);
    setSplitOpen(false);
  };

  const department = detail.state.status === 'success' ? detail.state.data : null;

  const groups = useMemo(() => groupSubjects(department?.subjects ?? []), [department]);
  const { fullCommon, majority, split } = groups;
  const summary = checkSummary(groups);

  return (
    <>
      <PageHeader
        title="학과 로드맵"
        subtitle="희망 학과를 검색하면 추천 선택과목을 보여줘요"
        actions={
          <Link to="/credit" className={styles.backLink}>
            ← 학점 현황
          </Link>
        }
      />

      <div className={styles.searchBox}>
        <input
          type="text"
          className={styles.searchInput}
          placeholder="학과명을 검색하세요 (예: 간호학과)"
          value={query}
          onChange={(event) => changeQuery(event.target.value)}
        />
        {keyword && results.state.status === 'success' && results.state.data.length > 0 && (
          <div className={styles.searchDropdown}>
            {results.state.data.map((item) => (
              <button type="button" key={item.id} className={styles.searchRow} onClick={() => selectDepartment(item)}>
                <span className={styles.searchName}>{item.name}</span>
                <span className={styles.searchMeta}>
                  {item.field ?? '분야 미분류'} · {item.totalUniversityCount}개 대학
                  {item.lowSample && <span className={styles.lowSampleChip}>표본부족</span>}
                </span>
              </button>
            ))}
          </div>
        )}
        {keyword && results.state.status === 'success' && results.state.data.length === 0 && (
          <div className={styles.searchDropdown}>
            <p className={styles.searchEmpty}>"{keyword}"와 일치하는 학과가 없어요.</p>
          </div>
        )}
      </div>

      {!selected && <p className={styles.hint}>학과명을 입력하면 자동완성으로 찾을 수 있어요.</p>}

      {selected && detail.state.status === 'loading' && <StateMessage kind="loading" />}
      {selected && detail.state.status === 'error' && <StateMessage kind="error" onRetry={detail.reload} />}

      {department && (
        <>
          <div className={styles.deptHeader}>
            <h2 className={styles.deptName}>{department.name}</h2>
            {department.field && <span className={styles.chip}>{department.field}</span>}
            {department.lowSample && <span className={styles.lowSampleChip}>표본부족</span>}
          </div>
          <p className={styles.deptMeta}>대교협 공개 자료 기준 {department.totalUniversityCount}개 대학 조사 · 2028학년도 입시 권장과목 반영</p>

          {/* 점검 요약: 다수 대학 공통 과목(전체 대학 공통 포함) 중 이미 등록한 과목 수. 해당 과목이 없으면 숨긴다 */}
          {summary.total > 0 && (
            <p className={styles.checkSummary} data-testid="roadmap-check-summary">
              다수 대학 공통 과목 <strong>{summary.total}개</strong> 중 <strong className={styles.checkSummaryCount}>{summary.registered}개</strong> 등록
            </p>
          )}

          <section className={styles.panel}>
            <h3 className={styles.panelTitle}>전체 대학 공통 과목</h3>
            <p className={styles.panelSub}>조사 대학 전원이 지정한 과목</p>
            {fullCommon.length === 0 ? (
              <p className={styles.empty}>이 학과는 대학 전원이 똑같이 지정한 과목은 없어요. 아래 "다수 대학 공통 과목"을 참고하세요.</p>
            ) : (
              <div className={styles.subjectList}>
                {fullCommon.map((subject) => (
                  <SubjectRow key={subject.catalogId} subject={subject} showUniversityToggle={false} />
                ))}
              </div>
            )}
          </section>

          <section className={styles.panel}>
            <h3 className={styles.panelTitle}>다수 대학 공통 과목</h3>
            <p className={styles.panelSub}>조사 대학의 50% 이상이 지정한 과목</p>
            {majority.length === 0 ? (
              <p className={styles.empty}>조사 대학의 절반 이상이 똑같이 지정한 과목은 없어요.</p>
            ) : (
              <div className={styles.subjectList}>
                {majority.map((subject) => (
                  <SubjectRow key={subject.catalogId} subject={subject} showUniversityToggle={false} />
                ))}
              </div>
            )}
          </section>

          <section className={styles.panel}>
            <h3 className={styles.panelTitle}>대학별로 다른 과목</h3>
            <p className={styles.panelSub}>50% 미만의 대학만 지정 · 지망 대학이 정해졌다면 "요구 대학 보기"로 확인하세요</p>
            {split.length > 0 && (
              <button type="button" className={styles.toggleLine} onClick={() => setSplitOpen((value) => !value)}>
                <span className={splitOpen ? styles.arrowOpen : styles.arrow}>▸</span> {split.length}개 과목 {splitOpen ? '접기' : '더보기'}
              </button>
            )}
            {splitOpen && (
              <div className={styles.subjectList} style={{ marginTop: 'var(--space-2)' }}>
                {split.map((subject) => (
                  <SubjectRow key={subject.catalogId} subject={subject} showUniversityToggle />
                ))}
              </div>
            )}
          </section>

          <p className={styles.legend}>
            <span className={styles.legendDot} /> 이미 등록한 과목은 테두리·막대 색이 강조돼요
          </p>

          {/* TODO: 서울시교육청교육연구정보원 자료 이용 승인 전까지 실제 배포 빌드에서는 이 섹션을 뺀다(docs/PROGRESS.md 미정) */}
          {SHOW_EDU_OFFICE_EXAMPLE &&
            (department.generalElectiveText || department.careerElectiveText || department.fusionElectiveText) && (
            <section className={styles.panel}>
              <h3 className={styles.panelTitle}>참고: 시도교육청 예시</h3>
              <p className={styles.panelSub}>목록이 전부가 아니라 "~ 등"으로 끝나요.</p>
              <dl className={styles.eduTrack}>
                {department.generalElectiveText && (
                  <>
                    <dt>일반 선택</dt>
                    <dd>{department.generalElectiveText}</dd>
                  </>
                )}
                {department.careerElectiveText && (
                  <>
                    <dt>진로 선택</dt>
                    <dd>{department.careerElectiveText}</dd>
                  </>
                )}
                {department.fusionElectiveText && (
                  <>
                    <dt>융합 선택</dt>
                    <dd>{department.fusionElectiveText}</dd>
                  </>
                )}
              </dl>
            </section>
          )}
        </>
      )}
    </>
  );
}
