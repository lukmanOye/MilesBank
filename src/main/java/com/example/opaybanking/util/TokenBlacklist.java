package com.example.opaybanking.util;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBlacklist {
    private final Set<String> blacklist = ConcurrentHashMap.newKeySet();

    public void blacklistToken(String jti) {
        blacklist.add(jti);
    }

    public boolean isBlacklisted(String jti) {
        return blacklist.contains(jti);
    }
}