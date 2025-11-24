package com.example.opaybanking.service;

import com.example.opaybanking.model.User;
import com.example.opaybanking.model.userPrincipal;
import com.example.opaybanking.repo.userRepo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailservice implements UserDetailsService {

    private final userRepo userRepo;

    public MyUserDetailservice(com.example.opaybanking.repo.userRepo userRepo) {
        this.userRepo = userRepo;
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepo.findByEmailIgnoreCase(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        return new userPrincipal(user);


    }
}

