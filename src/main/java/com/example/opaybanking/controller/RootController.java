package com.example.opaybanking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> home() {
        return ResponseEntity.ok(Map.of(
                "message", "Miles Bank API is LIVE",
                "version", "1.0.0",
                "docs", "/swagger-ui.html",
                "status", "ACTIVE"
        ));
    }
}