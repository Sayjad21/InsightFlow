package com.insightflow.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ScrapingUtil {

    /**
     * Extracts text from URL using Jsoup, mirroring BeautifulSoup extraction (limit 5000 chars).
     * @param url The URL to extract from.
     * @return Extracted text or error message.
     */
    public String extractTextFromUrl(String url) {
        try {
            Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(5000).get();
            // Remove scripts/styles mirroring soup decompose
            doc.select("script, style").remove();
            String text = doc.text();
            return text.length() > 5000 ? text.substring(0, 5000) : text;
        } catch (IOException e) {
            return "Erreur extraction " + url + ": " + e.getMessage();
        }
    }
}