
package com.example.opaybanking.service;

import com.example.opaybanking.dto.TransactionHistoryResponse;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfService {

    public byte[] generateReceipt(TransactionHistoryResponse history) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(out);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            document.add(new Paragraph("MILES BANK")
                    .setFontSize(24)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("OFFICIAL RECEIPT")
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            document.add(new Paragraph("Date: " + history.date()));
            document.add(new Paragraph("Reference: " + history.reference()));
            document.add(new Paragraph("Type: " + history.type()));
            document.add(new Paragraph("Amount: ₦" + String.format("%,.2f", history.amount())));
            document.add(new Paragraph("Status: " + history.status()));
            document.add(new Paragraph("Description: " + history.description()));

            if (history.beneficiary() != null && !history.beneficiary().trim().isEmpty()) {
                document.add(new Paragraph("Beneficiary: " + history.beneficiary()));
            }
            if (history.bank() != null && !history.bank().trim().isEmpty()) {
                document.add(new Paragraph("Bank: " + history.bank()));
            }
            if (history.network() != null) {
                document.add(new Paragraph("Network: " + history.network()));
            }

            document.add(new Paragraph("\nThank you for banking with Miles Bank.")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(30));

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage());
        }

        return out.toByteArray();
    }
}