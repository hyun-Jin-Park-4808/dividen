package com.example.dividend.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String TOKEN_HEADER = "Authorization"; // 어떤 기준으로 토큰 주고 받을지
    public static final String TOKEN_PREFIX = "Bearer "; // 인증 타입 나타내기 위함. Bearer 뒤에 토큰 붙게 됨.

    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = this.resolveTokenFromRequest(request);

        if (StringUtils.hasText(token) && this.tokenProvider.validateToken(token)) { // 토큰 가지고 있으면 토큰 유효한지 확인
            // 토큰 유효성 검증
            Authentication auth = this.tokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);

            log.info(String.format("[%s] -> %s", this.tokenProvider.getUsername(token), request.getRequestURI()));
            // 어떤 사용자가 어떤 경로로 접근했는지 로그에 남김.
        }

        filterChain.doFilter(request, response); // 필터 연속 실행 가능하도록 함.

    }

    private String resolveTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader(TOKEN_HEADER);

        if (!ObjectUtils.isEmpty(token) && token.startsWith(TOKEN_PREFIX)) { // 토큰이 비어있지않고, Prefix로 시작한다면
            return token.substring(TOKEN_PREFIX.length()); // 프리픽스 제외한 실제 토큰 부분 반환
        }
        return null;
    }
}
