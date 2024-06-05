package com.jeju.nanaland.domain.common.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class NicknameValidator implements ConstraintValidator<NicknameValid, String> {

  private static final Pattern LENGTH_PATTERN = Pattern.compile("^.{2,12}$");
  private static final Pattern VALID_CHARS_PATTERN = Pattern.compile(
      "^[a-zA-Z0-9\uAC00-\uD7AF\u4E00-\u9FFF\u00C0-\u024F\u1EFF ]+$");
  private static final Pattern START_END_PATTERN = Pattern.compile(
      "^[a-zA-Z0-9\uAC00-\uD7AF\u4E00-\u9FFF\u00C0-\u024F\u1EFF].*[a-zA-Z0-9\uAC00-\uD7AF\u4E00-\u9FFF\u00C0-\u024F\u1EFF]$");

  @Override
  public boolean isValid(String nickname, ConstraintValidatorContext context) {
    if (nickname == null || !LENGTH_PATTERN.matcher(nickname).matches()) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate("닉네임은 최소 2자에서 최대 12자여야 합니다.")
          .addConstraintViolation();
      return false;
    }

    if (!VALID_CHARS_PATTERN.matcher(nickname).matches()) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(
              "닉네임은 영문자, 숫자, 한국어, 중국어, 말레이어, 베트남어 문자, 공백만 포함할 수 있습니다.")
          .addConstraintViolation();
      return false;
    }

    if (!START_END_PATTERN.matcher(nickname).matches()) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(
              "닉네임은 영문자, 숫자, 한국어, 중국어, 말레이어, 베트남어 문자로 시작하고 끝나야 합니다.")
          .addConstraintViolation();
      return false;
    }

    return true;
  }
}
