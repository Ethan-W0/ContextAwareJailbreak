<script setup lang="ts">
import type { RoundDetail } from '@/types';

defineProps<{
  rounds: RoundDetail[];
  loading?: boolean;
}>();

const refusalLabel: Record<string, string> = {
  HARD_REFUSAL: '硬拒绝',
  SOFT_REFUSAL: '软拒绝',
  TOPIC_SHIFT: '话题转移',
  COMPLIANCE: '已合规',
};

const refusalColor: Record<string, string> = {
  HARD_REFUSAL: 'bg-accent-100 text-accent-700',
  SOFT_REFUSAL: 'bg-amber-100 text-amber-700',
  TOPIC_SHIFT: 'bg-primary-100 text-primary-600',
  COMPLIANCE: 'bg-sage-100 text-sage-700',
};

function scoreColor(score: number): string {
  if (score >= 0.8) return 'text-accent-500';
  if (score >= 0.4) return 'text-amber-500';
  return 'text-primary-400';
}
</script>

<template>
  <div class="space-y-4">
    <!-- 骨架屏 -->
    <div v-if="loading" class="space-y-4">
      <div v-for="i in 2" :key="i" class="card animate-pulse">
        <div class="shimmer h-4 w-20 rounded mb-3" />
        <div class="shimmer h-3 w-full rounded mb-2" />
        <div class="shimmer h-3 w-3/4 rounded" />
      </div>
    </div>

    <!-- 空状态 -->
    <EmptyState
      v-else-if="rounds.length === 0"
      icon="💭"
      title="尚无攻击记录"
      description="创建会话后将在此展示每轮攻击的时间线"
    />

    <!-- 时间线 -->
    <div v-else class="space-y-3">
      <div
        v-for="(rd, idx) in rounds"
        :key="idx"
        class="card !p-5 animate-slide-up"
        :style="{ animationDelay: idx * 50 + 'ms' }"
      >
        <!-- 头部 -->
        <div class="flex items-center justify-between mb-3">
          <div class="flex items-center gap-3">
            <span class="text-sm font-bold text-primary-600 tracking-wider">
              第 {{ rd.round }} 轮
            </span>
            <span v-if="rd.vector_name" class="badge-primary">
              {{ rd.vector_name }}
            </span>
            <span
              v-if="rd.refusal_type"
              class="badge"
              :class="refusalColor[rd.refusal_type] || 'bg-primary-100 text-primary-600'"
            >
              {{ refusalLabel[rd.refusal_type] || rd.refusal_type }}
            </span>
          </div>
          <span class="text-xs font-medium" :class="scoreColor(rd.harmfulness_score)">
            {{ (rd.harmfulness_score * 100).toFixed(0) }}% 有害度
          </span>
        </div>

        <!-- 攻击 Prompt -->
        <div class="mb-3">
          <span class="label-text">攻击 Prompt</span>
          <div class="bg-primary-50 rounded-soft p-3 text-sm leading-relaxed text-primary-700 max-h-32 overflow-y-auto">
            {{ rd.attack_prompt }}
          </div>
        </div>

        <!-- 目标回答 -->
        <div>
          <span class="label-text">目标模型回答</span>
          <div class="bg-primary-50 rounded-soft p-3 text-sm leading-relaxed text-primary-700 max-h-32 overflow-y-auto">
            {{ rd.target_response }}
          </div>
        </div>

        <!-- 策略理由 -->
        <div v-if="rd.reason" class="mt-3 pt-3 border-t border-primary-100">
          <span class="text-xs text-primary-400 italic">{{ rd.reason }}</span>
        </div>
      </div>
    </div>
  </div>
</template>
