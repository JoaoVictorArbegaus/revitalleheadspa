import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// A API exige mesma origem (sessão e CSRF, sem CORS): em desenvolvimento o proxy encaminha /api.
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: { '/api': 'http://localhost:8080' },
  },
});
