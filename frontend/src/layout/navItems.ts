import type { ComponentType, SVGProps } from 'react';

import { CapIcon, ChartIcon, ClipboardIcon, RecordIcon } from './icons';

export interface NavItem {
  path: string;
  label: string;
  Icon: ComponentType<SVGProps<SVGSVGElement>>;
}

// 큰 틀 안의 모듈 메뉴. 순서대로 상단 내비게이션에 표시한다.
export const NAV_ITEMS: NavItem[] = [
  { path: '/credit', label: '고교학점제 관리', Icon: CapIcon },
  { path: '/tasks', label: '수행평가', Icon: ClipboardIcon },
  { path: '/records', label: '생기부 관리', Icon: RecordIcon },
  { path: '/grades', label: '성적 관리', Icon: ChartIcon },
];
