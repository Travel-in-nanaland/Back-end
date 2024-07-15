package com.jeju.nanaland.domain.common.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnumValidator implements ConstraintValidator<EnumValid, Object> {

  private Set<String> acceptedValues;
  private boolean ignoreCase;

  @Override
  public void initialize(EnumValid annotation) {
    ignoreCase = annotation.ignoreCase();
    Class<? extends Enum<?>> enumClass = annotation.enumClass();
    String[] exclude = annotation.exclude();

    Enum<?>[] enumValues = enumClass.getEnumConstants();

    acceptedValues = Arrays.stream(enumValues)
        .map(Enum::name)
        .filter(value -> Arrays.stream(exclude).noneMatch(excludeValue ->
            ignoreCase ? excludeValue.equalsIgnoreCase(value) : excludeValue.equals(value)))
        .collect(Collectors.toSet());
  }

  @Override
  public boolean isValid(Object value,
      ConstraintValidatorContext context) { // String, List<> 경우 다 받기 위해 Object 로 변경
    if (value == null) {
      return true;
    }

    if (value instanceof String) { //단일 String일경우
      return isValidStringValue((String) value);
    } else if (value instanceof List) { // List일 경우
      return isValidListValue((List<?>) value);
    } else {
      return false;
    }
  }

  private boolean isValidStringValue(String value) {
    if (ignoreCase) {
      return acceptedValues.stream()
          .anyMatch(enumValue -> enumValue.equalsIgnoreCase(value));
    } else {
      return acceptedValues.contains(value);
    }
  }

  private boolean isValidListValue(List<?> values) { // List 형태인 경우 for문으로 isValidStringValue 호출
    for (Object value : values) {
      if (value == null) {
        return false; //  null이면 유효하지 않음
      }
      if (!isValidStringValue((String) value)) {
        return false; // 유효하지 않은 문자열이 포함되어 있으면 유효성 검사 실패
      }
    }
    return true; // 모든 값이 유효하면 유효성 검사 통과
  }
}
