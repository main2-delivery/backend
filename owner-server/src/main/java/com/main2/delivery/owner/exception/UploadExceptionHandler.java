package com.main2.delivery.owner.exception;

import com.main2.delivery.common.exception.ErrorCode;
import com.main2.delivery.common.response.ApiResponse;
import com.main2.delivery.common.response.ErrorBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.util.List;

// 업로드 관련 예외를 400 으로 바꾼다.
// 파일 크기 초과는 컨트롤러 앞(multipart 해석 단계)에서 나서 BusinessException 을 던질 기회가 없고,
// 공통 GlobalExceptionHandler 에는 이 예외들이 없어서 그대로 두면 마지막 Exception 처리로 500 이 나간다.
// 공통 처리기보다 먼저 보도록 우선순위를 가장 높게 둔다.
// TODO(팀장 확인): docs/api-response.md 규칙상 공통 처리기(common)에 옮기는 게 맞음 → 옮기면 이 클래스 삭제
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class UploadExceptionHandler {

    // 파일 크기 초과 (spring.servlet.multipart.max-file-size)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleTooLarge(MaxUploadSizeExceededException e) {
        return toResponse(OwnerErrorCode.IMAGE_TOO_LARGE);
    }

    // multipart 형식이 아니거나 깨진 요청
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiResponse<Void>> handleMultipart(MultipartException e) {
        log.warn("multipart 요청 오류: {}", e.getMessage());
        return toResponse(OwnerErrorCode.IMAGE_EMPTY);
    }

    private ResponseEntity<ApiResponse<Void>> toResponse(ErrorCode code) {
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.fail(new ErrorBody(code.name(), code.getMessage(), List.of())));
    }
}
