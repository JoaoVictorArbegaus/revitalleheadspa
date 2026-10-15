import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// A API exige mesma origem: em desenvolvimento o proxy encaminha /api ao nginx do ambiente Docker (8081).
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: { '/api': 'http://localhost:8081' },
  },
  build: {
    // duas páginas pré-renderizadas: a landing (/) e o agendamento (/agendar/); caminhos relativos à raiz do projeto
    rollupOptions: {
      input: { landing: 'index.html', agendar: 'agendar/index.html' },
    },
  },
});
