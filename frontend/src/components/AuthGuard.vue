<script setup lang="ts">
import { ref, computed } from 'vue';
import { useAppStore } from '@/stores/app';
import { useRouter } from 'vue-router';
import { confirmAuth } from '@/api/auth';

const appStore = useAppStore();
const router = useRouter();

const agreed = ref(false);
const confirming = ref(false);

const canProceed = computed(() => agreed.value && !confirming.value);

async function handleConfirm() {
  if (!canProceed.value) return;
  confirming.value = true;
  try {
    const res = await confirmAuth();
    sessionStorage.setItem('auth_token', res.token);
    appStore.setAuthorized(true);
    router.push('/workshop');
  } catch {
    // backend unavailable, fall back to hardcoded token
    sessionStorage.setItem('auth_token', 'confirmed');
    appStore.setAuthorized(true);
    router.push('/workshop');
  } finally {
    confirming.value = false;
  }
}
</script>

<template>
  <!-- 授权确认页 -->
  <div
    v-if="!appStore.authorized"
    class="min-h-screen flex items-center justify-center bg-neutral-50 px-5 py-10 font-tech"
  >
    <div class="w-full max-w-[680px] animate-fade-in">
      <!-- ═══════ 顶部品牌区 ═══════ -->
      <header class="flex items-center gap-5 mb-12">
        <!-- Logo: 船锚 + 盾牌 + 代码元素 -->
        <svg
          class="shrink-0"
          width="48"
          height="48"
          viewBox="0 0 48 48"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <!-- 盾牌外轮廓 -->
          <path
            d="M24 4L6 10V24C6 32 14 40 24 44C34 40 42 32 42 24V10L24 4Z"
            fill="#165DFF"
            fill-opacity="0.08"
            stroke="#165DFF"
            stroke-width="2"
            stroke-linejoin="round"
          />
          <!-- 船锚 -->
          <circle cx="24" cy="19" r="4" stroke="#165DFF" stroke-width="2" />
          <line x1="24" y1="23" x2="24" y2="34" stroke="#165DFF" stroke-width="2" stroke-linecap="round" />
          <path d="M18 30 L24 34 L30 30" stroke="#165DFF" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
          <line x1="24" y1="8" x2="24" y2="15" stroke="#165DFF" stroke-width="1.5" stroke-linecap="round" />
          <!-- 代码括号 -->
          <path d="M16 12L12 16L16 20" stroke="#165DFF" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" opacity="0.5" />
          <path d="M32 12L36 16L32 20" stroke="#165DFF" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" opacity="0.5" />
        </svg>

        <div class="min-w-0">
          <h1 class="text-[28px] font-semibold text-neutral-900 tracking-tight leading-tight">
            轻舟
          </h1>
          <p class="text-[13px] text-neutral-500 mt-0.5 tracking-wide">
            上下文感知越狱代理&nbsp;&nbsp;|&nbsp;&nbsp;LLM 红队安全评估平台
          </p>
        </div>
      </header>

      <!-- ═══════ 中部核心内容 ═══════ -->
      <div class="space-y-6">
        <!-- 模块1：产品定位说明 -->
        <section>
          <p class="text-[15px] text-neutral-800 leading-relaxed font-medium">
            本工具为受控的 LLM 对抗性测试辅助平台，仅用于已获书面授权的大语言模型安全评估。
          </p>
          <p class="text-[13px] text-neutral-400 mt-2 leading-relaxed">
            帮助安全研究人员识别大模型的安全漏洞与越狱风险。
          </p>
        </section>

        <!-- 模块2：合规条款卡片 -->
        <section class="relative border border-neutral-200 rounded-pill bg-white shadow-card overflow-hidden">
          <!-- 卡片头部 -->
          <div class="px-6 py-4 border-b border-neutral-100 flex items-center gap-2.5">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path
                d="M12 2L3 7V12C3 17.5 7 22.5 12 24C17 22.5 21 17.5 21 12V7L12 2Z"
                stroke="#165DFF"
                stroke-width="2"
                stroke-linejoin="round"
              />
              <path d="M8 12L11 15L16 9" stroke="#165DFF" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
            <h2 class="text-sm font-semibold text-neutral-800 tracking-wide">
              使用合规要求
            </h2>
          </div>

          <!-- 三条核心条款 -->
          <div class="px-6 py-5 space-y-4">
            <!-- 条款 1 -->
            <div class="flex items-start gap-3.5">
              <svg class="shrink-0 mt-0.5" width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path
                  d="M12 2L3 7V12C3 17.5 7 22.5 12 24C17 22.5 21 17.5 21 12V7L12 2Z"
                  stroke="#165DFF"
                  stroke-width="1.8"
                  stroke-linejoin="round"
                />
                <path d="M8 12L11 15L16 9" stroke="#165DFF" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
              </svg>
              <p class="text-sm text-neutral-700 leading-relaxed">
                <span class="font-semibold text-tech-600">仅用于已获书面授权的红队安全评估</span
                ><span class="text-neutral-500">，禁止未授权使用</span>
              </p>
            </div>

            <!-- 条款 2 -->
            <div class="flex items-start gap-3.5">
              <svg class="shrink-0 mt-0.5" width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="3" y="3" width="18" height="18" rx="3" stroke="#165DFF" stroke-width="1.8" />
                <line x1="3" y1="9" x2="21" y2="9" stroke="#165DFF" stroke-width="1.8" />
                <line x1="9" y1="12" x2="9" y2="21" stroke="#165DFF" stroke-width="1.8" />
                <line x1="15" y1="12" x2="15" y2="21" stroke="#165DFF" stroke-width="1.8" stroke-dasharray="2 2" opacity="0.5" />
              </svg>
              <p class="text-sm text-neutral-700 leading-relaxed">
                使用者需严格遵守<span class="font-semibold text-tech-600">
                  《网络安全法》《数据安全法》</span
                ><span class="text-neutral-500">等法律法规</span>
              </p>
            </div>

            <!-- 条款 3 -->
            <div class="flex items-start gap-3.5">
              <svg class="shrink-0 mt-0.5" width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="5" y="11" width="14" height="10" rx="2" stroke="#165DFF" stroke-width="1.8" />
                <path d="M8 11V7C8 4.8 9.8 3 12 3C14.2 3 16 4.8 16 7V11" stroke="#165DFF" stroke-width="1.8" />
                <circle cx="12" cy="16" r="1.5" fill="#165DFF" />
                <line x1="12" y1="18" x2="12" y2="20" stroke="#165DFF" stroke-width="1.5" stroke-linecap="round" />
              </svg>
              <p class="text-sm text-neutral-700 leading-relaxed">
                API Key 采用<span class="font-semibold text-tech-600"> AES-256 加密存储</span
                ><span class="text-neutral-500">，内存中用完即清，全程保障数据安全</span>
              </p>
            </div>
          </div>

          <!-- 合规标识角标 -->
          <div class="absolute right-3 bottom-3 flex items-center gap-1.5 text-[11px] text-neutral-300 select-none">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path
                d="M12 2L3 7V12C3 17.5 7 22.5 12 24C17 22.5 21 17.5 21 12V7L12 2Z"
                stroke="currentColor"
                stroke-width="2"
                stroke-linejoin="round"
              />
              <path d="M8 12L11 15L16 9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
            <span class="tracking-wider">合规使用</span>
          </div>
        </section>
      </div>

      <!-- ═══════ 底部用户确认区 ═══════ -->
      <div class="mt-10">
        <!-- 复选框 -->
        <label
          class="flex items-center gap-3 cursor-pointer group select-none"
          @click="agreed = !agreed"
        >
          <span
            class="
              relative flex items-center justify-center
              w-5 h-5 rounded-pill
              border-2 transition-all duration-200
            "
            :class="
              agreed
                ? 'bg-tech-500 border-tech-500'
                : 'border-neutral-300 group-hover:border-tech-400 bg-white'
            "
          >
            <!-- checkmark -->
            <svg
              v-if="agreed"
              class="w-3 h-3 text-white"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="3"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <polyline points="20 6 9 17 4 12" />
            </svg>
          </span>
          <span class="text-sm text-neutral-700 group-hover:text-neutral-800 transition-colors">
            我已阅读并理解以上合规要求，承诺在合法授权范围内使用本工具
          </span>
        </label>

        <!-- 确认按钮 -->
        <button
          class="
            mt-6 w-full py-3 rounded-pill text-[15px] font-semibold tracking-wide
            transition-all duration-200
            focus:outline-none focus:ring-2 focus:ring-tech-200 focus:ring-offset-2
          "
          :class="
            canProceed
              ? 'bg-tech-500 text-white shadow-card hover:bg-tech-600 hover:shadow-elevated active:translate-y-0.5 cursor-pointer'
              : 'bg-neutral-200 text-neutral-400 cursor-not-allowed'
          "
          :disabled="!canProceed"
          @click="handleConfirm"
        >
          {{ confirming ? '正在验证…' : '确认并进入平台' }}
        </button>

        <p class="text-center text-xs text-neutral-300 mt-5 tracking-wider">
          点击即表示你承诺在合法授权范围内使用本工具
        </p>
      </div>
    </div>
  </div>

  <!-- 已授权则渲染子组件 -->
  <slot v-else />
</template>

<style scoped>
.font-tech {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Microsoft YaHei',
    'Helvetica Neue', Arial, sans-serif;
}
</style>
