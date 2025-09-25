import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  base: '/',
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    historyApiFallback: true,
    proxy: {
      '^/api/.*': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '^/question/.*': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '^/exam/(?!.*selection).*': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '^/kb/.*': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '^/conversation/.*': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '^/message/.*': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '^/stream/.*': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '^/answer/.*': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})