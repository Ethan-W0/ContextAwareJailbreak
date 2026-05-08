package com.jailbreak.agent.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 授权确认拦截器 —— 拦截所有 /api/** 请求。
 * <p>
 * 行为:
 * <ul>
 *   <li>检查请求 Session/Header 中是否携带已授权标记</li>
 *   <li>未授权 → 返回 403 + 强制跳转授权确认页</li>
 *   <li>已授权 → 放行</li>
 * </ul>
 * <p>
 * 授权流程: POST /api/auth/confirm → 记录授权时间+IP → 后续请求携带 token。
 * <p>
 * 约束: 启动页强制授权确认勾选，未确认不可使用任何功能。
 */
public interface AuthorizationInterceptor {

    /**
     * 请求前拦截，检查授权状态。
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应（未授权时写入 403）
     * @return true 表示放行，false 表示已拦截
     */
    boolean preHandle(HttpServletRequest request, HttpServletResponse response);

    /**
     * 授权确认。
     *
     * @param request HTTP 请求（含用户 IP 等信息）
     * @return 授权 token，后续请求需携带
     */
    String confirmAuthorization(HttpServletRequest request);

    /**
     * 检查请求是否已授权。
     *
     * @param request HTTP 请求
     * @return true 表示已授权
     */
    boolean isAuthorized(HttpServletRequest request);
}
