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
import jakarta.persistence.Version;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts", indexes = {
        @Index(name = "idx_posts_status_created", columnList = "status, created_at DESC"),
        @Index(name = "idx_posts_category_created", columnList = "category, status, created_at DESC"),
        @Index(name = "idx_posts_giver", columnList = "giver_id, created_at DESC")
})
@EntityListeners(AuditingEntityListener.class)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "giver_id", nullable = false)
    private Long giverId;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "sub_category", length = 30)
    private SubCategory subCategory;

    @Column(nullable = false, length = 100)
    private String location;

    @Column(name = "image_keys", columnDefinition = "TEXT[]")
    private String[] imageKeys;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    @Column(name = "remaining_quantity", nullable = false)
    private Integer remainingQuantity;

    @Column(name = "available_time", nullable = false, length = 500)
    private String availableTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PostStatus status;

    @Version
    private Integer version;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected Post() {
    }

    public Post(Long giverId, String title, String description, Category category, SubCategory subCategory,
                String[] imageKeys, Integer totalQuantity, String location, String availableTime) {
        this.giverId = giverId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.subCategory = subCategory;
        this.imageKeys = imageKeys;
        this.totalQuantity = totalQuantity;
        this.remainingQuantity = totalQuantity;
        this.location = location;
        this.availableTime = availableTime;
        this.status = PostStatus.ACTIVE;
    }

    public Long getId() {
        return id;
    }

    public Long getGiverId() {
        return giverId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public SubCategory getSubCategory() {
        return subCategory;
    }

    public String getLocation() {
        return location;
    }

    public String[] getImageKeys() {
        return imageKeys;
    }

    public void setImageKeys(String[] imageKeys) {
        this.imageKeys = imageKeys;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public Integer getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(Integer remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }

    public String getAvailableTime() {
        return availableTime;
    }

    public void setAvailableTime(String availableTime) {
        this.availableTime = availableTime;
    }

    public PostStatus getStatus() {
        return status;
    }

    public void setStatus(PostStatus status) {
        this.status = status;
    }

    public Integer getVersion() {
        return version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
