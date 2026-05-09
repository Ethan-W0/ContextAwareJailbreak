<script setup lang="ts">
import { computed } from 'vue';
import type { CostBudget } from '@/types';

const props = defineProps<{
  budget?: CostBudget | null;
}>();

const percent = computed(() => {
  if (!props.budget || props.budget.max_total_tokens === 0) return 0;
  return Math.min((props.budget.tokens_used / props.budget.max_total_tokens) * 100, 100);
});

const statusColor = computed(() => {
  const p = percent.value;
  if (p >= 95) return 'bg-accent-400';
  if (p >= 80) return 'bg-amber-400';
  return 'bg-sage-400';
});

function fmtTokens(n: number): string {
  if (n >= 1_000_000) return (n / 1_000_000).toFixed(1) + 'M';
  if (n >= 1_000) return (n / 1_000).toFixed(0) + 'K';
  return n.toString();
}

function fmtCost(n: number): string {
  return '$' + n.toFixed(2);
}
</script>

<template>
  <div v-if="budget" class="card !p-4 space-y-2">
    <div class="flex items-center justify-between text-xs">
      <span class="text-primary-400 tracking-wider">成本预算</span>
      <span class="text-primary-500 font-medium">
        {{ fmtTokens(budget.tokens_used) }} / {{ fmtTokens(budget.max_total_tokens) }} tokens
      </span>
    </div>

    <!-- 进度条 -->
    <div class="h-1.5 bg-primary-100 rounded-full overflow-hidden">
      <div
        class="h-full rounded-full transition-all duration-500 ease-out"
        :class="statusColor"
        :style="{ width: percent + '%' }"
      />
    </div>

    <div class="flex items-center justify-between text-[10px] text-primary-300">
      <span>费用: {{ fmtCost(budget.cost_accrued) }} / {{ fmtCost(budget.max_estimated_cost) }}</span>
      <span>{{ percent.toFixed(0) }}%</span>
    </div>

    <!-- 分项统计 -->
    <div class="grid grid-cols-4 gap-2 pt-1">
      <div class="text-center">
        <div class="text-[10px] text-primary-300">决策</div>
        <div class="text-xs font-medium text-primary-500">{{ fmtTokens(budget.breakdown.decision_tokens) }}</div>
      </div>
      <div class="text-center">
        <div class="text-[10px] text-primary-300">评估</div>
        <div class="text-xs font-medium text-primary-500">{{ fmtTokens(budget.breakdown.evaluation_tokens) }}</div>
      </div>
      <div class="text-center">
        <div class="text-[10px] text-primary-300">仲裁</div>
        <div class="text-xs font-medium text-primary-500">{{ fmtTokens(budget.breakdown.arbitration_tokens) }}</div>
      </div>
      <div class="text-center">
        <div class="text-[10px] text-primary-300">目标</div>
        <div class="text-xs font-medium text-primary-500">{{ fmtTokens(budget.breakdown.target_tokens) }}</div>
      </div>
    </div>
  </div>
</template>
