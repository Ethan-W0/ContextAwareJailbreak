package com.jailbreak.agent.controller;

import com.jailbreak.agent.security.AuthorizationInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthorizationInterceptor authInterceptor;

    public AuthController(AuthorizationInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @PostMapping("/confirm")
    public Map<String, String> confirmAuth(HttpServletRequest request) {
        String token = authInterceptor.confirmAuthorization(request);
        return Map.of("token", token);
    }
}
