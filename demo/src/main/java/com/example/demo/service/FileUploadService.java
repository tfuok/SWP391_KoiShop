package com.example.demo.service;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;
@Service
public class FileUploadService {

    // Default Storage instance
    private final Storage storage = StorageOptions.getDefaultInstance().getService();

    // Method to upload file to Google Cloud Storage
    public void uploadFile(MultipartFile file) throws IOException {
        // Generate a unique file name to avoid conflicts
        String fileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();

        // Capture the file's MIME type
        String contentType = file.getContentType();

        // Build metadata with the correct MIME type
        BlobInfo blobInfo = BlobInfo.newBuilder("koimanagement-cd9bd.appspot.com", fileName)
                .setContentType(contentType)
                .build();

        // Upload the file with metadata
        storage.create(blobInfo, file.getBytes());
    }
}
