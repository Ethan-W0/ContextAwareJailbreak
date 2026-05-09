<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getTaskReport } from '@/api/task';
import type { AttackReport, RefusalTypeDistribution } from '@/types';
import Skeleton from '@/components/Skeleton.vue';
import ErrorMessage from '@/components/ErrorMessage.vue';
import RoundTimeline from '@/components/RoundTimeline.vue';

const route = useRoute();
const router = useRouter();
const taskId = route.params.taskId as string;

const report = ref<AttackReport | null>(null);
const loading = ref(true);
const error = ref('');

const refusalDist = computed<RefusalTypeDistribution>(
  () =>
    report.value?.refusal_dist ?? {
      hard_refusal: 0,
      soft_refusal: 0,
      topic_shift: 0,
      compliance: 0,
      total: 0,
    },
);

const distTotal = computed(() => {
  const d = refusalDist.value;
  return d.hard_refusal + d.soft_refusal + d.topic_shift + d.compliance || 1;
});

async function loadReport() {
  loading.value = true;
  error.value = '';
  try {
    report.value = await getTaskReport(taskId);
  } catch {
    error.value = '无法加载攻击报告';
  } finally {
    loading.value = false;
  }
}

function exportJSON() {
  if (!report.value) return;
  const blob = new Blob([JSON.stringify(report.value, null, 2)], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `attack-report-${taskId}.json`;
  a.click();
  URL.revokeObjectURL(url);
}

onMounted(loadReport);
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 lg:px-8 py-6 lg:py-10">
    <!-- 页头 -->
    <div class="mb-8">
      <button
        class="text-xs text-primary-400 hover:text-primary-600 transition-colors mb-2 tracking-wider"
        @click="router.push('/console')"
      >
        ← 返回控制台
      </button>
      <h2 class="text-2xl font-display font-bold text-primary-700 tracking-wide">
        📋 攻击报告
      </h2>
    </div>

    <!-- 加载态 -->
    <Skeleton v-if="loading" :lines="8" />

    <!-- 错误 -->
    <ErrorMessage v-else-if="error" :message="error" :retry="loadReport" />

    <!-- 报告内容 -->
    <template v-else-if="report">
      <!-- 摘要卡片 -->
      <div class="card mb-6 animate-slide-up">
        <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 mb-6">
          <div>
            <h3 class="text-lg font-bold text-primary-700 tracking-wide mb-1">
              {{ report.target_model }}
            </h3>
            <p class="text-sm text-primary-400">{{ report.attack_intent }}</p>
          </div>
          <div class="flex items-center gap-3">
            <span
              class="text-3xl font-bold"
              :class="report.success ? 'text-sage-500' : 'text-accent-500'"
            >
              {{ report.success ? '✓' : '✗' }}
            </span>
            <div>
              <div class="text-sm font-semibold" :class="report.success ? 'text-sage-600' : 'text-accent-600'">
                {{ report.success ? '攻击成功' : '攻击未成功' }}
              </div>
              <div class="text-xs text-primary-400">
                {{ report.total_rounds }} 轮 · ASR {{ (report.asr * 100).toFixed(1) }}%
              </div>
            </div>
          </div>
        </div>

        <!-- 关键指标 -->
        <div class="grid grid-cols-2 sm:grid-cols-4 gap-4">
          <div class="text-center p-3 bg-primary-50 rounded-soft">
            <div class="text-2xl font-bold text-primary-600">{{ report.total_rounds }}</div>
            <div class="text-[10px] text-primary-400 mt-1 tracking-wider">总轮次</div>
          </div>
          <div class="text-center p-3 bg-primary-50 rounded-soft">
            <div class="text-2xl font-bold text-primary-600">{{ (report.avg_harmfulness_score * 100).toFixed(0) }}%</div>
            <div class="text-[10px] text-primary-400 mt-1 tracking-wider">平均有害度</div>
          </div>
          <div class="text-center p-3 bg-primary-50 rounded-soft">
            <div class="text-2xl font-bold text-primary-600">{{ (report.asr * 100).toFixed(1) }}%</div>
            <div class="text-[10px] text-primary-400 mt-1 tracking-wider">ASR</div>
          </div>
          <div class="text-center p-3 bg-primary-50 rounded-soft">
            <div class="text-2xl font-bold text-primary-600">{{ report.mode === 'AUTOMATED' ? '全自动' : '交互' }}</div>
            <div class="text-[10px] text-primary-400 mt-1 tracking-wider">模式</div>
          </div>
        </div>
      </div>

      <!-- 拒答分布 -->
      <div class="card mb-6 animate-slide-up">
        <h4 class="text-sm font-bold text-primary-600 mb-4 tracking-wide">拒答类型分布</h4>
        <div class="grid grid-cols-4 gap-3">
          <div class="text-center">
            <div class="h-1.5 bg-accent-300 rounded-full mb-2" :style="{ width: (refusalDist.hard_refusal / distTotal * 100) + '%', minWidth: '4px' }" />
            <div class="text-xl font-bold text-accent-500">{{ refusalDist.hard_refusal }}</div>
            <div class="text-[10px] text-primary-400 mt-0.5">硬拒绝</div>
          </div>
          <div class="text-center">
            <div class="h-1.5 bg-amber-300 rounded-full mb-2" :style="{ width: (refusalDist.soft_refusal / distTotal * 100) + '%', minWidth: '4px' }" />
            <div class="text-xl font-bold text-amber-500">{{ refusalDist.soft_refusal }}</div>
            <div class="text-[10px] text-primary-400 mt-0.5">软拒绝</div>
          </div>
          <div class="text-center">
            <div class="h-1.5 bg-primary-300 rounded-full mb-2" :style="{ width: (refusalDist.topic_shift / distTotal * 100) + '%', minWidth: '4px' }" />
            <div class="text-xl font-bold text-primary-500">{{ refusalDist.topic_shift }}</div>
            <div class="text-[10px] text-primary-400 mt-0.5">话题转移</div>
          </div>
          <div class="text-center">
            <div class="h-1.5 bg-sage-300 rounded-full mb-2" :style="{ width: (refusalDist.compliance / distTotal * 100) + '%', minWidth: '4px' }" />
            <div class="text-xl font-bold text-sage-500">{{ refusalDist.compliance }}</div>
            <div class="text-[10px] text-primary-400 mt-0.5">已合规</div>
          </div>
        </div>
      </div>

      <!-- LLM 分析 -->
      <div v-if="report.analysis" class="card mb-6 animate-slide-up">
        <h4 class="text-sm font-bold text-primary-600 mb-3 tracking-wide">AI 分析总结</h4>
        <p class="text-sm text-primary-600 leading-relaxed whitespace-pre-wrap">{{ report.analysis }}</p>
      </div>

      <!-- 导出按钮 -->
      <div class="flex gap-3 mb-8">
        <button class="btn-secondary text-sm" @click="exportJSON">导出 JSON</button>
      </div>

      <!-- 轮次详情 -->
      <div>
        <h3 class="text-base font-bold text-primary-600 mb-4 tracking-wide">攻击轮次详情</h3>
        <RoundTimeline :rounds="report.rounds" />
      </div>
    </template>
  </div>
</template>
