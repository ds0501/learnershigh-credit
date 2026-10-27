import { NavLink, Outlet } from 'react-router-dom';

import { APP_NAME } from '../config/app';
import { NAV_ITEMS } from './navItems';
import styles from './AppLayout.module.css';

export function AppLayout() {
  return (
    <div className={styles.shell}>
      <header className={styles.topBar}>
        <span className={styles.brand}>{APP_NAME}</span>
        <nav className={styles.nav} aria-label={`${APP_NAME} 메뉴`}>
          {NAV_ITEMS.map(({ path, label, Icon }) => (
            <NavLink
              key={path}
              to={path}
              title={label}
              aria-label={label}
              className={({ isActive }) => (isActive ? `${styles.navItem} ${styles.active}` : styles.navItem)}
            >
              <Icon />
            </NavLink>
          ))}
        </nav>
        {/* 로그인 전 단계라 데모 학생으로 고정 */}
        <span className={styles.user}>데모 학생</span>
      </header>
      <main className={styles.content}>
        <Outlet />
      </main>
    </div>
  );
}
