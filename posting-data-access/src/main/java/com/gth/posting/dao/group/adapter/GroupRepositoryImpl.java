package com.gth.posting.dao.group.adapter;

import com.gth.posting.application.port.out.repository.GroupRepository;
import com.gth.posting.dao.group.mapper.GroupDataAccessMapper;
import com.gth.posting.dao.group.repository.FbGroupJpaRepository;
import com.gth.posting.domain.entity.FbGroup;
import com.gth.posting.domain.valueobject.GroupId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class GroupRepositoryImpl implements GroupRepository {

    private final FbGroupJpaRepository jpaRepository;

    public GroupRepositoryImpl(FbGroupJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public FbGroup save(FbGroup group) {
        return GroupDataAccessMapper.toDomain(
                jpaRepository.save(GroupDataAccessMapper.toEntity(group)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FbGroup> findById(GroupId id) {
        return jpaRepository.findById(id.value()).map(GroupDataAccessMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FbGroup> findAll() {
        return jpaRepository.findAll().stream().map(GroupDataAccessMapper::toDomain).toList();
    }
}
