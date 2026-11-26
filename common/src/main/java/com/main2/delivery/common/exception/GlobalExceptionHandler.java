package com.main2.delivery.common.exception;

import com.main2.delivery.common.response.ApiResponse;
import com.main2.delivery.common.response.ErrorBody;
import com.main2.delivery.common.response.ErrorBody.FieldErrorDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@Slf4j 
@RestControllerAdvice 
public class GlobalExceptionHandler {

    // 1) 우리가 직접 던진 예외
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e){
        log.warn("[{}] {}", e.getErrorCode().name(), e.getMessage());
        return toResponse(e.getErrorCode(), e.getMessage(), List.of());
    }

    // 2) @Valid @RequestBody 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleBodyValidation(MethodArgumentNotValidException e){
        List<FieldErrorDetail> fields = e.getBindingResult().getFieldErrors().stream()
                .map(f -> new FieldErrorDetail(f.getField(), f.getDefaultMessage()))
                .toList();
        return toResponse(CommonErrorCode.COMMON_INVALID_INPUT, null, fields);
    }

    // 3) @RequestParam, @PathVariable 검증 실패
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleParamValidation(HandlerMethodValidationException e){
        List<FieldErrorDetail> fields = e.getParameterValidationResults().stream()
                .flatMap(r -> r.getResolvableErrors().stream()
                    .map(err -> new FieldErrorDetail(
                            r.getMethodParameter().getParameterName(),
                            err.getDefaultMessage()
                    )))
                .toList();
        return toResponse(CommonErrorCode.COMMON_INVALID_INPUT, null, fields);
    }

    // 4) 파라미터 누락, 타입 불일치
    @ExceptionHandler({
        MissingServletRequestParameterException.class,
        MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadparam(Exception e){
        return toResponse(CommonErrorCode.COMMON_INVALID_INPUT, null, List.of());
    }

    // 5) JSON 문법 오류
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadable(HttpMessageNotReadableException e){
        return toResponse(CommonErrorCode.COMMON_INVALID_JSON, null, List.of());
    }

    // 6) 없는 URL
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException e){
        return toResponse(CommonErrorCode.COMMON_NOT_FOUND, null, List.of());
    }

    // 7) 지원하지 않는 요청 방식
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethod(HttpRequestMethodNotSupportedException e){
        return toResponse(CommonErrorCode.COMMON_METHOD_NOT_ALLOWED, null, List.of());
    }

    // 8) 그 외 전부 (예상 못한 버그)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknown(Exception e){
        log.error("처리되지 않은 예외", e);
        return toResponse(CommonErrorCode.COMMON_INTERNAL_ERROR, null, List.of());
    }

    private ResponseEntity<ApiResponse<Void>> toResponse(
        ErrorCode code, String message, List<FieldErrorDetail> fields                                                          
    ){
        String msg = ( message != null ) ? message : code.getMessage();
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.fail(new ErrorBody(code.name(), msg, fields)));
    }
}
