import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// https://vitejs.dev/config/
export default defineConfig({
  server: {
    host: "0.0.0.0", // Bind to all network interfaces
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      }
    }
  },
  optimizeDeps: {
    include: [
      'react-slick',
      'react-h5-audio-player',
      'react-quill',
      'axios',
      'react-router-dom'
    ]
  },
  plugins: [react()],
  build: {
    sourcemap: true, // Enable source maps
    outDir: "build", // Specify the output directory (relative to project root).
  },
});
