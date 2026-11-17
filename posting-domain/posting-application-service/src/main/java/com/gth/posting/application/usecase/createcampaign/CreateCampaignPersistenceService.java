package com.gth.posting.application.usecase.createcampaign;

import com.gth.posting.application.dto.PostJobDto;
import com.gth.posting.application.mapper.CampaignDtoMapper;
import com.gth.posting.application.mapper.PostJobDtoMapper;
import com.gth.posting.application.port.out.clock.TimeProvider;
import com.gth.posting.application.port.out.repository.CampaignRepository;
import com.gth.posting.application.port.out.repository.PostJobRepository;
import com.gth.posting.application.support.JobEventRecorder;
import com.gth.posting.domain.entity.Campaign;
import com.gth.posting.domain.entity.JobEventType;
import com.gth.posting.domain.entity.PostJob;
import com.gth.posting.domain.valueobject.Money;
import com.gth.posting.domain.valueobject.PostText;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Ghi chiến dịch và các bài nháp trong một giao dịch sau khi soạn nội dung xong. */
@Service
public class CreateCampaignPersistenceService {

    private final CampaignRepository campaignRepository;
    private final PostJobRepository postJobRepository;
    private final JobEventRecorder eventRecorder;
    private final TimeProvider timeProvider;

    /** Khởi tạo bộ ghi chiến dịch với các cổng lưu trữ và đồng hồ ứng dụng. */
    public CreateCampaignPersistenceService(CampaignRepository campaignRepository,
                                            PostJobRepository postJobRepository,
                                            JobEventRecorder eventRecorder,
                                            TimeProvider timeProvider) {
        this.campaignRepository = campaignRepository;
        this.postJobRepository = postJobRepository;
        this.eventRecorder = eventRecorder;
        this.timeProvider = timeProvider;
    }

    /** Lưu chiến dịch cùng toàn bộ bài nháp nguyên tử để tránh dữ liệu dở dang. */
    @Transactional
    public CreateCampaignResult persist(CreateCampaignCommand command, List<PreparedGroupContent> preparedGroups) {
        Instant now = timeProvider.now();
        Campaign campaign = Campaign.create(command.name(), command.productName(), Money.vnd(command.priceAmount()),
                command.warrantyText(), command.phoneNumber(), command.contentObjective(), command.brandName(),
                command.photoPaths(), now);
        campaign.activate(now);
        Campaign savedCampaign = campaignRepository.save(campaign);

        List<PostJobDto> jobs = new ArrayList<>();
        for (PreparedGroupContent prepared : preparedGroups) {
            PostJob job = PostJob.create(savedCampaign.id(), prepared.group().id(), prepared.group().name(),
                    PostText.of(prepared.text()), now);
            PostJob stored = postJobRepository.save(job);
            eventRecorder.recordStateChanges(stored);
            eventRecorder.record(stored.id(), JobEventType.NOTE,
                    "Khởi tạo trong chiến dịch " + savedCampaign.name(), now);
            jobs.add(PostJobDtoMapper.toDto(stored));
        }

        return new CreateCampaignResult(CampaignDtoMapper.toDto(savedCampaign), jobs, List.of());
    }
}
