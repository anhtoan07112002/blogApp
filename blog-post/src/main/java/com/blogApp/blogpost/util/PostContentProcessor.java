package com.blogApp.blogpost.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

/**
 * Tiện ích xử lý nội dung bài viết
 */
@Component
public class PostContentProcessor {

    /**
     * Làm sạch nội dung HTML để phòng tránh XSS
     * @param content Nội dung HTML
     * @return Nội dung HTML đã được làm sạch
     */
    public String sanitizeHtml(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }

        // Cho phép các thẻ HTML cơ bản và thuộc tính an toàn
        Safelist safelist = Safelist.basicWithImages();
        safelist.addTags("h1", "h2", "h3", "h4", "h5", "h6", "blockquote", "pre", "hr")
                .addAttributes("a", "target", "rel")
                .addAttributes("img", "width", "height", "alt");

        return Jsoup.clean(content, safelist);
    }

    /**
     * Trích xuất summary từ nội dung
     * @param content Nội dung HTML
     * @param maxLength Độ dài tối đa
     * @return Summary đã được trích xuất
     */
    public String extractSummary(String content, int maxLength) {
        if (content == null || content.isEmpty()) {
            return "";
        }

        // Xóa tất cả thẻ HTML và chỉ lấy text
        String plainText = Jsoup.parse(content).text();

        if (plainText.length() <= maxLength) {
            return plainText;
        }

        // Tìm vị trí của dấu cách gần nhất trước maxLength
        int lastSpaceIndex = plainText.lastIndexOf(' ', maxLength);
        if (lastSpaceIndex == -1) {
            return plainText.substring(0, maxLength) + "...";
        }

        return plainText.substring(0, lastSpaceIndex) + "...";
    }
}