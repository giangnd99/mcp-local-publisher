package com.gth.posting.dao.group.mapper;

import com.gth.posting.dao.group.entity.FbGroupEntity;
import com.gth.posting.domain.entity.FbGroup;
import com.gth.posting.domain.valueobject.GroupId;

public final class GroupDataAccessMapper {

    private GroupDataAccessMapper() {
    }

    public static FbGroupEntity toEntity(FbGroup group) {
        return new FbGroupEntity(
                group.id().value(),
                group.name(),
                group.memberCount(),
                group.region(),
                group.postingAllowed(),
                group.lastPostAt()
        );
    }

    public static FbGroup toDomain(FbGroupEntity entity) {
        return new FbGroup(
                GroupId.of(entity.getGroupId()),
                entity.getName(),
                entity.getMemberCount(),
                entity.getRegion(),
                entity.isPostingAllowed(),
                entity.getLastPostAt()
        );
    }
}
