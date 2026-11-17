package com.gth.posting.application.usecase.getcampaign;

import com.gth.posting.application.dto.GroupDto;
import com.gth.posting.application.mapper.CampaignDtoMapper;
import com.gth.posting.application.mapper.PostJobDtoMapper;
import com.gth.posting.application.port.out.repository.CampaignRepository;
import com.gth.posting.application.port.out.repository.GroupRepository;
import com.gth.posting.application.port.out.repository.PostJobRepository;
import com.gth.posting.domain.entity.Campaign;
import com.gth.posting.domain.exception.PostingDomainException;
import com.gth.posting.domain.valueobject.CampaignId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetCampaignService implements GetCampaignUseCase {

    private final CampaignRepository campaignRepository;
    private final PostJobRepository postJobRepository;
    private final GroupRepository groupRepository;

    public GetCampaignService(CampaignRepository campaignRepository,
                              PostJobRepository postJobRepository,
                              GroupRepository groupRepository) {
        this.campaignRepository = campaignRepository;
        this.postJobRepository = postJobRepository;
        this.groupRepository = groupRepository;
    }

    @Override
    public GetCampaignResult handle(String campaignId) {
        CampaignId id = CampaignId.of(campaignId);
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new PostingDomainException("CAMPAIGN_NOT_FOUND", "Khong thay campaign " + campaignId));

        List<GroupDto> groups = postJobRepository.findByCampaignId(id).stream()
                .map(job -> groupRepository.findById(job.groupId()).orElse(null))
                .filter(java.util.Objects::nonNull)
                .distinct()
                .map(GroupDto::from)
                .toList();

        return new GetCampaignResult(
                CampaignDtoMapper.toDto(campaign),
                postJobRepository.findByCampaignId(id).stream().map(PostJobDtoMapper::toDto).toList(),
                groups
        );
    }
}
