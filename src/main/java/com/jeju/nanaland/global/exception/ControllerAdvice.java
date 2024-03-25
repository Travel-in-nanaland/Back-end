package com.jeju.nanaland.global.exception;

import com.jeju.nanaland.global.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerAdvice {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ApiResponse<String> methodValidException(MethodArgumentNotValidException e) {
    String errorMessage = makeErrorResponse(e.getBindingResult());
    return ApiResponse.error(HttpStatus.BAD_REQUEST, errorMessage);

  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ApiResponse<String>> handleBadRequestException(BadRequestException e) {
    ApiResponse<String> httpRes = new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    return new ResponseEntity<>(httpRes, HttpStatus.BAD_REQUEST);
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
