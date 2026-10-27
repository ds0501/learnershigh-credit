import { createBrowserRouter, Navigate } from 'react-router-dom';

import { AppLayout } from './layout/AppLayout';
import { CreditPage } from './modules/credit/CreditPage';
import { GradesPage } from './modules/grades/GradesPage';
import { RecordsPage } from './modules/records/RecordsPage';
import { TasksPage } from './modules/tasks/TasksPage';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <AppLayout />,
    children: [
      { index: true, element: <Navigate to="/credit" replace /> },
      { path: 'credit', element: <CreditPage /> },
      { path: 'tasks', element: <TasksPage /> },
      { path: 'records', element: <RecordsPage /> },
      { path: 'grades', element: <GradesPage /> },
      { path: '*', element: <Navigate to="/credit" replace /> },
    ],
  },
]);
