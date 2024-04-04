package com.jeju.nanaland.domain.common.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = EnumValidator.class)
public @interface EnumValid {

  Class<? extends Enum<?>> enumClass();

  String message() default "해당하는 타입이 없습니다.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  boolean ignoreCase() default false;
}
