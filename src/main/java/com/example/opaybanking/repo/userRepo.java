package com.example.opaybanking.repo;

import com.example.opaybanking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface userRepo extends JpaRepository<User, Integer> {


    User findByEmailIgnoreCase(String email);
}
