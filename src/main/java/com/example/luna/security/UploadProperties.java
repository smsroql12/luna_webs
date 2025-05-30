package com.example.luna.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UploadProperties {

    @Value("${upload.root.path}")
    private String uploadRootPath;

    public String getUploadRootPath() {
        if (!uploadRootPath.endsWith("/")) {
            return uploadRootPath + "/";
        }
        return uploadRootPath;
    }
}