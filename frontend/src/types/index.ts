// ── 枚举 ──

export type AttackMode = 'INTERACTIVE' | 'AUTOMATED';

export type RefusalType = 'HARD_REFUSAL' | 'SOFT_REFUSAL' | 'TOPIC_SHIFT' | 'COMPLIANCE';

export type EventType =
  | 'STRATEGY_SELECTED'
  | 'PROMPT_GENERATED'
  | 'TARGET_RESPONSE_RECEIVED'
  | 'WAITING_USER_INPUT'
  | 'EVALUATION_DONE'
  | 'ATTACK_SUCCESS'
  | 'ATTACK_FAILED'
  | 'TASK_FINISHED'
  | 'ERROR'
  | 'COST_UPDATE'
  | 'BUDGET_WARNING'
  | 'BUDGET_EXCEEDED'
  | 'MODE_DEGRADE';

export type TaskStatus = 'CREATED' | 'RUNNING' | 'PAUSED' | 'FINISHED' | 'TERMINATED' | 'INTERRUPTED' | 'ARCHIVED';

export type StrategyDecisionMode = 'RULES' | 'LLM';

export type ExceedAction = 'ABORT' | 'DEGRADE' | 'NOTIFY';

export type CallType = 'DECISION' | 'EVALUATION' | 'ARBITRATION' | 'TARGET';

// ── 数据模型 ──

export interface Message {
  role: string;
  content: string;
}

export interface CreateSessionRequest {
  target_model: string;
  attack_intent: string;
  max_rounds: number;
  api_key?: string;
  base_url?: string;
  model_name?: string;
}

export interface SubmitAnswerRequest {
  target_response: string;
}

export interface OptimizePromptRequest {
  attack_prompt: string;
  target_response: string;
  api_key?: string;
  base_url?: string;
  model_name?: string;
}

export interface OptimizePromptResponse {
  optimized_prompt: string;
  analysis: string;
  weakness_found: string;
  improvement_strategy: string;
}

export interface CreateTaskRequest {
  target_model: string;
  attack_intent: string;
  max_rounds: number;
  strategy_mode?: StrategyDecisionMode;
  eval_mode?: string;
  api_config?: ApiConfig;
  max_total_tokens?: number;
  max_estimated_cost?: number;
  exceeded_action?: ExceedAction;
}

export interface ApiConfig {
  api_key: string;
  base_url: string;
  model_name: string;
}

export interface AttackState {
  task_id: string;
  mode: AttackMode;
  target_model: string;
  attack_intent: string;
  current_round: number;
  max_rounds: number;
  current_vector_id: string;
  last_attack_prompt: string;
  last_target_response: string;
  last_refusal_type: RefusalType;
  harmfulness_score: number;
  strategy_reason: string;
  attack_success: boolean;
  tried_vector_ids: string[];
  conversation: Message[];
  rounds: RoundDetail[];
  last_evaluation: EvaluationResult | null;
  cost_budget: CostBudget | null;
  interrupted: boolean;
  hasReachedMaxRounds?: boolean;
}

export interface EvaluationResult {
  harmfulness_score: number;
  refusal_type: RefusalType;
  summary: string;
}

export interface RoundDetail {
  round: number;
  vector_id: string;
  vector_name: string;
  attack_prompt: string;
  target_response: string;
  refusal_type: RefusalType;
  harmfulness_score: number;
  reason: string;
}

export interface AttackReport {
  task_id: string;
  target_model: string;
  mode: AttackMode;
  attack_intent: string;
  total_rounds: number;
  success: boolean;
  asr: number;
  avg_harmfulness_score: number;
  refusal_dist: RefusalTypeDistribution;
  rounds: RoundDetail[];
  analysis: string;
}

export interface RefusalTypeDistribution {
  hard_refusal: number;
  soft_refusal: number;
  topic_shift: number;
  compliance: number;
  total: number;
}

export interface AttackVector {
  id: string;
  name: string;
  category: string;
  description: string;
  enabled: boolean;
  usage_count: number;
  success_count: number;
  variant_templates: string[];
  successRate?: number;
}

export interface ExecutionEvent {
  type: EventType;
  round: number;
  payload: Record<string, unknown>;
  timestamp: string;
}

export interface CostBudget {
  max_total_tokens: number;
  max_estimated_cost: number;
  tokens_used: number;
  cost_accrued: number;
  breakdown: CostBreakdown;
  warning_thresholds: number[];
  exceeded_action: ExceedAction;
}

export interface CostBreakdown {
  decision_tokens: number;
  evaluation_tokens: number;
  arbitration_tokens: number;
  target_tokens: number;
}

export interface BudgetStatusDto {
  status: 'OK' | 'WARNING_80' | 'WARNING_90' | 'EXCEEDED';
  tokens_used: number;
  max_total_tokens: number;
  cost_accrued: number;
  max_estimated_cost: number;
  recommended_action: ExceedAction;
  breakdown: CostBreakdown;
  degraded_mode?: StrategyDecisionMode;
}

export interface PreFlightResult {
  allowed: boolean;
  estimated_tokens: number;
  estimated_cost: number;
  reject_reason?: string;
  quota_remaining: UserQuota;
}

export interface UserQuota {
  max_concurrent_tasks: number;
  max_daily_tasks: number;
  max_daily_platform_tokens: number;
  daily_tasks_run: number;
  daily_tokens_used: number;
  reset_at: string;
  remainingDailyTokens?: number;
  remainingDailyTasks?: number;
}

export interface StrategyDecision {
  vector_id: string;
  attack_prompt: string;
  reason: string;
}

// ── 任务列表项（后端可能返回的简化结构） ──

export interface TaskSummary {
  id: string;
  mode: AttackMode;
  target_model: string;
  attack_intent: string;
  max_rounds: number;
  status: TaskStatus;
  attack_success?: boolean;
  current_round?: number;
  created_at: string;
  updated_at: string;
}

// ── API 通用响应包装 ──

export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}
