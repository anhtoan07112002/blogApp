package com.blogApp.blogpost.util;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Tiện ích để tạo slug từ chuỗi
 */
@Component
public class SlugUtils {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern EDGESDHASHES = Pattern.compile("(^-|-$)");

    /**
     * Tạo slug từ một chuỗi
     * @param input Chuỗi đầu vào
     * @return Slug được tạo
     */
    public String createSlug(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        slug = EDGESDHASHES.matcher(slug).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }

    /**
     * Tạo slug duy nhất bằng cách thêm hậu tố là timestamp
     * @param input Chuỗi đầu vào
     * @return Slug duy nhất
     */
    public String createUniqueSlug(String input) {
        String baseSlug = createSlug(input);
        return baseSlug + "-" + System.currentTimeMillis();
    }
}
