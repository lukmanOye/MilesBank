package com.example.opaybanking.repo;

import com.example.opaybanking.model.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeRateRepo extends JpaRepository<ExchangeRate, Long> {}