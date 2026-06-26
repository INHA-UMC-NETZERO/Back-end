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

    @Column(name = "requested_time", nullable = false, length = 500)
    private String requestedTime;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private RequestStatus status;

    @Column(name = "total_carbon_saving_gram")
    private Long totalCarbonSavingGram;

    @Column(name = "giver_carbon_saving_gram")
    private Long giverCarbonSavingGram;

    @Column(name = "receiver_carbon_saving_gram")
    private Long receiverCarbonSavingGram;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected ShareRequest() {
    }

    public ShareRequest(Long postId, Long receiverId, String requestedTime, Integer quantity) {
        this.postId = postId;
        this.receiverId = receiverId;
        this.requestedTime = requestedTime;
        this.quantity = quantity;
        this.status = RequestStatus.APPLIED;
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

    public String getRequestedTime() {
        return requestedTime;
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

    public Long getTotalCarbonSavingGram() {
        return totalCarbonSavingGram;
    }

    public Long getGiverCarbonSavingGram() {
        return giverCarbonSavingGram;
    }

    public Long getReceiverCarbonSavingGram() {
        return receiverCarbonSavingGram;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void complete(long totalCarbonSavingGram,
                         long giverCarbonSavingGram,
                         long receiverCarbonSavingGram,
                         LocalDateTime completedAt) {
        this.status = RequestStatus.COMPLETED;
        this.totalCarbonSavingGram = totalCarbonSavingGram;
        this.giverCarbonSavingGram = giverCarbonSavingGram;
        this.receiverCarbonSavingGram = receiverCarbonSavingGram;
        this.completedAt = completedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
