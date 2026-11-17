package com.gth.posting.dao.jobevent.adapter;

import com.gth.posting.application.port.out.repository.JobEventRepository;
import com.gth.posting.dao.jobevent.mapper.JobEventDataAccessMapper;
import com.gth.posting.dao.jobevent.repository.JobEventJpaRepository;
import com.gth.posting.domain.entity.JobEvent;
import com.gth.posting.domain.valueobject.PostJobId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional
public class JobEventRepositoryImpl implements JobEventRepository {

    private final JobEventJpaRepository jpaRepository;

    public JobEventRepositoryImpl(JobEventJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public JobEvent save(JobEvent event) {
        return JobEventDataAccessMapper.toDomain(
                jpaRepository.save(JobEventDataAccessMapper.toEntity(event)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobEvent> findByPostJobId(PostJobId postJobId) {
        return jpaRepository.findByPostJobIdOrderByCreatedAtAsc(postJobId.value())
                .stream().map(JobEventDataAccessMapper::toDomain).toList();
    }
}
