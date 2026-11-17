package com.gth.posting.application.usecase.listcampaigns;

import com.gth.posting.application.dto.CampaignDto;

import java.util.List;

/** Ca sử dụng truy vấn các chiến dịch để agent có thể tiếp tục thao tác. */
public interface ListCampaignsUseCase {

    /** Trả về danh sách chiến dịch hiện có theo thứ tự gần nhất trước. */
    List<CampaignDto> handle();
}
