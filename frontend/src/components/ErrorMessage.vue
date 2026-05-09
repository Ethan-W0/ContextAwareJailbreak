<script setup lang="ts">
defineProps<{
  message?: string;
  retry?: () => void;
}>();

const emit = defineEmits<{
  retry: [];
}>();
</script>

<template>
  <div class="flex flex-col items-center justify-center py-16 text-center animate-fade-in">
    <span class="text-4xl mb-3 select-none">🌸</span>
    <h3 class="text-base font-medium text-accent-500 mb-2 tracking-wide">
      遇到了一点小问题
    </h3>
    <p class="text-sm text-primary-400 max-w-xs leading-relaxed mb-5">
      {{ message ?? '暂时无法加载内容，请稍后重试' }}
    </p>
    <button
      v-if="retry"
      class="btn-secondary text-xs"
      @click="emit('retry'); retry?.()"
    >
      再试一次
    </button>
  </div>
</template>
