package com.insightflow.utils;

import com.insightflow.utils.AiUtil;
import com.insightflow.utils.IndustryContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for orchestrating AI analysis workflows and strategic insights
 * generation.
 * Handles complex analysis coordination, industry context generation, and
 * strategic content preparation.
 */
@Component
public class AnalysisOrchestrationUtil {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisOrchestrationUtil.class);

    @Autowired
    private AiUtil aiUtil;

    @Autowired
    private IndustryContextUtil industryContextUtil;

    /**
     * Data class to hold strategic analysis results
     */
    public static class StrategicAnalysis {
        public final String industryContext;
        public final String keyMetrics;
        public final Map<String, List<String>> strategicThemes;
        public final List<String> strategicInsights;
        public final String formattedAnalysis;

        public StrategicAnalysis(String industryContext, String keyMetrics,
                Map<String, List<String>> strategicThemes,
                List<String> strategicInsights, String formattedAnalysis) {
            this.industryContext = industryContext;
            this.keyMetrics = keyMetrics;
            this.strategicThemes = strategicThemes;
            this.strategicInsights = strategicInsights;
            this.formattedAnalysis = formattedAnalysis;
        }
    }

    /**
     * Orchestrates comprehensive AI analysis of LinkedIn content
     * 
     * @param companyName The company name
     * @param profileName LinkedIn profile name (may differ from company name)
     * @param description Company description
     * @param posts       List of company posts
     * @return Complete AI analysis result
     */
    public String orchestrateLinkedInAnalysis(String companyName, String profileName,
            String description, List<String> posts) {
        logger.info("Starting AI analysis orchestration for company: {}", companyName);

        try {
            // Phase 1: Prepare strategic analysis components
            StrategicAnalysis strategicData = performStrategicAnalysis(companyName, description, posts);

            // Phase 2: Prepare optimized content for LLM
            String optimizedContent = prepareContentForLLM(companyName, profileName,
                    description, posts, strategicData);

            // Phase 3: Generate AI analysis
            String aiAnalysis = generateAIAnalysis(optimizedContent, companyName);

            // Phase 4: Enhance and format final result
            String finalAnalysis = enhanceAnalysisOutput(aiAnalysis, companyName, strategicData, posts.size());

            logger.info("AI analysis orchestration completed successfully for: {}", companyName);
            return finalAnalysis;

        } catch (Exception e) {
            logger.error("AI analysis orchestration failed for {}: {}", companyName, e.getMessage(), e);
            return createComprehensiveFallback(companyName, description, posts.size(), profileName);
        }
    }

    /**
     * Performs comprehensive strategic analysis of company data
     */
    private StrategicAnalysis performStrategicAnalysis(String companyName, String description, List<String> posts) {
        logger.debug("Performing strategic analysis for: {}", companyName);

        // Generate industry context
        String industryContext = getIndustryContext(companyName, description, posts);

        // Extract key metrics
        String keyMetrics = extractKeyMetrics(description);

        // Analyze strategic themes
        List<String> strategicPosts = prioritizeStrategicPosts(posts);
        Map<String, List<String>> strategicThemes = analyzeStrategicThemes(strategicPosts);

        // Extract strategic insights
        List<String> strategicInsights = strategicPosts.stream()
                .map(this::extractStrategicInsight)
                .collect(Collectors.toList());

        // Format comprehensive analysis
        String formattedAnalysis = formatStrategicAnalysis(companyName, industryContext,
                keyMetrics, strategicThemes, strategicInsights);

        return new StrategicAnalysis(industryContext, keyMetrics, strategicThemes,
                strategicInsights, formattedAnalysis);
    }

    /**
     * Prepares optimized content for LLM analysis with strategic context
     */
    private String prepareContentForLLM(String companyName, String profileName, String description,
            List<String> posts, StrategicAnalysis strategicData) {
        StringBuilder content = new StringBuilder();

        // Company basic info with competitive context
        content.append("=== COMPANY PROFILE ===\n");
        content.append("Company: ").append(companyName).append("\n");

        if (!profileName.equals(companyName)) {
            content.append("LinkedIn Profile: ").append(profileName).append("\n");
        }

        // Add industry context for competitive analysis
        if (!strategicData.industryContext.isEmpty()) {
            content.append("Industry Context: ").append(strategicData.industryContext).append("\n");
        }
        content.append("\n");

        // Company description with key metrics extraction
        if (!description.isEmpty()) {
            content.append("=== COMPANY DESCRIPTION ===\n");
            content.append(strategicData.keyMetrics.isEmpty() ? description : strategicData.keyMetrics).append("\n\n");
        }

        // Strategic post analysis (deduplicated and prioritized)
        if (!posts.isEmpty()) {
            content.append("=== STRATEGIC ACTIVITIES ANALYSIS ===\n");
            content.append("Total posts analyzed: ").append(posts.size()).append("\n\n");

            // Add strategic themes analysis
            for (Map.Entry<String, List<String>> theme : strategicData.strategicThemes.entrySet()) {
                content.append("STRATEGIC THEME - ").append(theme.getKey().toUpperCase()).append(":\n");
                for (String post : theme.getValue()) {
                    content.append("• ").append(extractStrategicInsight(post)).append("\n");
                }
                content.append("\n");
            }
        } else {
            content.append("=== ACTIVITIES ===\n");
            content.append("No recent LinkedIn posts available for analysis\n\n");
        }

        return content.toString();
    }

    /**
     * Generates AI analysis using the prepared content
     */
    private String generateAIAnalysis(String optimizedContent, String companyName) {
        logger.debug("Generating AI analysis for: {}", companyName);

        String prompt = String.format("""
                Analyze this LinkedIn company profile and provide a comprehensive strategic business analysis.
                Focus on competitive positioning, market strategy, and key insights for business intelligence.

                ANALYSIS REQUIREMENTS:
                1. **Strategic Market Position**: Company's market positioning and competitive advantages
                2. **Business Strategy Insights**: Key strategic initiatives and market approach
                3. **Competitive Intelligence**: How they position against competitors
                4. **Growth & Innovation**: Innovation focus and growth strategies
                5. **Market Opportunities**: Potential opportunities and strategic recommendations

                CONTENT TO ANALYZE:
                %s

                Provide a structured analysis with clear business insights and strategic recommendations.
                Focus on actionable intelligence that would be valuable for competitive analysis.
                """, optimizedContent);

        try {
            String analysis = aiUtil.invoke(prompt);
            if (analysis != null && !analysis.trim().isEmpty()) {
                logger.debug("Successfully generated AI analysis for: {}", companyName);
                return analysis;
            } else {
                logger.warn("AI generated empty analysis for: {}", companyName);
                throw new RuntimeException("Empty AI analysis response");
            }
        } catch (Exception e) {
            logger.error("AI analysis generation failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Enhances and formats the final analysis output
     */
    private String enhanceAnalysisOutput(String aiAnalysis, String companyName,
            StrategicAnalysis strategicData, int postCount) {
        StringBuilder enhanced = new StringBuilder();

        // Add executive summary header
        enhanced.append("<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); ")
                .append("color: white; padding: 20px; border-radius: 10px; margin-bottom: 20px;'>");
        enhanced.append("<h2 style='margin: 0; font-size: 24px;'>📊 Strategic Analysis: ")
                .append(companyName).append("</h2>");
        enhanced.append("<p style='margin: 10px 0 0 0; opacity: 0.9;'>Comprehensive LinkedIn Intelligence Report</p>");
        enhanced.append("</div>");

        // Add AI analysis content
        enhanced.append(formatAnalysisForHtml(aiAnalysis));

        // Add strategic data summary
        if (!strategicData.strategicThemes.isEmpty()) {
            enhanced.append("<div style='margin-top: 30px; padding: 20px; ")
                    .append("background-color: #f8f9fa; border-radius: 8px;'>");
            enhanced.append("<h3 style='color: #495057; margin-top: 0;'>📈 Strategic Activity Summary</h3>");

            for (Map.Entry<String, List<String>> theme : strategicData.strategicThemes.entrySet()) {
                enhanced.append("<div style='margin-bottom: 15px;'>");
                enhanced.append("<strong style='color: #6c757d;'>").append(theme.getKey()).append(":</strong> ");
                enhanced.append(theme.getValue().size()).append(" strategic activities identified");
                enhanced.append("</div>");
            }
            enhanced.append("</div>");
        }

        // Add metadata footer
        enhanced.append("<div style='margin-top: 30px; padding: 15px; ")
                .append("background-color: #e9ecef; border-radius: 5px; font-size: 14px; color: #6c757d;'>");
        enhanced.append("<strong>Analysis Metadata:</strong> ");
        enhanced.append("Posts Analyzed: ").append(postCount).append(" | ");
        enhanced.append("Strategic Themes: ").append(strategicData.strategicThemes.size()).append(" | ");
        enhanced.append("Industry Context: ")
                .append(!strategicData.industryContext.isEmpty() ? "Available" : "Limited");
        enhanced.append("</div>");

        return enhanced.toString();
    }

    /**
     * Provides general industry context based on company description and posts
     * Uses centralized IndustryContextUtil for consistent analysis
     */
    private String getIndustryContext(String companyName, String description, List<String> posts) {
        return industryContextUtil.getIndustryContext(companyName, description, posts);
    }

    /**
     * Extracts key metrics and scale indicators from description
     */
    private String extractKeyMetrics(String description) {
        if (description.isEmpty()) {
            return "";
        }

        StringBuilder enhanced = new StringBuilder(description);

        // Look for and highlight key metrics
        String desc = description.toLowerCase();
        if (desc.contains("countries")) {
            enhanced.append("\n[SCALE INDICATOR: Global presence mentioned]");
        }
        if (desc.contains("employees") || desc.contains("people")) {
            enhanced.append("\n[SCALE INDICATOR: Workforce size mentioned]");
        }
        if (desc.contains("billion") || desc.contains("million")) {
            enhanced.append("\n[SCALE INDICATOR: Large numbers mentioned - potential revenue/users]");
        }

        return enhanced.toString();
    }

    /**
     * Prioritizes posts based on strategic importance
     */
    private List<String> prioritizeStrategicPosts(List<String> posts) {
        return posts.stream()
                .filter(this::isStrategicallySignificant)
                .sorted((a, b) -> getStrategicScore(b) - getStrategicScore(a))
                .limit(10) // Limit to top 10 most strategic posts
                .collect(Collectors.toList());
    }

    /**
     * Checks if a post is strategically significant
     */
    private boolean isStrategicallySignificant(String post) {
        String postLower = post.toLowerCase();
        return postLower.contains("launch") || postLower.contains("partnership") ||
                postLower.contains("expansion") || postLower.contains("acquisition") ||
                postLower.contains("investment") || postLower.contains("milestone") ||
                postLower.contains("market") || postLower.contains("growth") ||
                postLower.contains("competition") || postLower.contains("strategy") ||
                postLower.contains("billion") || postLower.contains("million") ||
                postLower.contains("global") || postLower.contains("international");
    }

    /**
     * Scores posts by strategic importance
     */
    private int getStrategicScore(String post) {
        int score = 0;
        String postLower = post.toLowerCase();

        // High-value strategic indicators
        if (postLower.contains("acquisition") || postLower.contains("merger"))
            score += 10;
        if (postLower.contains("partnership") || postLower.contains("alliance"))
            score += 8;
        if (postLower.contains("launch") || postLower.contains("release"))
            score += 7;
        if (postLower.contains("expansion") || postLower.contains("market"))
            score += 6;
        if (postLower.contains("investment") || postLower.contains("funding"))
            score += 6;
        if (postLower.contains("billion") || postLower.contains("million"))
            score += 5;
        if (postLower.contains("global") || postLower.contains("international"))
            score += 4;
        if (postLower.contains("competition") || postLower.contains("vs") || postLower.contains("versus"))
            score += 4;

        return score;
    }

    /**
     * Analyzes posts for strategic themes
     */
    private Map<String, List<String>> analyzeStrategicThemes(List<String> posts) {
        Map<String, List<String>> themes = new HashMap<>();

        for (String post : posts) {
            String postLower = post.toLowerCase();

            if (postLower.contains("acquisition") || postLower.contains("merger") || postLower.contains("investment")) {
                themes.computeIfAbsent("Market Consolidation", k -> new ArrayList<>()).add(post);
            } else if (postLower.contains("partnership") || postLower.contains("alliance")
                    || postLower.contains("collaboration")) {
                themes.computeIfAbsent("Strategic Partnerships", k -> new ArrayList<>()).add(post);
            } else if (postLower.contains("launch") || postLower.contains("release")
                    || postLower.contains("introducing")) {
                themes.computeIfAbsent("Product Innovation", k -> new ArrayList<>()).add(post);
            } else if (postLower.contains("expansion") || postLower.contains("global")
                    || postLower.contains("international") || postLower.contains("market")) {
                themes.computeIfAbsent("Market Expansion", k -> new ArrayList<>()).add(post);
            } else if (postLower.contains("competition") || postLower.contains("vs") || postLower.contains("versus")
                    || postLower.contains("challenge")) {
                themes.computeIfAbsent("Competitive Positioning", k -> new ArrayList<>()).add(post);
            } else {
                themes.computeIfAbsent("Operational Updates", k -> new ArrayList<>()).add(post);
            }
        }

        // Limit posts per theme to avoid redundancy
        themes.replaceAll((theme, postList) -> postList.stream().limit(3).collect(Collectors.toList()));

        return themes;
    }

    /**
     * Extracts strategic insight from a post
     */
    private String extractStrategicInsight(String post) {
        String cleaned = post.replaceAll("\\s+", " ").trim();

        // If short enough, return as-is
        if (cleaned.length() <= 150) {
            return cleaned;
        }

        // Extract strategic core - first sentence plus any strategic keywords
        String[] sentences = cleaned.split("\\. ");
        StringBuilder insight = new StringBuilder();

        // Always include first sentence
        if (sentences.length > 0) {
            insight.append(sentences[0]);
            if (!sentences[0].endsWith(".")) {
                insight.append(".");
            }
        }

        // Add sentences with strategic value
        for (int i = 1; i < sentences.length && insight.length() < 140; i++) {
            String sentence = sentences[i].toLowerCase();
            if (sentence.contains("market") || sentence.contains("compete") ||
                    sentence.contains("customer") || sentence.contains("revenue") ||
                    sentence.contains("growth") || sentence.contains("scale") ||
                    sentence.contains("million") || sentence.contains("billion") ||
                    sentence.contains("partnership") || sentence.contains("expansion")) {
                insight.append(" ").append(sentences[i]);
                if (!sentences[i].endsWith(".")) {
                    insight.append(".");
                }
                break;
            }
        }

        return insight.toString();
    }

    /**
     * Formats strategic analysis for structured presentation
     */
    private String formatStrategicAnalysis(String companyName, String industryContext,
            String keyMetrics, Map<String, List<String>> strategicThemes,
            List<String> strategicInsights) {
        StringBuilder formatted = new StringBuilder();

        formatted.append("=== STRATEGIC ANALYSIS: ").append(companyName.toUpperCase()).append(" ===\n\n");

        if (!industryContext.isEmpty()) {
            formatted.append("INDUSTRY CONTEXT: ").append(industryContext).append("\n\n");
        }

        if (!keyMetrics.isEmpty()) {
            formatted.append("KEY METRICS:\n").append(keyMetrics).append("\n\n");
        }

        if (!strategicThemes.isEmpty()) {
            formatted.append("STRATEGIC THEMES:\n");
            strategicThemes.forEach((theme, posts) -> {
                formatted.append("- ").append(theme).append(": ").append(posts.size()).append(" activities\n");
            });
            formatted.append("\n");
        }

        if (!strategicInsights.isEmpty()) {
            formatted.append("TOP STRATEGIC INSIGHTS:\n");
            for (int i = 0; i < Math.min(5, strategicInsights.size()); i++) {
                formatted.append((i + 1)).append(". ").append(strategicInsights.get(i)).append("\n");
            }
        }

        return formatted.toString();
    }

    /**
     * Formats analysis output for HTML display
     */
    private String formatAnalysisForHtml(String analysis) {
        if (analysis == null || analysis.trim().isEmpty()) {
            return "<p>Analysis not available</p>";
        }

        return analysis
                .replaceAll("\\*\\*(.*?)\\*\\*", "<strong>$1</strong>") // Bold formatting
                .replaceAll("\\*(.*?)\\*", "<em>$1</em>") // Italic formatting
                .replaceAll("(?m)^#{1,6}\\s*(.*?)$",
                        "<h3 style='color: #495057; margin-top: 25px; margin-bottom: 15px;'>$1</h3>") // Headers
                .replaceAll("(?m)^-\\s*(.*?)$", "<li style='margin-bottom: 8px;'>$1</li>") // List items
                .replaceAll("(<li.*?</li>\\s*)+", "<ul style='padding-left: 20px; margin-bottom: 15px;'>$0</ul>") // Wrap
                                                                                                                  // lists
                .replaceAll("\\n\\n", "</p><p style='margin-bottom: 15px; line-height: 1.6;'>") // Paragraphs
                .replaceAll("^", "<p style='margin-bottom: 15px; line-height: 1.6;'>") // Start paragraph
                .replaceAll("$", "</p>") // End paragraph
                .replaceAll("-\\s*", "- ") // Clean bullet spacing
                .replaceAll("<br>\\s*-", "<br>-"); // Ensure clean bullet starts
    }

    /**
     * Creates comprehensive fallback when AI analysis fails
     */
    private String createComprehensiveFallback(String companyName, String description,
            int postCount, String profileName) {
        logger.info("Creating comprehensive fallback analysis for: {}", companyName);

        StringBuilder content = new StringBuilder();

        // Header with styling
        content.append("<div style='background: linear-gradient(135deg, #dc3545 0%, #c82333 100%); ")
                .append("color: white; padding: 20px; border-radius: 10px; margin-bottom: 20px;'>");
        content.append("<h2 style='margin: 0; font-size: 24px;'>⚠️ Limited Analysis: ")
                .append(companyName).append("</h2>");
        content.append("<p style='margin: 10px 0 0 0; opacity: 0.9;'>AI Analysis Currently Unavailable</p>");
        content.append("</div>");

        // Strategic positioning
        content.append("<h3 style='color: #495057; margin-top: 25px;'>I. Strategic Positioning</h3>");

        if (!description.isEmpty() && description.length() > 20) {
            String industryContext = getIndustryContext(companyName, description, new ArrayList<>());
            content.append("<p><strong>Market Position:</strong> ");
            String shortDesc = description.length() > 200 ? description.substring(0, 200) + "..." : description;
            content.append(shortDesc).append("</p>");

            if (!industryContext.isEmpty()) {
                content.append("<p><strong>Competitive Landscape:</strong> ").append(industryContext).append("</p>");
            }
        } else {
            content.append(
                    "<p><strong>Market Position:</strong> Limited company description available for strategic analysis.</p>");
        }

        // Activity analysis
        content.append("<h3 style='color: #495057; margin-top: 25px;'>II. Activity Analysis</h3>");

        if (postCount > 0) {
            content.append("<p><strong>Communication Strategy:</strong> Active LinkedIn presence with ")
                    .append(postCount).append(" recent posts indicating ongoing strategic communications.</p>");
            content.append("<p><strong>Market Engagement:</strong> Regular stakeholder communication suggests ")
                    .append("focus on brand positioning and market presence.</p>");
        } else {
            content.append("<p><strong>Communication Strategy:</strong> Limited recent LinkedIn activity detected, ")
                    .append("suggesting either strategic communication focus elsewhere or private company approach.</p>");
        }

        // Strategic implications
        content.append("<h3 style='color: #495057; margin-top: 25px;'>III. Strategic Intelligence</h3>");
        content.append(
                "<p><strong>Analysis Limitation:</strong> Detailed strategic analysis requires additional data sources. ");
        content.append("Current LinkedIn activity provides limited visibility into strategic direction.</p>");
        content.append("<p><strong>Recommendation:</strong> Supplement with financial reports, press releases, and ")
                .append("competitive intelligence for comprehensive strategic assessment.</p>");

        // Metadata
        content.append("<div style='margin-top: 30px; padding: 15px; ")
                .append("background-color: #f8d7da; border-radius: 5px; font-size: 14px; color: #721c24;'>");
        content.append("<strong>Fallback Analysis Metadata:</strong> ");
        content.append("Posts Available: ").append(postCount).append(" | ");
        content.append("Description Length: ").append(description.length()).append(" chars | ");
        content.append("Profile Name: ").append(profileName.equals(companyName) ? "Standard" : "Variant");
        content.append("</div>");

        return content.toString();
    }

    /**
     * Enhanced content preparation with strategic analysis
     */
    public String prepareStrategicContentForLLM(String companyName, String profileName, String description,
            List<String> posts) {
        StringBuilder content = new StringBuilder();

        // Basic company info
        content.append("=== COMPANY PROFILE ===\n");
        content.append("Company: ").append(companyName).append("\n");

        if (!profileName.isEmpty() && !profileName.equals(companyName)) {
            content.append("LinkedIn Profile Name: ").append(profileName).append("\n");
        }

        // Industry context analysis
        String industryContext = analyzeIndustryContext(companyName, description, posts);
        if (!industryContext.isEmpty()) {
            content.append("Industry Context: ").append(industryContext).append("\n");
        }

        // Enhanced description with metrics
        if (!description.isEmpty()) {
            content.append("\n=== COMPANY DESCRIPTION ===\n");
            content.append(description).append("\n");

            String metrics = extractBusinessMetrics(description);
            if (!metrics.isEmpty()) {
                content.append("\n=== KEY METRICS ===\n");
                content.append(metrics).append("\n");
            }
        }

        // Strategic post analysis
        if (!posts.isEmpty()) {
            content.append("\n=== STRATEGIC ACTIVITIES ===\n");
            Map<String, List<String>> themes = analyzeStrategicThemes(posts);

            for (Map.Entry<String, List<String>> entry : themes.entrySet()) {
                content.append("\n").append(entry.getKey().toUpperCase()).append(":\n");
                for (String post : entry.getValue()) {
                    content.append("• ").append(post).append("\n");
                }
            }
        }

        return content.toString();
    }

    /**
     * Analyzes industry context and competitive landscape
     * Uses centralized IndustryContextUtil for consistent analysis
     */
    private String analyzeIndustryContext(String companyName, String description, List<String> posts) {
        return industryContextUtil.getIndustryContext(companyName, description, posts);
    }

    /**
     * Extracts business metrics from company description
     */
    private String extractBusinessMetrics(String description) {
        StringBuilder metrics = new StringBuilder();
        String desc = description.toLowerCase();

        if (desc.contains("countries")) {
            metrics.append("Global presence across multiple countries\n");
        }
        if (desc.contains("billion")) {
            metrics.append("Billion-scale operations mentioned\n");
        }
        if (desc.contains("employees") && (desc.contains("thousand") || desc.contains("million"))) {
            metrics.append("Large workforce scale\n");
        }

        return metrics.toString();
    }

}