package com.gth.posting.dao.jobevent.repository;

import com.gth.posting.dao.jobevent.entity.JobEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobEventJpaRepository extends JpaRepository<JobEventEntity, String> {

    List<JobEventEntity> findByPostJobIdOrderByCreatedAtAsc(String postJobId);
}
