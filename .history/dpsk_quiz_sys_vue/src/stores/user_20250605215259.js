import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
    state: () => ({
        id: 0,       // 用户ID
        username: '', // 用户名
        email: '',
    }),
    actions: {
        // 更新用户信息（登录成功后调用）
        setUserInfo(id, username, email) {
            this.id = id
            this.username = username
            this.email = email
        },
        // 清除用户信息（退出登录时调用）
        clearUserInfo() {
            this.id = 0
            this.username = ''
            this.email = ''
        },
    },
    persist: true, // 开启持久化（默认使用 localStorage）
})