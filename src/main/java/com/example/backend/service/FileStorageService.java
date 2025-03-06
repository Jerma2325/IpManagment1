package com.example.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path storageLocation;

    public FileStorageService(@Value("${file.upload-dir:./uploads}") String uploadDir) throws IOException {
        this.storageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        Files.createDirectories(this.storageLocation);
        System.out.println("File storage location: " + this.storageLocation.toString());
    }

    public String storeFile(MultipartFile file) throws IOException {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        String fileExtension = "";
        int lastIndex = originalFilename.lastIndexOf('.');
        if (lastIndex > 0) {
            fileExtension = originalFilename.substring(lastIndex);
            originalFilename = originalFilename.substring(0, lastIndex);
        }

        String uniqueFilename = originalFilename + "_" + UUID.randomUUID().toString().substring(0, 8) + fileExtension;

        Path targetLocation = this.storageLocation.resolve(uniqueFilename);

        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        System.out.println("Stored file: " + uniqueFilename + " at " + targetLocation.toString());

        return uniqueFilename;
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.storageLocation.resolve(fileName).normalize();

            System.out.println("Looking for file at: " + filePath.toString());
            System.out.println("File exists: " + Files.exists(filePath));

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                System.out.println("File not found: " + fileName);
                try {
                    System.out.println("Files in directory:");
                    Files.list(this.storageLocation).forEach(path -> System.out.println(" - " + path.getFileName()));
                } catch (Exception e) {
                    System.out.println("Could not list files: " + e.getMessage());
                }
                return null;
            }
        } catch (MalformedURLException e) {
            System.out.println("MalformedURLException: " + e.getMessage());
            return null;
        }
    }
}