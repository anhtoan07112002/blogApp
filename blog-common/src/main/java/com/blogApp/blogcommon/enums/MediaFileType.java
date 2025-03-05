package com.blogApp.blogcommon.enums;

import com.blogApp.blogcommon.constant.AppConstants;

/**
 * Enum định nghĩa các loại media được hỗ trợ
 */
public enum MediaFileType {
    IMAGE("image"),
    VIDEO("video"),
    AUDIO("audio"),
    DOCUMENT("document"),
    OTHER("other");
    
    private final String type;
    
    MediaFileType(String type) {
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
    
    public static MediaFileType fromString(String type) {
        for (MediaFileType mediaFileType : MediaFileType.values()) {
            if (mediaFileType.getType().equalsIgnoreCase(type)) {
                return mediaFileType;
            }
        }
        return OTHER;
    }
    
    public String[] getAllowedExtensions() {
        switch (this) {
            case IMAGE:
                return AppConstants.ALLOWED_IMAGE_EXTENSIONS;
            case VIDEO:
                return AppConstants.ALLOWED_VIDEO_EXTENSIONS;
            case AUDIO:
                return AppConstants.ALLOWED_AUDIO_EXTENSIONS;
            case DOCUMENT:
                return AppConstants.ALLOWED_DOCUMENT_EXTENSIONS;
            default:
                return new String[0];
        }
    }
    
    public boolean isAllowedExtension(String extension) {
        String[] allowedExtensions = getAllowedExtensions();
        for (String allowed : allowedExtensions) {
            if (allowed.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }
}
