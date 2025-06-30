package com.springcloud.personalfollower.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDto<T> {

    private int code;
    private String status;
    private String message;
    private T data;

    // 성공 응답을 쉽게 반환하는 정적 메서드
    public static <T> ResponseDto<T> success(T data) {
        return ResponseDto.<T>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK.getReasonPhrase())
                .message("성공적으로 처리되었습니다.")
                .data(data)
                .build();
    }

    public static <T> ResponseDto<T> success(HttpStatus status, T data) {
        return ResponseDto.<T>builder()
                .code(status.value())
                .status(status.getReasonPhrase())
                .message("성공적으로 처리되었습니다.")
                .data(data)
                .build();
    }

    public static <T> ResponseDto<T> success() {
        return ResponseDto.<T>builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK.getReasonPhrase())
                .message("성공적으로 처리되었습니다.")
                .build();
    }

    // 실패 응답 (기본 500 에러)
    public static <T> ResponseDto<T> failure(String message) {
        return failure(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    // 실패 응답 (커스텀 상태)
    public static <T> ResponseDto<T> failure(HttpStatus status, String message) {
        return ResponseDto.<T>builder()
                .code(status.value())
                .status(status.getReasonPhrase())
                .message(message)
                .data(null)
                .build();
    }
}
