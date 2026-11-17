package com.gth.posting.dao.group.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "fb_group")
public class FbGroupEntity {

    @Id
    @Column(name = "group_id", nullable = false, length = 32)
    private String groupId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "member_count", nullable = false)
    private int memberCount;

    @Column(name = "region", length = 128)
    private String region;

    @Column(name = "posting_allowed", nullable = false)
    private boolean postingAllowed;

    @Column(name = "last_post_at")
    private Instant lastPostAt;

    protected FbGroupEntity() {
    }

    public FbGroupEntity(String groupId, String name, int memberCount, String region,
                         boolean postingAllowed, Instant lastPostAt) {
        this.groupId = groupId;
        this.name = name;
        this.memberCount = memberCount;
        this.region = region;
        this.postingAllowed = postingAllowed;
        this.lastPostAt = lastPostAt;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getName() {
        return name;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public String getRegion() {
        return region;
    }

    public boolean isPostingAllowed() {
        return postingAllowed;
    }

    public Instant getLastPostAt() {
        return lastPostAt;
    }
}
