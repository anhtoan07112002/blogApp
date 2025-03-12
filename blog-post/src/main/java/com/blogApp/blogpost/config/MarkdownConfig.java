package com.blogApp.blogpost.config;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughSubscriptExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class MarkdownConfig {

    @Bean
    public Parser markdownParser() {
        MutableDataSet options = new MutableDataSet();
        
        // Thêm các extension
        options.set(Parser.EXTENSIONS, Arrays.asList(
            TablesExtension.create(),
            EmojiExtension.create(),
            AnchorLinkExtension.create(),
            StrikethroughSubscriptExtension.create()
        ));

        return Parser.builder(options).build();
    }

    @Bean
    public HtmlRenderer htmlRenderer() {
        MutableDataSet options = new MutableDataSet();
        
        // Cấu hình HTML renderer
        options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");
        options.set(HtmlRenderer.ESCAPE_HTML, true);
        options.set(HtmlRenderer.SUPPRESS_HTML_BLOCKS, true);
        options.set(HtmlRenderer.SUPPRESS_INLINE_HTML, true);
        
        // Thêm các extension tương ứng với parser
        options.set(Parser.EXTENSIONS, Arrays.asList(
            TablesExtension.create(),
            EmojiExtension.create(),
            AnchorLinkExtension.create(),
            StrikethroughSubscriptExtension.create()
        ));

        return HtmlRenderer.builder(options).build();
    }
} 