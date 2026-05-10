package com.jailbreak.agent.security.impl;

import com.jailbreak.agent.security.AuthorizationInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthorizationInterceptorImpl implements AuthorizationInterceptor {

    private static final String AUTH_HEADER = "X-Auth-Token";
    private static final String AUTH_SESSION_KEY = "authorized";

    private final Map<String, String> tokens = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response) {
        if (isAuthorized(request)) return true;

        try {
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"error\":\"Unauthorized\",\"message\":\"请先完成授权确认\"}");
            response.getWriter().flush();
        } catch (Exception ignored) {}
        return false;
    }

    @Override
    public String confirmAuthorization(HttpServletRequest request) {
        String token = UUID.randomUUID().toString();
        String ip = request.getRemoteAddr();
        tokens.put(token, ip);
        request.getSession().setAttribute(AUTH_SESSION_KEY, true);
        return token;
    }

    @Override
    public boolean isAuthorized(HttpServletRequest request) {
        String xtoken = request.getHeader(AUTH_HEADER);
        if (xtoken == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                xtoken = authHeader.substring(7);
            }
        }
        if (xtoken != null && tokens.containsKey(xtoken)) return true;

        var session = request.getSession(false);
        if (session != null) {
            Object attr = session.getAttribute(AUTH_SESSION_KEY);
            return Boolean.TRUE.equals(attr);
        }
        return false;
    }
}
