package com.inventory.util;

import java.util.regex.Pattern;

/**
 * ValidationHelper - Utility class for input validation
 */
public class ValidationHelper {

    // Email regex pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Phone regex pattern (supports various formats)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+]?[(]?[0-9]{1,4}[)]?[-\\s.]?[(]?[0-9]{1,4}[)]?[-\\s.]?[0-9]{1,9}$");

    /**
     * Check if string is null or empty
     */
    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate phone number format
     */
    public static boolean isValidPhone(String phone) {
        if (isEmpty(phone)) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validate positive number
     */
    public static boolean isPositiveNumber(String value) {
        try {
            double num = Double.parseDouble(value);
            return num > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validate non-negative integer
     */
    public static boolean isNonNegativeInteger(String value) {
        try {
            int num = Integer.parseInt(value);
            return num >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validate string length
     */
    public static boolean isValidLength(String value, int minLength, int maxLength) {
        if (value == null) {
            return false;
        }
        int length = value.trim().length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * Sanitize string input (remove special characters)
     */
    public static String sanitize(String input) {
        if (input == null) {
            return "";
        }
        return input.trim().replaceAll("[<>\"']", "");
    }
}
