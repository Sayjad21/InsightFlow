package com.insightflow.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RagRequest {
    private MultipartFile file; // Required: TXT (or PDF later) file upload
    private String query;       // Required: Query to ask against the document
}