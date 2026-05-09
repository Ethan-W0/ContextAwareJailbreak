<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router';
import { computed } from 'vue';

const route = useRoute();
const router = useRouter();

interface NavItem {
  path: string;
  label: string;
  icon: string;
}

const navItems: NavItem[] = [
  { path: '/workshop', label: '交互式攻击工坊', icon: '✍' },
  { path: '/console', label: '全自动攻击控制台', icon: '⚡' },
  { path: '/vectors', label: '攻击向量库', icon: '📚' },
];

function isActive(item: NavItem): boolean {
  return route.path.startsWith(item.path);
}

function navigate(item: NavItem) {
  router.push(item.path);
}
</script>

<template>
  <aside
    class="
      fixed bottom-0 left-0 z-30
      w-full h-14
      lg:top-0 lg:h-full lg:w-56 lg:border-r
      bg-white/90 backdrop-blur-sm
      border-t border-primary-100
      lg:border-t-0 lg:border-r
      flex flex-row lg:flex-col
      items-center lg:items-stretch
      justify-around lg:justify-start
      lg:pt-8
      lg:shadow-none shadow-soft
    "
  >
    <!-- 桌面端 logo -->
    <div class="hidden lg:block px-6 mb-10">
      <router-link to="/" class="block group">
        <h1 class="text-2xl font-display font-bold text-primary-600 tracking-wider group-hover:text-primary-700 transition-colors">
          轻舟
        </h1>
        <p class="text-[10px] text-primary-300 tracking-[0.15em] mt-0.5">
          上下文感知越狱代理
        </p>
      </router-link>
    </div>

    <!-- 导航项 -->
    <nav class="flex lg:flex-col gap-0.5 lg:px-3 w-full lg:w-auto justify-around lg:justify-start">
      <button
        v-for="item in navItems"
        :key="item.path"
        @click="navigate(item)"
        class="
          flex flex-col lg:flex-row items-center justify-center lg:justify-start
          gap-1 lg:gap-3
          px-2 lg:px-4 py-1.5 lg:py-2.5
          rounded-soft
          text-xs lg:text-sm
          font-medium
          transition-all duration-200 ease-out
          tracking-wide
        "
        :class="
          isActive(item)
            ? 'bg-primary-100 text-primary-700'
            : 'text-primary-400 hover:text-primary-600 hover:bg-primary-50'
        "
      >
        <span class="text-base lg:text-lg">{{ item.icon }}</span>
        <span class="text-[10px] lg:text-sm whitespace-nowrap">{{ item.label }}</span>
      </button>
    </nav>

    <!-- 底部信息 -->
    <div class="hidden lg:block mt-auto px-6 pb-8">
      <router-link to="/" class="text-xs text-primary-300 hover:text-primary-400 transition-colors tracking-wider">
        ← 返回首页
      </router-link>
    </div>
  </aside>
</template>
