import type { ExecutionEvent } from '@/types';

export type SSECallback = (event: ExecutionEvent) => void;
export type SSEErrorCallback = (error: Event) => void;

/**
 * 创建 SSE 连接
 * @param url SSE 端点路径（如 /session/xxx/stream）
 * @param onEvent 事件回调
 * @param onError 错误回调
 * @returns 清理函数，调用后断开连接
 */
export function connectSSE(
  url: string,
  onEvent: SSECallback,
  onError?: SSEErrorCallback,
): () => void {
  const base = window.location.origin;
  const fullUrl = `${base}/api${url}`;

  const eventSource = new EventSource(fullUrl);

  eventSource.onmessage = (event: MessageEvent) => {
    try {
      const data: ExecutionEvent = JSON.parse(event.data);
      onEvent(data);
    } catch {
      // 非 JSON 数据忽略
    }
  };

  eventSource.onerror = (error: Event) => {
    onError?.(error);
  };

  // 也监听具名事件（如果后端使用 event.name）
  const namedEvents = [
    'STRATEGY_SELECTED',
    'PROMPT_GENERATED',
    'TARGET_RESPONSE_RECEIVED',
    'WAITING_USER_INPUT',
    'EVALUATION_DONE',
    'ATTACK_SUCCESS',
    'ATTACK_FAILED',
    'TASK_FINISHED',
    'ERROR',
    'COST_UPDATE',
    'BUDGET_WARNING',
    'BUDGET_EXCEEDED',
    'MODE_DEGRADE',
  ];

  namedEvents.forEach((name) => {
    eventSource.addEventListener(name, (event: Event) => {
      const me = event as MessageEvent;
      try {
        const data: ExecutionEvent = JSON.parse(me.data);
        onEvent(data);
      } catch {
        // 忽略
      }
    });
  });

  return () => {
    eventSource.close();
  };
}
