<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { getVectors } from '@/api/vectors';
import type { AttackVector } from '@/types';
import Skeleton from '@/components/Skeleton.vue';
import ErrorMessage from '@/components/ErrorMessage.vue';
import EmptyState from '@/components/EmptyState.vue';

const vectors = ref<AttackVector[]>([]);
const loading = ref(true);
const error = ref('');
const searchQuery = ref('');
const selectedCategory = ref('全部');

const categories = computed(() => {
  const cats = new Set(vectors.value.map((v) => v.category));
  return ['全部', ...Array.from(cats)];
});

const filteredVectors = computed(() => {
  let result = vectors.value;
  if (selectedCategory.value !== '全部') {
    result = result.filter((v) => v.category === selectedCategory.value);
  }
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.trim().toLowerCase();
    result = result.filter(
      (v) =>
        v.name.toLowerCase().includes(q) ||
        v.description.toLowerCase().includes(q),
    );
  }
  return result.filter((v) => v.enabled);
});

const categoryIcons: Record<string, string> = {
  '角色扮演类': '🎭',
  '逻辑操纵类': '🧩',
  '上下文污染类': '🫧',
  '渐进突破类': '🪜',
  '跨语言/编码类': '🌐',
  '情感操纵类': '💗',
};

async function load() {
  loading.value = true;
  error.value = '';
  try {
    vectors.value = await getVectors();
  } catch {
    error.value = '正在加载攻击向量库…暂时无法连接';
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <div class="max-w-5xl mx-auto px-4 lg:px-8 py-6 lg:py-10">
    <!-- 页头 -->
    <div class="mb-8">
      <h2 class="text-2xl font-display font-bold text-primary-700 tracking-wide">
        📚 攻击向量库
      </h2>
      <p class="text-sm text-primary-400 mt-1 leading-relaxed">
        共 20 种攻击向量，按 6 大类组织，每种向量包含多个 Prompt 模板变种
      </p>
    </div>

    <!-- 搜索与筛选 -->
    <div class="flex flex-col sm:flex-row gap-3 mb-6">
      <input
        v-model="searchQuery"
        class="input-field flex-1"
        placeholder="搜索攻击向量…"
      />
      <div class="flex flex-wrap gap-1.5">
        <button
          v-for="cat in categories"
          :key="cat"
          class="text-xs px-3 py-1.5 rounded-full transition-colors"
          :class="
            selectedCategory === cat
              ? 'bg-primary-500 text-white'
              : 'bg-primary-50 text-primary-500 hover:bg-primary-100'
          "
          @click="selectedCategory = cat"
        >
          {{ categoryIcons[cat] ?? '' }} {{ cat }}
        </button>
      </div>
    </div>

    <ErrorMessage v-if="error" :message="error" :retry="load" />

    <Skeleton v-if="loading" :lines="6" />

    <EmptyState
      v-else-if="filteredVectors.length === 0"
      icon="🔍"
      title="没有找到匹配的向量"
      :description="searchQuery ? '试试其他关键词？' : '暂无启用的攻击向量'"
    />

    <!-- 向量网格 -->
    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
      <div
        v-for="vec in filteredVectors"
        :key="vec.id"
        class="card !p-5 animate-slide-up group cursor-default"
      >
        <div class="flex items-start justify-between mb-3">
          <div>
            <span class="text-xl mr-2">{{ categoryIcons[vec.category] ?? '📌' }}</span>
            <h4 class="inline text-sm font-bold text-primary-700 tracking-wide">
              {{ vec.name }}
            </h4>
          </div>
          <span
            v-if="vec.usage_count > 0"
            class="text-[10px] text-primary-300"
            :title="`成功率: ${((vec.success_count / vec.usage_count) * 100).toFixed(0)}%`"
          >
            {{ ((vec.success_count / vec.usage_count) * 100).toFixed(0) }}%
          </span>
        </div>

        <p class="text-xs text-primary-400 leading-relaxed mb-3">
          {{ vec.description }}
        </p>

        <div class="flex items-center justify-between">
          <span class="badge-primary text-[10px]">{{ vec.category }}</span>
          <span class="text-[10px] text-primary-300">
            {{ vec.variant_templates?.length ?? 0 }} 个变种 · 使用 {{ vec.usage_count }} 次
          </span>
        </div>

        <!-- hover 展示变种模板 -->
        <div
          class="mt-3 pt-3 border-t border-primary-100 hidden group-hover:block animate-fade-in"
        >
          <span class="label-text">模板变种</span>
          <ul class="space-y-1 mt-1">
            <li
              v-for="(tmpl, i) in vec.variant_templates?.slice(0, 3)"
              :key="i"
              class="text-xs text-primary-500 bg-primary-50 rounded px-2 py-1 truncate"
            >
              {{ tmpl.slice(0, 60) }}{{ tmpl.length > 60 ? '…' : '' }}
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>
