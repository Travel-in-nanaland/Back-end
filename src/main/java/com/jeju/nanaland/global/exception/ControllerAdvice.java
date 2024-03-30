package com.jeju.nanaland.global.exception;

import com.jeju.nanaland.global.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ControllerAdvice {

  // 400에러 (valid exception)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ApiResponse<String> methodValidException(MethodArgumentNotValidException e) {
    String errorMessage = makeErrorResponse(e.getBindingResult());
    return ApiResponse.error(ErrorCode.REQUEST_VALIDATION_EXCEPTION, errorMessage);
  }

  //400에러 (bad request)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(BadRequestException.class)
  public ApiResponse<String> handleBadRequestException(BadRequestException e) {
    return ApiResponse.error(ErrorCode.BAD_REQUEST_EXCEPTION, e.getMessage());
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ApiResponse<String> methodValidException(HttpMessageNotReadableException e) {
    return ApiResponse.error(ErrorCode.REQUEST_VALIDATION_EXCEPTION, e.getMessage());
  }

  //401에러
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
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
