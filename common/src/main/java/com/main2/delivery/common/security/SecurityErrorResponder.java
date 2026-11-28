package com.main2.delivery.common.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.MediaType;

import com.main2.delivery.common.exception.ErrorCode;
import com.main2.delivery.common.response.ApiResponse;
import com.main2.delivery.common.response.ErrorBody;

import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

// 필터처럼 DispatcherServlet 밖에서 실패 응답을 쓸 때 사용 (컨트롤러와 같은 ApiResponse 모양 보장)
// #32의 401(AuthenticationEntryPoint)·403(AccessDeniedHandler)에서도 재사용
public class SecurityErrorResponder {
    private final JsonMapper jsonMapper;

    public SecurityErrorResponder(JsonMapper jsonMapper){
        this.jsonMapper = jsonMapper;
    }

    public void write(HttpServletResponse response, ErrorCode errorCode, String message)
        throws IOException{
            ErrorBody body = new ErrorBody(errorCode.name(), message, List.of());
            response.setStatus(errorCode.getStatus().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(jsonMapper.writeValueAsString(ApiResponse.fail(body)));
        }
    
        public void write(HttpServletResponse response, ErrorCode errorCode)
            throws IOException{
                write(response, errorCode, errorCode.getMessage());
            }
}
