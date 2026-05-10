<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-primary-50 px-4">
    <div class="max-w-2xl w-full text-center animate-fade-in">
      <div class="mb-12">
        <div class="text-7xl mb-5 select-none">🛡</div>
        <h1 class="text-4xl font-display font-bold text-primary-700 tracking-[0.15em] mb-3">
          轻舟
        </h1>
        <p class="text-sm text-primary-400 tracking-[0.2em]">
          上下文感知越狱代理
        </p>
        <p class="text-xs text-primary-300 mt-3 leading-relaxed max-w-md mx-auto">
          LLM 红队安全评估工具 · 支持交互式手动测试与全自动 API 攻击<br />
          智能策略决策 · 多轮评估反馈 · 攻击报告生成
        </p>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <!-- 交互模式卡片 -->
        <button
          class="card text-left group hover:border-primary-300 transition-all duration-200"
          @click="router.push('/workshop')"
        >
          <div class="text-3xl mb-3">✍</div>
          <h3 class="text-lg font-bold text-primary-700 mb-2 tracking-wide group-hover:text-primary-800 transition-colors">
            交互式攻击工坊
          </h3>
          <p class="text-sm text-primary-400 leading-relaxed">
            创建会话，Agent 生成越狱 Prompt，你手动测试后粘贴回答，Agent 评估并迭代优化。
          </p>
        </button>

        <!-- 全自动卡片 -->
        <button
          class="card text-left group hover:border-primary-300 transition-all duration-200"
          @click="router.push('/console')"
        >
          <div class="text-3xl mb-3">⚡</div>
          <h3 class="text-lg font-bold text-primary-700 mb-2 tracking-wide group-hover:text-primary-800 transition-colors">
            全自动攻击控制台
          </h3>
          <p class="text-sm text-primary-400 leading-relaxed">
            接入目标模型 API Key，自动执行多轮攻击，通过 SSE 实时推送每一步事件。
          </p>
        </button>
      </div>

      <p class="text-xs text-primary-300 mt-8">
        请确保在合法授权范围内使用本工具
      </p>
    </div>
  </div>
</template>
