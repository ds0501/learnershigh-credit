import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';

import { APP_NAME } from './config/app';
import { router } from './router';
import './styles/global.css';

document.title = `${APP_NAME} · 러너스하이`;

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <RouterProvider router={router} />
  </StrictMode>,
);
