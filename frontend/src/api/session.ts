import http from './client';
import type {
  CreateSessionRequest,
  SubmitAnswerRequest,
  AttackState,
  RoundDetail,
  ExecutionEvent,
  OptimizePromptRequest,
  OptimizePromptResponse,
} from '@/types';

/** 创建交互会话 */
export function createSession(data: CreateSessionRequest): Promise<{ session_id: string }> {
  return http.post('/session/create', data);
}

/** 提交目标回答，触发下一轮 */
export function submitAnswer(
  sessionId: string,
  data: SubmitAnswerRequest,
): Promise<AttackState> {
  return http.post(`/session/${sessionId}/next-round`, data);
}

/** 获取会话历史 */
export function getSessionHistory(sessionId: string): Promise<RoundDetail[]> {
  return http.get(`/session/${sessionId}/history`);
}

/** 获取会话当前状态 */
export function getSessionState(sessionId: string): Promise<AttackState> {
  return http.get(`/session/${sessionId}/state`);
}

/** 优化攻击 Prompt：分析 attack_prompt + target_response，返回优化结果 */
export function optimizePrompt(data: OptimizePromptRequest): Promise<OptimizePromptResponse> {
  return http.post('/session/optimize', data);
}
