package com.example.opaybanking.controller;

import com.example.opaybanking.dto.TransactionHistoryResponse;
import com.example.opaybanking.service.PdfService;
import com.example.opaybanking.service.TransactionHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class TransactionHistoryController {

    private final TransactionHistoryService historyService;
    private final PdfService pdfService;

    public TransactionHistoryController(TransactionHistoryService historyService,
                                        PdfService pdfService) {
        this.historyService = historyService;
        this.pdfService = pdfService;
    }

    private String token(String auth) {
        return auth.substring(7);
    }

    @GetMapping("/all")
    public ResponseEntity<List<TransactionHistoryResponse>> getAll(@RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(historyService.getAllHistory(token(auth)));
    }

    @GetMapping("/month")
    public ResponseEntity<List<TransactionHistoryResponse>> getByMonth(
            @RequestHeader("Authorization") String auth,
            @RequestParam String month) {
        return ResponseEntity.ok(historyService.getHistoryByMonth(token(auth), month));
    }

    @GetMapping("/{type}/{id}")
    public ResponseEntity<TransactionHistoryResponse> getOne(
            @RequestHeader("Authorization") String auth,
            @PathVariable String type,
            @PathVariable Long id) {
        return ResponseEntity.ok(historyService.getHistoryById(token(auth), id, type));
    }

    @GetMapping("/pdf/{type}/{id}")
    public ResponseEntity<byte[]> downloadPdf(
            @RequestHeader("Authorization") String auth,
            @PathVariable String type,
            @PathVariable Long id) {

        TransactionHistoryResponse history = historyService.getHistoryById(token(auth), id, type);
        byte[] pdf = pdfService.generateReceipt(history);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=receipt_" + history.reference() + ".pdf")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}