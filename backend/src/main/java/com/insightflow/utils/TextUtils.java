package com.insightflow.utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TextUtils {

    /**
     * Wraps text to fit within specified width
     */
    public static List<String> wrapText(String text, Font font, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return lines;
        }

        FontMetrics fm = new Canvas().getFontMetrics(font);

        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            int lineWidth = fm.stringWidth(testLine);

            if (lineWidth <= maxWidth) {
                currentLine.append(currentLine.length() == 0 ? word : " " + word);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    // Single word is too long, truncate it
                    lines.add(truncateText(word, font, maxWidth));
                }
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    /**
     * Truncates text to fit within specified width
     */
    public static String truncateText(String text, Font font, int maxWidth) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        FontMetrics fm = new Canvas().getFontMetrics(font);
        if (fm.stringWidth(text) <= maxWidth) {
            return text;
        }

        String ellipsis = "...";
        int ellipsisWidth = fm.stringWidth(ellipsis);
        int maxTextWidth = maxWidth - ellipsisWidth;

        for (int i = text.length() - 1; i > 0; i--) {
            String truncated = text.substring(0, i);
            if (fm.stringWidth(truncated) <= maxTextWidth) {
                return truncated + ellipsis;
            }
        }

        return ellipsis;
    }

    /**
     * Draws multiple lines of text at specified position
     */
    public static void drawMultiLineText(Graphics2D g2d, List<String> lines, int x, int y, int lineHeight) {
        for (int i = 0; i < lines.size(); i++) {
            g2d.drawString(lines.get(i), x, y + (i * lineHeight));
        }
    }
}
