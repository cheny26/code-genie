import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { message } from 'ant-design-vue'
import {
  userLogin,
  userLogout,
  userRegister,
  getLoginUser
} from '@/api/userController'

export const useUserStore = defineStore('user', () => {
  // 用户信息状态
  const userInfo = ref<API.UserVO | null>(null)

  // 计算属性
  const isLoggedIn = computed(() => !!userInfo.value)
  const isAdmin = computed(() => userInfo.value?.userRole === 'admin')
  const userName = computed(() => userInfo.value?.userName || '')
  const userAvatar = computed(() => userInfo.value?.userAvatar || '')

  // 初始化用户信息（从 localStorage 恢复）
  const initUserInfo = () => {
    try {
      const localUserInfo = localStorage.getItem('userInfo')
      if (localUserInfo) {
        userInfo.value = JSON.parse(localUserInfo)
      }
    } catch (error) {
      console.error('解析本地用户信息失败:', error)
      localStorage.removeItem('userInfo')
    }
  }

  // 保存用户信息到 localStorage
  const saveUserInfo = (user: API.UserVO) => {
    userInfo.value = user
    localStorage.setItem('userInfo', JSON.stringify(user))
  }

  // 清除用户信息
  const clearUserInfo = () => {
    userInfo.value = null
    localStorage.removeItem('userInfo')
  }

  // 获取当前登录用户信息
  const fetchCurrentUser = async () => {
    try {
      const response = await getLoginUser()
      if (response.data.code === 0 && response.data.data) {
        saveUserInfo(response.data.data)
        return response.data
      } else {
        clearUserInfo()
        return null
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
      clearUserInfo()
      return null
    } finally {
    }
  }

  // 用户登录
  const login = async (loginData: API.UserLoginRequest) => {
    try {
      const response = await userLogin(loginData)
      if (response.data.code === 0 && response.data.data) {
        saveUserInfo(response.data.data)
        message.success('登录成功')
        return { success: true, data: response.data }
      } else {
        message.error(response.data.message || '登录失败')
        return { success: false, message: response.data.message || '登录失败' }
      }
    } catch (error) {
      console.error('登录失败:', error)
      message.error('登录失败，请检查网络连接')
      return { success: false, message: '登录失败，请检查网络连接' }
    }
  }

  // 用户注册
  const register = async (registerData: API.UserRegisterRequest) => {
    try {
      const response = await userRegister(registerData)
      if (response.data.code === 0) {
        message.success('注册成功，请登录')
        return { success: true }
      } else {
        message.error(response.data.message || '注册失败')
        return { success: false, message: response.data.message || '注册失败' }
      }
    } catch (error) {
      console.error('注册失败:', error)
      message.error('注册失败，请检查网络连接')
      return { success: false, message: '注册失败，请检查网络连接' }
    }
  }

  // 用户登出
  const logout = async () => {
    try {
      await userLogout()
      clearUserInfo()
      message.success('退出登录成功')
      return { success: true }
    } catch (error) {
      console.error('退出登录失败:', error)
      // 即使接口调用失败，也清除本地用户信息
      clearUserInfo()
      message.success('退出登录成功')
      return { success: true }
    }
  }

  // 更新用户信息
  const updateUserInfo = (newUserInfo: Partial<API.UserVO>) => {
    if (userInfo.value) {
      const updatedUser = { ...userInfo.value, ...newUserInfo }
      saveUserInfo(updatedUser)
    }
  }

  return {
    // 状态
    userInfo,

    // 计算属性
    isLoggedIn,
    isAdmin,
    userName,
    userAvatar,

    // 方法
    initUserInfo,
    fetchCurrentUser,
    login,
    register,
    logout,
    updateUserInfo,
    clearUserInfo
  }
})
