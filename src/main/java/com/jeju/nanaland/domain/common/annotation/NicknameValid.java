package com.jeju.nanaland.domain.common.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = NicknameValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NicknameValid {

  String message() default "유효하지 않은 닉네임입니다.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
