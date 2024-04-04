package com.jeju.nanaland.domain.common.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnumValidator implements ConstraintValidator<EnumValid, String> {

  private EnumValid enumValid;

  @Override
  public void initialize(EnumValid constraintAnnotation) {
    this.enumValid = constraintAnnotation;
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {

    boolean result = false;
    // 해당 Enum 클래스의 모든 상수를 반환
    Enum<?>[] enumValues = this.enumValid.enumClass().getEnumConstants();

    // value로 들어온 값이 Enum에 존재하는지 확인
    if (enumValues != null) {
      for (Object enumValue : enumValues) {
        if (value.equals(enumValue.toString()) ||
            this.enumValid.ignoreCase() && value.equalsIgnoreCase(enumValue.toString())) {

          result = true;
          break;
        }
      }
    }

    return result;
  }
}
