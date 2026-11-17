package com.gth.posting.application.usecase.queuebatch;

public interface QueueBatchUseCase {

    QueueBatchResult handle(String campaignId, int batchSize);
}
