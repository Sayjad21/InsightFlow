package com.insightflow.utils;

import dev.langchain4j.model.ollama.OllamaChatModel;
import net.bytebuddy.asm.Advice.Return;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Map;

@Component
public class AiUtil {

    private static final Logger logger = LoggerFactory.getLogger(AiUtil.class);

    @Value("${ollama.base.url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${ollama.model:llama3.2:latest}")
    private String ollamaModel;

    @Value("${ollama.timeout.seconds:120}")
    private int defaultTimeoutSeconds;

    @Value("${ollama.max.retries:3}")
    private int defaultMaxRetries;

    public OllamaChatModel getModel() {
        return OllamaChatModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(ollamaModel)
                .temperature(0.3)
                .timeout(Duration.ofSeconds(defaultTimeoutSeconds))
                .maxRetries(defaultMaxRetries)
                .build();
    }

    /**
     * Get model with extended timeout and more retries for complex operations
     */
    private OllamaChatModel getExtendedModel(int timeoutSeconds, int maxRetries) {
        return OllamaChatModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(ollamaModel)
                .temperature(0.3)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .maxRetries(maxRetries)
                .build();
    }

    public String invoke(String prompt) {
        logger.info("Invoking LLM with prompt: {}", prompt);
        try {
            OllamaChatModel model = getModel();
            String response = model.chat(prompt);
            logger.info("LLM response: {}", response);
            return response;
        } catch (dev.langchain4j.exception.TimeoutException e) {
            logger.error("LLM request timed out after {} seconds. Retrying with extended timeout...",
                    defaultTimeoutSeconds);
            try {
                // Retry with extended model
                OllamaChatModel extendedModel = getExtendedModel(defaultTimeoutSeconds * 2, defaultMaxRetries + 2);
                String response = extendedModel.chat(prompt);
                logger.info("LLM response (extended timeout): {}", response);
                return response;
            } catch (Exception retryException) {
                logger.error("LLM request failed even with extended timeout: {}", retryException.getMessage());
                throw new RuntimeException("AI service is currently unavailable. Please try again later.",
                        retryException);
            }
        } catch (Exception e) {
            logger.error("LLM request failed with unexpected error: {}", e.getMessage());
            throw new RuntimeException("AI service error: " + e.getMessage(), e);
        }
    }

    public String invokeWithTemplate(String template, Map<String, Object> variables) {
        logger.info("Template before substitution: {}", template);
        logger.info("Variables: {}", variables);

        try {
            PromptTemplate promptTemplate = PromptTemplate.from(template);
            Prompt prompt = promptTemplate.apply(variables);
            String promptText = prompt.text();
            logger.info("Prompt after substitution: {}", promptText);

            OllamaChatModel model = getModel();
            String response = model.chat(promptText);
            logger.info("LLM response: {}", response);
            return response;
        } catch (dev.langchain4j.exception.TimeoutException e) {
            logger.error("LLM template request timed out after {} seconds. Retrying with extended timeout...",
                    defaultTimeoutSeconds);
            try {
                // Retry with extended model
                PromptTemplate promptTemplate = PromptTemplate.from(template);
                Prompt prompt = promptTemplate.apply(variables);
                String promptText = prompt.text();

                OllamaChatModel extendedModel = getExtendedModel(defaultTimeoutSeconds * 2, defaultMaxRetries + 2);
                String response = extendedModel.chat(promptText);
                logger.info("LLM response (extended timeout): {}", response);
                return response;
            } catch (Exception retryException) {
                logger.error("LLM template request failed even with extended timeout: {}", retryException.getMessage());
                throw new RuntimeException("AI service is currently unavailable. Please try again later.",
                        retryException);
            }
        } catch (Exception e) {
            logger.error("LLM template request failed with unexpected error: {}", e.getMessage());
            throw new RuntimeException("AI service error: " + e.getMessage(), e);
        }
    }

    /**
     * Special method for investment recommendations with extended timeout
     */
    public String invokeWithTemplateExtended(String template, Map<String, Object> variables) {
        logger.info("Invoking LLM with extended timeout for complex analysis");
        logger.info("Template before substitution: {}", template);
        logger.info("Variables: {}", variables);

        // Create model with extended timeout for investment recommendations
        OllamaChatModel extendedModel = getExtendedModel(300, 5); // 5 minutes with 5 retries

        PromptTemplate promptTemplate = PromptTemplate.from(template);
        Prompt prompt = promptTemplate.apply(variables);
        String promptText = prompt.text();
        logger.info("Prompt after substitution: {}", promptText);
        String response = extendedModel.chat(promptText);
        logger.info("LLM response: {}", response);
        return response;
    }

    public Map<String, Object> parseJsonToMap(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> result = mapper.readValue(json, Map.class);
            return result;
        } catch (Exception e) {
            // Optionally log or handle error
            return Map.of("error", "Failed to parse JSON", "raw", json);
        }
    }

    public String getLinkedInAnalysisTemplate() {
        return "You are a strategic intelligence expert. Analyze the LinkedIn information about {{company_name}}. Provide a structured summary with sections: I. Company Overview, II. Recent Activities, III. Market Presence, IV. Additional Insights. Use the provided content, even if incomplete or noisy. If no relevant data is found, provide a generic analysis based on the company name.\nContent: {{content}}";
    }

    public String getSummaryTemplate() {
        return "You are an experienced strategic analyst specializing in competitor analysis.\n" +
                "Here is the extracted content about {{company_name}}:\n{{content}}\n\n" +
                "Analyze ONLY the information related to {{company_name}} from the provided content. " +
                "Ignore any information about other companies mentioned in the text. " +
                "Give only the key strategic elements of {{company_name}} specifically (target, positioning, strengths, weaknesses). "
                +
                "Always respond in English and focus strictly on {{company_name}}.";
    } // public String getSummaryTemplate() {
      // return "You are an analyst. Summarize the following content about
      // {{company_name}}.\n\n"
      // + "Always respond in English and use concise business terms.\n"
      // + "Return ONLY valid JSON in this format:\n"
      // + "{\n"
      // + " \"name\": \"{{company_name}}\",\n"
      // + " \"target\": [ \"...\", \"...\" ],\n"
      // + " \"positioning\": \"...\",\n"
      // + " \"strengths\": [ \"...\", \"...\" ],\n"
      // + " \"weaknesses\": [ \"...\", \"...\" ]\n"
      // + "}\n\n"
      // + "Content: {{content}}";
      // }

    public String getDiffWithRagTemplate() {
        return "You are Fred, an expert in strategic marketing and competitive differentiation.\n" +
                "Internal context (from RAG for our company):\n{{rag_context}}\n\n" +
                "External context (monitoring of competitor {{competitor_name}} and similar others):\n{{competitor_summary}}\n\n"
                +
                "Based on this, propose 3 concrete differentiation axes for our company (described in the RAG context) against {{competitor_name}} and its peers.\n"
                +
                "Always respond in English and use clear, professional business language.";
    }

    public String getSwotTemplate() {
        return "You are a strategy expert. For the company '{{company_name}}', " +
                "provide exactly 5 strengths, 5 weaknesses, 5 opportunities, and 5 threats. " +
                "Each item must contain 1 to 2 words maximum, without commas or conjunctions. " +
                "Respond only with a JSON object with 4 keys: " +
                "strengths, weaknesses, opportunities, threats.";
    }

    public String getPestelTemplate() {
        return "You are a strategy expert. For the company '{{company_name}}', " +
                "provide exactly 5 political, 5 economic, 5 social, " +
                "5 technological, 5 environmental, and 5 legal factors. " +
                "Each item must contain 1 to 2 words maximum, without commas or conjunctions. " +
                "Respond only with a JSON object with 6 keys: " +
                "political, economic, social, technological, environmental, legal.";
    }

    public String getPorterTemplate() {
        return "You are an expert in strategic analysis. For the company \"{{company_name}}\", " +
                "analyze Porter's Five Forces. For each of the five forces (including central competitive rivalry), " +
                "provide exactly 3 factors of 1-2 words each. " +
                "Respond ONLY with a JSON object containing these five keys: " +
                "`rivalry`, `new_entrants`, `substitutes`, `buyer_power`, `supplier_power`.";
    }

    public String getBcgTemplate() {
        return "You are an expert in strategic analysis. For the company \"{{company_name}}\", " +
                "identify exactly 4 products/services developed or owned by {{company_name}} (each named in 1-2 words). "
                +
                "Do not include products from competitors or unrelated companies (e.g., for OpenAI, exclude Jasper or LLaMA; for Tesla, exclude Rivian or Lucid). "
                +
                "Position each product on the BCG Matrix with exactly two keys: `market_share` (a number between 0 and 2) and `growth_rate` (a number between 0 and 20). "
                +
                "Respond ONLY with a JSON object where each key is a product name and each value is an object with `market_share` and `growth_rate`. "
                +
                "Examples: " +
                "For OpenAI: {\"ChatGPT\": {\"market_share\": 1.0, \"growth_rate\": 15.0}, \"DALL-E\": {\"market_share\": 0.5, \"growth_rate\": 10.0}, \"Codex\": {\"market_share\": 0.8, \"growth_rate\": 12.0}, \"Whisper\": {\"market_share\": 0.3, \"growth_rate\": 8.0}} "
                +
                "For Tesla: {\"Model 3\": {\"market_share\": 1.5, \"growth_rate\": 10.0}, \"Model Y\": {\"market_share\": 1.2, \"growth_rate\": 12.0}, \"Cybertruck\": {\"market_share\": 0.4, \"growth_rate\": 15.0}, \"Powerwall\": {\"market_share\": 0.6, \"growth_rate\": 8.0}} "
                +
                "For Google: {\"Search Engine\": {\"market_share\": 1.8, \"growth_rate\": 5.0}, \"Google Cloud\": {\"market_share\": 0.7, \"growth_rate\": 18.0}, \"YouTube\": {\"market_share\": 1.6, \"growth_rate\": 10.0}, \"Pixel Phone\": {\"market_share\": 0.3, \"growth_rate\": 12.0}} "
                +
                "For Amazon: {\"AWS\": {\"market_share\": 1.4, \"growth_rate\": 15.0}, \"Prime Video\": {\"market_share\": 0.8, \"growth_rate\": 10.0}, \"Kindle\": {\"market_share\": 1.0, \"growth_rate\": 5.0}, \"Echo Devices\": {\"market_share\": 0.9, \"growth_rate\": 8.0}} "
                +
                "For Microsoft: {\"Azure\": {\"market_share\": 1.0, \"growth_rate\": 18.0}, \"Windows\": {\"market_share\": 1.7, \"growth_rate\": 5.0}, \"Office 365\": {\"market_share\": 1.5, \"growth_rate\": 10.0}, \"Surface\": {\"market_share\": 0.4, \"growth_rate\": 12.0}}";
    }

    public String getMckinseyTemplate() {
        return "You are an expert in strategic analysis. For the company \"{{company_name}}\", " +
                "analyze the McKinsey 7S Model. For each of the 7 elements, provide exactly 1-2 words. " +
                "Respond ONLY with a JSON object containing seven keys: " +
                "`strategy`, `structure`, `systems`, `style`, `staff`, `skills`, `shared_values`.";
    }

    public String getSentimentAnalysisTemplate() {
        return "Analyze the sentiment of the following business information about {{company_name}}. " +
                "Consider factors like market position, financial health, competitive landscape, and recent news. " +
                "Provide a sentiment score between 0 (very negative) and 100 (very positive). " +
                "Respond ONLY with a number between 0 and 100.\n\n" +
                "Information to analyze:\n{{information}}";
    }

    public String getRiskAssessmentTemplate() {
        return "Analyze the business risk of {{company_name}} based on the following information. " +
                "Consider financial stability, market competition, regulatory environment, technological disruption, " +
                "and operational factors. Provide a risk rating between 0 (very low risk) and 10 (very high risk). " +
                "Respond ONLY with a number between 0 and 10.\n\n" +
                "Information to analyze:\n{{information}}";
    }

    public String getCombinedAnalysisTemplate() {
        return "Analyze the following business information about {{company_name}} and provide:\n" +
                "1. A sentiment score between 0-100 (0=very negative, 100=very positive)\n" +
                "2. A risk rating between 0-10 (0=very low risk, 10=very high risk)\n" +
                "Respond ONLY with a JSON object in this exact format:\n" +
                "{\"sentiment_score\": 75, \"risk_rating\": 3.5}\n\n" +
                "Information to analyze:\n{{information}}";
    }

    public String getInvestmentRecommendationTemplate() {
        return "You are a strategic business analyst. Based on the following comparative metrics, " +
                "provide investment and strategic choice recommendations. Explain why someone would choose " +
                "one company over another based on these specific metrics:\n\n" +
                "{{metrics_data}}\n\n" +
                "Focus on:\n" +
                "1. Market share advantages and growth potential\n" +
                "2. Risk profiles and stability factors\n" +
                "3. Market sentiment and investor confidence\n" +
                "4. Strategic positioning for different investment goals\n\n" +
                "Provide clear, actionable insights about which company might be preferred for different " +
                "scenarios (growth-focused, stability-focused, market leadership, etc.). " +
                "Be specific about the metrics and provide concrete reasoning." +
                "Always respond in English and in a professional business tone.";
    }
}