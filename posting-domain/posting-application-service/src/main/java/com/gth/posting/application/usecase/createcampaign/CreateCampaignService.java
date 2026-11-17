package com.gth.posting.application.usecase.createcampaign;

import com.gth.posting.application.port.out.ai.ContentCritic;
import com.gth.posting.application.port.out.ai.CopyBrief;
import com.gth.posting.application.port.out.ai.Copywriter;
import com.gth.posting.application.port.out.repository.GroupRepository;
import com.gth.posting.domain.entity.FbGroup;
import com.gth.posting.domain.exception.PostingDomainException;
import com.gth.posting.domain.service.ContentPolicy;
import com.gth.posting.domain.service.ContentViolation;
import com.gth.posting.domain.service.GroupPostingPolicy;
import com.gth.posting.domain.valueobject.GroupId;
import com.gth.posting.domain.valueobject.ContentObjective;
import com.gth.posting.domain.valueobject.PostText;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CreateCampaignService implements CreateCampaignUseCase {

    private final GroupRepository groupRepository;
    private final ContentPolicy contentPolicy;
    private final Copywriter copywriter;
    private final ContentCritic contentCritic;
    private final CreateCampaignPersistenceService persistenceService;

    public CreateCampaignService(GroupRepository groupRepository,
                                 ContentPolicy contentPolicy,
                                 Copywriter copywriter,
                                 ContentCritic contentCritic,
                                 CreateCampaignPersistenceService persistenceService) {
        this.groupRepository = groupRepository;
        this.contentPolicy = contentPolicy;
        this.copywriter = copywriter;
        this.contentCritic = contentCritic;
        this.persistenceService = persistenceService;
    }

    /** Soạn và rà soát nội dung trước, sau đó mới ghi chiến dịch trong giao dịch riêng. */
    @Override
    public CreateCampaignResult handle(CreateCampaignCommand command) {
        if (command.contentObjective() == ContentObjective.DIRECT_SALE && command.priceAmount() <= 0) {
            throw new PostingDomainException("PRICE_REQUIRED",
                    "Bài bán hàng trực tiếp cần có giá lớn hơn 0.");
        }
        if (command.groups().isEmpty()) {
            throw new PostingDomainException("NO_GROUP", "Campaign phai co it nhat mot nhom");
        }
        rejectDuplicateGroups(command.groups());

        Map<GroupId, FbGroup> registeredGroups = resolveRegisteredGroups(command.groups());
        List<PreparedGroup> preparedGroups = command.groups().stream()
                .map(input -> prepareGroup(input, registeredGroups.get(GroupId.of(input.groupId())), command))
                .toList();
        List<PostText> texts = preparedGroups.stream().map(group -> PostText.of(group.text())).toList();
        List<ContentViolation> duplicates = contentPolicy.validateUniqueness(texts);
        if (!duplicates.isEmpty()) {
            throw new PostingDomainException("DUPLICATE_TEXT", duplicates.get(0).message());
        }

        List<PreparedGroupContent> persistenceInput = preparedGroups.stream()
                .map(group -> new PreparedGroupContent(group.group(), group.text()))
                .toList();
        return persistenceService.persist(command, persistenceInput);
    }

    /** Chỉ cho phép tạo bài vào nhóm đã được ứng dụng xác nhận và lưu trước đó. */
    private Map<GroupId, FbGroup> resolveRegisteredGroups(List<CreateCampaignCommand.GroupInput> inputs) {
        return inputs.stream()
                .map(input -> GroupId.of(input.groupId()))
                .distinct()
                .map(groupId -> groupRepository.findById(groupId)
                        .orElseThrow(() -> new PostingDomainException("GROUP_NOT_FOUND",
                                "Nhóm " + groupId.value() + " chưa được xác nhận. Hãy dùng list_groups để chọn nhóm đã lưu.")))
                .peek(group -> {
                    if (!GroupPostingPolicy.isTechnologyName(group.name())) {
                        throw new PostingDomainException("GROUP_TOPIC_NOT_ALLOWED",
                                GroupPostingPolicy.rejectionReason(group.name()));
                    }
                    if (!group.postingAllowed()) {
                        throw new PostingDomainException("GROUP_POSTING_DISABLED",
                                "Nhóm " + group.name() + " đang bị tắt quyền đăng.");
                    }
                })
                .collect(Collectors.toMap(FbGroup::id, Function.identity()));
    }

    /** Dùng nội dung người dùng truyền vào hoặc tạo và rà soát nội dung riêng cho nhóm. */
    private PreparedGroup prepareGroup(CreateCampaignCommand.GroupInput input,
                                       FbGroup group,
                                       CreateCampaignCommand command) {
        CopyBrief brief = new CopyBrief(command.productName(), command.priceAmount(), command.warrantyText(),
                command.phoneNumber(), command.productFacts(), group.name(), group.region(), "thân thiện, rõ ràng và trung thực",
                command.contentObjective(), command.brandName(), command.brandPromise());
        String text = input.text();
        if (text == null || text.isBlank()) {
            try {
                text = copywriter.write(brief);
            } catch (RuntimeException exception) {
                throw new PostingDomainException("CONTENT_GENERATION_FAILED",
                        "Không thể soạn nội dung cho nhóm " + group.name()
                                + ". Hãy kiểm tra cấu hình AI hoặc thử lại.");
            }
        }
        if (text == null || text.isBlank()) {
            throw new PostingDomainException("EMPTY_CONTENT", "Nội dung bài đăng không được để trống.");
        }

        try {
            var critique = contentCritic.critique(text, brief);
            if (!critique.acceptable()) {
                throw new PostingDomainException("CONTENT_REVIEW_FAILED",
                        "Nội dung cho nhóm " + group.name() + " chưa đạt: "
                                + String.join("; ", critique.issues()));
            }
        } catch (PostingDomainException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new PostingDomainException("CONTENT_REVIEW_UNAVAILABLE",
                    "Không thể rà soát nội dung cho nhóm " + group.name()
                            + ". Kiểm tra cấu hình AI rồi thử lại.");
        }
        return new PreparedGroup(group, text.strip());
    }

    /** Cặp nhóm đã xác nhận và nội dung cuối cùng trước khi tạo bài nháp. */
    private record PreparedGroup(FbGroup group, String text) {
    }

    /** Chặn một chiến dịch đưa cùng một mã nhóm vào nhiều lần. */
    private void rejectDuplicateGroups(List<CreateCampaignCommand.GroupInput> groups) {
        Set<String> seen = new HashSet<>();
        for (CreateCampaignCommand.GroupInput group : groups) {
            if (!seen.add(group.groupId())) {
                throw new PostingDomainException("DUPLICATE_GROUP",
                        "Nhom " + group.groupId() + " xuat hien nhieu lan trong campaign");
            }
        }
    }
}
