package com.interview_platform.payment_service.repository;

import com.interview_platform.payment_service.entity.WalletTransaction;
import com.interview_platform.payment_service.utils.TransactionCategory;
import com.interview_platform.payment_service.utils.TransactionStatus;
import com.interview_platform.payment_service.utils.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    Page<WalletTransaction> findByUserId(String userId, Pageable pageable);

    Page<WalletTransaction> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    List<WalletTransaction> findByUserIdAndCreatedAtBetween(
            String userId, LocalDateTime start, LocalDateTime end);

    Optional<WalletTransaction> findByReferenceId(String referenceId);

    boolean existsByReferenceId(String referenceId);

    List<WalletTransaction> findByWalletId(Long walletId);

    Page<WalletTransaction> findByStatus(TransactionStatus status, Pageable pageable);

    @Query("SELECT t FROM WalletTransaction t WHERE t.userId = :userId " +
            "AND t.transactionCategory = :category ORDER BY t.createdAt DESC")
    Page<WalletTransaction> findByUserIdAndCategory(
            @Param("userId") String userId,
            @Param("category") TransactionCategory category,
            Pageable pageable);

    @Query("SELECT SUM(t.amount) FROM WalletTransaction t WHERE t.userId = :userId " +
            "AND t.transactionType = :type AND t.status = 'SUCCESS' " +
            "AND DATE(t.createdAt) = CURRENT_DATE")
    BigDecimal getTodayTotalByUserIdAndType(
            @Param("userId") String userId,
            @Param("type") TransactionType type);

    @Query("SELECT COUNT(t) FROM WalletTransaction t WHERE t.userId = :userId " +
            "AND t.status = 'SUCCESS' AND DATE(t.createdAt) = CURRENT_DATE")
    long countTodayTransactionsByUserId(@Param("userId") String userId);
}

