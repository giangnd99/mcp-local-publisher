package com.gth.posting.dao.postjob.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "post_job")
public class PostJobEntity {

    @Id
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @Column(name = "campaign_id", nullable = false, length = 64)
    private String campaignId;

    @Column(name = "group_id", nullable = false, length = 32)
    private String groupId;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "attempt", nullable = false)
    private int attempt;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Column(name = "last_attempt_at")
    private Instant lastAttemptAt;

    @Column(name = "posted_at")
    private Instant postedAt;

    @Column(name = "fb_post_url", columnDefinition = "text")
    private String fbPostUrl;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PostJobEntity() {
    }

    public PostJobEntity(String id, String campaignId, String groupId, String groupName, String content,
                         String status, int attempt, Instant scheduledAt, Instant lastAttemptAt, Instant postedAt,
                         String fbPostUrl, String errorMessage, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.campaignId = campaignId;
        this.groupId = groupId;
        this.groupName = groupName;
        this.content = content;
        this.status = status;
        this.attempt = attempt;
        this.scheduledAt = scheduledAt;
        this.lastAttemptAt = lastAttemptAt;
        this.postedAt = postedAt;
        this.fbPostUrl = fbPostUrl;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public String getCampaignId() {
        return campaignId;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getContent() {
        return content;
    }

    public String getStatus() {
        return status;
    }

    public int getAttempt() {
        return attempt;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public Instant getLastAttemptAt() {
        return lastAttemptAt;
    }

    public Instant getPostedAt() {
        return postedAt;
    }

    public String getFbPostUrl() {
        return fbPostUrl;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
