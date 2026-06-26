package com.inhabada.repository;

import com.inhabada.entity.Post;
import com.inhabada.entity.PostStatus;
import com.inhabada.entity.Category;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Post p WHERE p.id = :id")
    Optional<Post> findByIdForUpdate(@Param("id") Long id);

    Page<Post> findByStatus(PostStatus status, Pageable pageable);

    Page<Post> findByStatusAndCategory(PostStatus status, Category category, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.status = :status AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Post> searchByKeyword(@Param("status") PostStatus status, @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.status = :status AND p.category = :category AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Post> searchByCategoryAndKeyword(@Param("status") PostStatus status,
                                          @Param("category") Category category,
                                          @Param("keyword") String keyword,
                                          Pageable pageable);

    @Modifying(flushAutomatically = true, clearAutomatically = false)
    @Query(value = """
            UPDATE posts
            SET embedding = CAST(:embedding AS vector)
            WHERE id = :postId
            """, nativeQuery = true)
    void updateEmbedding(@Param("postId") Long postId, @Param("embedding") String embedding);

    @Query(value = """
            SELECT
                p.id AS id,
                p.title AS title,
                CASE
                    WHEN p.image_keys IS NOT NULL AND array_length(p.image_keys, 1) > 0
                    THEN p.image_keys[1]
                    ELSE NULL
                END AS "thumbnailKey",
                p.remaining_quantity AS "remainingQuantity",
                1 - (p.embedding <=> CAST(:embedding AS vector)) AS similarity
            FROM posts p
            WHERE p.status = 'ACTIVE'
              AND p.embedding IS NOT NULL
              AND 1 - (p.embedding <=> CAST(:embedding AS vector)) >= :threshold
            ORDER BY p.embedding <=> CAST(:embedding AS vector)
            LIMIT :maxResults
            """, nativeQuery = true)
    List<PostSimilarityProjection> searchSimilarByEmbedding(@Param("embedding") String embedding,
                                                            @Param("threshold") double threshold,
                                                            @Param("maxResults") int maxResults);

    List<Post> findByGiverIdOrderByCreatedAtDesc(Long giverId);
}
