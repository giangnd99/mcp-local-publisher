package com.gth.posting.domain.entity;

import com.gth.posting.domain.event.DomainEvent;
import com.gth.posting.domain.event.PostJobStateChanged;
import com.gth.posting.domain.exception.InvalidStateTransitionException;
import com.gth.posting.domain.valueobject.CampaignId;
import com.gth.posting.domain.valueobject.GroupId;
import com.gth.posting.domain.valueobject.PostJobId;
import com.gth.posting.domain.valueobject.PostText;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregate root: mot lan dang bai vao mot nhom cu the.
 *
 * Moi thay doi trang thai deu di qua {@link #transition(PostStatus, Instant, String)}.
 * Khong co setter cong khai cho status: khong the "nhay coc" trang thai tu ben ngoai.
 */
public final class PostJob {

    private final PostJobId id;
    private final CampaignId campaignId;
    private final GroupId groupId;
    private final String groupName;

    private PostText text;
    private PostStatus status;
    private int attempt;

    private Instant scheduledAt;
    private Instant lastAttemptAt;
    private Instant postedAt;
    private String fbPostUrl;
    private String errorMessage;

    private final Instant createdAt;
    private Instant updatedAt;

    private final List<DomainEvent> pendingEvents = new ArrayList<>();

    private PostJob(PostJobId id,
                    CampaignId campaignId,
                    GroupId groupId,
                    String groupName,
                    PostText text,
                    PostStatus status,
                    int attempt,
                    Instant scheduledAt,
                    Instant lastAttemptAt,
                    Instant postedAt,
                    String fbPostUrl,
                    String errorMessage,
                    Instant createdAt,
                    Instant updatedAt) {
        this.id = id;
        this.campaignId = campaignId;
        this.groupId = groupId;
        this.groupName = groupName;
        this.text = text;
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

    public static PostJob create(CampaignId campaignId, GroupId groupId, String groupName, PostText text, Instant now) {
        if (campaignId == null || groupId == null) {
            throw new IllegalArgumentException("PostJob can campaignId va groupId");
        }
        return new PostJob(PostJobId.newId(), campaignId, groupId, groupName, text, PostStatus.DRAFT,
                0, null, null, null, null, null, now, now);
    }

    /** Dung cho tang data-access khi doc lai tu DB. Khong sinh event. */
    public static PostJob restore(PostJobId id,
                                  CampaignId campaignId,
                                  GroupId groupId,
                                  String groupName,
                                  PostText text,
                                  PostStatus status,
                                  int attempt,
                                  Instant scheduledAt,
                                  Instant lastAttemptAt,
                                  Instant postedAt,
                                  String fbPostUrl,
                                  String errorMessage,
                                  Instant createdAt,
                                  Instant updatedAt) {
        return new PostJob(id, campaignId, groupId, groupName, text, status, attempt, scheduledAt,
                lastAttemptAt, postedAt, fbPostUrl, errorMessage, createdAt, updatedAt);
    }

    // ------------------------------------------------------------------ nghiep vu

    public void validate(Instant now) {
        transition(PostStatus.VALIDATED, now, null);
    }

    public void queue(Instant now, Instant scheduledAt) {
        transition(PostStatus.QUEUED, now, null);
        this.scheduledAt = scheduledAt;
    }

    /** Bat dau chuoi tac dong trinh duyet. attempt tang o day, khong tang luc submit. */
    public void startComposing(Instant now) {
        transition(PostStatus.COMPOSING, now, null);
        this.attempt = this.attempt + 1;
        this.lastAttemptAt = now;
    }

    public void startUploading(Instant now) {
        transition(PostStatus.UPLOADING, now, null);
    }

    public void startSubmitting(Instant now) {
        transition(PostStatus.SUBMITTING, now, null);
    }

    public void markLive(String fbPostUrl, Instant now) {
        transition(PostStatus.LIVE, now, null);
        this.fbPostUrl = fbPostUrl;
        this.postedAt = now;
        this.errorMessage = null;
    }

    public void markPendingApproval(Instant now) {
        transition(PostStatus.PENDING_APPROVAL, now, null);
        this.errorMessage = null;
    }

    public void reject(String reason, Instant now) {
        transition(PostStatus.REJECTED, now, reason);
        this.errorMessage = reason;
    }

    public void fail(String reason, Instant now) {
        transition(PostStatus.FAILED, now, reason);
        this.errorMessage = reason;
    }

    /** Hủy bài chưa gửi hoặc có thể thử lại khi nhóm không còn đáp ứng chính sách. */
    public void cancel(String reason, Instant now) {
        transition(PostStatus.CANCELLED, now, reason);
        this.errorMessage = reason;
    }

    /** Cho phep retry chi tu FAILED, va phai qua queue lai de tranh dot ngot goi API. */
    public void retry(Instant now, Instant scheduledAt) {
        transition(PostStatus.QUEUED, now, "retry");
        this.scheduledAt = scheduledAt;
    }

    public void updateText(PostText newText, Instant now) {
        if (status != PostStatus.DRAFT && status != PostStatus.VALIDATED) {
            throw new InvalidStateTransitionException(status, PostStatus.DRAFT);
        }
        this.text = newText;
        this.status = PostStatus.DRAFT;
        this.updatedAt = now;
    }

    private void transition(PostStatus target, Instant now, String reason) {
        if (!status.canTransitionTo(target)) {
            throw new InvalidStateTransitionException(status, target);
        }
        PostStatus from = status;
        this.status = target;
        this.updatedAt = now;
        pendingEvents.add(new PostJobStateChanged(id, from, target, reason, now));
    }

    // ------------------------------------------------------------------ truy van

    public List<DomainEvent> pullPendingEvents() {
        List<DomainEvent> copy = List.copyOf(pendingEvents);
        pendingEvents.clear();
        return copy;
    }

    public boolean isReadyToDispatch() {
        return status == PostStatus.QUEUED;
    }

    public boolean isDue(Instant now) {
        return scheduledAt == null || !scheduledAt.isAfter(now);
    }

    public PostJobId id() {
        return id;
    }

    public CampaignId campaignId() {
        return campaignId;
    }

    public GroupId groupId() {
        return groupId;
    }

    public String groupName() {
        return groupName;
    }

    public PostText text() {
        return text;
    }

    public PostStatus status() {
        return status;
    }

    public int attempt() {
        return attempt;
    }

    public Instant scheduledAt() {
        return scheduledAt;
    }

    public Instant lastAttemptAt() {
        return lastAttemptAt;
    }

    public Instant postedAt() {
        return postedAt;
    }

    public String fbPostUrl() {
        return fbPostUrl;
    }

    public String errorMessage() {
        return errorMessage;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public List<DomainEvent> peekEvents() {
        return Collections.unmodifiableList(pendingEvents);
    }
}
