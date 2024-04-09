package com.jeju.nanaland.global.exception;

import com.jeju.nanaland.global.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerAdvice {

  // 400에러 (valid exception)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public BaseResponse<String> methodValidException(MethodArgumentNotValidException e) {
    String errorMessage = makeErrorResponse(e.getBindingResult());
    return BaseResponse.error(ErrorCode.REQUEST_VALIDATION_EXCEPTION, errorMessage);

  }

  //400에러 (bad request)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(BadRequestException.class)
  public BaseResponse<String> handleBadRequestException(BadRequestException e) {
    return BaseResponse.error(ErrorCode.BAD_REQUEST_EXCEPTION, e.getMessage());
  }


  //401에러
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  @ExceptionHandler(UnauthorizedException.class)
  public BaseResponse<String> handleUnauthorizedException(UnauthorizedException e) {
    return BaseResponse.error(ErrorCode.EXPIRED_TOKEN, e.getMessage());
  }

  //415에러 (파일 확장자 오류)
  @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
  @ExceptionHandler(UnsupportedFileFormatException.class)
  public BaseResponse<String> handleUnsupportedFileFormatException(
      UnsupportedFileFormatException e) {
    return BaseResponse.error(ErrorCode.UNSUPPORTED_FILE_FORMAT, e.getMessage());
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
