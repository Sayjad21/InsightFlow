package com.insightflow;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.insightflow.services.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AnalysisServiceTest {

    @Autowired
    private AnalysisService analysisService;

    @Test
    public void testGenerateSwot() {
        String companyName = "OpenAI";

        Map<String, List<String>> swot = analysisService.generateSwot(companyName);

        assertNotNull(swot, "SWOT map should not be null");
        assertTrue(swot.containsKey("strengths"), "SWOT map should contain 'strengths' key");
        assertTrue(swot.containsKey("weaknesses"), "SWOT map should contain 'weaknesses' key");
        assertTrue(swot.containsKey("opportunities"), "SWOT map should contain 'opportunities' key");
        assertTrue(swot.containsKey("threats"), "SWOT map should contain 'threats' key");

        assertFalse(swot.get("strengths").isEmpty(), "Strengths list should not be empty");
        assertFalse(swot.get("weaknesses").isEmpty(), "Weaknesses list should not be empty");
        assertFalse(swot.get("opportunities").isEmpty(), "Opportunities list should not be empty");
        assertFalse(swot.get("threats").isEmpty(), "Threats list should not be empty");

        System.out.println("SWOT Analysis Result: " + swot);
    }

    @Test
    public void testGeneratePestel() {
        String companyName = "OpenAI";

        Map<String, List<String>> pestel = analysisService.generatePestel(companyName);

        assertNotNull(pestel, "PESTEL map should not be null");
        assertTrue(pestel.containsKey("political"), "PESTEL map should contain 'political' key");
        assertTrue(pestel.containsKey("economic"), "PESTEL map should contain 'economic' key");
        assertTrue(pestel.containsKey("social"), "PESTEL map should contain 'social' key");
        assertTrue(pestel.containsKey("technological"), "PESTEL map should contain 'technological' key");
        assertTrue(pestel.containsKey("environmental"), "PESTEL map should contain 'environmental' key");
        assertTrue(pestel.containsKey("legal"), "PESTEL map should contain 'legal' key");

        assertFalse(pestel.get("political").isEmpty(), "Political list should not be empty");
        assertFalse(pestel.get("economic").isEmpty(), "Economic list should not be empty");
        assertFalse(pestel.get("social").isEmpty(), "Social list should not be empty");
        assertFalse(pestel.get("technological").isEmpty(), "Technological list should not be empty");
        assertFalse(pestel.get("environmental").isEmpty(), "Environmental list should not be empty");
        assertFalse(pestel.get("legal").isEmpty(), "Legal list should not be empty");

        System.out.println("PESTEL Analysis Result: " + pestel);
    }

    @Test
    public void testGeneratePorterForces() {
        String companyName = "OpenAI";

        Map<String, List<String>> porter = analysisService.generatePorterForces(companyName);

        assertNotNull(porter, "Porter Forces map should not be null");
        assertTrue(porter.containsKey("rivalry"), "Porter Forces map should contain 'rivalry' key");
        assertTrue(porter.containsKey("new_entrants"), "Porter Forces map should contain 'new_entrants' key");
        assertTrue(porter.containsKey("substitutes"), "Porter Forces map should contain 'substitutes' key");
        assertTrue(porter.containsKey("buyer_power"), "Porter Forces map should contain 'buyer_power' key");
        assertTrue(porter.containsKey("supplier_power"), "Porter Forces map should contain 'supplier_power' key");

        assertFalse(porter.get("rivalry").isEmpty(), "Rivalry list should not be empty");
        assertFalse(porter.get("new_entrants").isEmpty(), "New Entrants list should not be empty");
        assertFalse(porter.get("substitutes").isEmpty(), "Substitutes list should not be empty");
        assertFalse(porter.get("buyer_power").isEmpty(), "Buyer Power list should not be empty");
        assertFalse(porter.get("supplier_power").isEmpty(), "Supplier Power list should not be empty");

        System.out.println("Porter Forces Result: " + porter);
    }

    @Test
    public void testGenerateBcgMatrix() {
        String companyName = "OpenAI";

        Map<String, Map<String, Double>> bcg = analysisService.generateBcgMatrix(companyName);

        assertNotNull(bcg, "BCG Matrix map should not be null");
        assertFalse(bcg.isEmpty(), "BCG Matrix map should not be empty");

        bcg.forEach((product, values) -> {
            assertNotNull(values, "Values for " + product + " should not be null");
            assertTrue(values.containsKey("market_share"), "Values should contain 'market_share' key");
            assertTrue(values.containsKey("growth_rate"), "Values should contain 'growth_rate' key");
        });

        System.out.println("BCG Matrix Result: " + bcg);
    }

    @Test
    public void testGenerateMckinsey7s() {
        String companyName = "OpenAI";

        Map<String, String> mckinsey = analysisService.generateMckinsey7s(companyName);

        assertNotNull(mckinsey, "McKinsey 7S map should not be null");
        assertTrue(mckinsey.containsKey("strategy"), "McKinsey 7S map should contain 'strategy' key");
        assertTrue(mckinsey.containsKey("structure"), "McKinsey 7S map should contain 'structure' key");
        assertTrue(mckinsey.containsKey("systems"), "McKinsey 7S map should contain 'systems' key");
        assertTrue(mckinsey.containsKey("style"), "McKinsey 7S map should contain 'style' key");
        assertTrue(mckinsey.containsKey("staff"), "McKinsey 7S map should contain 'staff' key");
        assertTrue(mckinsey.containsKey("skills"), "McKinsey 7S map should contain 'skills' key");
        assertTrue(mckinsey.containsKey("shared_values"), "McKinsey 7S map should contain 'shared_values' key");

        assertFalse(mckinsey.get("strategy").isEmpty(), "Strategy should not be empty");

System.out.println("McKinsey 7S Result: " + mckinsey);
    }
}