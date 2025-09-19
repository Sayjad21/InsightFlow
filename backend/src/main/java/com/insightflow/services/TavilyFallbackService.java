package com.insightflow.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightflow.utils.AiUtil;
import com.insightflow.utils.LinkedInSlugUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class TavilyFallbackService {

    private static final Logger logger = LoggerFactory.getLogger(TavilyFallbackService.class);

    @Value("${tavily.api.key}")
    private String tavilyApiKey;

    @Autowired
    private AiUtil aiUtil;

    @Autowired
    private LinkedInSlugUtil linkedInSlugUtil;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public TavilyFallbackService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Fallback LinkedIn analysis using Tavily crawl API
     * 
     * @param companyName  The name of the company
     * @param linkedinSlug Optional LinkedIn slug. If null, will be generated
     *                     automatically
     */
    public String getLinkedInAnalysisFallback(String companyName, String linkedinSlug) {
        logger.info("====== STARTING TAVILY FALLBACK ANALYSIS FOR: '{}' (slug: '{}') ======", companyName,
                linkedinSlug);
        long analysisStartTime = System.currentTimeMillis();

        try {
            // Step 1: Determine LinkedIn slug
            String actualSlug;
            if (linkedinSlug != null && !linkedinSlug.trim().isEmpty()) {
                actualSlug = linkedinSlug.trim();
                logger.info("Using provided LinkedIn slug: '{}'", actualSlug);
            } else {
                // Use comprehensive LinkedIn slug discovery from LinkedInSlugUtil
                actualSlug = linkedInSlugUtil.getLinkedInCompanySlug(companyName);
                if (actualSlug == null || actualSlug.trim().isEmpty()) {
                    // Fallback to enhanced slug generation
                    actualSlug = linkedInSlugUtil.generateEnhancedFallbackSlug(companyName);
                }
                logger.info("Generated LinkedIn slug using comprehensive discovery: '{}'", actualSlug);
            }

            // Step 2: Crawl LinkedIn company page using Tavily
            String linkedinUrl = "https://www.linkedin.com/company/" + actualSlug + "/";
            logger.info("Crawling LinkedIn URL: {}", linkedinUrl);

            String crawlResponse = crawlLinkedInPage(linkedinUrl);
            if (crawlResponse == null || crawlResponse.isEmpty()) {
                logger.warn("No content retrieved from Tavily crawl, generating minimal analysis");
                return generateMinimalAnalysis(companyName);
            }

            // Step 3: Extract and process content
            String processedContent = extractAndProcessContent(crawlResponse, companyName);
            if (processedContent.length() < 100) {
                logger.warn("Insufficient content extracted, generating minimal analysis");
                return generateMinimalAnalysis(companyName);
            }

            // Step 4: Generate AI analysis
            String analysis = generateAIAnalysis(companyName, processedContent);

            long analysisEndTime = System.currentTimeMillis();
            long totalDuration = analysisEndTime - analysisStartTime;

            logger.info("====== TAVILY FALLBACK ANALYSIS COMPLETED ======");
            logger.info("Company: {}", companyName);
            logger.info("Total Duration: {} ms ({} seconds)", totalDuration, totalDuration / 1000.0);
            logger.info("Content Length: {} characters", analysis.length());

            return "<strong>LinkedIn Analysis of " + companyName + "</strong><br><br>" + analysis;

        } catch (Exception e) {
            logger.error("Tavily fallback analysis failed for {}: {}", companyName, e.getMessage(), e);
            return generateMinimalAnalysis(companyName);
        }
    }

    /**
     * Fallback LinkedIn analysis using Tavily crawl API (legacy method for backward
     * compatibility)
     */
    public String getLinkedInAnalysisFallback(String companyName) {
        return getLinkedInAnalysisFallback(companyName, null);
    }

    /**
     * Crawl LinkedIn company page using Tavily API
     */
    private String crawlLinkedInPage(String linkedinUrl) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("url", linkedinUrl);
            requestBody.put("instructions",
                    "Extract meaningful data about the company. Focus on company description, about section, recent posts, "
                            +
                            "company size, industry information, and key business activities. " +
                            "Ignore LinkedIn login/signup elements, navigation menus, and unrelated content. " +
                            "Prioritize plain text content over images or external URLs.");
            requestBody.put("extract_depth", "advanced");
            requestBody.put("categories", Arrays.asList("About", "Careers", "Enterprise", "Documentation"));

            String jsonPayload = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.tavily.com/crawl"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + tavilyApiKey)
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("Tavily crawl API response status: {}", response.statusCode());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                logger.error("Tavily crawl API error: {} - {}", response.statusCode(), response.body());
                return null;
            }

        } catch (Exception e) {
            logger.error("Failed to crawl LinkedIn page via Tavily: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract and process content from Tavily response
     */
    private String extractAndProcessContent(String crawlResponse, String companyName) {
        try {
            JsonNode responseNode = objectMapper.readTree(crawlResponse);
            StringBuilder processedContent = new StringBuilder();

            // Extract base URL info
            String baseUrl = responseNode.path("base_url").asText();
            if (!baseUrl.isEmpty()) {
                processedContent.append("Source: ").append(baseUrl).append("\n\n");
            }

            // Process results array
            JsonNode resultsNode = responseNode.path("results");
            if (resultsNode.isArray()) {
                for (JsonNode result : resultsNode) {
                    String url = result.path("url").asText();
                    String rawContent = result.path("raw_content").asText();

                    if (!rawContent.isEmpty()) {
                        // Clean and process the raw content
                        String cleanContent = cleanTavilyContent(rawContent, companyName);
                        if (!cleanContent.isEmpty()) {
                            processedContent.append("=== Content from: ").append(url).append(" ===\n");
                            processedContent.append(cleanContent).append("\n\n");
                        }
                    }
                }
            }

            String finalContent = processedContent.toString().trim();

            // Truncate if too long (similar to original scraping service)
            // if (finalContent.length() > 25000) {
            //     finalContent = finalContent.substring(0, 25000) + "... [Truncated for analysis]";
            //     logger.info("Truncated Tavily content to {} chars to avoid LLM timeouts", finalContent.length());
            // }

            logger.info("Processed Tavily content length: {} characters", finalContent.length());
            return finalContent;

        } catch (Exception e) {
            logger.error("Failed to extract content from Tavily response: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Clean Tavily raw content similar to LinkedIn scraping service
     */
    private String cleanTavilyContent(String rawContent, String companyName) {
        if (rawContent == null || rawContent.trim().isEmpty()) {
            return "";
        }

        StringBuilder cleanedContent = new StringBuilder();
        String[] lines = rawContent.split("\n");

        // Patterns to filter out irrelevant content
        Pattern skipPatterns = Pattern.compile(
                "(?i)(log in|sign up|skip to|back to main|menu|navigation|cookie|manage cookies|" +
                        "opens in a new window|subscribe|follow us|download|contact sales|help center|" +
                        "terms of use|privacy policy|home|search|filter|sort by)");

        Pattern relevantPatterns = Pattern.compile(
                "(?i)(about|company|business|mission|vision|values|products|services|team|" +
                        "employees|founded|headquarters|industry|revenue|customers|clients|innovation|" +
                        "technology|leadership|strategy|growth|market|solutions)");

        for (String line : lines) {
            String trimmedLine = line.trim();

            // Skip empty lines
            if (trimmedLine.isEmpty())
                continue;

            // Skip navigation and irrelevant elements
            if (skipPatterns.matcher(trimmedLine).find())
                continue;

            // Skip very short lines (likely navigation elements)
            if (trimmedLine.length() < 10)
                continue;

            // Skip lines that are just symbols or numbers
            if (trimmedLine.matches("^[\\d\\s\\-\\|\\*\\+\\.]+$"))
                continue;

            // Prioritize relevant content
            if (relevantPatterns.matcher(trimmedLine).find()) {
                cleanedContent.append(trimmedLine).append("\n");
            } else if (trimmedLine.length() > 30 && containsBusinessContent(trimmedLine)) {
                // Include longer lines that seem to contain business content
                cleanedContent.append(trimmedLine).append("\n");
            }
        }

        String result = cleanedContent.toString().trim();

        // Remove duplicate lines
        Set<String> uniqueLines = new LinkedHashSet<>();
        for (String line : result.split("\n")) {
            if (!line.trim().isEmpty()) {
                uniqueLines.add(line.trim());
            }
        }

        return String.join("\n", uniqueLines);
    }

    /**
     * Check if line contains business-relevant content
     */
    private boolean containsBusinessContent(String line) {
        String lowerLine = line.toLowerCase();

        // Check for business-related keywords
        String[] businessKeywords = {
                "company", "business", "enterprise", "solution", "service", "product",
                "customer", "client", "market", "industry", "technology", "innovation",
                "team", "employee", "revenue", "growth", "strategy", "mission", "vision"
        };

        for (String keyword : businessKeywords) {
            if (lowerLine.contains(keyword)) {
                return true;
            }
        }

        // Check if it's a substantial sentence (contains verbs and nouns)
        if (line.split("\\s+").length > 8 && line.contains(" ")) {
            return true;
        }

        return false;
    }

    /**
     * Generate AI analysis using FULL strategic pipeline like AnalysisOrchestrationUtil
     */
    private String generateAIAnalysis(String companyName, String processedContent) {
        try {
            // Phase 1: Strategic analysis (same as AnalysisOrchestrationUtil)
            StrategicAnalysisData strategicData = performStrategicAnalysis(companyName, processedContent, extractPostsFromContent(processedContent));
            
            // Phase 2: Content preparation with strategic context (same as AnalysisOrchestrationUtil)
            String optimizedContent = prepareContentForLLMWithStrategicContext(companyName, processedContent, strategicData);
            
            // Phase 3: AI analysis with strategic prompt (same as AnalysisOrchestrationUtil)
            String aiAnalysis = generateStrategicAIAnalysis(optimizedContent, companyName);
            
            // Phase 4: Enhanced formatting (same as AnalysisOrchestrationUtil)
            String finalAnalysis = enhanceAnalysisOutput(aiAnalysis, companyName, strategicData, strategicData.posts.size());
            
            return finalAnalysis;

        } catch (Exception e) {
            logger.error("Strategic AI analysis failed: {}", e.getMessage());
            return generateStrategicFallback(companyName, processedContent);
        }
    }

    /**
     * Data structure to hold strategic analysis results (same as AnalysisOrchestrationUtil)
     */
    private static class StrategicAnalysisData {
        public final String industryContext;
        public final Map<String, List<String>> strategicThemes;
        public final List<String> keyMetrics;
        public final List<String> posts;
        public final String competitivePosition;
        
        public StrategicAnalysisData(String industryContext, Map<String, List<String>> strategicThemes, 
                                    List<String> keyMetrics, List<String> posts, String competitivePosition) {
            this.industryContext = industryContext;
            this.strategicThemes = strategicThemes;
            this.keyMetrics = keyMetrics;
            this.posts = posts;
            this.competitivePosition = competitivePosition;
        }
    }

    /**
     * Phase 1: Strategic analysis (same logic as AnalysisOrchestrationUtil)
     */
    private StrategicAnalysisData performStrategicAnalysis(String companyName, String description, List<String> posts) {
        logger.debug("Phase 1: Performing strategic analysis for: {}", companyName);
        
        // Industry context analysis
        String industryContext = analyzeIndustryContext(companyName, description, posts);
        
        // Strategic themes analysis
        Map<String, List<String>> strategicThemes = analyzeStrategicThemes(posts);
        
        // Key metrics extraction
        List<String> keyMetrics = extractKeyMetrics(description);
        
        // Competitive positioning analysis
        String competitivePosition = analyzeCompetitivePosition(companyName, description, industryContext);
        
        return new StrategicAnalysisData(industryContext, strategicThemes, keyMetrics, posts, competitivePosition);
    }

    /**
     * Phase 2: Content preparation with strategic context (same as AnalysisOrchestrationUtil)
     */
    private String prepareContentForLLMWithStrategicContext(String companyName, String rawContent, StrategicAnalysisData strategicData) {
        StringBuilder content = new StringBuilder();

        // Company basic info with competitive context (same as AnalysisOrchestrationUtil)
        content.append("=== COMPANY PROFILE ===\n");
        content.append("Company: ").append(companyName).append("\n");
        content.append("Industry Context: ").append(strategicData.industryContext).append("\n");
        content.append("Competitive Position: ").append(strategicData.competitivePosition).append("\n");
        content.append("Source: LinkedIn (via Tavily crawl API)\n\n");

        // Company description with key metrics extraction (same as AnalysisOrchestrationUtil)
        content.append("=== COMPANY DESCRIPTION ===\n");
        String enhancedDescription = rawContent.length() > 1000 ? rawContent.substring(0, 1000) + "..." : rawContent;
        content.append(enhancedDescription).append("\n");
        
        if (!strategicData.keyMetrics.isEmpty()) {
            content.append("\n=== KEY METRICS IDENTIFIED ===\n");
            for (String metric : strategicData.keyMetrics) {
                content.append("• ").append(metric).append("\n");
            }
        }
        content.append("\n");

        // Strategic activities analysis (same as AnalysisOrchestrationUtil)
        if (!strategicData.posts.isEmpty()) {
            content.append("=== STRATEGIC ACTIVITIES ANALYSIS ===\n");
            content.append("Total strategic activities analyzed: ").append(strategicData.posts.size()).append("\n\n");

            // Group by strategic themes (same as AnalysisOrchestrationUtil)
            for (Map.Entry<String, List<String>> theme : strategicData.strategicThemes.entrySet()) {
                content.append("STRATEGIC THEME - ").append(theme.getKey().toUpperCase()).append(":\n");
                for (String post : theme.getValue()) {
                    content.append("• ").append(extractStrategicInsight(post)).append("\n");
                }
                content.append("\n");
            }
        } else {
            content.append("=== ACTIVITIES ===\n");
            content.append("No recent strategic activities identified in available content\n\n");
        }

        return content.toString();
    }

    /**
     * Phase 3: AI analysis with strategic prompt (same as AnalysisOrchestrationUtil)
     */
    private String generateStrategicAIAnalysis(String optimizedContent, String companyName) {
        // Use complex strategic prompt (same as AnalysisOrchestrationUtil)
        String prompt = String.format("""
            Analyze this LinkedIn company profile and provide a comprehensive strategic business analysis.
            Focus on competitive positioning, market strategy, and key insights for business intelligence.
            
            STRATEGIC ANALYSIS FRAMEWORK:
            1. **Executive Summary**: Key strategic insights and market position
            2. **Competitive Intelligence**: Market positioning vs competitors and strategic advantages
            3. **Business Strategy Analysis**: Core strategies, initiatives, and strategic direction
            4. **Growth & Innovation**: Innovation focus, growth strategies, and future opportunities
            5. **Strategic Recommendations**: Actionable insights and strategic opportunities
            
            ANALYSIS REQUIREMENTS:
            - Provide specific, actionable business intelligence
            - Focus on strategic implications and competitive dynamics
            - Identify growth opportunities and market positioning
            - Assess strategic strengths and potential vulnerabilities
            - Include competitive benchmarking where relevant
            
            COMPANY: %s
            
            STRATEGIC CONTENT TO ANALYZE:
            %s
            
            Provide a detailed strategic analysis with specific insights for executive decision-making.
            """, companyName, optimizedContent);

        return aiUtil.invoke(prompt);
    }

    /**
     * Phase 4: Enhanced formatting (same as AnalysisOrchestrationUtil)
     */
    private String enhanceAnalysisOutput(String aiAnalysis, String companyName, StrategicAnalysisData strategicData, int postCount) {
        StringBuilder enhanced = new StringBuilder();

        // Executive summary header with styling (same as AnalysisOrchestrationUtil)
        enhanced.append("<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); ")
                .append("color: white; padding: 20px; border-radius: 10px; margin-bottom: 20px;'>");
        enhanced.append("<h2 style='margin: 0; font-size: 24px;'>📊 Strategic Analysis: ").append(companyName).append("</h2>");
        enhanced.append("<p style='margin: 10px 0 0 0; opacity: 0.9;'>LinkedIn Intelligence Report (Tavily Fallback)</p>");
        enhanced.append("</div>");

        // Process and format the AI analysis (same as AnalysisOrchestrationUtil)
        String formattedAnalysis = formatAdvancedHtml(aiAnalysis);
        enhanced.append(formattedAnalysis);

        // Strategic activity summary section (same as AnalysisOrchestrationUtil)
        if (!strategicData.strategicThemes.isEmpty()) {
            enhanced.append("<div style='margin-top: 30px; padding: 20px; background-color: #f8f9fa; border-radius: 8px;'>");
            enhanced.append("<h3 style='color: #495057; margin-top: 0;'>🎯 Strategic Activity Summary</h3>");
            
            for (Map.Entry<String, List<String>> theme : strategicData.strategicThemes.entrySet()) {
                enhanced.append("<p><strong>").append(theme.getKey()).append(":</strong> ");
                enhanced.append(theme.getValue().size()).append(" activities identified</p>");
            }
            enhanced.append("</div>");
        }

        // Metadata footer (same as AnalysisOrchestrationUtil)
        enhanced.append("<div style='margin-top: 30px; padding: 15px; background-color: #fff3cd; border-radius: 5px; font-size: 14px; color: #856404;'>");
        enhanced.append("<strong>Analysis Metadata:</strong> ");
        enhanced.append("Source: Tavily Crawl API | ");
        enhanced.append("Strategic Activities: ").append(postCount).append(" | ");
        enhanced.append("Industry: ").append(strategicData.industryContext).append(" | ");
        enhanced.append("Analysis Method: Strategic Intelligence Pipeline");
        enhanced.append("</div>");

        return enhanced.toString();
    }

    /**
     * Extract posts from content for strategic analysis
     */
    private List<String> extractPostsFromContent(String content) {
        List<String> posts = new ArrayList<>();
        String[] lines = content.split("\n");
        
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.length() > 50 && isStrategicallySignificant(trimmedLine)) {
                posts.add(trimmedLine);
            }
        }
        
        return posts.stream()
                .distinct()
                .limit(10)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Analyze industry context (same logic as AnalysisOrchestrationUtil)
     */
    private String analyzeIndustryContext(String companyName, String description, List<String> posts) {
        String combined = (companyName + " " + description + " " + String.join(" ", posts)).toLowerCase();

        if (combined.contains("artificial intelligence") || combined.contains(" ai ") ||
                combined.contains("machine learning") || combined.contains("gpt") || combined.contains("llm")) {
            return "AI/ML Technology - competing with OpenAI, Google, Microsoft, Anthropic";
        }
        if (combined.contains("cloud") || combined.contains("azure") || combined.contains("aws")) {
            return "Cloud Computing - competing with AWS, Microsoft Azure, Google Cloud";
        }
        if (combined.contains("social") || combined.contains("platform") || combined.contains("network")) {
            return "Social Media/Platforms - competing with Meta, Twitter/X, TikTok, LinkedIn";
        }
        if (combined.contains("search") || combined.contains("advertising") || combined.contains("marketing")) {
            return "Digital Advertising - competing with Google, Meta, Amazon, Microsoft";
        }
        if (combined.contains("electric") || combined.contains("automotive") || combined.contains("vehicle")) {
            return "Electric Vehicles - competing with Tesla, BYD, Toyota, Volkswagen";
        }
        if (combined.contains("entertainment") || combined.contains("streaming") || combined.contains("media")) {
            return "Digital Media - competing with Netflix, Disney, Amazon Prime, Apple";
        }

        return "Technology sector with various digital services";
    }

    /**
     * Analyze strategic themes (same logic as AnalysisOrchestrationUtil)
     */
    private Map<String, List<String>> analyzeStrategicThemes(List<String> posts) {
        Map<String, List<String>> themes = new HashMap<>();

        for (String post : posts) {
            String postLower = post.toLowerCase();

            if (postLower.contains("acquisition") || postLower.contains("merger") || postLower.contains("investment")) {
                themes.computeIfAbsent("Market Consolidation", k -> new ArrayList<>()).add(post);
            } else if (postLower.contains("partnership") || postLower.contains("alliance") || postLower.contains("collaboration")) {
                themes.computeIfAbsent("Strategic Partnerships", k -> new ArrayList<>()).add(post);
            } else if (postLower.contains("launch") || postLower.contains("release") || postLower.contains("introducing")) {
                themes.computeIfAbsent("Product Innovation", k -> new ArrayList<>()).add(post);
            } else if (postLower.contains("expansion") || postLower.contains("global") || postLower.contains("international") || postLower.contains("market")) {
                themes.computeIfAbsent("Market Expansion", k -> new ArrayList<>()).add(post);
            } else if (postLower.contains("competition") || postLower.contains("vs") || postLower.contains("versus") || postLower.contains("challenge")) {
                themes.computeIfAbsent("Competitive Positioning", k -> new ArrayList<>()).add(post);
            } else {
                themes.computeIfAbsent("Operational Updates", k -> new ArrayList<>()).add(post);
            }
        }

        // Limit posts per theme (same as AnalysisOrchestrationUtil)
        themes.replaceAll((theme, postList) -> postList.stream().limit(3).collect(java.util.stream.Collectors.toList()));

        return themes;
    }

    /**
     * Extract key metrics (same logic as AnalysisOrchestrationUtil)
     */
    private List<String> extractKeyMetrics(String description) {
        List<String> metrics = new ArrayList<>();
        String desc = description.toLowerCase();
        
        if (desc.contains("countries")) {
            metrics.add("Global presence across multiple countries");
        }
        if (desc.contains("employees") || desc.contains("people")) {
            metrics.add("Workforce size mentioned");
        }
        if (desc.contains("billion") || desc.contains("million")) {
            metrics.add("Large-scale operations (billion/million scale)");
        }
        if (desc.contains("customers") || desc.contains("users")) {
            metrics.add("Customer/user base mentioned");
        }
        
        return metrics;
    }

    /**
     * Analyze competitive position
     */
    private String analyzeCompetitivePosition(String companyName, String description, String industryContext) {
        String combined = (companyName + " " + description).toLowerCase();
        
        if (combined.contains("leading") || combined.contains("leader") || combined.contains("number one")) {
            return "Market leader position claimed";
        }
        if (combined.contains("innovative") || combined.contains("pioneer") || combined.contains("first")) {
            return "Innovation-focused positioning";
        }
        if (combined.contains("enterprise") || combined.contains("b2b") || combined.contains("business")) {
            return "Enterprise-focused market position";
        }
        
        return "Competitive market participant";
    }

    /**
     * Check if content is strategically significant (same logic as AnalysisOrchestrationUtil)
     */
    private boolean isStrategicallySignificant(String text) {
        String textLower = text.toLowerCase();
        return textLower.contains("launch") || textLower.contains("partnership") ||
               textLower.contains("expansion") || textLower.contains("acquisition") ||
               textLower.contains("investment") || textLower.contains("milestone") ||
               textLower.contains("market") || textLower.contains("growth") ||
               textLower.contains("competition") || textLower.contains("strategy") ||
               textLower.contains("billion") || textLower.contains("million") ||
               textLower.contains("global") || textLower.contains("international");
    }

    /**
     * Extract strategic insight from content (same logic as AnalysisOrchestrationUtil)
     */
    private String extractStrategicInsight(String text) {
        String cleaned = text.replaceAll("\\s+", " ").trim();
        
        if (cleaned.length() <= 150) {
            return cleaned;
        }
        
        // Extract first sentence plus strategic elements
        String[] sentences = cleaned.split("\\. ");
        StringBuilder insight = new StringBuilder();
        
        if (sentences.length > 0) {
            insight.append(sentences[0]);
            if (!sentences[0].endsWith(".")) {
                insight.append(".");
            }
        }
        
        return insight.toString();
    }

    /**
     * Format advanced HTML (same as AnalysisOrchestrationUtil)
     */
    private String formatAdvancedHtml(String analysis) {
        return analysis
                .replaceAll("\\*\\*(.*?)\\*\\*", "<strong>$1</strong>")
                .replaceAll("\\*(.*?)\\*", "<em>$1</em>")
                .replaceAll("(?m)^#{1,6}\\s*(.*?)$", "<h3 style='color: #495057; margin-top: 25px; margin-bottom: 15px;'>$1</h3>")
                .replaceAll("(?m)^-\\s*(.*?)$", "<li style='margin-bottom: 8px;'>$1</li>")
                .replaceAll("(<li.*?</li>\\s*)+", "<ul style='padding-left: 20px; margin-bottom: 15px;'>$0</ul>")
                .replaceAll("\\n\\n", "</p><p style='margin-bottom: 15px; line-height: 1.6;'>")
                .replaceAll("^", "<p style='margin-bottom: 15px; line-height: 1.6;'>")
                .replaceAll("$", "</p>");
    }

    /**
     * Generate strategic fallback (enhanced version)
     */
    private String generateStrategicFallback(String companyName, String content) {
        // Use the strategic analysis structure even for fallback
        StrategicAnalysisData minimalData = new StrategicAnalysisData(
            "Technology sector", 
            new HashMap<>(), 
            new ArrayList<>(), 
            new ArrayList<>(), 
            "Market participant"
        );
        
        return enhanceAnalysisOutput(
            "Strategic analysis could not be completed due to limited data availability. " +
            "Company: " + companyName + " operates in the technology sector with limited public information available.",
            companyName, 
            minimalData, 
            0
        );
    }

    /**
     * Generate minimal analysis when Tavily crawl fails completely
     */
    private String generateMinimalAnalysis(String companyName) {
        logger.warn("Generating minimal analysis for {} due to crawl failure", companyName);
        
        StringBuilder minimal = new StringBuilder();
        minimal.append("<strong>LinkedIn Analysis of ").append(companyName).append("</strong><br><br>");
        minimal.append("<div style='background-color: #fff3cd; padding: 15px; border-radius: 5px; margin: 10px 0;'>");
        minimal.append("<h4 style='color: #856404; margin-top: 0;'>⚠️ Analysis Limitation</h4>");
        minimal.append("<p style='color: #856404; margin-bottom: 0;'>");
        minimal.append("Unable to retrieve detailed LinkedIn company information for <strong>").append(companyName).append("</strong>. ");
        minimal.append("This may be due to privacy settings, incorrect company slug, or temporary access issues.</p>");
        minimal.append("</div>");
        
        minimal.append("<h3>📊 Available Information</h3>");
        minimal.append("<p><strong>Company:</strong> ").append(companyName).append("</p>");
        minimal.append("<p><strong>Analysis Method:</strong> Tavily Fallback Service</p>");
        minimal.append("<p><strong>Status:</strong> Limited data available</p>");
        
        minimal.append("<h3>🔍 Recommendations</h3>");
        minimal.append("<ul>");
        minimal.append("<li>Verify the company name spelling and format</li>");
        minimal.append("<li>Check if the company has a public LinkedIn page</li>");
        minimal.append("<li>Try alternative company name variations</li>");
        minimal.append("<li>Consider using additional data sources for comprehensive analysis</li>");
        minimal.append("</ul>");
        
        return minimal.toString();
    }
}