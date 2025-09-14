package com.insightflow.services;

import com.insightflow.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VisualizationService {

    // @Autowired
    // private FirebaseStorageService firebaseStorageService;

    @Autowired
    private SupabaseStorageService supabaseStorageService;

    /**
     * Generates SWOT matrix image as base64, mirroring the Python implementation
     * Creates a 2x2 grid with colored quadrants for each SWOT element
     */
    public String generateSwotImage(Map<String, List<String>> swot) {
        try {
            int width = 1000;
            int height = 600;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();

            // Enable antialiasing for better text quality
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Fill background
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);

            // Define quadrant colors (matching Python implementation)
            Color strengthsColor = new Color(192, 223, 229); // #C0DFE5
            Color weaknessesColor = new Color(253, 217, 181); // #FDD9B5
            Color opportunitiesColor = new Color(217, 238, 207); // #D9EECF
            Color threatsColor = new Color(247, 207, 199); // #F7CFC7

            int quadWidth = width / 2;
            int quadHeight = height / 2;

            // Draw quadrants
            // Strengths (top-left)
            g2d.setColor(strengthsColor);
            g2d.fillRect(0, 0, quadWidth, quadHeight);

            // Weaknesses (top-right)
            g2d.setColor(weaknessesColor);
            g2d.fillRect(quadWidth, 0, quadWidth, quadHeight);

            // Opportunities (bottom-left)
            g2d.setColor(opportunitiesColor);
            g2d.fillRect(0, quadHeight, quadWidth, quadHeight);

            // Threats (bottom-right)
            g2d.setColor(threatsColor);
            g2d.fillRect(quadWidth, quadHeight, quadWidth, quadHeight);

            // Draw borders
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(quadWidth, 0, quadWidth, height); // Vertical line
            g2d.drawLine(0, quadHeight, width, quadHeight); // Horizontal line
            g2d.drawRect(0, 0, width - 1, height - 1); // Outer border

            // Add titles and content
            Font titleFont = new Font("Arial", Font.BOLD, 24);
            Font itemFont = new Font("Arial", Font.PLAIN, 16);
            g2d.setColor(Color.BLACK);

            // Strengths
            g2d.setFont(titleFont);
            g2d.drawString("Strengths", 20, 25);
            g2d.setFont(itemFont);
            List<String> strengths = swot.getOrDefault("strengths", List.of());
            for (int i = 0; i < Math.min(strengths.size(), 10); i++) {
                g2d.drawString("• " + strengths.get(i), 20, 50 + i * 28);
            }

            // Weaknesses
            g2d.setFont(titleFont);
            g2d.drawString("Weaknesses", quadWidth + 20, 25);
            g2d.setFont(itemFont);
            List<String> weaknesses = swot.getOrDefault("weaknesses", List.of());
            for (int i = 0; i < Math.min(weaknesses.size(), 10); i++) {
                g2d.drawString("• " + weaknesses.get(i), quadWidth + 20, 50 + i * 28);
            }

            // Opportunities
            g2d.setFont(titleFont);
            g2d.drawString("Opportunities", 20, quadHeight + 25);
            g2d.setFont(itemFont);
            List<String> opportunities = swot.getOrDefault("opportunities", List.of());
            for (int i = 0; i < Math.min(opportunities.size(), 10); i++) {
                g2d.drawString("• " + opportunities.get(i), 20, quadHeight + 50 + i * 28);
            }

            // Threats
            g2d.setFont(titleFont);
            g2d.drawString("Threats", quadWidth + 20, quadHeight + 25);
            g2d.setFont(itemFont);
            List<String> threats = swot.getOrDefault("threats", List.of());
            for (int i = 0; i < Math.min(threats.size(), 10); i++) {
                g2d.drawString("• " + threats.get(i), quadWidth + 20, quadHeight + 50 + i * 28);
            }

            g2d.dispose();

            // Upload to Supabase Storage first, then Firebase as fallback
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(image, "PNG", baos);

            // Try to upload to Supabase first
            if (supabaseStorageService.isAvailable()) {
                String supabaseUrl = supabaseStorageService.uploadImageFromStream(baos, "swot_analysis.png",
                        "image/png");
                System.out.println("Uploaded to Supabase: SWOT Analysis");
                if (supabaseUrl != null) {
                    return supabaseUrl;
                }
            }

            // Try to upload to Firebase as fallback
            // if (firebaseStorageService.isAvailable()) {
            //     String firebaseUrl = firebaseStorageService.uploadImageFromStream(baos, "swot_analysis.png",
            //             "image/png");
            //     if (firebaseUrl != null) {
            //         return firebaseUrl;
            //     }
            // }

            // Fallback to base64 if both uploads fail
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate SWOT image", e);
        }
    }

    /**
     * Generates PESTEL analysis image as base64, mirroring the Python
     * implementation
     * Creates a 2x3 grid with colored sections for each PESTEL factor
     */
    public String generatePestelImage(Map<String, List<String>> pestel) {
        try {
            int width = 1200;
            int height = 800;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);

            // Define colors for each PESTEL factor
            Map<String, Color> colors = new HashMap<>();
            colors.put("political", new Color(232, 213, 179)); // #E8D5B3
            colors.put("economic", new Color(213, 232, 212)); // #D5E8D4
            colors.put("social", new Color(212, 225, 245)); // #D4E1F5
            colors.put("technological", new Color(245, 212, 225)); // #F5D4E1
            colors.put("environmental", new Color(245, 240, 212)); // #F5F0D4
            colors.put("legal", new Color(225, 212, 245)); // #E1D4F5

            int sectionWidth = width / 3;
            int sectionHeight = height / 2;

            String[] factors = { "political", "economic", "social", "technological", "environmental", "legal" };
            String[] titles = { "Political", "Economic", "Social", "Technological", "Environmental", "Legal" };

            for (int i = 0; i < factors.length; i++) {
                int col = i % 3;
                int row = i / 3;
                int x = col * sectionWidth;
                int y = row * sectionHeight;

                // Fill section background
                g2d.setColor(colors.get(factors[i]));
                g2d.fillRect(x, y, sectionWidth, sectionHeight);

                // Draw section border
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRect(x, y, sectionWidth, sectionHeight);

                // Add title
                g2d.setFont(new Font("Arial", Font.BOLD, 24));
                g2d.drawString(titles[i], x + 20, y + 30);

                // Add items
                g2d.setFont(new Font("Arial", Font.PLAIN, 16));
                List<String> items = pestel.getOrDefault(factors[i], List.of());
                for (int j = 0; j < Math.min(items.size(), 12); j++) {
                    g2d.drawString("• " + items.get(j), x + 20, y + 60 + j * 28);
                }
            }

            g2d.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(image, "PNG", baos);

            // Try Superbase first
            if (supabaseStorageService.isAvailable()) {
                String supabaseUrl = supabaseStorageService.uploadImageFromStream(baos, "pestel_analysis.png",
                        "image/png");
                System.out.println("Uploaded to superbase: PESTEL Analysis");
                if (supabaseUrl != null) {
                    return supabaseUrl;
                }
            }
            
            // Try to upload to Firebase, fallback to base64 if Firebase is not available
            // if (firebaseStorageService.isAvailable()) {
            //     String firebaseUrl = firebaseStorageService.uploadImageFromStream(baos, "pestel_analysis.png",
            //             "image/png");
            //     if (firebaseUrl != null) {
            //         return firebaseUrl;
            //     }
            // }

            // Fallback to base64 if Firebase upload fails
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PESTEL image", e);
        }
    }

    /**
     * Generates Porter's Five Forces diagram as base64
     * Creates a center circle with 4 surrounding force circles and connecting
     * arrows
     */
    public String generatePorterImage(Map<String, List<String>> forces) {
        try {
            int width = 1000;
            int height = 1000;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);

            int centerX = width / 2;
            int centerY = height / 2;
            int centerRadius = 120;
            int outerRadius = 80;
            int distance = 200;

            // Colors
            Color centerColor = new Color(217, 232, 245); // #D9E8F5
            Color outerColor = new Color(232, 240, 248); // #E8F0F8

            // Draw center circle (Competitive Rivalry)
            g2d.setColor(centerColor);
            g2d.fillOval(centerX - centerRadius, centerY - centerRadius, centerRadius * 2, centerRadius * 2);
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(centerX - centerRadius, centerY - centerRadius, centerRadius * 2, centerRadius * 2);

            // Center title and content
            g2d.setFont(new Font("Arial", Font.BOLD, 18));
            g2d.drawString("Competitive", centerX - 50, centerY - 20);
            g2d.drawString("Rivalry", centerX - 30, centerY - 5);
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            List<String> rivalry = forces.getOrDefault("rivalry", List.of());
            for (int i = 0; i < Math.min(rivalry.size(), 3); i++) {
                g2d.drawString("• " + rivalry.get(i), centerX - 60, centerY + 15 + i * 16);
            }

            // Define outer force positions and data
            String[] forceKeys = { "new_entrants", "supplier_power", "buyer_power", "substitutes" };
            String[] forceTitles = { "New Entrants", "Supplier Power", "Buyer Power", "Substitutes" };
            int[][] positions = {
                    { centerX, centerY - distance }, // Top
                    { centerX - distance, centerY }, // Left
                    { centerX + distance, centerY }, // Right
                    { centerX, centerY + distance } // Bottom
            };

            for (int i = 0; i < forceKeys.length; i++) {
                int x = positions[i][0];
                int y = positions[i][1];

                // Draw outer circle
                g2d.setColor(outerColor);
                g2d.fillOval(x - outerRadius, y - outerRadius, outerRadius * 2, outerRadius * 2);
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawOval(x - outerRadius, y - outerRadius, outerRadius * 2, outerRadius * 2);

                // Title
                g2d.setFont(new Font("Arial", Font.BOLD, 16));
                FontMetrics fm = g2d.getFontMetrics();
                int titleWidth = fm.stringWidth(forceTitles[i]);
                g2d.drawString(forceTitles[i], x - titleWidth / 2, y - 15);

                // Items
                g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                List<String> items = forces.getOrDefault(forceKeys[i], List.of());
                for (int j = 0; j < Math.min(items.size(), 3); j++) {
                    String item = "• " + items.get(j);
                    int itemWidth = g2d.getFontMetrics().stringWidth(item);
                    g2d.drawString(item, x - itemWidth / 2, y + 5 + j * 16);
                }

                // Draw arrow from outer circle to center
                g2d.setStroke(new BasicStroke(2));
                double angle = Math.atan2(centerY - y, centerX - x);
                int arrowStartX = x + (int) (Math.cos(angle) * outerRadius);
                int arrowStartY = y + (int) (Math.sin(angle) * outerRadius);
                int arrowEndX = centerX - (int) (Math.cos(angle) * centerRadius);
                int arrowEndY = centerY - (int) (Math.sin(angle) * centerRadius);

                g2d.drawLine(arrowStartX, arrowStartY, arrowEndX, arrowEndY);

                // Draw arrowhead
                double arrowAngle = Math.atan2(arrowEndY - arrowStartY, arrowEndX - arrowStartX);
                int arrowLength = 10;
                int arrowX1 = arrowEndX - (int) (Math.cos(arrowAngle - 0.5) * arrowLength);
                int arrowY1 = arrowEndY - (int) (Math.sin(arrowAngle - 0.5) * arrowLength);
                int arrowX2 = arrowEndX - (int) (Math.cos(arrowAngle + 0.5) * arrowLength);
                int arrowY2 = arrowEndY - (int) (Math.sin(arrowAngle + 0.5) * arrowLength);

                g2d.drawLine(arrowEndX, arrowEndY, arrowX1, arrowY1);
                g2d.drawLine(arrowEndX, arrowEndY, arrowX2, arrowY2);
            }

            g2d.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(image, "PNG", baos);

            // Try Superbase first
            if (supabaseStorageService.isAvailable()) {
                String supabaseUrl = supabaseStorageService.uploadImageFromStream(baos, "porters_five_forces.png",
                        "image/png");
                System.out.println("Uploaded to Supabase: Porter's Five Forces");
                if (supabaseUrl != null) {
                    return supabaseUrl;
                }
            }
            
            // Try to upload to Firebase, fallback to base64 if Firebase is not available
            // if (firebaseStorageService.isAvailable()) {
            //     String firebaseUrl = firebaseStorageService.uploadImageFromStream(baos, "porter_forces.png",
            //             "image/png");
            //     if (firebaseUrl != null) {
            //         return firebaseUrl;
            //     }
            // }

            // Fallback to base64 if Firebase upload fails
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Porter image", e);
        }
    }

    /**
     * Generates BCG Matrix image as base64, mirroring the Python implementation
     * Creates a 2x2 matrix with quadrant labels and product bubbles
     */
    public String generateBcgImage(Map<String, Map<String, Double>> products) {
        try {
            int width = 1000;
            int height = 800;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);

            // Define margins and dimensions
            int margin = 80;
            int chartWidth = width - 2 * margin;
            int chartHeight = height - 2 * margin;
            int chartX = margin;
            int chartY = margin;

            // Define quadrant colors (matching Python implementation)
            Color questionMarkColor = new Color(255, 214, 153, 77); // #FFD699 with alpha
            Color starColor = new Color(153, 214, 255, 77); // #99D6FF with alpha
            Color dogColor = new Color(255, 153, 153, 77); // #FF9999 with alpha
            Color cashCowColor = new Color(153, 255, 153, 77); // #99FF99 with alpha

            // Draw quadrant backgrounds
            int halfWidth = chartWidth / 2;
            int halfHeight = chartHeight / 2;

            // Question Mark (top-left: low share, high growth)
            g2d.setColor(questionMarkColor);
            g2d.fillRect(chartX, chartY, halfWidth, halfHeight);

            // Star (top-right: high share, high growth)
            g2d.setColor(starColor);
            g2d.fillRect(chartX + halfWidth, chartY, halfWidth, halfHeight);

            // Dog (bottom-left: low share, low growth)
            g2d.setColor(dogColor);
            g2d.fillRect(chartX, chartY + halfHeight, halfWidth, halfHeight);

            // Cash Cow (bottom-right: high share, low growth)
            g2d.setColor(cashCowColor);
            g2d.fillRect(chartX + halfWidth, chartY + halfHeight, halfWidth, halfHeight);

            // Draw grid lines
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1));
            g2d.drawLine(chartX + halfWidth, chartY, chartX + halfWidth, chartY + chartHeight); // Vertical
            g2d.drawLine(chartX, chartY + halfHeight, chartX + chartWidth, chartY + halfHeight); // Horizontal
            g2d.drawRect(chartX, chartY, chartWidth, chartHeight); // Border

            // Add quadrant labels
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            FontMetrics fm = g2d.getFontMetrics();

            String[] labels = { "QUESTION MARK", "STAR", "DOG", "CASH COW" };
            int[][] labelPositions = {
                    { chartX + halfWidth / 2, chartY + 30 },
                    { chartX + halfWidth + halfWidth / 2, chartY + 30 },
                    { chartX + halfWidth / 2, chartY + halfHeight + 30 },
                    { chartX + halfWidth + halfWidth / 2, chartY + halfHeight + 30 }
            };

            for (int i = 0; i < labels.length; i++) {
                int labelWidth = fm.stringWidth(labels[i]);
                g2d.drawString(labels[i], labelPositions[i][0] - labelWidth / 2, labelPositions[i][1]);
            }

            // Add axis labels
            g2d.setFont(new Font("Arial", Font.BOLD, 18));
            fm = g2d.getFontMetrics();

            // X-axis label
            String xLabel = "Relative Market Share";
            int xLabelWidth = fm.stringWidth(xLabel);
            g2d.drawString(xLabel, chartX + chartWidth / 2 - xLabelWidth / 2, height - 20);

            // Y-axis label (rotated)
            String yLabel = "Market Growth Rate (%)";
            g2d.rotate(-Math.PI / 2, 20, chartY + chartHeight / 2);
            int yLabelWidth = fm.stringWidth(yLabel);
            g2d.drawString(yLabel, 20 - yLabelWidth / 2, chartY + chartHeight / 2);
            g2d.rotate(Math.PI / 2, 20, chartY + chartHeight / 2);

            // Plot products as bubbles
            int bubbleSize = 30;
            g2d.setColor(new Color(51, 102, 153, 180)); // Semi-transparent blue

            for (Map.Entry<String, Map<String, Double>> entry : products.entrySet()) {
                String productName = entry.getKey();
                Map<String, Double> values = entry.getValue();

                double marketShare = values.getOrDefault("market_share", 0.5);
                double growthRate = values.getOrDefault("growth_rate", 5.0);

                // Convert to screen coordinates
                // X: 0-2 market share maps to chartX to chartX+chartWidth
                int x = chartX + (int) ((marketShare / 2.0) * chartWidth);
                // Y: 0-20 growth rate maps to chartY+chartHeight to chartY (inverted)
                int y = chartY + chartHeight - (int) ((growthRate / 20.0) * chartHeight);

                // Draw bubble
                g2d.fillOval(x - bubbleSize / 2, y - bubbleSize / 2, bubbleSize, bubbleSize);
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawOval(x - bubbleSize / 2, y - bubbleSize / 2, bubbleSize, bubbleSize);

                // Add product name
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                FontMetrics nameFm = g2d.getFontMetrics();
                int nameWidth = nameFm.stringWidth(productName);
                g2d.drawString(productName, x - nameWidth / 2, y - bubbleSize / 2 - 5);

                g2d.setColor(new Color(51, 102, 153, 180)); // Reset bubble color
            }

            g2d.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(image, "PNG", baos);

            // Try Superbase first
            if (supabaseStorageService.isAvailable()) {
                String supabaseUrl = supabaseStorageService.uploadImageFromStream(baos, "bcg_matrix.png",
                        "image/png");
                System.out.println("Uploaded to Supabase: BCG Matrix");
                if (supabaseUrl != null) {
                    return supabaseUrl;
                }
            }
            
            // Try to upload to Firebase, fallback to base64 if Firebase is not available
            // if (firebaseStorageService.isAvailable()) {
            //     String firebaseUrl = firebaseStorageService.uploadImageFromStream(baos, "bcg_matrix.png", "image/png");
            //     if (firebaseUrl != null) {
            //         return firebaseUrl;
            //     }
            // }

            // Fallback to base64 if Firebase upload fails
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate BCG image", e);
        }
    }

    /**
     * Generates McKinsey 7S model image as base64, mirroring the Python
     * implementation
     * Creates a central circle with 6 surrounding circles connected by lines
     */
    public String generateMckinseyImage(Map<String, String> model7s) {
        try {
            int width = 1000;
            int height = 1000;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);

            int centerX = width / 2;
            int centerY = height / 2;
            int centerRadius = 120;
            int outerRadius = 120;
            int distance = 350;

            // Define colors for each element
            Map<String, Color> colors = new HashMap<>();
            colors.put("strategy", new Color(255, 182, 193)); // #FFB6C1
            colors.put("structure", new Color(173, 216, 230)); // #ADD8E6
            colors.put("systems", new Color(255, 218, 185)); // #FFDAB9
            colors.put("style", new Color(152, 251, 152)); // #98FB98
            colors.put("staff", new Color(216, 191, 216)); // #D8BFD8
            colors.put("skills", new Color(255, 250, 205)); // #FFFACD
            colors.put("shared_values", new Color(230, 230, 250)); // #E6E6FA

            // Draw central circle (Shared Values)
            g2d.setColor(colors.get("shared_values"));
            g2d.fillOval(centerX - centerRadius, centerY - centerRadius, centerRadius * 2, centerRadius * 2);
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(centerX - centerRadius, centerY - centerRadius, centerRadius * 2, centerRadius * 2);

            // Center title and content
            g2d.setFont(new Font("Arial", Font.BOLD, 18));
            FontMetrics fm = g2d.getFontMetrics();
            String centerTitle = "Shared Values";
            int titleWidth = fm.stringWidth(centerTitle);
            g2d.drawString(centerTitle, centerX - titleWidth / 2, centerY - 20);

            g2d.setFont(new Font("Arial", Font.PLAIN, 14));
            String sharedValues = model7s.getOrDefault("shared_values", "");
            fm = g2d.getFontMetrics();
            int valueWidth = fm.stringWidth(sharedValues);
            g2d.drawString(sharedValues, centerX - valueWidth / 2, centerY + 10);

            // Define outer elements
            String[] elements = { "strategy", "structure", "systems", "style", "staff", "skills" };
            String[] titles = { "Strategy", "Structure", "Systems", "Style", "Staff", "Skills" };

            // Calculate positions in a circle around the center
            for (int i = 0; i < elements.length; i++) {
                double angle = (2.0 * Math.PI * i) / elements.length;
                int x = centerX + (int) (distance * Math.cos(angle));
                int y = centerY + (int) (distance * Math.sin(angle));

                // Draw connecting line
                g2d.setColor(Color.GRAY);
                g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.drawLine(centerX, centerY, x, y);

                // Draw outer circle
                g2d.setColor(colors.get(elements[i]));
                g2d.fillOval(x - outerRadius, y - outerRadius, outerRadius * 2, outerRadius * 2);
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawOval(x - outerRadius, y - outerRadius, outerRadius * 2, outerRadius * 2);

                // Add title
                g2d.setFont(new Font("Arial", Font.BOLD, 18));
                fm = g2d.getFontMetrics();
                titleWidth = fm.stringWidth(titles[i]);
                g2d.drawString(titles[i], x - titleWidth / 2, y - 20);

                // Add content
                g2d.setFont(new Font("Arial", Font.PLAIN, 14));
                String content = model7s.getOrDefault(elements[i], "");
                fm = g2d.getFontMetrics();
                int contentWidth = fm.stringWidth(content);
                g2d.drawString(content, x - contentWidth / 2, y + 10);
            }

            g2d.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(image, "PNG", baos);

            // Try Superbase first
            if (supabaseStorageService.isAvailable()) {
                String supabaseUrl = supabaseStorageService.uploadImageFromStream(baos, "mckinsey_7s.png",
                        "image/png");
                System.out.println("Uploaded to Supabase: McKinsey 7S");
                if (supabaseUrl != null) {
                    return supabaseUrl;
                }
            }

            // Try to upload to Firebase, fallback to base64 if Firebase is not available
            // if (firebaseStorageService.isAvailable()) {
            //     String firebaseUrl = firebaseStorageService.uploadImageFromStream(baos, "mckinsey_7s.png", "image/png");
            //     if (firebaseUrl != null) {
            //         return firebaseUrl;
            //     }
            // }

            // Fallback to base64 if Firebase upload fails
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate McKinsey image", e);
        }
    }
}