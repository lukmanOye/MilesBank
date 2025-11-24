package com.example.opaybanking.repo;

import com.example.opaybanking.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepo extends JpaRepository<Transaction, Long> {

    List<Transaction> findByWalletWalletIdOrderByCreatedAtDesc(Long walletId);

    // For admin: get all
    List<Transaction> findAllByOrderByCreatedAtDesc();

    // Fixed: Date range + sort by amount descending
    @Query("SELECT t FROM Transaction t WHERE t.user.userId = :userId " +
            "AND (CAST(:startDate AS timestamp) IS NULL OR t.createdAt >= :startDate) " +
            "AND (CAST(:endDate AS timestamp) IS NULL OR t.createdAt <= :endDate) " +
            "ORDER BY t.amount DESC")
    List<Transaction> findByUserAndDateRangeOrderByAmountDesc(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM Transaction t WHERE t.user.userId = :userId ORDER BY t.createdAt DESC")
    List<Transaction> findByUserUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT t FROM Transaction t WHERE t.user.userId = :userId ORDER BY t.amount DESC")
    List<Transaction> findByUserUserIdOrderByAmountDesc(@Param("userId") Long userId);

    // Fixed: Date range + default sort by date desc
    @Query("SELECT t FROM Transaction t WHERE t.user.userId = :userId " +
            "AND (CAST(:startDate AS timestamp) IS NULL OR t.createdAt >= :startDate) " +
            "AND (CAST(:endDate AS timestamp) IS NULL OR t.createdAt <= :endDate) " +
            "ORDER BY t.createdAt DESC")
    List<Transaction> findByUserAndDateRangeOrderByDateDesc(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Alternative method without NULL checks for better performance
    @Query("SELECT t FROM Transaction t WHERE t.user.userId = :userId " +
            "AND t.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY t.createdAt DESC")
    List<Transaction> findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    List<Transaction> findByUserUserIdAndCreatedAtBetween(long longValue, LocalDateTime start, LocalDateTime end);

    List<Transaction> findByUserUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(long longValue, LocalDateTime start, LocalDateTime end);
}