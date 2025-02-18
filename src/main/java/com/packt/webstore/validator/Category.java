package com.packt.webstore.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Target( { FIELD, METHOD, ANNOTATION_TYPE } )
@Constraint(validatedBy = CategoryValidator.class)
public @interface Category {
    String message() default "{com.packt.webstore.validator.Category.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
