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
import java.util.Objects;
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
     * Builds RAG pipeline, mirroring build_rag_pipeline (load, split, embed, store,
     * chain).
     * 
     * @param filePath Path to uploaded file (TXT/PDF).
     * @return ConversationalRetrievalChain for QA.
     */
    public ConversationalRetrievalChain buildRagPipeline(String filePath) {
        try {
            // Validate file path
            if (filePath == null || filePath.trim().isEmpty()) {
                throw new IllegalArgumentException("File path is null or empty");
            }

            // Check if file exists
            if (!fileUtil.fileExists(filePath)) {
                throw new IllegalArgumentException("File does not exist: " + filePath);
            }

            // Load document (mirroring loader)
            String text = fileUtil.loadDocumentText(filePath);

            // Validate document content
            if (text == null || text.trim().isEmpty()) {
                throw new IllegalArgumentException("Document text is empty or null for file: " + filePath);
            }

            Document document = Document.from(text);

            // Split into chunks (mirroring RecursiveCharacterTextSplitter)
            DocumentSplitter splitter = DocumentSplitters.recursive(800, 100);
            List<TextSegment> segments = splitter.split(document);

            // Validate segments
            if (segments == null || segments.isEmpty()) {
                throw new IllegalStateException("No text segments created from document");
            }

            // Embed and store (mirroring NVIDIAEmbeddings + Chroma)
            EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
            List<String> texts = segments.stream().map(TextSegment::text).collect(Collectors.toList());

            // Validate text content before embedding
            texts = texts.stream()
                    .filter(t -> t != null && !t.trim().isEmpty())
                    .collect(Collectors.toList());

            if (texts.isEmpty()) {
                throw new IllegalStateException("No valid text content found in document segments");
            }

            List<Embedding> embeddings = embeddingUtil.embedDocuments(texts);

            // Validate embeddings
            if (embeddings == null || embeddings.size() != texts.size()) {
                throw new IllegalStateException("Embedding count mismatch. Expected: " + texts.size() + ", Got: "
                        + (embeddings != null ? embeddings.size() : 0));
            }

            // Store embeddings with their corresponding segments
            for (int i = 0; i < embeddings.size(); i++) {
                if (embeddings.get(i) != null && i < segments.size()) {
                    embeddingStore.add(embeddings.get(i), segments.get(i));
                }
            }

            // Validate AI model
            OllamaChatModel llm = aiUtil.getModel();
            if (llm == null) {
                throw new IllegalStateException("AI model is not available");
            }

            // Validate embedding model
            if (embeddingModel == null) {
                throw new IllegalStateException("Embedding model is not available");
            }

            // Build chain (mirroring RetrievalQA)
            ContentRetriever retriever = new EmbeddingStoreContentRetriever(embeddingStore, embeddingModel);
            return ConversationalRetrievalChain.builder()
                    .chatModel(llm)
                    .contentRetriever(retriever)
                    .build();

        } catch (Exception e) {
            // Log the actual error with more details
            String errorMsg = "Failed to build RAG pipeline for file: " + filePath + ". Error: " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            throw new RuntimeException(errorMsg, e);
        }
    }

    /**
     * Performs competitive analysis workflow, mirroring analyze_competitor and
     * build_analysis_graph (sequential steps).
     * 
     * @param filePath    Path to uploaded file for RAG (optional).
     * @param companyName The company name.
     * @return Map with "company_name", "summaries", "strategy_recommendations",
     *         "links".
     */
    public Map<String, Object> analyzeCompetitor(String filePath, String companyName) {
        ConversationalRetrievalChain ragChain = filePath != null ? buildRagPipeline(filePath) : null;

        // Step 1: Search (mirroring search_step) - Use specific search terms focused on
        // the target company
        List<Map<String, Object>> searchResults = tavilyUtil
                .search("\"" + companyName + "\" company overview business model strategy analysis", 5);
        List<String> links = searchResults.stream().map(r -> (String) r.get("url")).filter(url -> url != null)
                .collect(Collectors.toList());

        // Step 2: Extract (mirroring extract_step) - Filter out failed extractions and
        // focus on target company
        List<String> extractedTexts = links.stream()
                .map(scrapingUtil::extractTextFromUrl)
                .filter(text -> text != null && !text.trim().isEmpty()) // Filter out null/empty extractions
                .map(text -> filterRelevantContent(text, companyName)) // Filter for relevant content
                .filter(text -> !text.trim().isEmpty()) // Remove empty filtered results
                .collect(Collectors.toList());

        // Check if we have any successful extractions
        if (extractedTexts.isEmpty()) {
            // Fallback: provide general analysis without web content
            extractedTexts.add("Limited public information available for " + companyName +
                    ". Analysis based on general market knowledge and provided context.");
        }

        // Step 3: Summarize (mirroring summarize_step)
        List<String> summaries = extractedTexts.stream().map(text -> {
            String template = aiUtil.getSummaryTemplate();
            Map<String, Object> variables = Map.of("company_name", companyName, "content", text);
            String summary = aiUtil.invokeWithTemplate(template, variables);
            return convertMarkdownToHtml(summary); // Convert markdown to HTML
        }).collect(Collectors.toList());

        // Step 4: Strategy recommendations (mirroring strategy_step)
        String strategyRecommendations;
        String combinedSummaries = String.join("\n\n", summaries);

        try {
            if (ragChain != null) {
                // Mirror suggest_strategic_differentiation with RAG
                String ragContext = ragChain
                        .execute("General strategic context, positioning and offerings of our company");

                // Truncate context if too long to avoid timeouts
                if (ragContext.length() > 3000) {
                    ragContext = ragContext.substring(0, 3000) + "...";
                }
                if (combinedSummaries.length() > 2000) {
                    combinedSummaries = combinedSummaries.substring(0, 2000) + "...";
                }

                String template = aiUtil.getDiffWithRagTemplate();
                Map<String, Object> variables = Map.of(
                        "rag_context", ragContext,
                        "competitor_name", companyName,
                        "competitor_summary", combinedSummaries);
                strategyRecommendations = aiUtil.invokeWithTemplate(template, variables);

                // Convert markdown to HTML and format nicely
                strategyRecommendations = convertMarkdownToHtml(strategyRecommendations);
            } else {
                // Fallback without RAG - use a simpler approach
                String simpleTemplate = "You are a strategic consultant. Based on the following competitor analysis of {{competitor_name}}, provide 3 key differentiation strategies:\n\n{{competitor_summary}}";
                Map<String, Object> variables = Map.of(
                        "competitor_name", companyName,
                        "competitor_summary",
                        combinedSummaries.length() > 2000 ? combinedSummaries.substring(0, 2000) + "..."
                                : combinedSummaries);
                strategyRecommendations = aiUtil.invokeWithTemplate(simpleTemplate, variables);
                strategyRecommendations = convertMarkdownToHtml(strategyRecommendations);
            }

        } catch (Exception e) {
            // Fallback strategy if AI call fails
            strategyRecommendations = generateFallbackStrategy(companyName, combinedSummaries);
        }

        // Return mirroring original dict
        Map<String, Object> result = new HashMap<>();
        result.put("company_name", companyName);
        result.put("summaries", summaries);
        result.put("strategy_recommendations", strategyRecommendations);
        result.put("links", links);
        return result;
    }

    /**
     * Converts markdown formatting to HTML
     * 
     * @param text Text with markdown formatting
     * @return Text with HTML formatting
     */
    private String convertMarkdownToHtml(String text) {
        if (text == null)
            return "";

        return text
                // Convert **bold** to <strong>bold</strong>
                .replaceAll("\\*\\*([^\\*]+)\\*\\*", "<strong>$1</strong>")
                // Convert *italic* to <em>italic</em>
                .replaceAll("\\*([^\\*]+)\\*", "<em>$1</em>")
                // Convert ### Header to <h3>Header</h3>
                .replaceAll("###\\s*([^\n]+)", "<h3>$1</h3>")
                // Convert ## Header to <h2>Header</h2>
                .replaceAll("##\\s*([^\n]+)", "<h2>$1</h2>")
                // Convert # Header to <h1>Header</h1>
                .replaceAll("#\\s*([^\n]+)", "<h1>$1</h1>")
                // Convert bullet points * item to • item
                .replaceAll("^\\*\\s+", "• ")
                .replaceAll("\n\\*\\s+", "\n• ")
                // Convert line breaks to <br>
                .replaceAll("\n", "<br>")
                // Clean up multiple <br> tags
                .replaceAll("<br>\\s*<br>", "<br><br>");
    }

    /**
     * Handles RAG queries from RagController, using the RAG pipeline to answer
     * questions based on context.
     *
     * @param query   The user's question.
     * @param context The context for retrieval (file path to build RAG pipeline).
     * @return Map with the answer and input parameters.
     */
    public Map<String, Object> query(String query, String context) {
        Map<String, Object> result = new HashMap<>();
        result.put("query", query);
        result.put("context", context);

        try {
            ConversationalRetrievalChain ragChain;
            if (context != null && fileUtil.fileExists(context)) {
                // If context is a file path, build pipeline from file
                ragChain = buildRagPipeline(context);
            } else {
                // If context is plain text, build pipeline from text
                Document document = Document.from(context);
                DocumentSplitter splitter = DocumentSplitters.recursive(800, 100);
                List<TextSegment> segments = splitter.split(document);

                EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
                List<Embedding> embeddings = embeddingUtil
                        .embedDocuments(segments.stream().map(TextSegment::text).collect(Collectors.toList()));
                for (int i = 0; i < segments.size(); i++) {
                    embeddingStore.add(embeddings.get(i), segments.get(i));
                }

                OllamaChatModel llm = aiUtil.getModel();
                ContentRetriever retriever = new EmbeddingStoreContentRetriever(embeddingStore, embeddingModel);
                ragChain = ConversationalRetrievalChain.builder()
                        .chatModel(llm)
                        .contentRetriever(retriever)
                        .build();
            }
            String answer = ragChain.execute(query);
            result.put("answer", answer);
        } catch (Exception e) {
            result.put("answer", "Error in RAG query: " + e.getMessage());
        }
        return result;
    }

    public Map<String, Object> analyzeCompetitor1(String filePath, String companyName) {
        ConversationalRetrievalChain ragChain = filePath != null ? buildRagPipeline(filePath) : null;

        // Step 1: Search
        List<Map<String, Object>> searchResults = tavilyUtil
                .search("information about " + companyName + " and similar companies business analysis", 3);
        List<String> links = searchResults.stream()
                .map(r -> (String) r.get("url"))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Step 2: Extract
        List<String> extractedTexts = links.stream()
                .map(scrapingUtil::extractTextFromUrl)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Step 3: Summarize (force JSON)
        List<Map<String, Object>> competitorSummaries = extractedTexts.stream()
                .map(text -> {
                    String template = aiUtil.getSummaryTemplate();
                    Map<String, Object> variables = Map.of("company_name", companyName, "content", text);
                    String jsonOutput = aiUtil.invokeWithTemplate(template, variables);
                    return aiUtil.parseJsonToMap(jsonOutput); // <-- implement a JSON parser helper
                })
                .collect(Collectors.toList());

        // Split between main company vs competitors
        Map<String, Object> companySummary = competitorSummaries.stream()
                .filter(s -> companyName.equalsIgnoreCase((String) s.get("name")))
                .findFirst()
                .orElse(Map.of("name", companyName));

        List<Map<String, Object>> competitorList = competitorSummaries.stream()
                .filter(s -> !companyName.equalsIgnoreCase((String) s.get("name")))
                .collect(Collectors.toList());

        // Step 4: Strategy recommendations
        String strategyRecommendations;
        if (ragChain != null) {
            String ragContext = ragChain
                    .execute("General strategic context, positioning and offering of our company");
            String template = aiUtil.getDiffWithRagTemplate();
            Map<String, Object> variables = Map.of(
                    "rag_context", ragContext,
                    "competitor_name", companyName,
                    "competitor_summary", competitorSummaries.toString());
            strategyRecommendations = aiUtil.invokeWithTemplate(template, variables);
        } else {
            strategyRecommendations = "No RAG context available. Summaries only.";
        }

        // Return structured JSON
        Map<String, Object> result = new HashMap<>();
        result.put("company_name", companyName);
        result.put("links", links);
        result.put("summaries", Map.of(
                companyName, companySummary,
                "Competitors", competitorList));
        result.put("strategy_recommendations", strategyRecommendations);

        return result;
    }

    /**
     * Generates a fallback strategy when AI calls fail
     */
    private String generateFallbackStrategy(String companyName, String combinedSummaries) {
        StringBuilder fallback = new StringBuilder();
        fallback.append("<h3>Differentiation Strategy Analysis for ").append(companyName).append("</h3>");
        fallback.append("<p><strong>Analysis Status:</strong> Generated from available market intelligence data.</p>");

        fallback.append("<h4>Market Intelligence Summary:</h4>");
        fallback.append(
                "<div style='background: #f8f9fa; padding: 10px; margin: 10px 0; border-left: 4px solid #007bff;'>");
        fallback.append(convertMarkdownToHtml(
                combinedSummaries.length() > 1000 ? combinedSummaries.substring(0, 1000) + "..." : combinedSummaries));
        fallback.append("</div>");

        fallback.append("<h4>Strategic Recommendations:</h4>");
        fallback.append("<ul>");
        fallback.append(
                "<li><strong>Market Positioning:</strong> Focus on unique value propositions that differentiate from ")
                .append(companyName).append(" and competitors</li>");
        fallback.append(
                "<li><strong>Innovation Strategy:</strong> Leverage gaps identified in competitor analysis to drive innovation</li>");
        fallback.append(
                "<li><strong>Customer Experience:</strong> Develop superior customer experience based on competitor weaknesses</li>");
        fallback.append("</ul>");

        fallback.append(
                "<p><em>Note: For more detailed strategic analysis, please ensure stable AI model connectivity.</em></p>");

        return fallback.toString();
    }

    /**
     * Filter extracted content to focus on the target company
     * and remove content about other companies that might cause confusion
     */
    private String filterRelevantContent(String content, String targetCompany) {
        if (content == null || content.trim().isEmpty()) {
            return content;
        }

        // Split content into sentences and paragraphs
        String[] sentences = content.split("(?<=\\.)\\s+");
        StringBuilder filteredContent = new StringBuilder();

        for (String sentence : sentences) {
            String lowerSentence = sentence.toLowerCase();
            String lowerTarget = targetCompany.toLowerCase();

            // Include sentence if it mentions the target company
            if (lowerSentence.contains(lowerTarget)) {
                filteredContent.append(sentence).append(" ");
            }
            // Include general industry context sentences (without specific company names)
            else if (!containsCompanyNames(lowerSentence) &&
                    (lowerSentence.contains("industry") ||
                            lowerSentence.contains("market") ||
                            lowerSentence.contains("sector") ||
                            lowerSentence.contains("technology") ||
                            lowerSentence.contains("business"))) {
                filteredContent.append(sentence).append(" ");
            }
        }

        // If filtered content is too short, return original content
        String result = filteredContent.toString().trim();
        if (result.length() < 100) {
            return content;
        }

        return result;
    }

    /**
     * Check if a sentence contains common company names that might cause confusion
     */
    private boolean containsCompanyNames(String sentence) {
        String[] commonCompanyNames = {
                "watson", "ibm", "google", "microsoft", "amazon", "apple",
                "facebook", "meta", "netflix", "tesla", "nvidia", "intel",
                "oracle", "salesforce", "adobe", "uber", "airbnb"
        };

        for (String companyName : commonCompanyNames) {
            if (sentence.contains(companyName)) {
                return true;
            }
        }
        return false;
    }

}