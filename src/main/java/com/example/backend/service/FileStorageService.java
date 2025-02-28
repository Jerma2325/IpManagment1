package com.example.backend.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;

@Service
public class FileStorageService {

    private final Path storageLocation;

    public FileStorageService() throws IOException {
        this.storageLocation = Paths.get("uploads").toAbsolutePath().normalize();
        Files.createDirectories(this.storageLocation);
    }

    public String storeFile(MultipartFile file) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        // need to add more checks (file size, type, etc.)
        Path targetLocation = this.storageLocation.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }

    public Resource loadFileAsResource(String fileName) throws MalformedURLException {
        Path filePath = this.storageLocation.resolve(fileName).normalize();
        Resource resource = new UrlResource(filePath.toUri());
        if (resource.exists()) {
            return resource;
        } else {
            throw new RuntimeException("File not found: " + fileName);
        }
    }
}
