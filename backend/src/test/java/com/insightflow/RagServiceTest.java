package com.insightflow;

import com.insightflow.utils.*;
import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.data.message.AiMessage;
import org.mockito.stubbing.Answer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.insightflow.services.RagService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest
public class RagServiceTest {

    @Autowired
    private RagService ragService;

    @MockBean
    private AiUtil aiUtil;

    @MockBean
    private TavilyUtil tavilyUtil;

    @MockBean
    private ScrapingUtil scrapingUtil;

    @MockBean
    private EmbeddingUtil embeddingUtil;

    @MockBean
    private FileUtil fileUtil;

    @MockBean
    private GeminiEmbeddingModel geminiEmbeddingModel;

    @BeforeEach
    public void setUp() {
        // Mock FileUtil
        try {
            Mockito.when(fileUtil.loadDocumentText(anyString())).thenReturn("Sample document text");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Mock EmbeddingUtil
        Mockito.when(embeddingUtil.embedDocuments(any())).thenReturn(Arrays.asList(new Embedding(new float[]{0.1f, 0.2f})));

        // Mock AiUtil
        OllamaChatModel mockModel = Mockito.mock(OllamaChatModel.class);
        Mockito.when(aiUtil.getModel()).thenReturn(mockModel);

        // Mock chat(String)
        Mockito.when(mockModel.chat(anyString())).thenReturn("Mocked LLM response");

        // Create a proper AiMessage for the ChatResponse
        AiMessage aiMessage = AiMessage.from("Mocked RAG response");
        
        // Mock chat(List) to return a proper ChatResponse
        Mockito.when(mockModel.chat(anyList())).thenAnswer((Answer<ChatResponse>) invocation -> {
            ChatResponse mockChatResponse = Mockito.mock(ChatResponse.class);
            Mockito.when(mockChatResponse.aiMessage()).thenReturn(aiMessage);
            return mockChatResponse;
        });

        // Fix AiUtil mock for invokeWithTemplate - use more specific matchers
        Mockito.when(aiUtil.invokeWithTemplate(anyString(), any(Map.class))).thenReturn("Mocked summary");
        
        // Also mock the template methods to return specific templates
        Mockito.when(aiUtil.getSummaryTemplate()).thenReturn("Summary template");
        Mockito.when(aiUtil.getDiffWithRagTemplate()).thenReturn("Diff with RAG template");

        // Mock TavilyUtil
        Map<String, Object> searchResult = new HashMap<>();
        searchResult.put("url", "https://example.com");
        Mockito.when(tavilyUtil.search(anyString(), anyInt())).thenReturn(Arrays.asList(searchResult));

        // Mock ScrapingUtil
        Mockito.when(scrapingUtil.extractTextFromUrl(anyString())).thenReturn("Extracted text from URL");

        // Fix GeminiEmbeddingModel mock
        Embedding embedding = new Embedding(new float[]{0.1f, 0.2f});
        Response<Embedding> embeddingResponse = Response.from(embedding);
        Mockito.when(geminiEmbeddingModel.embed(anyString())).thenReturn(embeddingResponse);
    }

    @Test
    public void testBuildRagPipeline() {
        String filePath = "test.txt";
        ConversationalRetrievalChain chain = ragService.buildRagPipeline(filePath);

        assertNotNull(chain, "RAG pipeline should not be null");
    }

    @Test
    public void testAnalyzeCompetitorWithFile() {
        String filePath = "test.txt";
        String companyName = "OpenAI";

        Map<String, Object> result = ragService.analyzeCompetitor(filePath, companyName);

        assertNotNull(result, "Result map should not be null");
        assertEquals(companyName, result.get("company_name"), "Company name should match");
        assertTrue(result.containsKey("summaries"), "Result should contain 'summaries'");
        assertTrue(result.containsKey("strategy_recommendations"), "Result should contain 'strategy_recommendations'");
        assertTrue(result.containsKey("links"), "Result should contain 'links'");

        List<String> summaries = (List<String>) result.get("summaries");
        assertFalse(summaries.isEmpty(), "Summaries list should not be empty");
        assertEquals("Mocked summary", summaries.get(0), "Summary should match mocked response");

        String recommendations = (String) result.get("strategy_recommendations");
        assertNotNull(recommendations, "Strategy recommendations should not be null");
        assertFalse(recommendations.isEmpty(), "Strategy recommendations should not be empty");

        List<String> links = (List<String>) result.get("links");
        assertFalse(links.isEmpty(), "Links list should not be empty");
        assertEquals("https://example.com", links.get(0), "Link should match mocked URL");
    }

    @Test
    public void testAnalyzeCompetitorWithoutFile() {
        String filePath = null;
        String companyName = "OpenAI";

        Map<String, Object> result = ragService.analyzeCompetitor(filePath, companyName);

        assertNotNull(result, "Result map should not be null");
        assertEquals(companyName, result.get("company_name"), "Company name should match");
        assertTrue(result.containsKey("summaries"), "Result should contain 'summaries'");
        assertTrue(result.containsKey("strategy_recommendations"), "Result should contain 'strategy_recommendations'");
        assertTrue(result.containsKey("links"), "Result should contain 'links'");

        List<String> summaries = (List<String>) result.get("summaries");
        assertFalse(summaries.isEmpty(), "Summaries list should not be empty");
        assertEquals("Mocked summary", summaries.get(0), "Summary should match mocked response");

        String recommendations = (String) result.get("strategy_recommendations");
        assertNotNull(recommendations, "Strategy recommendations should not be null");
        assertFalse(recommendations.isEmpty(), "Strategy recommendations should not be empty");

        List<String> links = (List<String>) result.get("links");
        assertFalse(links.isEmpty(), "Links list should not be empty");
        assertEquals("https://example.com", links.get(0), "Link should match mocked URL");
    }
}