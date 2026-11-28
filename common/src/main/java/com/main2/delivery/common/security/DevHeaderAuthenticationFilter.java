package com.main2.delivery.common.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.main2.delivery.common.exception.CommonErrorCode;
import com.main2.delivery.common.type.Role;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// 개발용: 테스트 헤더로 "누구인지"를 SecurityContext에 넣는다 (#32에서 JWT 쿠키 필터로 교체)
// @Component를 붙이지 않는다 → 붙이면 톰캣 필터로도 등록돼 두 번 실행됨. SecurityConfig에서 new로 넣음
public class DevHeaderAuthenticationFilter extends OncePerRequestFilter {

    public static final String USER_ID_HEADER = "X-Test-User-Id";
    public static final String ROLE_HEADER = "X-Test-Role";

    // 헤더가 둘 다 없으면 기본 고객 1번 (#12 합의)
    private static final LoginUser DEFAULT_USER = new LoginUser(1L, Role.CUSTOMER);

    private final SecurityErrorResponder errorResponder;

    public DevHeaderAuthenticationFilter(SecurityErrorResponder errorResponder) {
        this.errorResponder = errorResponder;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        LoginUser user;
        try {
            user = resolve(request.getHeader(USER_ID_HEADER), request.getHeader(ROLE_HEADER));
        } catch (IllegalArgumentException e) {
            // 필터에서 난 오류는 GlobalExceptionHandler가 못 잡으므로 여기서 공통 응답으로 씀
            errorResponder.write(response, CommonErrorCode.COMMON_INVALID_INPUT, e.getMessage());
            return;
        }

        UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(
                user, null, List.of(new SimpleGrantedAuthority(user.role().authority())));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        chain.doFilter(request, response);
    }

    private LoginUser resolve(String idHeader, String roleHeader) {
        if (idHeader == null && roleHeader == null) {
            return DEFAULT_USER;
        }
        if (idHeader == null) {
            throw new IllegalArgumentException(USER_ID_HEADER + " 헤더가 필요합니다.");
        }

        long id;
        try {
            id = Long.parseLong(idHeader.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(USER_ID_HEADER + "는 숫자여야 합니다.");
        }
        if (id < 1) {
            throw new IllegalArgumentException(USER_ID_HEADER + "는 1 이상이어야 합니다.");
        }

        Role role;
        try {
            role = (roleHeader == null) ? Role.CUSTOMER : Role.valueOf(roleHeader.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(ROLE_HEADER + "는 CUSTOMER, OWNER, ADMIN 중 하나여야 합니다.");
        }
        return new LoginUser(id, role);
    }
}