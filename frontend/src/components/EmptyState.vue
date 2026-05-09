<script setup lang="ts">
defineProps<{
  icon?: string;
  title?: string;
  description?: string;
}>();
</script>

<template>
  <div class="flex flex-col items-center justify-center py-20 text-center animate-fade-in">
    <span class="text-5xl mb-4 select-none">{{ icon ?? '🌿' }}</span>
    <h3 class="text-base font-medium text-primary-500 mb-2 tracking-wide">
      {{ title ?? '这里还空空的呢' }}
    </h3>
    <p class="text-sm text-primary-300 max-w-xs leading-relaxed">
      {{ description ?? '还没有任何内容，去创建一个吧' }}
    </p>
  </div>
</template>
