package com.insightflow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.insightflow.services.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.io.FileOutputStream;
import java.io.File;
import java.awt.Desktop;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class VisualizationServiceTest {

    @Autowired
    private VisualizationService visualizationService;

    @MockBean
    private AnalysisService analysisService;

    @BeforeEach
    public void setUp() {
        // Mock SWOT data
        Map<String, List<String>> swot = new HashMap<>();
        swot.put("strengths", Arrays.asList("AI Expertise", "Innovation Leader"));
        swot.put("weaknesses", Arrays.asList("Regulatory Burden", "Talent Acquisition"));
        swot.put("opportunities", Arrays.asList("Market Expansion", "Education Initiatives"));
        swot.put("threats", Arrays.asList("Competition Rise", "Ethics Scrutiny"));
        Mockito.when(analysisService.generateSwot("OpenAI")).thenReturn(swot);

        // Mock PESTEL data
        Map<String, List<String>> pestel = new HashMap<>();
        pestel.put("political", Arrays.asList("Regulatory", "Government"));
        pestel.put("economic", Arrays.asList("Competition", "Innovation"));
        pestel.put("social", Arrays.asList("Ethics", "Bias"));
        pestel.put("technological", Arrays.asList("AI safety", "Cybersecurity"));
        pestel.put("environmental", Arrays.asList("Energy use", "Carbon footprint"));
        pestel.put("legal", Arrays.asList("Intellectual", "Patent law"));
        Mockito.when(analysisService.generatePestel("OpenAI")).thenReturn(pestel);

        // Mock Porter Forces data
        Map<String, List<String>> porter = new HashMap<>();
        porter.put("rivalry", Arrays.asList("High", "Competition"));
        porter.put("new_entrants", Arrays.asList("Low", "Barriers"));
        porter.put("supplier_power", Arrays.asList("Moderate", "Significant"));
        porter.put("buyer_power", Arrays.asList("Growing", "Increasing"));
        porter.put("substitutes", Arrays.asList("Few", "Limited"));
        Mockito.when(analysisService.generatePorterForces("OpenAI")).thenReturn(porter);

        // Mock BCG Matrix data with uneven values
        Map<String, Map<String, Double>> bcg = new HashMap<>();
        Map<String, Double> chatGpt = new HashMap<>();
        chatGpt.put("market_share", 1.8);
        chatGpt.put("growth_rate", 18.0);
        Map<String, Double> gpt4 = new HashMap<>();
        gpt4.put("market_share", 0.7);
        gpt4.put("growth_rate", 12.0);
        Map<String, Double> codex = new HashMap<>();
        codex.put("market_share", 0.2);
        codex.put("growth_rate", 8.0);
        Map<String, Double> whisper = new HashMap<>();
        whisper.put("market_share", 1.2);
        whisper.put("growth_rate", 15.0);
        bcg.put("ChatGPT", chatGpt);
        bcg.put("GPT-4", gpt4);
        bcg.put("Codex", codex);
        bcg.put("Whisper", whisper);
        Mockito.when(analysisService.generateBcgMatrix("OpenAI")).thenReturn(bcg);

        // Mock McKinsey 7S data
        Map<String, String> mckinsey = new HashMap<>();
        mckinsey.put("strategy", "Innovative");
        mckinsey.put("structure", "Flat");
        mckinsey.put("systems", "Digital");
        mckinsey.put("style", "Collaborative");
        mckinsey.put("staff", "Talented");
        mckinsey.put("skills", "Adaptable");
        mckinsey.put("shared_values", "Empathetic");
        Mockito.when(analysisService.generateMckinsey7s("OpenAI")).thenReturn(mckinsey);
    }

    @Test
    public void testGenerateSwotImage() throws Exception {
        String base64 = visualizationService.generateSwotImage(analysisService.generateSwot("OpenAI"));
        assertNotNull(base64);
        assertFalse(base64.isEmpty());
        assertTrue(base64.matches("^[A-Za-z0-9+/=]+$"));
        byte[] decoded = java.util.Base64.getDecoder().decode(base64);
        assertTrue(decoded.length > 0 && decoded[0] == (byte) 0x89); // PNG signature

        // Save and show image
        File file = new File("swot.png");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(decoded);
        }
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(file);
        }
    }

    @Test
    public void testGeneratePestelImage() throws Exception {
        String base64 = visualizationService.generatePestelImage(analysisService.generatePestel("OpenAI"));
        assertNotNull(base64);
        assertFalse(base64.isEmpty());
        assertTrue(base64.matches("^[A-Za-z0-9+/=]+$"));
        byte[] decoded = java.util.Base64.getDecoder().decode(base64);
        assertTrue(decoded.length > 0 && decoded[0] == (byte) 0x89);

        File file = new File("pestel.png");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(decoded);
        }
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(file);
        }
    }

    @Test
    public void testGeneratePorterImage() throws Exception {
        String base64 = visualizationService.generatePorterImage(analysisService.generatePorterForces("OpenAI"));
        assertNotNull(base64);
        assertFalse(base64.isEmpty());
        assertTrue(base64.matches("^[A-Za-z0-9+/=]+$"));
        byte[] decoded = java.util.Base64.getDecoder().decode(base64);
        assertTrue(decoded.length > 0 && decoded[0] == (byte) 0x89);

        File file = new File("porter.png");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(decoded);
        }
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(file);
        }
    }

    @Test
    public void testGenerateBcgImage() throws Exception {
        String base64 = visualizationService.generateBcgImage(analysisService.generateBcgMatrix("OpenAI"));
        assertNotNull(base64);
        assertFalse(base64.isEmpty());
        assertTrue(base64.matches("^[A-Za-z0-9+/=]+$"));
        byte[] decoded = java.util.Base64.getDecoder().decode(base64);
        assertTrue(decoded.length > 0 && decoded[0] == (byte) 0x89);

        File file = new File("bcg.png");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(decoded);
        }
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(file);
        }
    }

    @Test
    public void testGenerateMckinseyImage() throws Exception {
        String base64 = visualizationService.generateMckinseyImage(analysisService.generateMckinsey7s("OpenAI"));
        assertNotNull(base64);
        assertFalse(base64.isEmpty());
        assertTrue(base64.matches("^[A-Za-z0-9+/=]+$"));
        byte[] decoded = java.util.Base64.getDecoder().decode(base64);
        assertTrue(decoded.length > 0 && decoded[0] == (byte) 0x89);

        File file = new File("mckinsey.png");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(decoded);
        }
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(file);
        }
    }
}