package com.gth.posting.dao.group.repository;

import com.gth.posting.dao.group.entity.FbGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FbGroupJpaRepository extends JpaRepository<FbGroupEntity, String> {
}
