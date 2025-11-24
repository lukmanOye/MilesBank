package com.example.opaybanking.controller;

import com.example.opaybanking.dto.*;
import com.example.opaybanking.enums.Currency;
import com.example.opaybanking.model.Card;
import com.example.opaybanking.service.CardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/card")
@CrossOrigin(origins = "*")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    private String token(String auth) {
        return auth.startsWith("Bearer ") ? auth.substring(7) : auth;
    }

    @PostMapping("/create/ngn")
    public ResponseEntity<CreateCardResponse> createNgnCard(
            @RequestBody CreateCardRequest req,
            @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(cardService.createVirtualCard(req, token(auth), Currency.NGN));
    }

    @PostMapping("/create/usd")
    public ResponseEntity<CreateCardResponse> createUsdCard(
            @RequestBody CreateCardRequest req,
            @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(cardService.createVirtualCard(req, token(auth), Currency.USD));
    }

    @PostMapping("/topup/miles/ngn")
    public ResponseEntity<TopUpResponse> topUpWithNgnCard(
            @RequestBody TopUpFromMilesCardRequest req,
            @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(cardService.topUpWithNgnCard(req, token(auth)));
    }


    @GetMapping("/ngn")
    public ResponseEntity<Card> getNgnCard(@RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(cardService.getNgnCard(token(auth)));
    }

    @GetMapping("/usd")
    public ResponseEntity<Card> getUsdCard(@RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(cardService.getUsdCard(token(auth)));
    }
}