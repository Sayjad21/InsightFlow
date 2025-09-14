package com.insightflow.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "comparison_results")
public class ComparisonResult {

    @Id
    private String id;
    private String requestedBy; // User email/ID who requested the comparison
    private LocalDateTime comparisonDate;
    private String comparisonType; // "existing", "enhanced", "mixed"
    private List<String> savedAnalysisIds; // IDs of saved analyses used in comparison

    // Company analyses included in comparison
    private List<CompanyAnalysis> analyses;

    // Comparison metrics and benchmarks
    private List<ComparisonMetric> metrics;
    private ComparisonBenchmarks benchmarks;

    // Analysis insights and recommendations
    private List<String> insights;
    private String investmentRecommendations;

    // Visualization data (base64 encoded images)
    private String radarChart;
    private String barGraph;
    private String scatterPlot;

    // Constructors
    public ComparisonResult() {
        this.comparisonDate = LocalDateTime.now();
    }

    public ComparisonResult(String requestedBy, String comparisonType) {
        this();
        this.requestedBy = requestedBy;
        this.comparisonType = comparisonType;
    }

    // Nested classes for complex data structures
    public static class CompanyAnalysis {
        private String companyName;
        private String analysisId; // Reference to UserAnalysis if available
        private List<String> summaries;
        private List<String> competitorAnalysis;
        private String strategyRecommendations;
        private SwotLists swotLists;
        private String swotImage;
        private PestelLists pestelLists;
        private String pestelImage;
        private PorterForces porterForces;
        private String porterImage;
        private Map<String, BcgProduct> bcgMatrix;
        private String bcgImage;
        private McKinsey7s mckinsey7s;
        private String mckinseyImage;
        private List<String> sources;
        private String linkedinAnalysis;
        private String ragContext;

        public CompanyAnalysis() {
        }

        public CompanyAnalysis(String companyName) {
            this.companyName = companyName;
        }

        // Getters and Setters
        public String getCompanyName() {
            return companyName;
        }

        public void setCompanyName(String companyName) {
            this.companyName = companyName;
        }

        public String getAnalysisId() {
            return analysisId;
        }

        public void setAnalysisId(String analysisId) {
            this.analysisId = analysisId;
        }

        public List<String> getSummaries() {
            return summaries;
        }

        public void setSummaries(List<String> summaries) {
            this.summaries = summaries;
        }

        public List<String> getCompetitorAnalysis() {
            return competitorAnalysis;
        }

        public void setCompetitorAnalysis(List<String> competitorAnalysis) {
            this.competitorAnalysis = competitorAnalysis;
        }

        public String getStrategyRecommendations() {
            return strategyRecommendations;
        }

        public void setStrategyRecommendations(String strategyRecommendations) {
            this.strategyRecommendations = strategyRecommendations;
        }

        public SwotLists getSwotLists() {
            return swotLists;
        }

        public void setSwotLists(SwotLists swotLists) {
            this.swotLists = swotLists;
        }

        public String getSwotImage() {
            return swotImage;
        }

        public void setSwotImage(String swotImage) {
            this.swotImage = swotImage;
        }

        public PestelLists getPestelLists() {
            return pestelLists;
        }

        public void setPestelLists(PestelLists pestelLists) {
            this.pestelLists = pestelLists;
        }

        public String getPestelImage() {
            return pestelImage;
        }

        public void setPestelImage(String pestelImage) {
            this.pestelImage = pestelImage;
        }

        public PorterForces getPorterForces() {
            return porterForces;
        }

        public void setPorterForces(PorterForces porterForces) {
            this.porterForces = porterForces;
        }

        public String getPorterImage() {
            return porterImage;
        }

        public void setPorterImage(String porterImage) {
            this.porterImage = porterImage;
        }

        public Map<String, BcgProduct> getBcgMatrix() {
            return bcgMatrix;
        }

        public void setBcgMatrix(Map<String, BcgProduct> bcgMatrix) {
            this.bcgMatrix = bcgMatrix;
        }

        public String getBcgImage() {
            return bcgImage;
        }

        public void setBcgImage(String bcgImage) {
            this.bcgImage = bcgImage;
        }

        public McKinsey7s getMckinsey7s() {
            return mckinsey7s;
        }

        public void setMckinsey7s(McKinsey7s mckinsey7s) {
            this.mckinsey7s = mckinsey7s;
        }

        public String getMckinseyImage() {
            return mckinseyImage;
        }

        public void setMckinseyImage(String mckinseyImage) {
            this.mckinseyImage = mckinseyImage;
        }

        public List<String> getSources() {
            return sources;
        }

        public void setSources(List<String> sources) {
            this.sources = sources;
        }

        public String getLinkedinAnalysis() {
            return linkedinAnalysis;
        }

        public void setLinkedinAnalysis(String linkedinAnalysis) {
            this.linkedinAnalysis = linkedinAnalysis;
        }

        public String getRagContext() {
            return ragContext;
        }

        public void setRagContext(String ragContext) {
            this.ragContext = ragContext;
        }
    }

    public static class ComparisonMetric {
        private Double sentimentScore;
        private Double growthRate;
        private Double riskRating;
        private Double marketShare;

        public ComparisonMetric() {
        }

        public ComparisonMetric(Double sentimentScore, Double growthRate, Double riskRating, Double marketShare) {
            this.sentimentScore = sentimentScore;
            this.growthRate = growthRate;
            this.riskRating = riskRating;
            this.marketShare = marketShare;
        }

        // Getters and Setters
        public Double getSentimentScore() {
            return sentimentScore;
        }

        public void setSentimentScore(Double sentimentScore) {
            this.sentimentScore = sentimentScore;
        }

        public Double getGrowthRate() {
            return growthRate;
        }

        public void setGrowthRate(Double growthRate) {
            this.growthRate = growthRate;
        }

        public Double getRiskRating() {
            return riskRating;
        }

        public void setRiskRating(Double riskRating) {
            this.riskRating = riskRating;
        }

        public Double getMarketShare() {
            return marketShare;
        }

        public void setMarketShare(Double marketShare) {
            this.marketShare = marketShare;
        }
    }

    public static class ComparisonBenchmarks {
        private Double avgMarketShare;
        private Double avgGrowthRate;
        private Double avgRiskRating;
        private Double avgSentimentScore;

        public ComparisonBenchmarks() {
        }

        public ComparisonBenchmarks(Double avgMarketShare, Double avgGrowthRate,
                Double avgRiskRating, Double avgSentimentScore) {
            this.avgMarketShare = avgMarketShare;
            this.avgGrowthRate = avgGrowthRate;
            this.avgRiskRating = avgRiskRating;
            this.avgSentimentScore = avgSentimentScore;
        }

        // Getters and Setters
        public Double getAvgMarketShare() {
            return avgMarketShare;
        }

        public void setAvgMarketShare(Double avgMarketShare) {
            this.avgMarketShare = avgMarketShare;
        }

        public Double getAvgGrowthRate() {
            return avgGrowthRate;
        }

        public void setAvgGrowthRate(Double avgGrowthRate) {
            this.avgGrowthRate = avgGrowthRate;
        }

        public Double getAvgRiskRating() {
            return avgRiskRating;
        }

        public void setAvgRiskRating(Double avgRiskRating) {
            this.avgRiskRating = avgRiskRating;
        }

        public Double getAvgSentimentScore() {
            return avgSentimentScore;
        }

        public void setAvgSentimentScore(Double avgSentimentScore) {
            this.avgSentimentScore = avgSentimentScore;
        }
    }

    // Reuse nested classes from UserAnalysis for consistency
    public static class SwotLists {
        private List<String> strengths;
        private List<String> weaknesses;
        private List<String> opportunities;
        private List<String> threats;

        public SwotLists() {
        }

        // Getters and Setters
        public List<String> getStrengths() {
            return strengths;
        }

        public void setStrengths(List<String> strengths) {
            this.strengths = strengths;
        }

        public List<String> getWeaknesses() {
            return weaknesses;
        }

        public void setWeaknesses(List<String> weaknesses) {
            this.weaknesses = weaknesses;
        }

        public List<String> getOpportunities() {
            return opportunities;
        }

        public void setOpportunities(List<String> opportunities) {
            this.opportunities = opportunities;
        }

        public List<String> getThreats() {
            return threats;
        }

        public void setThreats(List<String> threats) {
            this.threats = threats;
        }
    }

    public static class PestelLists {
        private List<String> political;
        private List<String> economic;
        private List<String> social;
        private List<String> technological;
        private List<String> environmental;
        private List<String> legal;

        public PestelLists() {
        }

        // Getters and Setters
        public List<String> getPolitical() {
            return political;
        }

        public void setPolitical(List<String> political) {
            this.political = political;
        }

        public List<String> getEconomic() {
            return economic;
        }

        public void setEconomic(List<String> economic) {
            this.economic = economic;
        }

        public List<String> getSocial() {
            return social;
        }

        public void setSocial(List<String> social) {
            this.social = social;
        }

        public List<String> getTechnological() {
            return technological;
        }

        public void setTechnological(List<String> technological) {
            this.technological = technological;
        }

        public List<String> getEnvironmental() {
            return environmental;
        }

        public void setEnvironmental(List<String> environmental) {
            this.environmental = environmental;
        }

        public List<String> getLegal() {
            return legal;
        }

        public void setLegal(List<String> legal) {
            this.legal = legal;
        }
    }

    // Additional nested classes for Porter Forces, BCG Matrix, and McKinsey 7S
    public static class PorterForces {
        private List<String> rivalry;
        private List<String> newEntrants;
        private List<String> substitutes;
        private List<String> buyerPower;
        private List<String> supplierPower;

        public PorterForces() {
        }

        // Getters and Setters
        public List<String> getRivalry() {
            return rivalry;
        }

        public void setRivalry(List<String> rivalry) {
            this.rivalry = rivalry;
        }

        public List<String> getNewEntrants() {
            return newEntrants;
        }

        public void setNewEntrants(List<String> newEntrants) {
            this.newEntrants = newEntrants;
        }

        public List<String> getSubstitutes() {
            return substitutes;
        }

        public void setSubstitutes(List<String> substitutes) {
            this.substitutes = substitutes;
        }

        public List<String> getBuyerPower() {
            return buyerPower;
        }

        public void setBuyerPower(List<String> buyerPower) {
            this.buyerPower = buyerPower;
        }

        public List<String> getSupplierPower() {
            return supplierPower;
        }

        public void setSupplierPower(List<String> supplierPower) {
            this.supplierPower = supplierPower;
        }
    }

    public static class BcgProduct {
        private double marketShare;
        private double growthRate;

        public BcgProduct() {
        }

        public BcgProduct(double marketShare, double growthRate) {
            this.marketShare = marketShare;
            this.growthRate = growthRate;
        }

        // Getters and Setters
        public double getMarketShare() {
            return marketShare;
        }

        public void setMarketShare(double marketShare) {
            this.marketShare = marketShare;
        }

        public double getGrowthRate() {
            return growthRate;
        }

        public void setGrowthRate(double growthRate) {
            this.growthRate = growthRate;
        }
    }

    public static class McKinsey7s {
        private String strategy;
        private String structure;
        private String systems;
        private String style;
        private String staff;
        private String skills;
        private String sharedValues;

        public McKinsey7s() {
        }

        // Getters and Setters
        public String getStrategy() {
            return strategy;
        }

        public void setStrategy(String strategy) {
            this.strategy = strategy;
        }

        public String getStructure() {
            return structure;
        }

        public void setStructure(String structure) {
            this.structure = structure;
        }

        public String getSystems() {
            return systems;
        }

        public void setSystems(String systems) {
            this.systems = systems;
        }

        public String getStyle() {
            return style;
        }

        public void setStyle(String style) {
            this.style = style;
        }

        public String getStaff() {
            return staff;
        }

        public void setStaff(String staff) {
            this.staff = staff;
        }

        public String getSkills() {
            return skills;
        }

        public void setSkills(String skills) {
            this.skills = skills;
        }

        public String getSharedValues() {
            return sharedValues;
        }

        public void setSharedValues(String sharedValues) {
            this.sharedValues = sharedValues;
        }
    }

    // Main getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

    public LocalDateTime getComparisonDate() {
        return comparisonDate;
    }

    public void setComparisonDate(LocalDateTime comparisonDate) {
        this.comparisonDate = comparisonDate;
    }

    public String getComparisonType() {
        return comparisonType;
    }

    public void setComparisonType(String comparisonType) {
        this.comparisonType = comparisonType;
    }

    public List<String> getSavedAnalysisIds() {
        return savedAnalysisIds;
    }

    public void setSavedAnalysisIds(List<String> savedAnalysisIds) {
        this.savedAnalysisIds = savedAnalysisIds;
    }

    public List<CompanyAnalysis> getAnalyses() {
        return analyses;
    }

    public void setAnalyses(List<CompanyAnalysis> analyses) {
        this.analyses = analyses;
    }

    public List<ComparisonMetric> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<ComparisonMetric> metrics) {
        this.metrics = metrics;
    }

    public ComparisonBenchmarks getBenchmarks() {
        return benchmarks;
    }

    public void setBenchmarks(ComparisonBenchmarks benchmarks) {
        this.benchmarks = benchmarks;
    }

    public List<String> getInsights() {
        return insights;
    }

    public void setInsights(List<String> insights) {
        this.insights = insights;
    }

    public String getInvestmentRecommendations() {
        return investmentRecommendations;
    }

    public void setInvestmentRecommendations(String investmentRecommendations) {
        this.investmentRecommendations = investmentRecommendations;
    }

    public String getRadarChart() {
        return radarChart;
    }

    public void setRadarChart(String radarChart) {
        this.radarChart = radarChart;
    }

    public String getBarGraph() {
        return barGraph;
    }

    public void setBarGraph(String barGraph) {
        this.barGraph = barGraph;
    }

    public String getScatterPlot() {
        return scatterPlot;
    }

    public void setScatterPlot(String scatterPlot) {
        this.scatterPlot = scatterPlot;
    }
}