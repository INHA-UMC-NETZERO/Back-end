package com.inhabada.repository;

import com.inhabada.entity.KeywordSubscription;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KeywordSubscriptionRepository extends JpaRepository<KeywordSubscription, Long> {

    List<KeywordSubscription> findByUserId(Long userId);

    List<KeywordSubscription> findByKeyword(String keyword);

    boolean existsByUserIdAndKeyword(Long userId, String keyword);

    @Transactional
    void deleteByUserIdAndKeyword(Long userId, String keyword);

    int countByUserId(Long userId);

    @Modifying(flushAutomatically = true, clearAutomatically = false)
    @Query(value = """
            UPDATE keyword_subscriptions
            SET embedding = CAST(:embedding AS vector)
            WHERE id = :subscriptionId
            """, nativeQuery = true)
    void updateEmbedding(@Param("subscriptionId") Long subscriptionId, @Param("embedding") String embedding);

    @Query(value = """
            SELECT ks.id, ks.user_id, ks.keyword, ks.created_at
            FROM keyword_subscriptions ks
            JOIN posts p ON p.id = :postId
            WHERE p.embedding IS NOT NULL
              AND ks.embedding IS NOT NULL
              AND ks.user_id <> :excludedUserId
              AND 1 - (ks.embedding <=> p.embedding) >= :threshold
            ORDER BY ks.embedding <=> p.embedding
            """, nativeQuery = true)
    List<KeywordSubscription> findSimilarByPostEmbedding(@Param("postId") Long postId,
                                                         @Param("excludedUserId") Long excludedUserId,
                                                         @Param("threshold") double threshold);
}
