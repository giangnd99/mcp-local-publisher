package com.gth.posting.application.port.out.repository;

import com.gth.posting.domain.entity.Campaign;
import com.gth.posting.domain.valueobject.CampaignId;

import java.util.List;
import java.util.Optional;

public interface CampaignRepository {

    Campaign save(Campaign campaign);

    Optional<Campaign> findById(CampaignId id);

    List<Campaign> findAll();
}
