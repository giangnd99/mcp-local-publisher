package com.gth.posting.dao.campaign.adapter;

import com.gth.posting.application.port.out.repository.CampaignRepository;
import com.gth.posting.dao.campaign.mapper.CampaignDataAccessMapper;
import com.gth.posting.dao.campaign.repository.CampaignJpaRepository;
import com.gth.posting.domain.entity.Campaign;
import com.gth.posting.domain.valueobject.CampaignId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class CampaignRepositoryImpl implements CampaignRepository {

    private final CampaignJpaRepository jpaRepository;

    public CampaignRepositoryImpl(CampaignJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Campaign save(Campaign campaign) {
        return CampaignDataAccessMapper.toDomain(
                jpaRepository.save(CampaignDataAccessMapper.toEntity(campaign)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Campaign> findById(CampaignId id) {
        return jpaRepository.findById(id.value()).map(CampaignDataAccessMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> findAll() {
        return jpaRepository.findAll().stream().map(CampaignDataAccessMapper::toDomain).toList();
    }
}
