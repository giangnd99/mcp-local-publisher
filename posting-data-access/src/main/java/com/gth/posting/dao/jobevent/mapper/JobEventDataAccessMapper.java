package com.gth.posting.dao.jobevent.mapper;

import com.gth.posting.dao.jobevent.entity.JobEventEntity;
import com.gth.posting.domain.entity.JobEvent;
import com.gth.posting.domain.entity.JobEventType;
import com.gth.posting.domain.valueobject.PostJobId;

public final class JobEventDataAccessMapper {

    private JobEventDataAccessMapper() {
    }

    public static JobEventEntity toEntity(JobEvent event) {
        return new JobEventEntity(event.id(), event.postJobId().value(), event.type().name(),
                event.payload(), event.createdAt());
    }

    public static JobEvent toDomain(JobEventEntity entity) {
        return new JobEvent(entity.getId(), PostJobId.of(entity.getPostJobId()),
                JobEventType.valueOf(entity.getEventType()), entity.getPayload(), entity.getCreatedAt());
    }
}
