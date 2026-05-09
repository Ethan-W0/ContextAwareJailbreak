<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getTaskList, createTask, preFlightCheck } from '@/api/task';
import type { TaskSummary, CreateTaskRequest, StrategyDecisionMode, ExceedAction, PreFlightResult } from '@/types';
import EmptyState from '@/components/EmptyState.vue';
import ErrorMessage from '@/components/ErrorMessage.vue';
import Skeleton from '@/components/Skeleton.vue';

const router = useRouter();

// 任务列表
const tasks = ref<TaskSummary[]>([]);
const tasksLoading = ref(true);
const tasksError = ref('');

// 创建表单
const showCreate = ref(false);
const creating = ref(false);
const createError = ref('');
const preFlight = ref<PreFlightResult | null>(null);

const form = ref<CreateTaskRequest>({
  target_model: 'GPT-4',
  attack_intent: '',
  max_rounds: 10,
  strategy_mode: 'RULES' as StrategyDecisionMode,
  eval_mode: 'SINGLE',
  api_config: {
    api_key: '',
    base_url: 'https://api.openai.com/v1',
    model_name: 'gpt-4',
  },
  max_total_tokens: 500000,
  max_estimated_cost: 2.0,
  exceeded_action: 'ABORT' as ExceedAction,
});

// 状态映射
const statusLabel: Record<string, string> = {
  CREATED: '待启动',
  RUNNING: '运行中',
  PAUSED: '已暂停',
  FINISHED: '已完成',
  TERMINATED: '已终止',
  INTERRUPTED: '异常中断',
  ARCHIVED: '已归档',
};

const statusColor: Record<string, string> = {
  CREATED: 'badge-primary',
  RUNNING: 'badge-sage',
  PAUSED: 'badge bg-amber-100 text-amber-700',
  FINISHED: 'badge bg-primary-100 text-primary-600',
  TERMINATED: 'badge-accent',
  INTERRUPTED: 'badge-accent',
  ARCHIVED: 'badge bg-primary-100 text-primary-400',
};

// 加载任务列表
async function loadTasks() {
  tasksLoading.value = true;
  tasksError.value = '';
  try {
    tasks.value = await getTaskList();
  } catch {
    tasksError.value = '无法加载任务列表';
  } finally {
    tasksLoading.value = false;
  }
}

// 预检
async function handlePreflight() {
  if (!form.value.attack_intent.trim()) return;
  try {
    preFlight.value = await preFlightCheck(form.value);
  } catch {
    preFlight.value = null;
  }
}

// 创建任务
async function handleCreate() {
  if (!form.value.attack_intent.trim()) return;

  creating.value = true;
  createError.value = '';

  try {
    const res = await createTask(form.value);
    showCreate.value = false;
    preFlight.value = null;
    router.push(`/console/${res.task_id}`);
  } catch (e: unknown) {
    createError.value = e instanceof Error ? e.message : '创建任务失败';
  } finally {
    creating.value = false;
  }
}

function viewTask(task: TaskSummary) {
  if (task.status === 'FINISHED' || task.status === 'TERMINATED') {
    router.push(`/report/${task.id}`);
  } else {
    router.push(`/console/${task.id}`);
  }
}

onMounted(loadTasks);
</script>

<template>
  <div class="max-w-5xl mx-auto px-4 lg:px-8 py-6 lg:py-10">
    <!-- 页头 -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between mb-8 gap-4">
      <div>
        <h2 class="text-2xl font-display font-bold text-primary-700 tracking-wide">
          ⚡ 全自动攻击控制台
        </h2>
        <p class="text-sm text-primary-400 mt-1 leading-relaxed">
          配置目标模型 API，自动执行多轮攻击，实时观看每一步推理过程
        </p>
      </div>
      <button class="btn-primary text-sm" @click="showCreate = true">
        创建新任务
      </button>
    </div>

    <!-- 任务列表 -->
    <div v-if="!showCreate">
      <ErrorMessage v-if="tasksError" :message="tasksError" :retry="loadTasks" />

      <Skeleton v-if="tasksLoading" :lines="5" />

      <EmptyState
        v-else-if="tasks.length === 0"
        icon="🪁"
        title="还没有任何任务"
        description="创建第一个全自动攻击任务，感受智能攻击引擎的威力"
      />

      <div v-else class="space-y-3">
        <div
          v-for="task in tasks"
          :key="task.id"
          class="card !p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3 cursor-pointer animate-slide-up"
          @click="viewTask(task)"
        >
          <div class="min-w-0">
            <div class="flex items-center gap-3 mb-1">
              <span class="font-bold text-primary-700 text-sm tracking-wide truncate">
                {{ task.attack_intent.slice(0, 40) }}{{ task.attack_intent.length > 40 ? '…' : '' }}
              </span>
              <span :class="statusColor[task.status] ?? 'badge-primary'">
                {{ statusLabel[task.status] ?? task.status }}
              </span>
              <span v-if="task.attack_success" class="badge-sage">突破成功</span>
            </div>
            <div class="flex items-center gap-4 text-xs text-primary-400">
              <span>{{ task.target_model }}</span>
              <span>{{ task.mode === 'AUTOMATED' ? '全自动' : '交互' }}</span>
              <span v-if="task.current_round">轮次 {{ task.current_round }}/{{ task.max_rounds }}</span>
              <span>{{ new Date(task.created_at).toLocaleDateString('zh-CN') }}</span>
            </div>
          </div>
          <span class="text-xl text-primary-300 shrink-0 hidden sm:block">→</span>
        </div>
      </div>
    </div>

    <!-- 创建任务表单 -->
    <div v-if="showCreate" class="card animate-slide-up">
      <div class="flex items-center justify-between mb-5">
        <h3 class="text-base font-bold text-primary-600 tracking-wide">创建全自动攻击任务</h3>
        <button
          class="text-xs text-primary-400 hover:text-primary-600 transition-colors"
          @click="showCreate = false"
        >
          取消
        </button>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <!-- 目标模型 -->
        <div>
          <label class="label-text">目标模型名称</label>
          <input v-model="form.target_model" type="text" class="input-field" placeholder="GPT-4" />
        </div>

        <!-- 最大轮次 -->
        <div>
          <label class="label-text">最大轮次</label>
          <input v-model.number="form.max_rounds" type="number" class="input-field" min="1" max="50" />
        </div>

        <!-- 策略模式 -->
        <div>
          <label class="label-text">策略决策模式</label>
          <select v-model="form.strategy_mode" class="input-field">
            <option value="RULES">规则引擎（RULES）</option>
            <option value="LLM">LLM 智能决策</option>
          </select>
        </div>

        <!-- 评估模式 -->
        <div>
          <label class="label-text">评估模式</label>
          <select v-model="form.eval_mode" class="input-field">
            <option value="SINGLE">单评估</option>
            <option value="DUAL">双评估 + 仲裁</option>
          </select>
        </div>

        <!-- 攻击意图 -->
        <div class="md:col-span-2">
          <label class="label-text">攻击意图</label>
          <textarea
            v-model="form.attack_intent"
            class="textarea-field"
            rows="2"
            placeholder="描述你想突破的安全限制…"
            @blur="handlePreflight"
          />
        </div>

        <!-- API 配置 -->
        <fieldset class="md:col-span-2 border border-primary-200 rounded-soft p-4">
          <legend class="text-xs font-semibold text-primary-500 px-2 tracking-wider">目标模型 API 配置</legend>
          <div class="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <div>
              <label class="label-text">API Key</label>
              <input v-model="form.api_config!.api_key" type="password" class="input-field" placeholder="sk-..." />
            </div>
            <div>
              <label class="label-text">Base URL</label>
              <input v-model="form.api_config!.base_url" type="text" class="input-field" placeholder="https://api.openai.com/v1" />
            </div>
            <div>
              <label class="label-text">模型名称</label>
              <input v-model="form.api_config!.model_name" type="text" class="input-field" placeholder="gpt-4" />
            </div>
          </div>
        </fieldset>

        <!-- 成本控制 -->
        <fieldset class="md:col-span-2 border border-primary-200 rounded-soft p-4">
          <legend class="text-xs font-semibold text-primary-500 px-2 tracking-wider">成本控制（可选）</legend>
          <div class="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <div>
              <label class="label-text">Token 上限</label>
              <input v-model.number="form.max_total_tokens" type="number" class="input-field" />
            </div>
            <div>
              <label class="label-text">费用上限 (USD)</label>
              <input v-model.number="form.max_estimated_cost" type="number" class="input-field" step="0.1" />
            </div>
            <div>
              <label class="label-text">超限动作</label>
              <select v-model="form.exceeded_action" class="input-field">
                <option value="ABORT">终止任务</option>
                <option value="DEGRADE">降级继续</option>
                <option value="NOTIFY">仅通知</option>
              </select>
            </div>
          </div>
        </fieldset>
      </div>

      <!-- 预检结果 -->
      <div v-if="preFlight?.allowed" class="mt-4 p-3 bg-sage-50 rounded-soft border border-sage-200 text-xs text-sage-700">
        <span class="font-semibold">成本预估：</span>
        ~{{ preFlight.estimated_tokens.toLocaleString() }} tokens / ~${{ preFlight.estimated_cost.toFixed(2) }}
        <span class="text-sage-500 ml-3">
          剩余配额：{{ preFlight.quota_remaining?.remainingDailyTokens?.toLocaleString() ?? '-' }} tokens
        </span>
      </div>
      <div v-else-if="preFlight && !preFlight.allowed" class="mt-4 p-3 bg-accent-50 rounded-soft border border-accent-200 text-xs text-accent-700">
        {{ preFlight.reject_reason }}
      </div>

      <ErrorMessage v-if="createError" :message="createError" class="!py-0 mt-4" />

      <button
        class="btn-primary w-full mt-6 text-sm py-3 tracking-wider"
        :disabled="creating || !form.attack_intent.trim()"
        @click="handleCreate"
      >
        <span v-if="creating" class="pulse-dot mr-2" />
        {{ creating ? '正在创建任务…' : '创建任务' }}
      </button>
    </div>
  </div>
</template>
