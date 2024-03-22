package com.jeju.nanaland.global;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

  private final int status;
  private final String message;

  // null일 경우 response에 포함되지 않는다.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private T data;

  // status의 message는 영어로 OK, created 등이어서 커스텀할 경우 success code, error code로 확장 예정

  //전달할 Json이 없는 경우
  public static <T> ApiResponse<T> success(HttpStatus status) {
    return new ApiResponse<>(status.value(), status.getReasonPhrase());
  }

  //json 전달이 필요한 경우
  public static <T> ApiResponse<T> success(HttpStatus status, T data) {
    return new ApiResponse<>(status.value(), status.getReasonPhrase(), data);
  }

  public static <T> ApiResponse<T> error(HttpStatus status) {
    return new ApiResponse<>(status.value(), status.getReasonPhrase());
  }

  //메세지를 추가해서 에러를 반환하고 싶은 경우
  public static <T> ApiResponse<T> error(HttpStatus status, @Nullable String message) {
    return new ApiResponse<>(status.value(), message);
  }
}
