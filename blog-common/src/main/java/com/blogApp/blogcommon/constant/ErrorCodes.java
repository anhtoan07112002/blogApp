package com.blogApp.blogcommon.constant;

public class ErrorCodes {

    // User Service Error Codes
    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String INVALID_TOKEN = "INVALID_TOKEN";
    public static final String EXPIRED_TOKEN = "EXPIRED_TOKEN";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String FORBIDDEN = "FORBIDDEN";
    public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
    public static final String FILE_READ_ERROR = "FILE_READ_ERROR";

    // File Service Error Codes 
    public static final String FILE_UPLOAD_ERROR = "FILE_UPLOAD_ERROR";
    public static final String FILE_DOWNLOAD_ERROR = "FILE_DOWNLOAD_ERROR";
    public static final String FILE_INFO_ERROR = "FILE_INFO_ERROR";
    public static final String FILE_DELETE_ERROR = "FILE_DELETE_ERROR";
    public static final String FILE_CHECK_ERROR = "FILE_CHECK_ERROR";
    public static final String URL_GENERATION_ERROR = "URL_GENERATION_ERROR";
    
    // Media Service Error Codes
    public static final String MEDIA_PROCESSING_ERROR = "MEDIA_PROCESSING_ERROR";
    public static final String MEDIA_UPLOAD_ERROR = "MEDIA_UPLOAD_ERROR";
    public static final String MEDIA_NOT_FOUND = "MEDIA_NOT_FOUND";
    public static final String MEDIA_DELETE_ERROR = "MEDIA_DELETE_ERROR";
    public static final String MEDIA_DOWNLOAD_ERROR = "MEDIA_DOWNLOAD_ERROR";
    public static final String MEDIA_INVALID_TYPE = "MEDIA_INVALID_TYPE";
    public static final String MEDIA_SIZE_EXCEEDED = "MEDIA_SIZE_EXCEEDED";
}
