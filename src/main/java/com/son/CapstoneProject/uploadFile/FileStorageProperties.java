package com.son.CapstoneProject.uploadFile;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * This annotation is to bind the property in application.properties
 * having prefix "file" and has name like: uploadDir, upload-dir, etc.
 * (Spring uses some relaxed rules for binding properties)
 */
@ConfigurationProperties(prefix = "file")
// Must have getter and setter
@Getter
@Setter
public class FileStorageProperties {
    private String uploadDir;
}
