package com.gth.posting.application.port.out.repository;

import com.gth.posting.domain.entity.JobEvent;
import com.gth.posting.domain.valueobject.PostJobId;

import java.util.List;

public interface JobEventRepository {

    JobEvent save(JobEvent event);

    List<JobEvent> findByPostJobId(PostJobId postJobId);
}
