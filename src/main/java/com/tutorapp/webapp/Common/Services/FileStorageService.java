package com.tutorapp.webapp.Common.Services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootLocation = Paths.get("uploads/profile-pictures");

     public FileStorageService() throws IOException {
        if (!Files.exists(rootLocation)) {
            Files.createDirectories(rootLocation);
        }
    }
     public String saveProfilePicture(MultipartFile file, Long studentId) throws IOException {
        String fileName = "student_" + studentId + "_" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path targetLocation = rootLocation.resolve(fileName);

        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/profile-pictures/" + fileName; // relative URL
    }

    public void deleteOldProfilePicture(String oldImageUrl) {
        if (oldImageUrl == null || !oldImageUrl.contains("/uploads/profile-pictures/")) return;
        try {
            String fileName = Paths.get(oldImageUrl).getFileName().toString();
            Path filePath = rootLocation.resolve(fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Failed to delete old image: " + e.getMessage());
        }
    }
}
