package com.insightflow.services;

import com.insightflow.config.SupabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    @Autowired
    private SupabaseConfig supabaseConfig;

    /**
     * Upload image to Supabase Storage and return public URL
     * 
     * @param imageData   Image data as byte array
     * @param fileName    File name for the image
     * @param contentType Content type (e.g., "image/png")
     * @return Public URL of the uploaded image
     */
    public String uploadImage(byte[] imageData, String fileName, String contentType) {
        try {
            // Generate unique filename to avoid conflicts
            String uniqueFileName = generateUniqueFileName(fileName);
            String objectPath = "visualizations/" + uniqueFileName;

            // Build the upload URL
            String uploadUrl = supabaseConfig.getStorageApiUrl() + "/object/" + supabaseConfig.getStorageBucket() + "/"
                    + objectPath;

            // Create WebClient and upload the image
            WebClient webClient = WebClient.builder()
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseConfig.getSupabaseServiceRoleKey())
                    .build();

            // Upload the image using WebClient
            Mono<String> responseMono = webClient.post()
                    .uri(uploadUrl)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(BodyInserters.fromResource(new ByteArrayResource(imageData)))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(60));

            // Block and get the response (since we need synchronous behavior)
            responseMono.block();

            // If we get here without exception, the upload was successful
            String publicUrl = supabaseConfig.getSupabaseUrl() + "/storage/v1/object/public/" +
                    supabaseConfig.getStorageBucket() + "/" + objectPath;

            System.out.println("Successfully uploaded image to Supabase: " + publicUrl);
            return publicUrl;

        } catch (Exception e) {
            System.err.println("Error uploading image to Supabase Storage: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Upload image from ByteArrayOutputStream to Supabase Storage
     * 
     * @param outputStream ByteArrayOutputStream containing image data
     * @param fileName     File name for the image
     * @param contentType  Content type (e.g., "image/png")
     * @return Public URL of the uploaded image
     */
    public String uploadImageFromStream(ByteArrayOutputStream outputStream, String fileName, String contentType) {
        return uploadImage(outputStream.toByteArray(), fileName, contentType);
    }

    /**
     * Generate a unique filename to avoid conflicts
     * 
     * @param originalFileName Original filename
     * @return Unique filename with timestamp and UUID
     */
    private String generateUniqueFileName(String originalFileName) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        // Extract file extension
        String extension = "";
        int lastDot = originalFileName.lastIndexOf('.');
        if (lastDot > 0) {
            extension = originalFileName.substring(lastDot);
        }

        return timestamp + "_" + uuid + "_" + originalFileName.replace(extension, "") + extension;
    }

    /**
     * Check if Supabase Storage is available
     * 
     * @return true if Supabase Storage is configured and available
     */
    public boolean isAvailable() {
        try {
            return supabaseConfig.getSupabaseUrl() != null &&
                    !supabaseConfig.getSupabaseUrl().isEmpty() &&
                    supabaseConfig.getSupabaseServiceRoleKey() != null &&
                    !supabaseConfig.getSupabaseServiceRoleKey().isEmpty();
        } catch (Exception e) {
            System.err.println("Supabase configuration check failed: " + e.getMessage());
            return false;
        }
    }
}