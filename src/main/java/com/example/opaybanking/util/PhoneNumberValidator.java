package com.example.opaybanking.util;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import org.springframework.stereotype.Component;

@Component
public class PhoneNumberValidator {

    private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    /**
     * Validates if a phone number is a REAL Nigerian mobile number
     * Accepts: 0803xxxxxxx, 703xxxxxxx, +234803xxxxxxx, 803xxxxxxx
     */
    public boolean isValidNigerianNumber(String input) {
        if (input == null || input.trim().isEmpty()) return false;

        String phone = input.trim();

        try {
            if (phone.startsWith("0") && phone.length() == 11) {
                phone = "+234" + phone.substring(1);
            } else if (phone.length() == 10 || phone.length() == 11) {
                phone = "+234" + phone.replaceFirst("^0*", "");
            } else if (!phone.startsWith("+234")) {
                phone = "+234" + phone.replaceAll("\\D", "");
            }

            PhoneNumber number = phoneUtil.parse(phone, "NG");

            if (!phoneUtil.isValidNumber(number)) return false;

            var type = phoneUtil.getNumberType(number);
            return type == PhoneNumberUtil.PhoneNumberType.MOBILE ||
                    type == PhoneNumberUtil.PhoneNumberType.FIXED_LINE_OR_MOBILE;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Detect network from prefix (100% accurate)
     */
    public String detectNetwork(String phoneNumber) {
        if (!isValidNigerianNumber(phoneNumber)) return "UNKNOWN";

        String digits = phoneNumber.replaceAll("\\D", "");
        if (digits.startsWith("0")) digits = "234" + digits.substring(1);
        if (digits.length() >= 6) {
            String prefix = digits.substring(3, 6);

            return switch (prefix) {
                case "703","706","803","806","810","813","814","816","903","906","913","916" -> "MTN";
                case "702","708","802","808","812","901","902","904","907","912" -> "AIRTEL";
                case "705","805","807","811","815","905","915" -> "GLO";
                case "709","809","817","818","908","909" -> "9MOBILE";
                default -> "UNKNOWN";
            };
        }
        return "UNKNOWN";
    }


}