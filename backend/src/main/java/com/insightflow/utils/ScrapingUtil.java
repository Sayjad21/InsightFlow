package com.insightflow.utils;

import org.apache.tika.Tika;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

@Component
public class ScrapingUtil {

    private final Tika tika = new Tika();

    /**
     * Extracts text from URL using intelligent content type detection and
     * appropriate parsers
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

            // Detect content type first using URLConnection
            URLConnection connection = new URL(url).openConnection();
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            String contentType = connection.getContentType();

            System.out.println("Processing URL: " + url + " with content type: " + contentType);

            // Handle different content types appropriately
            if (contentType != null) {
                String lowerContentType = contentType.toLowerCase();

                if (lowerContentType.contains("application/pdf")) {
                    System.out.println("Detected PDF, using Tika extraction for: " + url);
                    return extractWithTika(url);
                } else if (lowerContentType.contains("application/vnd.openxmlformats") ||
                        lowerContentType.contains("application/msword") ||
                        lowerContentType.contains("application/vnd.ms-")) {
                    System.out.println("Detected Office document, using Tika extraction for: " + url);
                    return extractWithTika(url);
                } else if (isHtmlContent(contentType)) {
                    System.out.println("Detected HTML content, using Jsoup extraction for: " + url);
                    return extractWithJsoup(url);
                } else {
                    System.out.println("Unsupported content type: " + contentType + " for URL: " + url);
                    return null;
                }
            } else {
                // If content type is unknown, try to determine from URL
                if (isPdfFromUrl(url)) {
                    System.out.println("PDF detected from URL pattern, using Tika extraction for: " + url);
                    return extractWithTika(url);
                } else {
                    // Default to HTML extraction for unknown types
                    System.out.println("Unknown content type, attempting HTML extraction for: " + url);
                    return extractWithJsoup(url);
                }
            }

        } catch (Exception e) {
            System.err.println("Failed to extract from URL: " + url + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Extracts text using Apache Tika (handles PDF, DOCX, PPTX, etc.)
     */
    private String extractWithTika(String url) {
        try (InputStream inputStream = new URL(url).openStream()) {
            String text = tika.parseToString(inputStream);

            if (text != null && text.trim().length() > 100) {
                // Limit text length to prevent context overflow
                return text.length() > 5000 ? text.substring(0, 5000) : text;
            }
            System.out.println("Tika extraction returned insufficient content for: " + url);
            return null;

        } catch (Exception e) {
            System.err.println("Tika extraction failed for URL: " + url + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Extracts text using Jsoup (handles HTML/XML content)
     */
    private String extractWithJsoup(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.5")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Cache-Control", "no-cache")
                    .timeout(10000)
                    .followRedirects(true)
                    .ignoreHttpErrors(true)
                    .get();

            // Remove scripts/styles and unwanted elements
            doc.select("script, style, nav, footer, aside, .advertisement, .ads, .cookie-banner").remove();

            String text = doc.text().trim();
            if (text.length() < 100) {
                System.out.println("Jsoup extraction returned insufficient content for: " + url);
                return null;
            }

            return text.length() > 5000 ? text.substring(0, 5000) : text;

        } catch (Exception e) {
            System.err.println("Jsoup extraction failed for URL: " + url + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Checks if URL appears to be a PDF from URL pattern
     */
    private boolean isPdfFromUrl(String url) {
        if (url == null)
            return false;

        String lowerUrl = url.toLowerCase();
        return lowerUrl.endsWith(".pdf") ||
                lowerUrl.contains(".pdf?") ||
                lowerUrl.contains("atlantis-press.com/article/") ||
                lowerUrl.contains("arxiv.org/pdf/");
    }

    /**
     * Checks if content type is HTML or supported text format for Jsoup
     */
    private boolean isHtmlContent(String contentType) {
        if (contentType == null)
            return true; // Assume HTML if unknown

        String lowerContentType = contentType.toLowerCase();
        return lowerContentType.contains("text/html") ||
                lowerContentType.contains("application/xhtml") ||
                lowerContentType.contains("text/plain") ||
                lowerContentType.contains("application/xml") ||
                lowerContentType.contains("text/xml") ||
                lowerContentType.matches(".*\\*/xml.*") ||
                lowerContentType.matches(".*\\*\\*\\+xml.*");
    }
}