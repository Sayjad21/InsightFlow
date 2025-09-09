package com.insightflow.services;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class VisualizationServiceTest {

    private final VisualizationService visualizationService = new VisualizationService();

    @Test
    public void testGenerateSwotImage() {
        Map<String, List<String>> swot = new HashMap<>();
        swot.put("strengths", Arrays.asList("Innovation", "Brand Recognition", "Market Position"));
        swot.put("weaknesses", Arrays.asList("High Costs", "Limited Resources"));
        swot.put("opportunities", Arrays.asList("New Markets", "Technology Trends"));
        swot.put("threats", Arrays.asList("Competition", "Economic Downturn"));

        String result = visualizationService.generateSwotImage(swot);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.matches("^[A-Za-z0-9+/]*={0,2}$")); // Basic Base64 validation
    }

    @Test
    public void testGeneratePestelImage() {
        Map<String, List<String>> pestel = new HashMap<>();
        pestel.put("political", Arrays.asList("Government Policy", "Tax Policy"));
        pestel.put("economic", Arrays.asList("Interest Rates", "Inflation"));
        pestel.put("social", Arrays.asList("Demographics", "Culture"));
        pestel.put("technological", Arrays.asList("Automation", "AI"));
        pestel.put("environmental", Arrays.asList("Climate Change", "Sustainability"));
        pestel.put("legal", Arrays.asList("Regulations", "Employment Law"));

        String result = visualizationService.generatePestelImage(pestel);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.matches("^[A-Za-z0-9+/]*={0,2}$"));
    }

    @Test
    public void testGeneratePorterImage() {
        Map<String, List<String>> forces = new HashMap<>();
        forces.put("rivalry", Arrays.asList("Price Wars", "Marketing"));
        forces.put("new_entrants", Arrays.asList("Low Barriers", "Capital"));
        forces.put("supplier_power", Arrays.asList("Few Suppliers", "Switching Costs"));
        forces.put("buyer_power", Arrays.asList("Price Sensitive", "Volume"));
        forces.put("substitutes", Arrays.asList("Alternatives", "Technology"));

        String result = visualizationService.generatePorterImage(forces);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.matches("^[A-Za-z0-9+/]*={0,2}$"));
    }

    @Test
    public void testGenerateBcgImage() {
        Map<String, Map<String, Double>> products = new HashMap<>();

        Map<String, Double> product1 = new HashMap<>();
        product1.put("market_share", 1.5);
        product1.put("growth_rate", 15.0);
        products.put("Product A", product1);

        Map<String, Double> product2 = new HashMap<>();
        product2.put("market_share", 0.8);
        product2.put("growth_rate", 5.0);
        products.put("Product B", product2);

        String result = visualizationService.generateBcgImage(products);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.matches("^[A-Za-z0-9+/]*={0,2}$"));
    }

    @Test
    public void testGenerateMckinseyImage() {
        Map<String, String> model7s = new HashMap<>();
        model7s.put("strategy", "Growth");
        model7s.put("structure", "Hierarchical");
        model7s.put("systems", "Integrated");
        model7s.put("style", "Collaborative");
        model7s.put("staff", "Skilled");
        model7s.put("skills", "Technical");
        model7s.put("shared_values", "Innovation");

        String result = visualizationService.generateMckinseyImage(model7s);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.matches("^[A-Za-z0-9+/]*={0,2}$"));
    }
}
