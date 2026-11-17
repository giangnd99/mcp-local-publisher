package com.gth.posting.application.usecase.listcampaigns;

import com.gth.posting.application.dto.CampaignDto;
import com.gth.posting.application.mapper.CampaignDtoMapper;
import com.gth.posting.application.port.out.repository.CampaignRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/** Truy vấn chiến dịch qua repository port, không phụ thuộc adapter lưu trữ. */
@Service
@Transactional(readOnly = true)
public class ListCampaignsService implements ListCampaignsUseCase {

    private final CampaignRepository campaignRepository;

    /** Khởi tạo ca sử dụng với cổng đọc chiến dịch. */
    public ListCampaignsService(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    /** Đọc chiến dịch mới nhất trước rồi chuyển sang DTO cho client. */
    @Override
    public List<CampaignDto> handle() {
        return campaignRepository.findAll().stream()
                .sorted(Comparator.comparing(com.gth.posting.domain.entity.Campaign::createdAt).reversed())
                .map(CampaignDtoMapper::toDto)
                .toList();
    }
}
