package com.inhabada.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "share_requests", indexes = {
        @Index(name = "idx_requests_post_status", columnList = "post_id, status"),
        @Index(name = "idx_requests_receiver", columnList = "receiver_id, created_at DESC")
})
@EntityListeners(AuditingEntityListener.class)
public class ShareRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Column(name = "slot_id", nullable = false)
    private Long slotId;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private RequestStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected ShareRequest() {
    }

    public ShareRequest(Long postId, Long receiverId, Long slotId, Integer quantity) {
        this.postId = postId;
        this.receiverId = receiverId;
        this.slotId = slotId;
        this.quantity = quantity;
        this.status = RequestStatus.PENDING;
    }

    public Long getId() {
        return id;
    }

    public Long getPostId() {
        return postId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public Long getSlotId() {
        return slotId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
