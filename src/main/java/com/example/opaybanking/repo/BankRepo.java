package com.example.opaybanking.repo;

import com.example.opaybanking.model.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankRepo extends JpaRepository<Bank, Long> {
    Optional<Bank> findByBankCode(String bankCode);
    boolean existsByBankCode(String bankCode);
    List<Bank> findAllByOrderByBankNameAsc();
}