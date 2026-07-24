import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api/auth': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      '/api/user': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      '/api/resume': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      '/api/interview': {
        target: 'http://localhost:8082',
        changeOrigin: true
      },
      '/api/job': {
        target: 'http://localhost:8082',
        changeOrigin: true
      },
      '/api/hot': {
        target: 'http://localhost:8082',
        changeOrigin: true
      },
      '/api/question': {
        target: 'http://localhost:8083',
        changeOrigin: true
      },
      '/api/evaluation': {
        target: 'http://localhost:8082',
        changeOrigin: true
      },
      '/api/report': {
        target: 'http://localhost:8082',
        changeOrigin: true
      },
      '/api/ai': {
        target: 'http://localhost:8086',
        changeOrigin: true
      },
      '/ws': {
        target: 'ws://localhost:8086',
        ws: true
      }
    }
  }
})