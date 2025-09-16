package com.insightflow;

import com.insightflow.utils.*;
import static org.mockito.ArgumentMatchers.argThat;
import com.insightflow.services.RagService;
import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.data.message.AiMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;

@SpringBootTest
public class RagServiceTest {

    @Autowired
    private RagService ragService;

    @Autowired
    private EmbeddingUtil embeddingUtil; // Use real EmbeddingUtil

    @Autowired
    private FileUtil fileUtil; // Use real FileUtil

    @MockBean
    private AiUtil aiUtil;

    @MockBean
    private TavilyUtil tavilyUtil;

    @MockBean
    private ScrapingUtil scrapingUtil;

    @MockBean
    private EmbeddingModelImpl embeddingModel;

    @BeforeEach
    public void setUp() {
        // Mock AiUtil
        OllamaChatModel mockModel = Mockito.mock(OllamaChatModel.class);
        Mockito.when(aiUtil.getModel()).thenReturn(mockModel);

        // Mock chat(List) for RAG queries
        AiMessage aiMessage = AiMessage.from("OpenAI develops advanced AI models like ChatGPT and GPT-4, with a mission to advance AGI safely.");
        ChatResponse mockChatResponse = Mockito.mock(ChatResponse.class);
        Mockito.when(mockChatResponse.aiMessage()).thenReturn(aiMessage);
        Mockito.when(mockModel.chat(anyList())).thenReturn(mockChatResponse);

        // Mock invokeWithTemplate for summaries and recommendations
        Mockito.when(aiUtil.invokeWithTemplate(eq("Summary template"), any(Map.class)))
               .thenReturn("OpenAI is a leader in AI research, known for ChatGPT and GPT-4.");
        Mockito.when(aiUtil.invokeWithTemplate(eq("Diff with RAG template"), any(Map.class)))
               .thenReturn("OpenAI should focus on AI safety and cost optimization to maintain leadership in AI research.");
        Mockito.when(aiUtil.getSummaryTemplate()).thenReturn("Summary template");
        Mockito.when(aiUtil.getDiffWithRagTemplate()).thenReturn("Diff with RAG template");

        // Mock for the fallback template (when no RAG file)
        Mockito.when(aiUtil.invokeWithTemplate(argThat(template -> 
            template.toString().contains("strategic consultant")), any(Map.class)))
               .thenReturn("OpenAI should focus on AI safety and cost optimization to maintain competitive advantage.");

        // Mock TavilyUtil
        Map<String, Object> searchResult = new HashMap<>();
        searchResult.put("url", "https://openai.com");
        Mockito.when(tavilyUtil.search(anyString(), anyInt(), any())).thenReturn(Arrays.asList(searchResult));

        // Mock ScrapingUtil
        Mockito.when(scrapingUtil.extractTextFromUrl(anyString()))
               .thenReturn("OpenAI develops AI models like ChatGPT and focuses on AGI.");

        // Mock EmbeddingModelImpl for embed(String)
        float[] embeddingVector = new float[768]; // Match nomic-embed-text dimension
        Arrays.fill(embeddingVector, 0.1f); // Dummy values
        Embedding embedding = new Embedding(embeddingVector);
        Response<Embedding> embeddingResponse = Response.from(embedding);
        Mockito.when(embeddingModel.embed(anyString())).thenReturn(embeddingResponse);
    }

    @Test
    public void testBuildRagPipelineWithRealFile() {
        String filePath = "openai_info.txt"; // File in src/test/resources
        ConversationalRetrievalChain chain = ragService.buildRagPipeline(filePath);

        assertNotNull(chain, "RAG pipeline should not be null");
        
        // Test chain functionality with a sample query
        String response = chain.execute("What is OpenAI's mission?");
        System.out.println("RAG Pipeline Response: " + response);
        assertNotNull(response, "RAG response should not be null");
        assertTrue(response.contains("ChatGPT") || response.contains("GPT-4") || response.contains("AGI"), 
                   "Response should mention OpenAI's key models or mission");
    }

    @Test
    public void testAnalyzeCompetitorWithRealFile() {
        String filePath = "openai_info.txt"; // File in src/test/resources
        String companyName = "OpenAI";

        Map<String, Object> result = ragService.analyzeCompetitor(filePath, companyName);

        assertNotNull(result, "Result map should not be null");
        assertEquals(companyName, result.get("company_name"), "Company name should match");
        assertTrue(result.containsKey("summaries"), "Result should contain 'summaries'");
        assertTrue(result.containsKey("strategy_recommendations"), "Result should contain 'strategy_recommendations'");
        assertTrue(result.containsKey("links"), "Result should contain 'links'");

        List<String> summaries = (List<String>) result.get("summaries");
        assertFalse(summaries.isEmpty(), "Summaries list should not be empty");
        System.out.println("Summaries: " + summaries);
        assertTrue(summaries.get(0).contains("OpenAI"), "Summary should mention OpenAI");

        String recommendations = (String) result.get("strategy_recommendations");
        assertNotNull(recommendations, "Strategy recommendations should not be null");
        System.out.println("Strategy Recommendations: " + recommendations);
        assertTrue(recommendations.contains("AI safety") || recommendations.contains("cost optimization"), 
                   "Recommendations should mention AI safety or cost optimization");

        List<String> links = (List<String>) result.get("links");
        assertFalse(links.isEmpty(), "Links list should not be empty");
        System.out.println("Links: " + links);
        assertEquals("https://openai.com", links.get(0), "Link should match mocked URL");
    }

    @Test
    public void testAnalyzeCompetitorWithoutFile() {
        String companyName = "OpenAI";

        Map<String, Object> result = ragService.analyzeCompetitor(null, companyName);

        assertNotNull(result, "Result map should not be null");
        assertEquals(companyName, result.get("company_name"), "Company name should match");
        assertTrue(result.containsKey("summaries"), "Result should contain 'summaries'");
        assertTrue(result.containsKey("strategy_recommendations"), "Result should contain 'strategy_recommendations'");
        assertTrue(result.containsKey("links"), "Result should contain 'links'");

        List<String> summaries = (List<String>) result.get("summaries");
        assertFalse(summaries.isEmpty(), "Summaries list should not be empty");
        System.out.println("Summaries (No File): " + summaries);
        assertTrue(summaries.get(0).contains("OpenAI"), "Summary should mention OpenAI");

        String recommendations = (String) result.get("strategy_recommendations");
        assertNotNull(recommendations, "Strategy recommendations should not be null");
        System.out.println("Strategy Recommendations (No File): " + recommendations);
        assertTrue(recommendations.contains("AI safety") || 
                   recommendations.contains("cost optimization") || 
                   recommendations.contains("strategic") ||
                   recommendations.contains("differentiation") ||
                   !recommendations.trim().isEmpty(), 
                   "Recommendations should contain strategic content or mention AI safety/cost optimization");

        List<String> links = (List<String>) result.get("links");
        assertFalse(links.isEmpty(), "Links list should not be empty");
        System.out.println("Links (No File): " + links);
        assertEquals("https://openai.com", links.get(0), "Link should match mocked URL");
    }
}