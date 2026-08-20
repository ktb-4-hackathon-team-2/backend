package com.example.ktb_hktn_team2.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 값이 지정한 배수인지 검증한다. (예: 10 단위)
 * <p>
 * null 은 통과시키므로 필수 여부는 {@code @NotNull} 로 따로 지정한다.
 */
@Documented
@Constraint(validatedBy = MultipleOfValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MultipleOf {

    int value();

    String message() default "{value} 단위여야 합니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
