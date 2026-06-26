package com.inhabada.service;

import com.inhabada.dto.KeywordResponse;
import com.inhabada.entity.KeywordSubscription;
import com.inhabada.exception.ConflictException;
import com.inhabada.exception.ValidationException;
import com.inhabada.repository.KeywordSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class KeywordService {

    private static final int MAX_KEYWORDS = 20;

    private final KeywordSubscriptionRepository keywordSubscriptionRepository;

    public KeywordService(KeywordSubscriptionRepository keywordSubscriptionRepository) {
        this.keywordSubscriptionRepository = keywordSubscriptionRepository;
    }

    @Transactional(readOnly = true)
    public List<KeywordResponse> getKeywords(Long userId) {
        return keywordSubscriptionRepository.findByUserId(userId).stream()
                .map(KeywordResponse::from)
                .toList();
    }

    @Transactional
    public KeywordResponse addKeyword(Long userId, String keyword) {
        String normalized = keyword.trim();
        if (normalized.isEmpty() || normalized.length() > 20) {
            throw new ValidationException("키워드는 1자 이상 20자 이하여야 합니다");
        }

        if (keywordSubscriptionRepository.existsByUserIdAndKeyword(userId, normalized)) {
            throw new ConflictException("이미 등록된 키워드입니다");
        }

        if (keywordSubscriptionRepository.countByUserId(userId) >= MAX_KEYWORDS) {
            throw new ValidationException("관심 키워드는 최대 " + MAX_KEYWORDS + "개까지 등록할 수 있습니다");
        }

        KeywordSubscription saved =
                keywordSubscriptionRepository.save(new KeywordSubscription(userId, normalized));
        return KeywordResponse.from(saved);
    }

    @Transactional
    public void removeKeyword(Long userId, String keyword) {
        keywordSubscriptionRepository.deleteByUserIdAndKeyword(userId, keyword);
    }
}
