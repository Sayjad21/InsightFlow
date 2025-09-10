package com.insightflow;

import com.insightflow.utils.AiUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import com.insightflow.services.ComparisonService;
@ExtendWith(MockitoExtension.class)
public class ComparisonServiceTest {

    @Mock
    private AiUtil aiUtil;

    @InjectMocks
    private ComparisonService comparisonService;

    private List<Map<String, Object>> mockAnalyses;

    @BeforeEach
    void setUp() {
        mockAnalyses = new ArrayList<>();
        
        // Create mock analysis for Company A
        Map<String, Object> companyAAnalysis = new HashMap<>();
        companyAAnalysis.put("company_name", "Company A");
        
        // Mock summaries
        companyAAnalysis.put("summaries", Arrays.asList("Summary 1", "Summary 2"));
        
        // Mock SWOT
        Map<String, List<String>> swot = new HashMap<>();
        swot.put("strengths", Arrays.asList("Strong brand", "Innovative products"));
        swot.put("weaknesses", Arrays.asList("High costs", "Limited market share"));
        swot.put("opportunities", Arrays.asList("Market expansion", "New technologies"));
        swot.put("threats", Arrays.asList("Competition", "Regulatory changes"));
        companyAAnalysis.put("swot_lists", swot);
        
        // Mock PESTEL
        Map<String, List<String>> pestel = new HashMap<>();
        pestel.put("political", Arrays.asList("Stable government", "Favorable policies"));
        pestel.put("economic", Arrays.asList("Growing economy", "High disposable income"));
        pestel.put("social", Arrays.asList("Brand conscious", "Tech savvy"));
        pestel.put("technological", Arrays.asList("Advanced R&D", "Digital transformation"));
        pestel.put("environmental", Arrays.asList("Eco-friendly initiatives", "Carbon neutral goals"));
        pestel.put("legal", Arrays.asList("Compliant with regulations", "Strong IP protection"));
        companyAAnalysis.put("pestel_lists", pestel);
        
        // Mock BCG matrix
        Map<String, Map<String, Double>> bcg = new HashMap<>();
        Map<String, Double> product1 = new HashMap<>();
        product1.put("market_share", 25.0);
        product1.put("growth_rate", 20.0); // Increased from 10.0 to 20.0 to trigger insight
        bcg.put("Product 1", product1);
        companyAAnalysis.put("bcg_matrix", bcg);
        
        // Mock LinkedIn analysis
        companyAAnalysis.put("linkedin_analysis", "Strong employee satisfaction, high retention rates");
        
        // Create mock analysis for Company B (similar structure)
        Map<String, Object> companyBAnalysis = new HashMap<>();
        companyBAnalysis.put("company_name", "Company B");
        companyBAnalysis.put("summaries", Arrays.asList("Summary 1", "Summary 2"));
        companyBAnalysis.put("swot_lists", swot);
        companyBAnalysis.put("pestel_lists", pestel);
        
        Map<String, Map<String, Double>> bcgB = new HashMap<>();
        Map<String, Double> productB = new HashMap<>();
        productB.put("market_share", 15.0);
        productB.put("growth_rate", 5.0); // Lower, so the difference is > 5
        bcgB.put("Product 1", productB);
        companyBAnalysis.put("bcg_matrix", bcgB);
        companyBAnalysis.put("linkedin_analysis", "Moderate growth, expanding to new markets");
        
        mockAnalyses.add(companyAAnalysis);
        mockAnalyses.add(companyBAnalysis);
    }

    @Test
    void testComputeComparison() {
        // Mock AI responses
        when(aiUtil.invokeWithTemplate(any(), any())).thenReturn("{\"sentiment_score\": 75.0, \"risk_rating\": 3.0}");
        
        Map<String, Object> result = comparisonService.computeComparison(mockAnalyses);
        
        assertNotNull(result);
        assertTrue(result.containsKey("metrics"));
        assertTrue(result.containsKey("benchmarks"));
        assertTrue(result.containsKey("insights"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Double>> metrics = (List<Map<String, Double>>) result.get("metrics");
        assertEquals(2, metrics.size());
        
        @SuppressWarnings("unchecked")
        Map<String, Double> benchmarks = (Map<String, Double>) result.get("benchmarks");
        assertTrue(benchmarks.containsKey("avg_market_share"));
        assertTrue(benchmarks.containsKey("avg_growth_rate"));
        
        @SuppressWarnings("unchecked")
        List<String> insights = (List<String>) result.get("insights");
        assertTrue(insights.size() > 0); // Change from assertFalse(insights.isEmpty())
    }

    @Test
    void testPrepareAnalysisText() {
        String analysisText =comparisonService.prepareAnalysisText(mockAnalyses.get(0));
        
        assertNotNull(analysisText);
        assertTrue(analysisText.contains("Company Summaries"));
        assertTrue(analysisText.contains("SWOT Analysis"));
        assertTrue(analysisText.contains("PESTEL Analysis"));
        assertTrue(analysisText.contains("LinkedIn Intelligence"));
    }
    
    // Add this helper method to ComparisonService for testing (or use reflection)
    // public String invokePrepareAnalysisText(Map<String, Object> analysis) {
    //     // This would require making prepareAnalysisText protected or package-private
    //     // or using reflection to access the private method
    //     return "Mock analysis text";
    // }
}