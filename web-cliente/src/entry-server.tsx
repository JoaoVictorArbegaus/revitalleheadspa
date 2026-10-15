import { StrictMode } from 'react';
import { renderToString } from 'react-dom/server';
import { App } from './App';
import { AgendarApp } from './agendar/AgendarApp';

/** Usado só no build (scripts/prerender.mjs) para gerar o HTML estático de cada página. */
export const paginas = {
  'index.html': () =>
    renderToString(
      <StrictMode>
        <App />
      </StrictMode>,
    ),
  'agendar/index.html': () =>
    renderToString(
      <StrictMode>
        <AgendarApp />
      </StrictMode>,
    ),
};
