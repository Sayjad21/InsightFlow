package com.insightflow;

import com.insightflow.services.ComparisonService;
import com.insightflow.services.ComparisonVisualizationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

@SpringBootTest
public class RealisticComparisonTest {

    @Autowired
    private ComparisonService comparisonService;

    @Autowired
    private ComparisonVisualizationService visualizationService;

    @Test
    public void testRealisticCompanyComparison() {
        System.out.println("=== REALISTIC COMPANY COMPARISON ANALYSIS ===\n");

        // Create realistic mock data for actual companies
        List<Map<String, Object>> analyses = createRealisticCompanyData();

        // Perform comparison
        Map<String, Object> comparisonResult = comparisonService.computeComparison(analyses);

        // Print detailed analysis to console
        printComparisonResults(analyses, comparisonResult);

        // Generate and display visualization data (Base64 encoded images)
        System.out.println("\n=== VISUALIZATION DATA ===");
        String radarChart = visualizationService.generateRadarChart(comparisonResult);
        String barGraph = visualizationService.generateBarGraph(comparisonResult);
        String scatterPlot = visualizationService.generateScatterPlot(comparisonResult);

        System.out.println("Radar Chart Base64 length: " + radarChart.length());
        System.out.println("Bar Graph Base64 length: " + barGraph.length());
        System.out.println("Scatter Plot Base64 length: " + scatterPlot.length());
        System.out.println("Visualizations generated successfully!");
    }

    private List<Map<String, Object>> createRealisticCompanyData() {
        List<Map<String, Object>> analyses = new ArrayList<>();

        // Tesla Analysis
        analyses.add(createTeslaAnalysis());

        // Toyota Analysis
        analyses.add(createToyotaAnalysis());

        // Ford Analysis
        analyses.add(createFordAnalysis());

        return analyses;
    }

    private Map<String, Object> createTeslaAnalysis() {
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("company_name", "Tesla Inc.");

        // Summaries
        analysis.put("summaries", Arrays.asList(
            "Tesla is a leading electric vehicle manufacturer founded by Elon Musk",
            "Pioneer in autonomous driving technology and sustainable energy solutions",
            "Strong brand recognition and innovative culture driving rapid growth"
        ));

        // SWOT Analysis
        Map<String, List<String>> swot = new HashMap<>();
        swot.put("strengths", Arrays.asList(
            "Industry-leading battery technology",
            "Strong brand and Elon Musk's leadership",
            "Vertical integration (manufacturing, software, charging network)",
            "First-mover advantage in EV market"
        ));
        swot.put("weaknesses", Arrays.asList(
            "High production costs compared to traditional automakers",
            "Quality control issues in early production models",
            "Limited model variety compared to competitors",
            "Dependence on government incentives"
        ));
        swot.put("opportunities", Arrays.asList(
            "Global transition to electric vehicles",
            "Expansion into energy storage and solar products",
            "Autonomous driving technology licensing",
            "Emerging markets adoption"
        ));
        swot.put("threats", Arrays.asList(
            "Intense competition from traditional automakers",
            "Regulatory changes affecting incentives",
            "Supply chain constraints for batteries",
            "Economic downturns affecting luxury purchases"
        ));
        analysis.put("swot_lists", swot);

        // PESTEL Analysis
        Map<String, List<String>> pestel = new HashMap<>();
        pestel.put("political", Arrays.asList(
            "Government subsidies for EV purchases",
            "Carbon emission regulations favoring EVs",
            "Trade policies affecting global operations"
        ));
        pestel.put("economic", Arrays.asList(
            "Strong consumer spending on sustainable products",
            "Rising energy costs increasing EV appeal",
            "Interest rate changes affecting financing"
        ));
        pestel.put("social", Arrays.asList(
            "Growing environmental consciousness",
            "Urbanization increasing demand for clean transportation",
            "Tech-savvy consumers embracing innovation"
        ));
        pestel.put("technological", Arrays.asList(
            "Rapid advancements in battery technology",
            "AI and machine learning for autonomous driving",
            "Improving charging infrastructure"
        ));
        pestel.put("environmental", Arrays.asList(
            "Climate change driving EV adoption",
            "Reduced carbon footprint compared to ICE vehicles",
            "Sustainable manufacturing initiatives"
        ));
        pestel.put("legal", Arrays.asList(
            "Intellectual property protection challenges",
            "Autonomous vehicle regulations",
            "Safety standards and certifications"
        ));
        analysis.put("pestel_lists", pestel);

        // BCG Matrix
        Map<String, Map<String, Double>> bcg = new HashMap<>();
        
        Map<String, Double> model3 = new HashMap<>();
        model3.put("market_share", 18.5);
        model3.put("growth_rate", 25.0);
        bcg.put("Model 3", model3);

        Map<String, Double> modelY = new HashMap<>();
        modelY.put("market_share", 22.3);
        modelY.put("growth_rate", 45.0);
        bcg.put("Model Y", modelY);

        Map<String, Double> energy = new HashMap<>();
        energy.put("market_share", 8.2);
        energy.put("growth_rate", 60.0);
        bcg.put("Energy Storage", energy);

        analysis.put("bcg_matrix", bcg);

        // LinkedIn Analysis
        analysis.put("linkedin_analysis", "Tesla shows strong employee engagement with 4.2/5 rating. " +
            "Key talent in AI and battery technology. High retention in engineering roles. " +
            "Growing presence in European and Asian markets. Strong leadership pipeline.");

        return analysis;
    }

    private Map<String, Object> createToyotaAnalysis() {
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("company_name", "Toyota Motor Corporation");

        // Summaries
        analysis.put("summaries", Arrays.asList(
            "World's largest automaker with strong hybrid technology expertise",
            "Renowned for quality, reliability, and efficient production systems",
            "Global presence with diverse product portfolio across all segments"
        ));

        // SWOT Analysis
        Map<String, List<String>> swot = new HashMap<>();
        swot.put("strengths", Arrays.asList(
            "Strong global brand reputation for quality",
            "Efficient Toyota Production System (TPS)",
            "Diverse product portfolio across all segments",
            "Strong hybrid technology leadership"
        ));
        swot.put("weaknesses", Arrays.asList(
            "Slower EV adoption compared to competitors",
            "Bureaucratic decision-making process",
            "Dependence on North American and Asian markets",
            "Aging product designs in some segments"
        ));
        swot.put("opportunities", Arrays.asList(
            "Expansion of hydrogen fuel cell technology",
            "Growing demand for hybrids in emerging markets",
            "Autonomous driving technology partnerships",
            "Subscription-based mobility services"
        ));
        swot.put("threats", Arrays.asList(
            "Rapid shift to pure electric vehicles",
            "Intense competition in all markets",
            "Trade tensions affecting global operations",
            "Changing consumer preferences"
        ));
        analysis.put("swot_lists", swot);

        // BCG Matrix
        Map<String, Map<String, Double>> bcg = new HashMap<>();
        
        Map<String, Double> camry = new HashMap<>();
        camry.put("market_share", 15.8);
        camry.put("growth_rate", 5.0);
        bcg.put("Camry", camry);

        Map<String, Double> rav4 = new HashMap<>();
        rav4.put("market_share", 20.1);
        rav4.put("growth_rate", 12.0);
        bcg.put("RAV4", rav4);

        Map<String, Double> prius = new HashMap<>();
        prius.put("market_share", 9.5);
        prius.put("growth_rate", -2.0);
        bcg.put("Prius", prius);

        analysis.put("bcg_matrix", bcg);

        // LinkedIn Analysis
        analysis.put("linkedin_analysis", "Toyota maintains strong corporate culture with 4.0/5 employee satisfaction. " +
            "Stable workforce with low turnover. Strong presence in manufacturing and engineering talent. " +
            "Expanding R&D in autonomous and electric vehicles. Good diversity and inclusion metrics.");

        return analysis;
    }

    private Map<String, Object> createFordAnalysis() {
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("company_name", "Ford Motor Company");

        // Summaries
        analysis.put("summaries", Arrays.asList(
            "Iconic American automaker with strong truck and SUV lineup",
            "Undergoing major transformation toward electric vehicles",
            "Strong commercial vehicle business and brand heritage"
        ));

        // SWOT Analysis
        Map<String, List<String>> swot = new HashMap<>();
        swot.put("strengths", Arrays.asList(
            "Strong brand heritage and customer loyalty",
            "Market leadership in trucks and commercial vehicles",
            "Global manufacturing footprint",
            "Strong dealership network"
        ));
        swot.put("weaknesses", Arrays.asList(
            "High legacy costs and pension obligations",
            "Slower innovation compared to new entrants",
            "Dependence on North American market",
            "Quality perception issues in some segments"
        ));
        swot.put("opportunities", Arrays.asList(
            "Electric F-150 and Mustang Mach-E success",
            "Commercial vehicle electrification",
            "Subscription and mobility services",
            "Emerging market expansion"
        ));
        swot.put("threats", Arrays.asList(
            "Intense competition in EV space",
            "Economic cycles affecting truck sales",
            "Supply chain disruptions",
            "Regulatory compliance costs"
        ));
        analysis.put("swot_lists", swot);

        // BCG Matrix
        Map<String, Map<String, Double>> bcg = new HashMap<>();
        
        Map<String, Double> f150 = new HashMap<>();
        f150.put("market_share", 35.2);
        f150.put("growth_rate", 8.0);
        bcg.put("F-150", f150);

        Map<String, Double> mustang = new HashMap<>();
        mustang.put("market_share", 12.4);
        mustang.put("growth_rate", 15.0);
        bcg.put("Mustang Mach-E", mustang);

        Map<String, Double> transit = new HashMap<>();
        transit.put("market_share", 28.7);
        transit.put("growth_rate", 6.0);
        bcg.put("Transit", transit);

        analysis.put("bcg_matrix", bcg);

        // LinkedIn Analysis
        analysis.put("linkedin_analysis", "Ford is undergoing cultural transformation with 3.8/5 employee rating. " +
            "Strong engineering talent in Michigan. Challenges in retaining software talent. " +
            "Good diversity initiatives. Focus on EV and autonomous vehicle recruitment.");

        return analysis;
    }

    private void printComparisonResults(List<Map<String, Object>> analyses, Map<String, Object> comparisonResult) {
        System.out.println("=== COMPANY COMPARISON RESULTS ===\n");

        // Print individual company metrics
        @SuppressWarnings("unchecked")
        List<Map<String, Double>> metrics = (List<Map<String, Double>>) comparisonResult.get("metrics");
        
        for (int i = 0; i < analyses.size(); i++) {
            String companyName = (String) analyses.get(i).get("company_name");
            Map<String, Double> companyMetrics = metrics.get(i);
            
            System.out.println("COMPANY: " + companyName);
            System.out.println("Market Share: " + String.format("%.1f%%", companyMetrics.get("market_share")));
            System.out.println("Growth Rate: " + String.format("%.1f%%", companyMetrics.get("growth_rate")));
            System.out.println("Sentiment Score: " + String.format("%.0f/100", companyMetrics.get("sentiment_score")));
            System.out.println("Risk Rating: " + String.format("%.1f/10", companyMetrics.get("risk_rating")));
            System.out.println("----------------------------------------");
        }

        // Print benchmarks
        @SuppressWarnings("unchecked")
        Map<String, Double> benchmarks = (Map<String, Double>) comparisonResult.get("benchmarks");
        
        System.out.println("\n=== INDUSTRY BENCHMARKS ===");
        System.out.println("Average Market Share: " + String.format("%.1f%%", benchmarks.get("avg_market_share")));
        System.out.println("Average Growth Rate: " + String.format("%.1f%%", benchmarks.get("avg_growth_rate")));
        System.out.println("Average Sentiment: " + String.format("%.0f/100", benchmarks.get("avg_sentiment_score")));
        System.out.println("Average Risk Rating: " + String.format("%.1f/10", benchmarks.get("avg_risk_rating")));

        // Print insights
        @SuppressWarnings("unchecked")
        List<String> insights = (List<String>) comparisonResult.get("insights");
        
        System.out.println("\n=== STRATEGIC INSIGHTS ===");
        for (String insight : insights) {
            System.out.println("• " + insight);
        }

        // Print detailed SWOT comparison
        System.out.println("\n=== SWOT COMPARISON ===");
        for (Map<String, Object> analysis : analyses) {
            String companyName = (String) analysis.get("company_name");
            @SuppressWarnings("unchecked")
            Map<String, List<String>> swot = (Map<String, List<String>>) analysis.get("swot_lists");
            
            System.out.println("\n" + companyName + " - Key Strengths:");
            swot.get("strengths").stream().limit(3).forEach(s -> System.out.println("  ✓ " + s));
        }

        // Print BCG matrix analysis
        System.out.println("\n=== BCG MATRIX ANALYSIS ===");
        for (Map<String, Object> analysis : analyses) {
            String companyName = (String) analysis.get("company_name");
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Double>> bcg = (Map<String, Map<String, Double>>) analysis.get("bcg_matrix");
            
            System.out.println("\n" + companyName + " Product Portfolio:");
            bcg.forEach((product, data) -> {
                String classification = classifyBcgProduct(data.get("market_share"), data.get("growth_rate"));
                System.out.println(String.format("  %s: %.1f%% share, %.1f%% growth (%s)", 
                    product, data.get("market_share"), data.get("growth_rate"), classification));
            });
        }
    }

    private String classifyBcgProduct(double marketShare, double growthRate) {
        if (marketShare > 15 && growthRate > 10) return "Star";
        if (marketShare > 15 && growthRate <= 10) return "Cash Cow";
        if (marketShare <= 15 && growthRate > 10) return "Question Mark";
        return "Dog";
    }
}