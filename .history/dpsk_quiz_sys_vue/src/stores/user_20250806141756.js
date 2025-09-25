import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
    state: () => ({
        id: 0,
        username: '',
        email: '',
        role: 'user' // 新增角色字段，默认普通用户
    }),
    actions: {
        // 修改方法签名，增加role参数
        setUserInfo(id, username, email, role) {
            this.id = id
            this.username = username
            this.email = email
            this.role = role 
        },
        // 新增专门设置角色的方法
        setUserRole(role) {
            this.role = role
        },
        clearUserInfo() {
            this.id = 0
            this.username = ''
            this.email = ''
            this.role = 'user' // 重置角色状态
        }
    },
    persist: {
        paths: ['role'] // 持久化角色字段
    }
})