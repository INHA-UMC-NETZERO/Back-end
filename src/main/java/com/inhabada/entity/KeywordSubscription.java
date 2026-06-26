package com.inhabada.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "keyword_subscriptions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "keyword"}),
        indexes = {
                @Index(name = "idx_keyword_subs_user", columnList = "user_id"),
                @Index(name = "idx_keyword_subs_keyword", columnList = "keyword")
        })
@EntityListeners(AuditingEntityListener.class)
public class KeywordSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 20)
    private String keyword;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected KeywordSubscription() {
    }

    public KeywordSubscription(Long userId, String keyword) {
        this.userId = userId;
        this.keyword = keyword;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getKeyword() {
        return keyword;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
