<script setup lang="ts">
import { ref, watch, nextTick, onUnmounted } from 'vue';
import type { ExecutionEvent } from '@/types';

const props = defineProps<{
  events: ExecutionEvent[];
  loading?: boolean;
  status?: string;
}>();

const container = ref<HTMLElement | null>(null);

watch(
  () => props.events.length,
  async () => {
    await nextTick();
    if (container.value) {
      container.value.scrollTop = container.value.scrollHeight;
    }
  },
);

function eventIcon(type: string): string {
  const map: Record<string, string> = {
    STRATEGY_SELECTED: '🎯',
    PROMPT_GENERATED: '💬',
    TARGET_RESPONSE_RECEIVED: '📩',
    WAITING_USER_INPUT: '⏳',
    EVALUATION_DONE: '📊',
    ATTACK_SUCCESS: '✅',
    ATTACK_FAILED: '❌',
    TASK_FINISHED: '🏁',
    ERROR: '⚠️',
    COST_UPDATE: '💰',
    BUDGET_WARNING: '🟡',
    BUDGET_EXCEEDED: '🔴',
    MODE_DEGRADE: '🔽',
  };
  return map[type] ?? '📌';
}

function eventLabel(type: string): string {
  const map: Record<string, string> = {
    STRATEGY_SELECTED: '策略选定',
    PROMPT_GENERATED: 'Prompt 已生成',
    TARGET_RESPONSE_RECEIVED: '收到目标回答',
    WAITING_USER_INPUT: '等待用户输入',
    EVALUATION_DONE: '评估完成',
    ATTACK_SUCCESS: '攻击成功',
    ATTACK_FAILED: '攻击未成功',
    TASK_FINISHED: '任务结束',
    ERROR: '异常',
    COST_UPDATE: '成本更新',
    BUDGET_WARNING: '预算预警',
    BUDGET_EXCEEDED: '预算超限',
    MODE_DEGRADE: '策略降级',
  };
  return map[type] ?? type;
}

function formatTime(ts: string): string {
  return new Date(ts).toLocaleTimeString('zh-CN', { hour12: false });
}

function fmtPayload(payload: Record<string, unknown>): string {
  const skip = ['timestamp', 'type', 'round'];
  const entries = Object.entries(payload).filter(([k]) => !skip.includes(k));
  if (entries.length === 0) return '';
  return entries
    .map(([k, v]) => {
      const val = typeof v === 'string' ? v : JSON.stringify(v);
      return `${k}: ${val.length > 80 ? val.slice(0, 80) + '…' : val}`;
    })
    .join('\n');
}
</script>

<template>
  <div
    ref="container"
    class="bg-primary-800 rounded-soft overflow-y-auto font-mono text-xs leading-relaxed p-4 space-y-2"
    style="max-height: 480px"
  >
    <!-- 空状态 -->
    <div v-if="!loading && events.length === 0" class="text-primary-500 text-center py-10">
      <p class="text-lg mb-1">🌊</p>
      <p>等待事件流连接…</p>
      <p v-if="status" class="text-primary-400 mt-1">{{ status }}</p>
    </div>

    <!-- 加载态 -->
    <div v-if="loading" class="text-primary-400 text-center py-10">
      <span class="pulse-dot mr-2" />
      正在连接事件流…
    </div>

    <!-- 事件列表 -->
    <div
      v-for="(event, idx) in events"
      :key="idx"
      class="terminal-line flex gap-3"
    >
      <span class="shrink-0 text-[10px] text-primary-500 select-none w-20 text-right">
        {{ formatTime(event.timestamp) }}
      </span>
      <span class="shrink-0 text-sm">{{ eventIcon(event.type) }}</span>
      <div class="min-w-0">
        <span
          class="font-semibold"
          :class="{
            'text-sage-300': event.type === 'ATTACK_SUCCESS' || event.type === 'TASK_FINISHED',
            'text-accent-300': event.type === 'ATTACK_FAILED' || event.type === 'ERROR' || event.type === 'BUDGET_EXCEEDED',
            'text-amber-300': event.type === 'BUDGET_WARNING' || event.type === 'MODE_DEGRADE',
            'text-primary-200': true,
          }"
        >
          {{ eventLabel(event.type) }}
        </span>
        <span class="text-primary-500 ml-2">[轮次 {{ event.round }}]</span>
        <div
          v-if="fmtPayload(event.payload)"
          class="text-primary-400 mt-0.5 whitespace-pre-wrap break-all"
        >
          {{ fmtPayload(event.payload) }}
        </div>
      </div>
    </div>
  </div>
</template>
