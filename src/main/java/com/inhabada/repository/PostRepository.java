package com.inhabada.repository;

import com.inhabada.entity.Post;
import com.inhabada.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByStatusOrderByCreatedAtDesc(PostStatus status, Pageable pageable);

    Page<Post> findByStatusAndCategoryOrderByCreatedAtDesc(PostStatus status, String category, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.status = :status AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Post> searchByKeyword(@Param("status") PostStatus status, @Param("keyword") String keyword, Pageable pageable);

    List<Post> findByGiverIdOrderByCreatedAtDesc(Long giverId);
}
