import http from './client';
import type {
  CreateTaskRequest,
  AttackReport,
  PreFlightResult,
  TaskSummary,
} from '@/types';

/** 创建全自动攻击任务 */
export function createTask(data: CreateTaskRequest): Promise<{ task_id: string }> {
  return http.post('/task/create', data);
}

/** 启动任务 */
export function startTask(taskId: string): Promise<void> {
  return http.post(`/task/${taskId}/start`);
}

/** 暂停任务 */
export function pauseTask(taskId: string): Promise<void> {
  return http.post(`/task/${taskId}/pause`);
}

/** 恢复任务 */
export function resumeTask(taskId: string): Promise<void> {
  return http.post(`/task/${taskId}/resume`);
}

/** 终止任务 */
export function stopTask(taskId: string): Promise<void> {
  return http.post(`/task/${taskId}/stop`);
}

/** 获取任务报告 */
export function getTaskReport(taskId: string): Promise<AttackReport> {
  return http.get(`/task/${taskId}/report`);
}

/** 获取任务列表 */
export function getTaskList(): Promise<TaskSummary[]> {
  return http.get('/task/list');
}

/** 成本预检 */
export function preFlightCheck(data: CreateTaskRequest): Promise<PreFlightResult> {
  return http.post('/task/preflight', data);
}
