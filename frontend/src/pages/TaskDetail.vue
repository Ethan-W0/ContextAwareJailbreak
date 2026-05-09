<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { startTask, pauseTask, resumeTask, stopTask } from '@/api/task';
import { connectSSE } from '@/api/sse';
import type { ExecutionEvent, CostBudget, AttackState, RoundDetail } from '@/types';
import EventTerminal from '@/components/EventTerminal.vue';
import CostBar from '@/components/CostBar.vue';
import RoundTimeline from '@/components/RoundTimeline.vue';
import ErrorMessage from '@/components/ErrorMessage.vue';

const route = useRoute();
const router = useRouter();
const taskId = route.params.taskId as string;

// 状态
const events = ref<ExecutionEvent[]>([]);
const sseConnected = ref(false);
const taskStatus = ref<string>('CREATED');
const currentRound = ref(0);
const maxRounds = ref(10);
const costBudget = ref<CostBudget | null>(null);
const rounds = ref<RoundDetail[]>([]);
const error = ref('');
const acting = ref(false);

// 计算属性
const canStart = computed(() => ['CREATED', 'PAUSED'].includes(taskStatus.value));
const canPause = computed(() => taskStatus.value === 'RUNNING');
const canResume = computed(() => taskStatus.value === 'PAUSED');
const canStop = computed(() => ['RUNNING', 'PAUSED'].includes(taskStatus.value));
const isRunning = computed(() => taskStatus.value === 'RUNNING');
const isFinished = computed(() => ['FINISHED', 'TERMINATED'].includes(taskStatus.value));

let closeSSE: (() => void) | null = null;

function connect() {
  sseConnected.value = true;
  closeSSE = connectSSE(
    `/task/${taskId}/execute-stream`,
    (event: ExecutionEvent) => {
      events.value.push(event);
      currentRound.value = event.round;

      switch (event.type) {
        case 'COST_UPDATE':
          if (event.payload.budget) {
            costBudget.value = event.payload.budget as CostBudget;
          }
          break;

        case 'MODE_DEGRADE':
          if (costBudget.value) {
            costBudget.value.exceeded_action = 'DEGRADE';
          }
          break;

        case 'BUDGET_EXCEEDED':
          error.value = '预算已达上限，任务已自动停止';
          break;

        case 'ATTACK_SUCCESS':
          taskStatus.value = 'FINISHED';
          break;

        case 'TASK_FINISHED':
          taskStatus.value = 'FINISHED';
          break;

        case 'ERROR':
          error.value = (event.payload.message as string) ?? '发生错误';
          break;

        // 收集轮次数据
        case 'PROMPT_GENERATED':
        case 'TARGET_RESPONSE_RECEIVED':
        case 'EVALUATION_DONE':
          // 这些事件会被用于构建轮次详情
          break;
      }
    },
    () => {
      sseConnected.value = false;
    },
  );
}

// 操作
async function handleStart() {
  acting.value = true;
  try {
    await startTask(taskId);
    taskStatus.value = 'RUNNING';
    connect();
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '启动失败';
  } finally {
    acting.value = false;
  }
}

async function handlePause() {
  acting.value = true;
  try {
    await pauseTask(taskId);
    taskStatus.value = 'PAUSED';
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '暂停失败';
  } finally {
    acting.value = false;
  }
}

async function handleResume() {
  acting.value = true;
  try {
    await resumeTask(taskId);
    taskStatus.value = 'RUNNING';
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '恢复失败';
  } finally {
    acting.value = false;
  }
}

async function handleStop() {
  acting.value = true;
  try {
    await stopTask(taskId);
    taskStatus.value = 'TERMINATED';
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '终止失败';
  } finally {
    acting.value = false;
  }
}

function viewReport() {
  router.push(`/report/${taskId}`);
}

onMounted(() => {
  // 页面加载后自动连接 SSE，后端会推送任务状态
  connect();
});

onUnmounted(() => {
  closeSSE?.();
});
</script>

<template>
  <div class="max-w-6xl mx-auto px-4 lg:px-8 py-6 lg:py-10">
    <!-- 页头 -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between mb-6 gap-4">
      <div>
        <button
          class="text-xs text-primary-400 hover:text-primary-600 transition-colors mb-2 tracking-wider"
          @click="router.push('/console')"
        >
          ← 返回控制台
        </button>
        <h2 class="text-xl font-display font-bold text-primary-700 tracking-wide">
          任务详情
        </h2>
        <p class="text-xs text-primary-400 font-mono mt-1">{{ taskId }}</p>
      </div>

      <div class="flex items-center gap-2">
        <span
          v-if="isRunning"
          class="flex items-center gap-1.5 text-xs text-sage-600"
        >
          <span class="pulse-dot" />
          运行中
        </span>
        <span v-else-if="taskStatus === 'PAUSED'" class="text-xs text-amber-600">已暂停</span>
        <span v-else-if="isFinished" class="text-xs text-primary-400">已结束</span>
      </div>
    </div>

    <!-- 操作栏 -->
    <div class="card !p-3 mb-6 flex flex-wrap items-center gap-2">
      <button
        v-if="canStart"
        class="btn-primary text-xs py-2 px-4"
        :disabled="acting"
        @click="handleStart"
      >
        ▶ 启动攻击
      </button>
      <button
        v-if="canPause"
        class="btn-secondary text-xs py-2 px-4"
        :disabled="acting"
        @click="handlePause"
      >
        ⏸ 暂停
      </button>
      <button
        v-if="canResume"
        class="btn-primary text-xs py-2 px-4"
        :disabled="acting"
        @click="handleResume"
      >
        ▶ 恢复
      </button>
      <button
        v-if="canStop"
        class="btn-danger text-xs py-2 px-4"
        :disabled="acting"
        @click="handleStop"
      >
        ⏹ 终止
      </button>

      <div class="h-5 w-px bg-primary-200 mx-1 hidden sm:block" />

      <span class="text-xs text-primary-400 tracking-wider">
        轮次: {{ currentRound }} / {{ maxRounds }}
      </span>

      <div class="flex-1" />

      <button
        v-if="isFinished"
        class="btn-primary text-xs py-2 px-4"
        @click="viewReport"
      >
        查看报告
      </button>
    </div>

    <ErrorMessage v-if="error" :message="error" class="mb-6" />

    <div class="grid grid-cols-1 xl:grid-cols-3 gap-6">
      <!-- 左侧：SSE 终端 -->
      <div class="xl:col-span-2">
        <EventTerminal
          :events="events"
          :loading="sseConnected && events.length === 0"
          :status="sseConnected ? 'SSE 已连接' : 'SSE 未连接'"
        />
      </div>

      <!-- 右侧：成本 & 信息 -->
      <div class="space-y-4">
        <CostBar :budget="costBudget" />

        <!-- 状态卡片 -->
        <div class="card">
          <h4 class="text-xs font-semibold text-primary-500 tracking-wider mb-3">任务信息</h4>
          <dl class="space-y-2 text-xs">
            <div class="flex justify-between">
              <dt class="text-primary-400">状态</dt>
              <dd class="text-primary-600 font-medium">{{ taskStatus }}</dd>
            </div>
            <div class="flex justify-between">
              <dt class="text-primary-400">当前轮次</dt>
              <dd class="text-primary-600 font-medium">{{ currentRound }} / {{ maxRounds }}</dd>
            </div>
            <div class="flex justify-between">
              <dt class="text-primary-400">SSE 连接</dt>
              <dd :class="sseConnected ? 'text-sage-500' : 'text-accent-400'" class="font-medium">
                {{ sseConnected ? '已连接' : '未连接' }}
              </dd>
            </div>
          </dl>
        </div>
      </div>
    </div>

    <!-- 底部：轮次时间线 -->
    <div v-if="rounds.length > 0" class="mt-8">
      <h3 class="text-base font-bold text-primary-600 mb-4 tracking-wide">攻击轮次</h3>
      <RoundTimeline :rounds="rounds" />
    </div>
  </div>
</template>
