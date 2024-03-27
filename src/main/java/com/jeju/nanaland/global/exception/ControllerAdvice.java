package com.jeju.nanaland.global.exception;

import com.jeju.nanaland.global.ApiResponse;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerAdvice {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ApiResponse<String> methodValidException(MethodArgumentNotValidException e) {
    String errorMessage = makeErrorResponse(e.getBindingResult());
    return ApiResponse.error(ErrorCode.REQUEST_VALIDATION_EXCEPTION, errorMessage);

  }

  @ExceptionHandler(BadRequestException.class)
  public ApiResponse<String> handleBadRequestException(BadRequestException e) {
    return ApiResponse.error(ErrorCode.REQUEST_VALIDATION_EXCEPTION, e.getMessage());
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ApiResponse<String> handleUnauthorizedException(UnauthorizedException e) {
    return ApiResponse.error(ErrorCode.EXPIRED_TOKEN, e.getMessage());
  }

  private String makeErrorResponse(BindingResult bindingResult) {
    String description = "";

    //에러가 있다면
    if (bindingResult.hasErrors()) {
      String bindResultCode = bindingResult.getFieldError().getCode();

      switch (bindResultCode) {
        case "NotNull":
          description = "필수 값을 채워주세요.";
          break;

// 나중에 필요한 validation 추가
//        case "Min":
//          description = ;
//          break;
      }
    }

    return description;
  }
}
