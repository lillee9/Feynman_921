import { createRouter, createWebHistory } from 'vue-router'
import Login from '../components/Login.vue'
import Register from '../components/Register.vue'
import Home from '../components/Home.vue'
import AiChat from '../components/AiChat.vue'
import Quiz from '../components/Quiz.vue'
import Generator from '../components/Generator.vue'
import Exercise from '../components/Exercise.vue'
import Analysis from '../components/Analysis.vue'

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
            requiresAuth: true
        },

    },
    {
  path: "/quiz-chart",
  name: "QuizChart",
  component: () => import("../components/QuizChart.vue"),
},
    {
        path: '/chat',
        name: 'AiChat',
        component: AiChat,
        meta: {
            title: '智能问答',
            requiresAuth: true
        }
    },
    {
        path: '/quiz',
        name: 'Quiz',
        component: Quiz,
        meta: {
            title: '在线答题',
            requiresAuth: true
        }
    },
    {
        path: '/generator',
        name: 'Generator',
        component: Generator,
        meta: {
            title: '题目生成',
            requiresAuth: true
        }
    },
    {
        path: '/exercise',
        name: 'Exercise',
        component: Exercise,
        meta: {
            title: '题目展示',
            requiresAuth: true
        }
    },
    {
  path: '/analysis',
  name: 'Analysis',
  component: Analysis,
  meta: {
    title: '错题分析',
    requiresAuth: true
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
    // 设置页面标题
    document.title = to.meta.title ? `${to.meta.title} - 答题系统` : '答题系统'
    // 检查目标路由是否需要登录
    const requiresAuth = to.meta.requiresAuth

    // 检查 cookie 中的 token
    const token = getCookie('token') // 获取 cookie 中的 token

    if (requiresAuth && !token) {
        // console.log("---拦截成功---")
        // // 如果需要登录但用户未登录，重定向到登录页面
        next('/login') 
        // next() 
    } else {
        // 如果不需要登录或用户已登录，允许访问
        next()
    }
})

// 从 cookie 中获取指定名称的值
function getCookie(name) {
    const cookieArray = document.cookie.split('; ')
    const cookie = cookieArray.find(row => row.startsWith(name + '='))
    return cookie ? cookie.split('=')[1] : null
}

export default router

