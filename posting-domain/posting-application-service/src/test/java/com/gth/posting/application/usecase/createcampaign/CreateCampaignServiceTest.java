package com.gth.posting.application.usecase.createcampaign;

import com.gth.posting.application.port.out.ai.ContentCritic;
import com.gth.posting.application.port.out.ai.CopyBrief;
import com.gth.posting.application.port.out.ai.Copywriter;
import com.gth.posting.application.port.out.clock.TimeProvider;
import com.gth.posting.application.port.out.repository.CampaignRepository;
import com.gth.posting.application.port.out.repository.GroupRepository;
import com.gth.posting.application.port.out.repository.PostJobRepository;
import com.gth.posting.application.support.JobEventRecorder;
import com.gth.posting.domain.entity.FbGroup;
import com.gth.posting.domain.exception.PostingDomainException;
import com.gth.posting.domain.service.ContentPolicy;
import com.gth.posting.domain.valueobject.GroupId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Kiểm tra tạo chiến dịch gọi đúng cổng AI và chỉ dùng nhóm đã xác nhận. */
class CreateCampaignServiceTest {

    /** Khi nội dung để trống, ca sử dụng soạn và rà soát bài theo tên nhóm đã lưu. */
    @Test
    void generatesAndReviewsBlankGroupContent() {
        CampaignRepository campaignRepository = mock(CampaignRepository.class);
        PostJobRepository postJobRepository = mock(PostJobRepository.class);
        GroupRepository groupRepository = mock(GroupRepository.class);
        Copywriter copywriter = mock(Copywriter.class);
        ContentCritic contentCritic = mock(ContentCritic.class);
        TimeProvider timeProvider = mock(TimeProvider.class);
        FbGroup group = new FbGroup(GroupId.of("123456"), "Laptop Thủ Dầu Một", 1200,
                "Bình Dương", true, null);
        when(groupRepository.findById(group.id())).thenReturn(Optional.of(group));
        when(timeProvider.now()).thenReturn(Instant.parse("2026-01-01T00:00:00Z"));
        when(copywriter.write(any(CopyBrief.class))).thenReturn(
                "Mình chia sẻ ThinkPad T480 i5 8350U RAM 16GB SSD 512GB đến anh chị trong nhóm tại Bình Dương. Giá 7500000 đồng. "
                        + "Anh chị quan tâm có thể nhắn tin để hỏi thêm thông tin và trao đổi rõ tình trạng máy trước khi quyết định.");
        when(contentCritic.critique(any(), any())).thenReturn(com.gth.posting.application.port.out.ai.CritiqueResult.ok());
        when(campaignRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(postJobRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        JobEventRecorder eventRecorder = mock(JobEventRecorder.class);
        CreateCampaignPersistenceService persistenceService = new CreateCampaignPersistenceService(
                campaignRepository, postJobRepository, eventRecorder, timeProvider);
        CreateCampaignService service = new CreateCampaignService(groupRepository, new ContentPolicy(),
                copywriter, contentCritic, persistenceService);
        CreateCampaignCommand command = new CreateCampaignCommand("Chiến dịch tháng 1", "ThinkPad T480",
                7_500_000, "6 tháng", "0900000000", List.of(),
                List.of(new CreateCampaignCommand.GroupInput(group.id().value(), "Tên từ agent không đáng tin",
                        1, "Tỉnh khác", "")));

        var result = service.handle(command);

        assertEquals("Laptop Thủ Dầu Một", result.jobs().getFirst().groupName());
        verify(copywriter).write(any(CopyBrief.class));
        verify(contentCritic).critique(any(), any());
    }

    /** Không cho tạo job bằng mã nhóm agent tự đưa nhưng chưa có trong danh sách đã lưu. */
    @Test
    void rejectsUnknownGroupBeforeGeneratingContent() {
        GroupRepository groupRepository = mock(GroupRepository.class);
        Copywriter copywriter = mock(Copywriter.class);
        when(groupRepository.findById(GroupId.of("123456"))).thenReturn(Optional.empty());
        CreateCampaignService service = new CreateCampaignService(groupRepository, new ContentPolicy(),
                copywriter, mock(ContentCritic.class), mock(CreateCampaignPersistenceService.class));

        assertThrows(PostingDomainException.class, () -> service.handle(new CreateCampaignCommand(
                "Chiến dịch", "ThinkPad T480", 7_500_000, "", "", List.of(),
                List.of(new CreateCampaignCommand.GroupInput("123456", "Nhóm giả", 0, "", "")))));
        verify(copywriter, never()).write(any());
    }
}
