package com.blogApp.blogcommon.constant;

public class AppConstants {
    // General constants
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final String DEFAULT_SORT_BY = "id";
    public static final String DEFAULT_SORT_DIRECTION = "desc";

    // Service Names
    public static final String AUTH_SERVICE = "auth";
    public static final String POST_SERVICE = "post";
    public static final String MEDIA_SERVICE = "media";
    public static final String NOTIFICATION_SERVICE = "notification";
    public static final String GATEWAY_SERVICE = "gateway";

    // Cache constants
    public static final String USERS_CACHE = "users";
    public static final String POSTS_CACHE = "posts";
    public static final String USER_POSTS_CACHE = "user:posts";
    public static final String REFRESH_TOKEN_CACHE = "auth:token:refresh";
    public static final String ACCESS_TOKEN_BLACKLIST_CACHE = "auth:token:blacklist";
    public static final String USER_PROFILE_CACHE = "users:profile";



    // API Endpoints
    public static final String API_BASE_PATH = "/api";
    public static final String AUTH_BASE_PATH = API_BASE_PATH + "/auth";
    public static final String POSTS_BASE_PATH = API_BASE_PATH + "/posts";
    public static final String USERS_BASE_PATH = API_BASE_PATH + "/users";
    public static final String MEDIA_BASE_PATH = API_BASE_PATH + "/media";

    // File upload constants
    public static final String FILE_UPLOAD_DIR = "uploads";
    public static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    public static final String[] ALLOWED_IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif"};

    // Token related
    public static final String TOKEN_TYPE = "Bearer";
    public static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    // Notification
    public static final String EMAIL_TEMPLATE_DIR = "email-templates/";
}