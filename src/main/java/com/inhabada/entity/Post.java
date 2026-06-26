package com.inhabada.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(nullable = false, length = 30)
    private String category;

    @Column(name = "image_keys", columnDefinition = "TEXT[]")
    private String[] imageKeys;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    @Column(name = "remaining_quantity", nullable = false)
    private Integer remainingQuantity;

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

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Slot> slots = new ArrayList<>();

    protected Post() {
    }

    public Post(Long giverId, String title, String description, String category,
                String[] imageKeys, Integer totalQuantity) {
        this.giverId = giverId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.imageKeys = imageKeys;
        this.totalQuantity = totalQuantity;
        this.remainingQuantity = totalQuantity;
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

    public String getCategory() {
        return category;
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

    public List<Slot> getSlots() {
        return slots;
    }

    public void addSlot(Slot slot) {
        slots.add(slot);
        slot.setPost(this);
    }

    public void removeSlot(Slot slot) {
        slots.remove(slot);
        slot.setPost(null);
    }
}
