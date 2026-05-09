<script setup lang="ts">
defineProps<{
  lines?: number;
  width?: string;
  height?: string;
}>();
</script>

<template>
  <div class="animate-pulse space-y-3">
    <div
      v-for="i in (lines ?? 3)"
      :key="i"
      class="shimmer rounded-soft"
      :style="{ width: i === lines ? (width ?? '60%') : '100%', height: height ?? '14px' }"
    />
  </div>
</template>
