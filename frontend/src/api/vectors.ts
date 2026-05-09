import http from './client';
import type { AttackVector } from '@/types';

/** 获取所有攻击向量 */
export function getVectors(): Promise<AttackVector[]> {
  return http.get('/vectors');
}

/** 按分类获取攻击向量 */
export function getVectorsByCategory(category: string): Promise<AttackVector[]> {
  return http.get('/vectors', { params: { category } });
}
