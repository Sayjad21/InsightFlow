package com.insightflow.services;

import com.insightflow.utils.AiUtil;
import com.insightflow.utils.EmbeddingUtil;
import com.insightflow.utils.FileUtil;
import com.insightflow.utils.GeminiEmbeddingModel;
import com.insightflow.utils.ScrapingUtil;
import com.insightflow.utils.TavilyUtil;
import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.language.LanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RagService {

    @Autowired
    private AiUtil aiUtil;

    @Autowired
    private TavilyUtil tavilyUtil;

    @Autowired
    private ScrapingUtil scrapingUtil;

    @Autowired
    private EmbeddingUtil embeddingUtil;

    @Autowired
    private FileUtil fileUtil;

    @Autowired
    private GeminiEmbeddingModel embeddingModel;

    @Value("${ollama.base.url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${ollama.model:llama3}")
    private String ollamaModel;

    /**
     * Builds RAG pipeline, mirroring build_rag_pipeline (load, split, embed, store, chain).
     * @param filePath Path to uploaded file (TXT/PDF).
     * @return ConversationalRetrievalChain for QA.
     */
    public ConversationalRetrievalChain buildRagPipeline(String filePath) {
        try {
            // Load document (mirroring loader)
            String text = fileUtil.loadDocumentText(filePath);
            Document document = Document.from(text);

            // Split into chunks (mirroring RecursiveCharacterTextSplitter)
            DocumentSplitter splitter = DocumentSplitters.recursive(800, 100);
            List<TextSegment> segments = splitter.split(document);

            // Embed and store (mirroring NVIDIAEmbeddings + Chroma)
            EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
            List<Embedding> embeddings = embeddingUtil.embedDocuments(segments.stream().map(TextSegment::text).collect(Collectors.toList()));
            for (int i = 0; i < segments.size(); i++) {
                embeddingStore.add(embeddings.get(i), segments.get(i));
            }

            // Build chain (mirroring RetrievalQA)
            OllamaChatModel llm = aiUtil.getModel(); // Ollama from AiUtil
            ContentRetriever retriever = new EmbeddingStoreContentRetriever(embeddingStore, embeddingModel);
            return ConversationalRetrievalChain.builder()
                    .chatModel(llm)
                    .contentRetriever(retriever)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build RAG pipeline", e);
        }
    }

    /**
     * Performs competitive analysis workflow, mirroring analyze_competitor and build_analysis_graph (sequential steps).
     * @param filePath Path to uploaded file for RAG (optional).
     * @param companyName The company name.
     * @return Map with "company_name", "summaries", "strategy_recommendations", "links".
     */
    public Map<String, Object> analyzeCompetitor(String filePath, String companyName) {
        ConversationalRetrievalChain ragChain = filePath != null ? buildRagPipeline(filePath) : null;

        // Step 1: Search (mirroring search_step)
        List<Map<String, Object>> searchResults = tavilyUtil.search("informations sur " + companyName + " et entreprises similaires secteur santé digital", 3);
        List<String> links = searchResults.stream().map(r -> (String) r.get("url")).filter(url -> url != null).collect(Collectors.toList());

        // Step 2: Extract (mirroring extract_step)
        List<String> extractedTexts = links.stream().map(scrapingUtil::extractTextFromUrl).collect(Collectors.toList());

        // Step 3: Summarize (mirroring summarize_step)
        List<String> summaries = extractedTexts.stream().map(text -> {
            String template = aiUtil.getSummaryTemplate();
            Map<String, Object> variables = Map.of("company_name", companyName, "content", text);
            return aiUtil.invokeWithTemplate(template, variables);
        }).collect(Collectors.toList());

        // Step 4: Strategy recommendations (mirroring strategy_step)
        String strategyRecommendations;
        String combinedSummaries = String.join("\n\n", summaries);
        if (ragChain != null) {
            // Mirror suggest_strategic_differentiation with RAG
            String ragContext = ragChain.execute("Contexte stratégique général, positionnement et offre de notre entreprise");
            String template = aiUtil.getDiffWithRagTemplate();
            Map<String, Object> variables = Map.of(
                    "rag_context", ragContext,
                    "competitor_name", companyName,
                    "competitor_summary", combinedSummaries
            );
            strategyRecommendations = aiUtil.invokeWithTemplate(template, variables);
        } else {
            strategyRecommendations = combinedSummaries; // Fallback if no RAG
        }

        // Return mirroring original dict
        Map<String, Object> result = new HashMap<>();
        result.put("company_name", companyName);
        result.put("summaries", summaries);
        result.put("strategy_recommendations", strategyRecommendations);
        result.put("links", links);
        return result;
    }
}