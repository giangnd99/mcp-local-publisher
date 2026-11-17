package com.gth.posting.api.controller;

import com.gth.posting.api.dto.CampaignRequestMapper;
import com.gth.posting.api.dto.CreateCampaignRequest;
import com.gth.posting.application.usecase.createcampaign.CreateCampaignResult;
import com.gth.posting.application.usecase.createcampaign.CreateCampaignUseCase;
import com.gth.posting.application.usecase.dispatchpost.DispatchPostResult;
import com.gth.posting.application.usecase.dispatchpost.DispatchPostUseCase;
import com.gth.posting.application.usecase.getcampaign.GetCampaignResult;
import com.gth.posting.application.usecase.getcampaign.GetCampaignUseCase;
import com.gth.posting.application.usecase.queuebatch.QueueBatchResult;
import com.gth.posting.application.usecase.queuebatch.QueueBatchUseCase;
import com.gth.posting.application.usecase.validatecampaign.ValidateCampaignResult;
import com.gth.posting.application.usecase.validatecampaign.ValidateCampaignUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adapter vao HTTP. Khong chua nghiep vu: chi doi kieu du lieu va goi use case.
 */
@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {

    private final CreateCampaignUseCase createCampaignUseCase;
    private final ValidateCampaignUseCase validateCampaignUseCase;
    private final QueueBatchUseCase queueBatchUseCase;
    private final DispatchPostUseCase dispatchPostUseCase;
    private final GetCampaignUseCase getCampaignUseCase;

    public CampaignController(CreateCampaignUseCase createCampaignUseCase,
                              ValidateCampaignUseCase validateCampaignUseCase,
                              QueueBatchUseCase queueBatchUseCase,
                              DispatchPostUseCase dispatchPostUseCase,
                              GetCampaignUseCase getCampaignUseCase) {
        this.createCampaignUseCase = createCampaignUseCase;
        this.validateCampaignUseCase = validateCampaignUseCase;
        this.queueBatchUseCase = queueBatchUseCase;
        this.dispatchPostUseCase = dispatchPostUseCase;
        this.getCampaignUseCase = getCampaignUseCase;
    }

    @PostMapping
    public ResponseEntity<CreateCampaignResult> create(@RequestBody CreateCampaignRequest request) {
        CreateCampaignResult result = createCampaignUseCase.handle(CampaignRequestMapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{campaignId}")
    public GetCampaignResult detail(@PathVariable String campaignId) {
        return getCampaignUseCase.handle(campaignId);
    }

    @PostMapping("/{campaignId}/validate")
    public ValidateCampaignResult validate(@PathVariable String campaignId) {
        return validateCampaignUseCase.handle(campaignId);
    }

    @PostMapping("/{campaignId}/queue")
    public QueueBatchResult queue(@PathVariable String campaignId,
                                  @RequestParam(defaultValue = "6") int batchSize) {
        return queueBatchUseCase.handle(campaignId, batchSize);
    }

    @PostMapping("/{campaignId}/dispatch")
    public DispatchPostResult dispatch(@PathVariable String campaignId) {
        return dispatchPostUseCase.handle(campaignId);
    }
}
