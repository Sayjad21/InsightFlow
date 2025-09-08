package com.insightflow.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
public class ScrapingUtil {

    /**
     * Extracts text from URL using Jsoup, mirroring BeautifulSoup extraction (limit
     * 5000 chars).
     * 
     * @param url The URL to extract from.
     * @return Extracted text or null if extraction fails.
     */
    public String extractTextFromUrl(String url) {
        try {
            // Validate URL
            if (url == null || url.trim().isEmpty()) {
                return null;
            }

            // Enhanced user agent and headers to avoid blocking
            Document doc = Jsoup.connect(url)
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.5")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Cache-Control", "no-cache")
                    .timeout(10000) // Increased timeout
                    .followRedirects(true)
                    .ignoreHttpErrors(true) // Don't throw exceptions for HTTP errors
                    .get();

            // Remove scripts/styles mirroring soup decompose
            doc.select("script, style, nav, footer, aside, .advertisement, .ads, .cookie-banner").remove();

            String text = doc.text().trim();
            if (text.length() < 100) {
                // If text is too short, the page might be blocked or empty
                return null;
            }

            return text.length() > 5000 ? text.substring(0, 5000) : text;
        } catch (Exception e) {
            // Log the error but return null instead of error message
            System.err.println("Failed to extract from URL: " + url + " - " + e.getMessage());
            return null;
        }
    }
}