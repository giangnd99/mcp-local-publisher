package com.gth.posting.api.controller;

import com.gth.posting.application.usecase.verifypost.VerifyPostResult;
import com.gth.posting.application.usecase.verifypost.VerifyPostUseCase;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final VerifyPostUseCase verifyPostUseCase;

    public JobController(VerifyPostUseCase verifyPostUseCase) {
        this.verifyPostUseCase = verifyPostUseCase;
    }

    @PostMapping("/{postJobId}/verify")
    public VerifyPostResult verify(@PathVariable String postJobId) {
        return verifyPostUseCase.handle(postJobId);
    }
}
