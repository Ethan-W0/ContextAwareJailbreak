import http from './client';

/** 授权确认 */
export function confirmAuth(): Promise<{ token: string }> {
  return http.post('/auth/confirm');
}
