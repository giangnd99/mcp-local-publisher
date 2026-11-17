package com.gth.posting.application.port.out.ai;

import java.util.List;

public record CritiqueResult(boolean acceptable, List<String> issues) {

    public CritiqueResult {
        issues = issues == null ? List.of() : List.copyOf(issues);
    }

    public static CritiqueResult ok() {
        return new CritiqueResult(true, List.of());
    }

    public static CritiqueResult rejected(List<String> issues) {
        return new CritiqueResult(false, issues);
    }
}
