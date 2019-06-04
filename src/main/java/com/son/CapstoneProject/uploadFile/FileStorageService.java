package com.son.CapstoneProject.uploadFile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {

    private Path fileStorageLocation;

    /**
     * Autowire the FileStorageProperties and get upload-dir from application.properties
     * @param fileStorageProperties
     */
    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath()
                .normalize();

        // Then create a dir
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Copy the file to the directory specified in application.properties
     * @param file
     * @return
     */
    public String storeFile(MultipartFile file) {
        try {
            // a.png
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            // C:file
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            // C:file/a.png
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            }
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
