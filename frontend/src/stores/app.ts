import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export const useAppStore = defineStore('app', () => {
  // 授权状态
  const authorized = ref(false);
  const authChecked = ref(false);

  // 侧边栏
  const sidebarCollapsed = ref(false);

  // 全局加载
  const globalLoading = ref(false);
  const globalMessage = ref('');

  function setAuthorized(val: boolean) {
    authorized.value = val;
    authChecked.value = true;
  }

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value;
  }

  function showLoading(msg: string) {
    globalLoading.value = true;
    globalMessage.value = msg;
  }

  function hideLoading() {
    globalLoading.value = false;
    globalMessage.value = '';
  }

  const isReady = computed(() => authChecked.value && authorized.value);

  return {
    authorized,
    authChecked,
    sidebarCollapsed,
    globalLoading,
    globalMessage,
    setAuthorized,
    toggleSidebar,
    showLoading,
    hideLoading,
    isReady,
  };
});
