import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
  const router = useRouter()
  
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  const isLoggedIn = computed(() => !!token.value)
  const isResident = computed(() => userInfo.value.role === 'RESIDENT')
  const isProperty = computed(() => userInfo.value.role === 'PROPERTY' || userInfo.value.role === 'ADMIN')
  const isAdmin = computed(() => userInfo.value.role === 'ADMIN')

  const login = async (loginForm) => {
    const res = await request.post('/api/auth/login', loginForm)
    if (res.code === 200) {
      token.value = res.data.token
      userInfo.value = res.data
      
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('userRole', res.data.role)
      localStorage.setItem('userInfo', JSON.stringify(res.data))
      
      ElMessage.success('登录成功')
      
      if (res.data.role === 'RESIDENT') {
        router.push('/resident/home')
      } else {
        router.push('/property/home')
      }
    }
  }

  const register = async (registerForm) => {
    const res = await request.post('/api/auth/register', registerForm)
    if (res.code === 200) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    }
  }

  const logout = () => {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userRole')
    localStorage.removeItem('userInfo')
    ElMessage.success('已退出登录')
    router.push('/login')
  }

  const getCurrentUser = async () => {
    const res = await request.get('/api/auth/current')
    if (res.code === 200) {
      userInfo.value = res.data
      localStorage.setItem('userInfo', JSON.stringify(res.data))
    }
  }

  const updateUserInfo = async (userData) => {
    const res = await request.put('/api/user', userData)
    if (res.code === 200) {
      ElMessage.success('更新成功')
      await getCurrentUser()
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isResident,
    isProperty,
    isAdmin,
    login,
    register,
    logout,
    getCurrentUser,
    updateUserInfo
  }
})