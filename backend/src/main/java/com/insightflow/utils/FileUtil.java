package com.insightflow.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class FileUtil {

    private final Path uploadDir = Paths.get("uploaded_files");

    public FileUtil() {
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload dir", e);
        }
    }

    /**
     * Saves uploaded file and returns path, mirroring UPLOAD_DIR.
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
        return targetPath.toString();
    }

    /**
     * Loads document text, mirroring loaders (TXT/PDF).
     * @param filePath The file path.
     * @return Full text content.
     */
    public String loadDocumentText(String filePath) throws IOException {
        File file = new File(filePath);
        if (filePath.toLowerCase().endsWith(".pdf")) {
            try (PDDocument document = PDDocument.load(file)) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document);
            }
        } else {
            // TXT
            return Files.readString(Paths.get(filePath));
        }
    }
}