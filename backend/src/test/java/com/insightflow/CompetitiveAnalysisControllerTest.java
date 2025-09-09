package com.insightflow;

import com.insightflow.controllers.CompetitiveAnalysisController;
import com.insightflow.services.AnalysisService;
import com.insightflow.services.RagService;
import com.insightflow.services.ScrapingService;
import com.insightflow.services.VisualizationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompetitiveAnalysisController.class)
@AutoConfigureMockMvc(addFilters = false) // This disables Spring Security for the test
public class CompetitiveAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RagService ragService;

    @MockBean
    private AnalysisService analysisService;

    @MockBean
    private ScrapingService scrapingService;

    @MockBean
    private VisualizationService visualizationService;

    @Test
    public void testAnalyze() throws Exception {
        String companyName = "OpenAI";

        Map<String, Object> ragResult = new HashMap<>();
        ragResult.put("summaries", List.of("Summary 1"));
        ragResult.put("strategy_recommendations", "Recommendations");
        ragResult.put("links", List.of("https://example.com"));
        Mockito.when(ragService.analyzeCompetitor(anyString(), eq(companyName))).thenReturn(ragResult);

        Map<String, List<String>> swot = new HashMap<>();
        swot.put("strengths", List.of("Strength 1"));
        Mockito.when(analysisService.generateSwot(companyName)).thenReturn(swot);

        Map<String, List<String>> pestel = new HashMap<>();
        pestel.put("political", List.of("Political 1"));
        Mockito.when(analysisService.generatePestel(companyName)).thenReturn(pestel);

        Map<String, List<String>> porter = new HashMap<>();
        porter.put("rivalry", List.of("Rivalry 1"));
        Mockito.when(analysisService.generatePorterForces(companyName)).thenReturn(porter);

        Map<String, Map<String, Double>> bcg = new HashMap<>();
        Map<String, Double> productData = new HashMap<>();
        productData.put("market_share", 1.0);
        productData.put("growth_rate", 15.0);
        bcg.put("ChatGPT", productData);
        Mockito.when(analysisService.generateBcgMatrix(companyName)).thenReturn(bcg);

        Map<String, String> mckinsey = new HashMap<>();
        mckinsey.put("strategy", "Innovate");
        Mockito.when(analysisService.generateMckinsey7s(companyName)).thenReturn(mckinsey);

        Mockito.when(visualizationService.generateSwotImage(swot)).thenReturn("swot_base64");
        Mockito.when(visualizationService.generatePestelImage(pestel)).thenReturn("pestel_base64");
        Mockito.when(visualizationService.generatePorterImage(porter)).thenReturn("porter_base64");
        Mockito.when(visualizationService.generateBcgImage(bcg)).thenReturn("bcg_base64");
        Mockito.when(visualizationService.generateMckinseyImage(mckinsey)).thenReturn("mckinsey_base64");

        Mockito.when(scrapingService.getLinkedInAnalysis(companyName)).thenReturn("LinkedIn Analysis");

        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "Sample content".getBytes());

        MvcResult result = mockMvc.perform(multipart("/api/analyze")
                .file(file)
                .file(new MockMultipartFile("company_name", "", "text/plain", companyName.getBytes())))
                .andExpect(status().isOk())
                .andReturn();

        System.out.println(result.getResponse().getContentAsString());

        // Test without file
        mockMvc.perform(multipart("/api/analyze")
                .file(new MockMultipartFile("company_name", "", "text/plain", companyName.getBytes())))
                .andExpect(status().isOk());
    }
}