import type { SVGProps } from 'react';

// 아이콘 라이브러리 없이 쓰는 단순한 선 아이콘
type IconProps = SVGProps<SVGSVGElement>;

function BaseIcon({ children, ...props }: IconProps) {
  return (
    <svg
      width="22"
      height="22"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.6"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      {...props}
    >
      {children}
    </svg>
  );
}

// 고교학점제 관리: 학사모
export function CapIcon(props: IconProps) {
  return (
    <BaseIcon {...props}>
      <path d="M2 9.5 12 5l10 4.5-10 4.5L2 9.5Z" />
      <path d="M6 11.5v4c0 1.4 2.7 3 6 3s6-1.6 6-3v-4" />
      <path d="M22 9.5v5" />
    </BaseIcon>
  );
}

// 수행평가: 체크리스트 클립보드
export function ClipboardIcon(props: IconProps) {
  return (
    <BaseIcon {...props}>
      <rect x="5" y="4.5" width="14" height="16.5" rx="2" />
      <path d="M9 3h6v3H9z" />
      <path d="m8.5 11 1.2 1.2L12 10" />
      <path d="M14 11h2.5" />
      <path d="m8.5 16 1.2 1.2L12 15" />
      <path d="M14 16h2.5" />
    </BaseIcon>
  );
}

// 생기부 관리: 문서
export function RecordIcon(props: IconProps) {
  return (
    <BaseIcon {...props}>
      <path d="M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8l-5-5Z" />
      <path d="M14 3v5h5" />
      <path d="M9 13h6" />
      <path d="M9 17h4" />
    </BaseIcon>
  );
}

// 성적 관리: 막대그래프
export function ChartIcon(props: IconProps) {
  return (
    <BaseIcon {...props}>
      <path d="M4 20h16" />
      <path d="M7 16v-4" />
      <path d="M12 16V8" />
      <path d="M17 16V5" />
    </BaseIcon>
  );
}
