const TOKEN_KEY = 'huzhu-token'
const USER_KEY = 'huzhu-user'

export interface HuzhuAuthUser {
  id?: number
  username?: string
  realName?: string
  roleId?: number
  [key: string]: unknown
}

export const setHuzhuAuth = (token: string, user?: HuzhuAuthUser | null) => {
  localStorage.setItem(TOKEN_KEY, token)
  localStorage.setItem(USER_KEY, JSON.stringify(user || {}))
}

export const clearHuzhuAuth = () => {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export const getHuzhuToken = () => localStorage.getItem(TOKEN_KEY)

export const getHuzhuUser = (): HuzhuAuthUser | null => {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
}

export const isHuzhuAuthenticated = () => Boolean(getHuzhuToken())
