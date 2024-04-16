package com.jeju.nanaland.global.exception;

import static com.jeju.nanaland.global.exception.ErrorCode.BAD_REQUEST_EXCEPTION;
import static com.jeju.nanaland.global.exception.ErrorCode.CONFLICT_DATA;
import static com.jeju.nanaland.global.exception.ErrorCode.EXPIRED_TOKEN;
import static com.jeju.nanaland.global.exception.ErrorCode.INTERNAL_SERVER_ERROR;
import static com.jeju.nanaland.global.exception.ErrorCode.REQUEST_VALIDATION_EXCEPTION;
import static com.jeju.nanaland.global.exception.ErrorCode.UNSUPPORTED_FILE_FORMAT;

import com.jeju.nanaland.global.BaseResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
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
  public BaseResponse<Map<String, String>> methodValidException(MethodArgumentNotValidException e) {
    Map<String, String> errors = new HashMap<>();
    e.getBindingResult().getAllErrors().forEach(error -> {
      FieldError fieldError = (FieldError) error;
      String fieldName = fieldError.getField();
      String errorMessage = error.getDefaultMessage();
      // 필드명 : 에러명
      errors.put(fieldName, errorMessage);
    });

    return BaseResponse.error(REQUEST_VALIDATION_EXCEPTION, errors);
  }

  //400에러 (bad request)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(BadRequestException.class)
  public BaseResponse<String> handleBadRequestException(BadRequestException e) {
    return BaseResponse.error(BAD_REQUEST_EXCEPTION, e.getMessage());
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public BaseResponse<String> methodValidException(HttpMessageNotReadableException e) {
    return BaseResponse.error(REQUEST_VALIDATION_EXCEPTION, e.getMessage());
  }

  //401에러
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  @ExceptionHandler(UnauthorizedException.class)
  public BaseResponse<String> handleUnauthorizedException(UnauthorizedException e) {
    return BaseResponse.error(EXPIRED_TOKEN, e.getMessage());
  }

  // 409에러
  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(ConflictException.class)
  public BaseResponse<String> handleConflictException(ConflictException e) {
    return BaseResponse.error(CONFLICT_DATA, e.getMessage());
  }

  //415에러 (파일 확장자 오류)
  @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
  @ExceptionHandler(UnsupportedFileFormatException.class)
  public BaseResponse<String> handleUnsupportedFileFormatException(
      UnsupportedFileFormatException e) {
    return BaseResponse.error(UNSUPPORTED_FILE_FORMAT, e.getMessage());
  }

  // 500 에러
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(ServerErrorException.class)
  public BaseResponse<String> handleServerErrorException(
      ServerErrorException e) {
    return BaseResponse.error(INTERNAL_SERVER_ERROR, e.getMessage());
  }

  private String makeErrorResponse(BindingResult bindingResult) {
    String description = "";

    //에러가 있다면
    if (bindingResult.hasErrors()) {
      String bindResultDtoCode = bindingResult.getFieldError().getCode();
      switch (bindResultDtoCode) {
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
