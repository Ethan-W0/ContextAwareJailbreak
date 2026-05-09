<script setup lang="ts">
import { RouterView } from 'vue-router';
import Sidebar from '@/components/Sidebar.vue';
import AuthGuard from '@/components/AuthGuard.vue';
import { useAppStore } from '@/stores/app';
import { useRoute } from 'vue-router';
import { computed } from 'vue';

const appStore = useAppStore();
const route = useRoute();

const isHomePage = computed(() => route.path === '/');
</script>

<template>
  <AuthGuard>
    <div class="flex h-screen overflow-hidden">
      <Sidebar v-if="!isHomePage" />
      <main
        class="flex-1 overflow-y-auto transition-all duration-300"
        :class="isHomePage ? '' : 'ml-0 lg:ml-56'"
      >
        <RouterView v-slot="{ Component: Page }">
          <transition name="page" mode="out-in">
            <component :is="Page" />
          </transition>
        </RouterView>
      </main>
    </div>
  </AuthGuard>
</template>

<style>
.page-enter-active,
.page-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.page-enter-from {
  opacity: 0;
  transform: translateY(6px);
}
.page-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>
