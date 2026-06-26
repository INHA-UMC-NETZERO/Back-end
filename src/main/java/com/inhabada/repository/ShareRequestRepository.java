package com.inhabada.repository;

import com.inhabada.entity.RequestStatus;
import com.inhabada.entity.ShareRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShareRequestRepository extends JpaRepository<ShareRequest, Long> {

    List<ShareRequest> findByPostIdAndStatus(Long postId, RequestStatus status);

    List<ShareRequest> findByPostIdOrderByCreatedAtDesc(Long postId);

    List<ShareRequest> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);

    boolean existsByPostIdAndReceiverIdAndStatus(Long postId, Long receiverId, RequestStatus status);

    @Query("SELECT COALESCE(SUM(r.quantity), 0) FROM ShareRequest r WHERE r.postId = :postId AND r.status = com.inhabada.entity.RequestStatus.APPLIED")
    int sumAppliedQuantityByPostId(@Param("postId") Long postId);
}
