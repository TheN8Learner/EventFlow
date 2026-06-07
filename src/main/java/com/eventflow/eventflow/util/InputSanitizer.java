package com.eventflow.eventflow.util;

public final class InputSanitizer {

    private InputSanitizer() {
    }

    public static String text(String value) {
        if (value == null) {
            return null;
        }
        return stripControlCharacters(value).trim();
    }

    public static String multiline(String value) {
        if (value == null) {
            return null;
        }
        return stripControlCharacters(value)
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .trim();
    }

    public static String email(String value) {
        String cleaned = text(value);
        return cleaned == null ? null : cleaned.toLowerCase();
    }

    private static String stripControlCharacters(String value) {
        return value.replaceAll("[\\p{Cntrl}&&[^\n\t]]", "");
    }
}
