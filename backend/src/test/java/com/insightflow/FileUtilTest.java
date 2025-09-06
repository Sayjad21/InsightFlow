package com.insightflow;

import com.insightflow.utils.FileUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootTest
public class FileUtilTest {

    @Autowired
    private FileUtil fileUtil;

    @Test
    public void testFileUploadAndRead() throws IOException {
        // Test file upload
        String testContent = "This is a test file content for FileUtil.";
        MockMultipartFile mockFile = new MockMultipartFile(
            "file", "test.txt", "text/plain", testContent.getBytes()
        );
        
        String savedPath = fileUtil.saveUploadedFile(mockFile);
        System.out.println("✅ File saved to: " + savedPath);
        
        // Test file reading
        String readContent = fileUtil.loadDocumentText(savedPath);
        System.out.println("✅ File content read: " + readContent);
        
        // Cleanup
        Files.deleteIfExists(Paths.get(savedPath));
        System.out.println("✅ Test file cleaned up");
    }
}
