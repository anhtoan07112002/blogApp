package com.blogApp.blogcommon.util;

import java.util.UUID;
import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringUtils {

    public static String generateUniqueString() {
        return UUID.randomUUID().toString();
    }

    public static String generateSlug(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        // Convert to lowercase and normalize
        String normalized = Normalizer.normalize(input.toLowerCase(), Normalizer.Form.NFD);
        // Remove accents and diacritics
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String withoutAccents = pattern.matcher(normalized).replaceAll("");
        // Replace spaces with hyphens and remove invalid characters
        return withoutAccents.replaceAll("[^\\p{Alnum}]", "-")
                .replaceAll("-{2,}", "-") // Replace multiple hyphens with a single one
                .replaceAll("^-|-$", ""); // Remove leading and trailing hyphens
    }

    public static String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }
}
