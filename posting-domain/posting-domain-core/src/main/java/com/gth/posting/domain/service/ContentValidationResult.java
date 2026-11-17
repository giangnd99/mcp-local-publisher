package com.gth.posting.domain.service;

import java.util.List;

public record ContentValidationResult(boolean valid, List<ContentViolation> violations) {

    public ContentValidationResult {
        violations = violations == null ? List.of() : List.copyOf(violations);
    }

    public static ContentValidationResult passed() {
        return new ContentValidationResult(true, List.of());
    }

    public static ContentValidationResult of(List<ContentViolation> violations) {
        return new ContentValidationResult(violations.isEmpty(), violations);
    }

    public String describe() {
        return violations.stream()
                .map(v -> v.code() + ": " + v.message())
                .reduce((a, b) -> a + "; " + b)
                .orElse("");
    }
}
