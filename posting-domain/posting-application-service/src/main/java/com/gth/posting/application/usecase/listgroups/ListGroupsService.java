package com.gth.posting.application.usecase.listgroups;

import com.gth.posting.application.dto.GroupSummaryDto;
import com.gth.posting.application.port.out.repository.GroupRepository;
import com.gth.posting.domain.entity.FbGroup;
import com.gth.posting.domain.service.GroupPostingPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Truy vấn nhóm qua cổng lưu trữ và trả về DTO không phụ thuộc hạ tầng. */
@Service
@Transactional(readOnly = true)
public class ListGroupsService implements ListGroupsUseCase {

    private final GroupRepository groupRepository;

    /** Khởi tạo ca sử dụng với cổng đọc nhóm Facebook. */
    public ListGroupsService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    /** Đọc nhóm và chuyển từng thực thể miền sang DTO phù hợp cho giao diện. */
    @Override
    public List<GroupSummaryDto> handle() {
        return groupRepository.findAll().stream().map(this::toDto).toList();
    }

    /** Chuyển một nhóm miền thành bản tóm tắt an toàn để trả ra ngoài. */
    private GroupSummaryDto toDto(FbGroup group) {
        return new GroupSummaryDto(group.id().value(), group.name(), group.memberCount(),
                group.region(), group.postingAllowed() && GroupPostingPolicy.isTechnologyName(group.name()),
                group.lastPostAt());
    }
}
