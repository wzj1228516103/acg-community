import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loginApi, registerApi, getUserInfoApi, logoutApi } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role >= 3)
  const isMakeupArtist = computed(() => user.value?.role === 1)
  const isMerchant = computed(() => user.value?.role === 2)

  async function login(form) {
    const res = await loginApi(form)
    token.value = res.data.token
    user.value = res.data.user
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('user', JSON.stringify(res.data.user))
    return res
  }

  async function register(form) {
    return await registerApi(form)
  }

  async function fetchUserInfo() {
    const res = await getUserInfoApi()
    user.value = res.data
    localStorage.setItem('user', JSON.stringify(res.data))
    return res.data
  }

  async function logout() {
    try {
      await logoutApi()
    } catch (e) {
      // ignore
    }
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return { token, user, isLoggedIn, isAdmin, isMakeupArtist, isMerchant, login, register, fetchUserInfo, logout }
})
