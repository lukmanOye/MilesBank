// src/main/java/com/example/opaybanking/repo/billPaymentRepo.java

package com.example.opaybanking.repo;

import com.example.opaybanking.enums.BillType;
import com.example.opaybanking.model.BillPayment;
import com.example.opaybanking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface billPaymentRepo extends JpaRepository<BillPayment, Integer> {

    List<BillPayment> findByUserUserIdOrderByCreatedAtDesc(Integer userId);

    Optional<BillPayment> findByReference(String reference);

    List<BillPayment> findByBillTypeAndUserUserId(BillType billType, Integer userId);

    List<BillPayment> findByUserUserIdAndCreatedAtBetween(
            Integer userId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<BillPayment> findByUserUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(Integer userId, LocalDateTime start, LocalDateTime end);
}