import { StrictMode } from 'react';
import { createRoot, hydrateRoot } from 'react-dom/client';
import { AgendarApp } from './AgendarApp';

const root = document.getElementById('root')!;
const app = (
  <StrictMode>
    <AgendarApp />
  </StrictMode>
);

// No build a página chega pré-renderizada e é hidratada; no `npm run dev` o React monta do zero.
if (root.hasChildNodes()) hydrateRoot(root, app);
else createRoot(root).render(app);
