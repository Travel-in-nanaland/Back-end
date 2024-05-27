package com.jeju.nanaland.domain.common.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnumValidator implements ConstraintValidator<EnumValid, String> {

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
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }

    if (ignoreCase) {
      return acceptedValues.stream()
          .anyMatch(enumValue -> enumValue.equalsIgnoreCase(value));
    } else {
      return acceptedValues.contains(value);
    }
  }
}
