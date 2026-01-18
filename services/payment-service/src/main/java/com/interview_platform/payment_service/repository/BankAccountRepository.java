package com.interview_platform.payment_service.repository;

import com.interview_platform.payment_service.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    List<BankAccount> findByUserId(Long userId);

    Optional<BankAccount> findByUserIdAndIsPrimary(Long userId, Boolean isPrimary);

    @Query("SELECT b FROM BankAccount b WHERE b.userId = :userId AND b.isPrimary = true")
    Optional<BankAccount> findPrimaryByUserId(@Param("userId") String userId);

    boolean existsByUserIdAndIsPrimary(String userId, Boolean isPrimary);

    long countByUserId(String userId);

    List<BankAccount> findByUserIdAndIsVerified(String userId, Boolean isVerified);
}