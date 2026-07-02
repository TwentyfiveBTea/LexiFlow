package com.btea.lexiflow.infrastructure.web.interceptor;

import com.btea.lexiflow.common.context.UserContext;
import com.btea.lexiflow.common.convention.errorcode.BaseErrorCode;
import com.btea.lexiflow.common.convention.exception.ClientException;
import com.btea.lexiflow.infrastructure.security.config.JwtConfig;
import com.btea.lexiflow.infrastructure.security.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/2 12:51
 * @Description: 认证拦截器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final JwtConfig jwtConfig;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authHeader = request.getHeader(jwtConfig.getHeader());
        if (authHeader == null || !authHeader.startsWith(jwtConfig.getPrefix())) {
            throw new ClientException(BaseErrorCode.USER_NOT_LOGIN);
        }

        String token = jwtUtil.extractTokenFromHeader(authHeader);
        String userId;
        try {
            userId = jwtUtil.parseUserId(token);
        } catch (Exception e) {
            log.warn("解析用户ID失败: {}", e.getMessage());
            throw new ClientException(BaseErrorCode.TOKEN_INVALID);
        }

        if (userId == null || userId.isEmpty()) {
            log.warn("解析得到的用户ID为空");
            throw new ClientException(BaseErrorCode.TOKEN_INVALID);
        }

        if (!jwtUtil.isTokenExists(userId) || !jwtUtil.isTokenMatched(userId, token)) {
            log.warn("Redis中不存在该用户的有效token: {}", userId);
            throw new ClientException(BaseErrorCode.TOKEN_INVALID);
        }

        UserContext.setCurrentUserId(userId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
