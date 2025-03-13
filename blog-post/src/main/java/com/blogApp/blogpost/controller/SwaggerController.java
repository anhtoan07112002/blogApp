package com.blogApp.blogpost.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class SwaggerController {
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @GetMapping("/swagger")
    public RedirectView redirectToSwaggerUi() {
        return new RedirectView(contextPath + "/swagger-ui/index.html");
    }

    @GetMapping("/")
    public RedirectView redirectRootToSwaggerUi() {
        return new RedirectView(contextPath + "/swagger-ui/index.html");
    }

    @GetMapping("/api-docs")
    public RedirectView redirectToApiDocs() {
        return new RedirectView(contextPath + "/v3/api-docs");
    }
} 