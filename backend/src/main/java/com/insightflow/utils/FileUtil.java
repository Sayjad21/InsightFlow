package com.insightflow.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.stream.Collectors;
@Component
public class FileUtil {

    private final Path uploadDir;

    public FileUtil() {
        // Use absolute path to ensure files are saved in the project directory
        String projectRoot = System.getProperty("user.dir");
        this.uploadDir = Paths.get(projectRoot, "uploaded_files").toAbsolutePath();

        try {
            Files.createDirectories(uploadDir);
            System.out.println("Upload directory initialized at: " + uploadDir.toString());
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload dir at: " + uploadDir, e);
        }
    }

    /**
     * Saves uploaded file and returns path, mirroring UPLOAD_DIR.
     * 
     * @param file The MultipartFile.
     * @return Saved file path.
     */
    public String saveUploadedFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path targetPath = uploadDir.resolve(fileName);
        file.transferTo(targetPath.toFile());
        return targetPath.toAbsolutePath().toString();
    }

    /**
     * Checks if a file exists, handling both filesystem and classpath resources.
     */
    public boolean fileExists(String filePath) {
        // Try as classpath resource first (for tests)
        InputStream resource = getClass().getClassLoader().getResourceAsStream(filePath);
        if (resource != null) {
            try {
                resource.close();
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        // Fallback to filesystem (for production)
        return new File(filePath).exists();
    }

    /**
     * Loads text from a TXT or PDF file, handling both filesystem and classpath resources.
     */
    public String loadDocumentText(String filePath) throws IOException {
        // Try as classpath resource first (for tests)
        InputStream resource = getClass().getClassLoader().getResourceAsStream(filePath);
        if (resource != null) {
            try {
                if (filePath.toLowerCase().endsWith(".pdf")) {
                    try (PDDocument document = PDDocument.load(resource)) {
                        PDFTextStripper stripper = new PDFTextStripper();
                        return stripper.getText(document);
                    }
                } else {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource))) {
                        return reader.lines().collect(Collectors.joining("\n"));
                    }
                }
            } finally {
                resource.close();
            }
        }

        // Fallback to filesystem (for production)
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("File does not exist: " + filePath);
        }
        if (filePath.toLowerCase().endsWith(".pdf")) {
            try (PDDocument document = PDDocument.load(file)) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document);
            }
        } else {
            return Files.readString(Paths.get(filePath));
        }
    }
}