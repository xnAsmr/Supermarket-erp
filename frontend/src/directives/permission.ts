import type { Directive } from 'vue'
import { useUserStore } from '../stores/user'

const ALL_PERMISSION = '*:*:*'

export function hasPermission(permission: string | string[]): boolean {
  const userStore = useUserStore()
  const perms = userStore.userInfo?.permissions
  if (!perms) return false
  if (perms.includes(ALL_PERMISSION)) return true
  const list = Array.isArray(permission) ? permission : [permission]
  return list.some(p => perms.includes(p))
}

const permission: Directive<HTMLElement, string | string[]> = {
  mounted(el, binding) {
    if (!hasPermission(binding.value)) {
      el.parentNode?.removeChild(el)
    }
  }
}

export default permission
