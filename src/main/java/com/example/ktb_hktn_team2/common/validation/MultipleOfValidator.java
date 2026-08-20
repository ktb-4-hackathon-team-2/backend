package com.example.ktb_hktn_team2.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MultipleOfValidator implements ConstraintValidator<MultipleOf, Integer> {

    private int multiple;

    @Override
    public void initialize(MultipleOf constraint) {
        this.multiple = constraint.value();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return value == null || value % multiple == 0;
    }
}
