package com.inhabada.repository;

import com.inhabada.entity.RequestStatus;
import com.inhabada.entity.ShareRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ShareRequestRepository extends JpaRepository<ShareRequest, Long> {

    List<ShareRequest> findByPostIdAndStatus(Long postId, RequestStatus status);

    List<ShareRequest> findByPostIdOrderByCreatedAtDesc(Long postId);

    List<ShareRequest> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);

    boolean existsByPostIdAndReceiverIdAndStatus(Long postId, Long receiverId, RequestStatus status);

    @Query("SELECT COALESCE(SUM(r.quantity), 0) FROM ShareRequest r WHERE r.postId = :postId AND r.status = com.inhabada.entity.RequestStatus.APPLIED")
    int sumAppliedQuantityByPostId(@Param("postId") Long postId);

    @Query("""
            SELECT COUNT(r)
            FROM ShareRequest r
            JOIN Post p ON p.id = r.postId
            WHERE p.giverId = :userId
              AND r.status = com.inhabada.entity.RequestStatus.COMPLETED
            """)
    long countCompletedGivenByGiverId(@Param("userId") Long userId);

    long countByReceiverIdAndStatus(Long receiverId, RequestStatus status);

    default long countCompletedReceivedByReceiverId(Long receiverId) {
        return countByReceiverIdAndStatus(receiverId, RequestStatus.COMPLETED);
    }

    @Query("""
            SELECT COUNT(r)
            FROM ShareRequest r
            JOIN Post p ON p.id = r.postId
            WHERE p.giverId = :userId
               OR r.receiverId = :userId
            """)
    long countAllInvolvingUser(@Param("userId") Long userId);

    @Query("""
            SELECT COUNT(r)
            FROM ShareRequest r
            JOIN Post p ON p.id = r.postId
            WHERE (p.giverId = :userId OR r.receiverId = :userId)
              AND r.status = com.inhabada.entity.RequestStatus.COMPLETED
            """)
    long countCompletedInvolvingUser(@Param("userId") Long userId);

    @Query(value = """
            SELECT
                to_char(r.completed_at, 'YYYY-MM') AS month,
                COALESCE(SUM(
                    CASE WHEN p.giver_id = :userId THEN r.giver_carbon_saving_gram ELSE 0 END
                    + CASE WHEN r.receiver_id = :userId THEN r.receiver_carbon_saving_gram ELSE 0 END
                ), 0) AS "carbonSavingGram"
            FROM share_requests r
            JOIN posts p ON p.id = r.post_id
            WHERE r.status = 'COMPLETED'
              AND r.completed_at >= :from
              AND (p.giver_id = :userId OR r.receiver_id = :userId)
            GROUP BY to_char(r.completed_at, 'YYYY-MM')
            ORDER BY month
            """, nativeQuery = true)
    List<MonthlyCarbonProjection> sumMonthlyCarbonSavingByUser(@Param("userId") Long userId,
                                                               @Param("from") LocalDateTime from);

    @Query(value = """
            SELECT
                u.id AS "userId",
                u.nickname AS nickname,
                COALESCE(SUM(x.carbon_saving_gram), 0) AS "carbonSavingGram"
            FROM (
                SELECT
                    p.giver_id AS user_id,
                    COALESCE(r.giver_carbon_saving_gram, 0) AS carbon_saving_gram
                FROM share_requests r
                JOIN posts p ON p.id = r.post_id
                WHERE r.status = 'COMPLETED'
                  AND r.completed_at >= :from
                  AND r.completed_at < :to
                UNION ALL
                SELECT
                    r.receiver_id AS user_id,
                    COALESCE(r.receiver_carbon_saving_gram, 0) AS carbon_saving_gram
                FROM share_requests r
                WHERE r.status = 'COMPLETED'
                  AND r.completed_at >= :from
                  AND r.completed_at < :to
            ) x
            JOIN users u ON u.id = x.user_id
            GROUP BY u.id, u.nickname
            ORDER BY COALESCE(SUM(x.carbon_saving_gram), 0) DESC, u.id ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<CarbonRankingProjection> findMonthlyCarbonRanking(@Param("from") LocalDateTime from,
                                                           @Param("to") LocalDateTime to,
                                                           @Param("limit") int limit);
}
