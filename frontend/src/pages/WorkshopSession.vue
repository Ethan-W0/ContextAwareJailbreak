<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { submitAnswer, getSessionState } from '@/api/session';
import { connectSSE } from '@/api/sse';
import type { AttackState, RoundDetail, ExecutionEvent, CostBudget } from '@/types';
import RoundTimeline from '@/components/RoundTimeline.vue';
import CostBar from '@/components/CostBar.vue';
import ErrorMessage from '@/components/ErrorMessage.vue';
import Skeleton from '@/components/Skeleton.vue';

const route = useRoute();
const router = useRouter();
const sessionId = route.params.sessionId as string;

// 状态
const attackState = ref<AttackState | null>(null);
const targetResponse = ref('');
const submitting = ref(false);
const loading = ref(true);
const error = ref('');
const sseEvents = ref<ExecutionEvent[]>([]);
const waitingForInput = ref(false);
const isFinished = ref(false);

let closeSSE: (() => void) | null = null;

// 计算属性
const rounds = computed<RoundDetail[]>(() => attackState.value?.rounds ?? []);
const currentPrompt = computed(() => attackState.value?.last_attack_prompt ?? '');
const costBudget = computed<CostBudget | null>(() => attackState.value?.cost_budget ?? null);
const lastEvaluation = computed(() => attackState.value?.last_evaluation);

const refusalLabel: Record<string, string> = {
  HARD_REFUSAL: '硬拒绝',
  SOFT_REFUSAL: '软拒绝',
  TOPIC_SHIFT: '话题转移',
  COMPLIANCE: '已合规',
};

function copyToClipboard(text: string) {
  navigator.clipboard.writeText(text);
}

const refusalColor: Record<string, string> = {
  HARD_REFUSAL: 'text-accent-500',
  SOFT_REFUSAL: 'text-amber-500',
  TOPIC_SHIFT: 'text-primary-500',
  COMPLIANCE: 'text-sage-500',
};

// 加载会话状态
async function loadState() {
  try {
    loading.value = true;
    const state = await getSessionState(sessionId);
    attackState.value = state;
    waitingForInput.value = !!(state.last_attack_prompt && !state.attack_success
      && !(state.current_round >= state.max_rounds));
    if (state.attack_success || (state.current_round >= state.max_rounds && !state.attack_success)) {
      isFinished.value = true;
    }
  } catch {
    error.value = '无法加载会话状态';
  } finally {
    loading.value = false;
  }
}

// SSE 连接
function connect() {
  closeSSE = connectSSE(
    `/session/${sessionId}/stream`,
    (event: ExecutionEvent) => {
      sseEvents.value.push(event);

      switch (event.type) {
        case 'PROMPT_GENERATED':
        case 'STRATEGY_SELECTED':
          waitingForInput.value = true;
          if (event.payload.attack_prompt) {
            attackState.value = {
              ...attackState.value!,
              last_attack_prompt: event.payload.attack_prompt as string,
              current_round: event.round,
              current_vector_id: event.payload.vector_id as string,
              strategy_reason: event.payload.reason as string,
            };
          }
          break;

        case 'EVALUATION_DONE':
          waitingForInput.value = false;
          if (attackState.value) {
            attackState.value = {
              ...attackState.value!,
              harmfulness_score: (event.payload.harmfulnessScore as number) ?? 0,
              last_refusal_type: event.payload.refusalType as AttackState['last_refusal_type'],
              last_evaluation: {
                harmfulness_score: (event.payload.harmfulnessScore as number) ?? 0,
                refusal_type: (event.payload.refusalType ?? 'HARD_REFUSAL') as AttackState['last_refusal_type'],
                summary: (event.payload.summary as string) ?? '',
              },
            };
          }
          break;

        case 'ATTACK_SUCCESS':
          isFinished.value = true;
          waitingForInput.value = false;
          if (attackState.value) {
            attackState.value.attack_success = true;
          }
          // 刷新状态获取完整 rounds
          loadState();
          break;

        case 'ATTACK_FAILED':
          // 本轮未成功，但会话继续 — 策略引擎将切换到下一轮
          waitingForInput.value = false;
          break;

        case 'TASK_FINISHED':
          isFinished.value = true;
          waitingForInput.value = false;
          loadState();
          break;

        case 'WAITING_USER_INPUT':
          waitingForInput.value = true;
          break;

        case 'COST_UPDATE':
          if (event.payload.budget && attackState.value) {
            attackState.value.cost_budget = event.payload.budget as CostBudget;
          }
          break;

        case 'ERROR':
          error.value = (event.payload.message as string) ?? '发生了未知错误';
          break;
      }
    },
    () => {
      // SSE 连接失败时静默处理
    },
  );
}

// 提交目标回答
async function handleSubmitAnswer() {
  if (!targetResponse.value.trim()) return;

  submitting.value = true;
  error.value = '';

  try {
    const state = await submitAnswer(sessionId, {
      target_response: targetResponse.value.trim(),
    });
    attackState.value = state;
    targetResponse.value = '';

    // Determine next state from the returned AttackState, not blind false.
    // SSE events may have already fired PROMPT_GENERATED (setting waiting=true),
    // but we are the authoritative source after the POST completes.
    if (state.attack_success) {
      isFinished.value = true;
      waitingForInput.value = false;
    } else if (state.current_round >= state.max_rounds) {
      isFinished.value = true;
      waitingForInput.value = false;
    } else {
      // Session continues — show input area for the next round
      waitingForInput.value = true;
    }
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : '提交失败';
    error.value = msg;
  } finally {
    submitting.value = false;
  }
}

onMounted(async () => {
  await loadState();
  connect();
});

onUnmounted(() => {
  closeSSE?.();
});
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 lg:px-8 py-6 lg:py-10">
    <!-- 页头 -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between mb-8 gap-4">
      <div>
        <button
          class="text-xs text-primary-400 hover:text-primary-600 transition-colors mb-2 tracking-wider"
          @click="router.push('/workshop')"
        >
          ← 返回工坊
        </button>
        <h2 class="text-xl font-display font-bold text-primary-700 tracking-wide">
          攻击会话
        </h2>
        <p class="text-xs text-primary-400 mt-1">
          {{ attackState?.target_model }} · {{ attackState?.attack_intent?.slice(0, 30) }}{{ (attackState?.attack_intent?.length ?? 0) > 30 ? '…' : '' }}
        </p>
      </div>

      <!-- 进度指示 -->
      <div class="flex items-center gap-2 text-sm text-primary-500">
        <span class="text-xs tracking-wider">
          第 {{ attackState?.current_round ?? 0 }} / {{ attackState?.max_rounds ?? '-' }} 轮
        </span>
        <span v-if="attackState?.attack_success" class="badge-sage">攻击成功</span>
        <span v-else-if="isFinished" class="badge-accent">已达上限</span>
        <span v-else-if="waitingForInput" class="badge-primary animate-pulse-soft">等待输入</span>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- 左侧：交互区域 -->
      <div class="lg:col-span-2 space-y-6">
        <!-- 当前越狱 Prompt -->
        <div v-if="currentPrompt" class="card animate-slide-up">
          <div class="flex items-center justify-between mb-3">
            <span class="label-text">当前越狱 Prompt</span>
            <button
              class="text-xs text-primary-400 hover:text-primary-600 transition-colors"
              @click="copyToClipboard(currentPrompt)"
            >
              复制到剪贴板
            </button>
          </div>
          <div
            class="bg-primary-50 rounded-soft p-4 text-sm leading-relaxed text-primary-700 max-h-48 overflow-y-auto whitespace-pre-wrap"
          >
            {{ currentPrompt }}
          </div>
          <p v-if="attackState?.strategy_reason" class="text-xs text-primary-400 mt-3 italic">
            💡 {{ attackState.strategy_reason }}
          </p>
        </div>

        <!-- 评估结果 -->
        <div v-if="lastEvaluation" class="card animate-slide-up">
          <h4 class="text-sm font-bold text-primary-600 mb-3 tracking-wide">上一轮评估</h4>
          <div class="flex items-center gap-4">
            <div class="text-center">
              <div class="text-3xl font-bold" :class="(lastEvaluation.harmfulness_score >= 0.8) ? 'text-accent-500' : 'text-primary-400'">
                {{ (lastEvaluation.harmfulness_score * 100).toFixed(0) }}%
              </div>
              <div class="text-[10px] text-primary-300 mt-1">有害度</div>
            </div>
            <div class="h-10 w-px bg-primary-100" />
            <div>
              <div class="text-sm font-medium" :class="refusalColor[lastEvaluation.refusal_type]">
                {{ refusalLabel[lastEvaluation.refusal_type] ?? lastEvaluation.refusal_type }}
              </div>
              <div class="text-xs text-primary-400 mt-0.5">{{ lastEvaluation.summary }}</div>
            </div>
          </div>
        </div>

        <!-- 粘贴回答区 -->
        <div v-if="waitingForInput && !isFinished" class="card animate-slide-up">
          <h4 class="text-sm font-bold text-primary-600 mb-3 tracking-wide">
            粘贴目标模型的回答
          </h4>
          <textarea
            v-model="targetResponse"
            class="textarea-field mb-3"
            rows="5"
            placeholder="将目标模型的完整回答粘贴到这里…&#10;&#10;例如：对不起，我不能提供制作危险物品的步骤。作为AI助手，我的职责是…"
          />
          <button
            class="btn-primary w-full text-sm py-2.5 tracking-wider"
            :disabled="submitting || !targetResponse.trim()"
            @click="handleSubmitAnswer"
          >
            <span v-if="submitting" class="pulse-dot mr-2" />
            {{ submitting ? '正在评估中…' : '提交回答，继续下一轮' }}
          </button>
          <ErrorMessage v-if="error" :message="error" class="!py-0 mt-3" />
        </div>

        <!-- 结束提示 -->
        <div v-if="isFinished" class="card text-center animate-slide-up">
          <div class="text-4xl mb-3">{{ attackState?.attack_success ? '🎉' : '🌿' }}</div>
          <h4 class="text-lg font-bold text-primary-700 mb-2 tracking-wide">
            {{ attackState?.attack_success ? '攻击成功！' : '会话已结束' }}
          </h4>
          <p class="text-sm text-primary-400 mb-4">
            {{
              attackState?.attack_success
                ? `目标模型在第 ${attackState.current_round} 轮被突破`
                : `已完成 ${attackState?.current_round ?? '-'} 轮攻击，未突破目标模型`
            }}
          </p>
          <div class="flex gap-3 justify-center">
            <button class="btn-secondary text-sm" @click="router.push('/workshop')">
              创建新会话
            </button>
          </div>
        </div>

        <!-- 加载态 -->
        <Skeleton v-if="loading && !currentPrompt" :lines="4" />
      </div>

      <!-- 右侧：成本 & 事件 -->
      <div class="space-y-4">
        <CostBar :budget="costBudget" />

        <!-- SSE 事件摘要 -->
        <div class="card !p-0 overflow-hidden">
          <div class="px-4 py-3 border-b border-primary-100">
            <h4 class="text-xs font-semibold text-primary-500 tracking-wider">
              实时事件流
            </h4>
          </div>
          <div class="max-h-64 overflow-y-auto p-3 space-y-1.5">
            <div v-if="sseEvents.length === 0" class="text-xs text-primary-300 text-center py-6">
              等待事件…
            </div>
            <div
              v-for="(evt, i) in sseEvents.slice(-20).reverse()"
              :key="i"
              class="text-xs text-primary-500 flex gap-2"
            >
              <span class="text-primary-300 shrink-0">{{ new Date(evt.timestamp).toLocaleTimeString('zh-CN', { hour12: false }) }}</span>
              <span>{{ evt.type }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部：轮次时间线 -->
    <div class="mt-8">
      <h3 class="text-base font-bold text-primary-600 mb-4 tracking-wide">攻击时间线</h3>
      <RoundTimeline :rounds="rounds" :loading="loading" />
    </div>
  </div>
</template>
