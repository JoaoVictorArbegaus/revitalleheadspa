import path from 'node:path';
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';

// Servido em /admin/ pelo mesmo nginx da landing. Em desenvolvimento, /api vai para o
// ambiente Docker (nginx em :8081 encaminha para a API): mesma origem, sessão e CSRF.
export default defineConfig({
  base: '/admin/',
  plugins: [react(), tailwindcss()],
  resolve: { alias: { '@': path.resolve(import.meta.dirname, 'src') } },
  server: {
    port: 5174,
    // /img vem da landing (logo e favicon), servida pelo mesmo nginx
    proxy: { '/api': 'http://localhost:8081', '/img': 'http://localhost:8081' },
  },
});
