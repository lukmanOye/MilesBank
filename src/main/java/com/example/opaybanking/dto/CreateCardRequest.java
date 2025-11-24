    package com.example.opaybanking.dto;

    import com.example.opaybanking.enums.CardType;

    public record CreateCardRequest(
            CardType cardType,
            String pin
    ) {}