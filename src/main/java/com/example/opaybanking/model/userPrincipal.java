package com.example.opaybanking.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class userPrincipal implements UserDetails {

    private final User user;

    public userPrincipal(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return user.getAccountStatus() != AccountStatus.SUSPENDED &&
                user.getAccountStatus() != AccountStatus.DELETED;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getAccountStatus() != AccountStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return user.getAccountStatus() == AccountStatus.VERIFIED;
    }

    @Override
    public boolean isEnabled() {
        // FIXED: Added missing "user.getAccountStatus() == " before AccountStatus.VERIFIED
        return user.getAccountStatus() == AccountStatus.PENDING ||
                user.getAccountStatus() == AccountStatus.VERIFIED;
    }
}