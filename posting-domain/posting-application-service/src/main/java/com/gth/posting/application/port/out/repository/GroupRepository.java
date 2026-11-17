package com.gth.posting.application.port.out.repository;

import com.gth.posting.domain.entity.FbGroup;
import com.gth.posting.domain.valueobject.GroupId;

import java.util.List;
import java.util.Optional;

public interface GroupRepository {

    FbGroup save(FbGroup group);

    Optional<FbGroup> findById(GroupId id);

    List<FbGroup> findAll();
}
