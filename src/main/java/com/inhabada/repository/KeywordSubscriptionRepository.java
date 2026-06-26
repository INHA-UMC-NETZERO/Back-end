package com.inhabada.repository;

import com.inhabada.entity.KeywordSubscription;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KeywordSubscriptionRepository extends JpaRepository<KeywordSubscription, Long> {

    List<KeywordSubscription> findByUserId(Long userId);

    List<KeywordSubscription> findByKeyword(String keyword);

    boolean existsByUserIdAndKeyword(Long userId, String keyword);

    @Transactional
    void deleteByUserIdAndKeyword(Long userId, String keyword);

    int countByUserId(Long userId);
}
