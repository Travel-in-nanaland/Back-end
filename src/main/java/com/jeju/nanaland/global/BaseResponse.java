package com.jeju.nanaland.global;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.exception.SuccessCode;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {

  private final int status;
  private final String message;

  // null일 경우 response에 포함되지 않는다.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private T data;

  // status의 message는 영어로 OK, created 등이어서 커스텀할 경우 success code, error code로 확장 예정

  //전달할 Json이 없는 경우
  public static <T> BaseResponse<T> success(SuccessCode successCode) {
    return new BaseResponse<>(successCode.getHttpStatusCode(), successCode.getMessage());
  }

  //json 전달이 필요한 경우
  public static <T> BaseResponse<T> success(SuccessCode successCode, T data) {
    return new BaseResponse<>(successCode.getHttpStatusCode(), successCode.getMessage(), data);
  }

  public static <T> BaseResponse<T> error(ErrorCode errorCode) {
    return new BaseResponse<>(errorCode.getHttpStatusCode(), errorCode.getMessage());
  }

  //메세지를 추가해서 에러를 반환하고 싶은 경우
  public static <T> BaseResponse<T> error(ErrorCode errorCode, @Nullable String message) {
    return new BaseResponse<>(errorCode.getHttpStatusCode(), message);
  }

  public static <T> BaseResponse<T> error(ErrorCode errorCode, T data) {
    return new BaseResponse<>(errorCode.getHttpStatusCode(), errorCode.getMessage(), data);
  }
}
