import { StrictMode } from 'react';
import { createRoot, hydrateRoot } from 'react-dom/client';
import { App } from './App';

const root = document.getElementById('root')!;
const app = (
  <StrictMode>
    <App />
  </StrictMode>
);

// No build a página chega pré-renderizada e é hidratada; no `npm run dev` o React monta do zero.
if (root.hasChildNodes()) hydrateRoot(root, app);
else createRoot(root).render(app);
