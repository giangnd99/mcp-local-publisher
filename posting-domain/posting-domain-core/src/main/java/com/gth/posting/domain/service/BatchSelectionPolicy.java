package com.gth.posting.domain.service;

import com.gth.posting.domain.entity.PostJob;
import com.gth.posting.domain.entity.PostStatus;

import java.util.List;
import java.util.Objects;

/**
 * Chon bai nao dua vao lo tiep theo.
 *
 * Quy tac nghiep vu: chi lay bai da VALIDATED, uu tien bai den han truoc,
 * moi nhom chi mot bai trong cung mot lo.
 */
public final class BatchSelectionPolicy {

    public List<PostJob> select(List<PostJob> jobs, int batchSize, java.time.Instant now) {
        if (jobs == null || jobs.isEmpty() || batchSize <= 0) {
            return List.of();
        }
        java.util.Set<String> usedGroups = new java.util.HashSet<>();
        return jobs.stream()
                .filter(Objects::nonNull)
                .filter(job -> job.status() == PostStatus.VALIDATED)
                .sorted(java.util.Comparator.comparing(PostJob::createdAt))
                .filter(job -> usedGroups.add(job.groupId().value()))
                .limit(batchSize)
                .toList();
    }
}
