import { createRouter, createWebHistory } from 'vue-router'
import Login from '../components/Login.vue'
import Register from '../components/Register.vue'
import Home from '../components/Home.vue'
import AiChat from '../components/AiChat.vue'
import Quiz from '../components/Quiz.vue'
import Generator from '../components/Generator.vue'
import Exercise from '../components/Exercise.vue'
import Analysis from '../components/Analysis.vue'
import Chart  from '../components/Chart.vue'
import { useUserStore } from '@/stores/user'

const routes = [
    {
        path: '/',
        redirect: '/login'
    },
    {
        path: '/login',
        name: 'Login',
        component: Login,
        meta: {
            title: '登录',
            requiresAuth: false
        }
    },
    {
        path: '/register',
        name: 'Register',
        component: Register,
        meta: {
            title: '注册',
            requiresAuth: false
        }
    },
    {
        path: '/home',
        name: 'Home',
        component: Home,
        meta: {
            title: '首页',
            requiresAuth: true,
    roles: ['user']
        },

    },
    {
        path: '/chat',
        name: 'AiChat',
        component: AiChat,
        meta: {
            title: '智能问答',
            requiresAuth: true,
    roles: ['user']
        }
    },
    {
        path: '/quiz',
        name: 'Quiz',
        component: Quiz,
        meta: {
            title: '在线答题',
            requiresAuth: true,
    roles: ['user']
        }
    },
    // router/index.js
{ path: '/chart',
     name: 'Chart',
      component: Chart,
    meta:{
        title: '统计',
        requiresAuth:true,
    roles: ['admin']
    } },
    {
        path: '/generator',
        name: 'Generator',
        component: Generator,
        meta: {
            title: '题目生成',
            requiresAuth: true,
    roles: ['user']
        }
    },
    {
        path: '/exercise',
        name: 'Exercise',
        component: Exercise,
        meta: {
            title: '题目展示',
            requiresAuth: true,
    roles: ['user']
        }
    },
    {
  path: '/analysis',
  name: 'Analysis',
  component: Analysis,
  meta: {
    title: '错题分析',
    requiresAuth: true,
    roles: ['user']
  }
},
    {
        // 捕获所有未匹配的路由
        path: '/:pathMatch(.*)*',
        redirect: '/login'
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 全局路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 答题系统` : '答题系统'

  // 检查登录状态
  if (to.meta.requiresAuth && !userStore.id) {
    ElMessage.warning('请先登录')
    return next('/login')
  }

  // 检查角色权限
  if (to.meta.roles) {
    if (!to.meta.roles.includes(userStore.role)) {
      ElMessage.error('权限不足')
      return next(from.path === '/login' ? false : '/login')
    }
  }
  
  next()
})

// 从 cookie 中获取指定名称的值
function getCookie(name) {
    const cookieArray = document.cookie.split('; ')
    const cookie = cookieArray.find(row => row.startsWith(name + '='))
    return cookie ? cookie.split('=')[1] : null
}

export default router