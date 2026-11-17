package com.gth.posting.dao.campaign.repository;

import com.gth.posting.dao.campaign.entity.CampaignEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignJpaRepository extends JpaRepository<CampaignEntity, String> {
}
