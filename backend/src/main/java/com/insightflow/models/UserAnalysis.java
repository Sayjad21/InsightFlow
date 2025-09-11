package com.insightflow.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "user_analyses")
public class UserAnalysis {

    @Id
    private String id;
    private String userId;
    private String companyName;
    private LocalDateTime analysisDate;
    private AnalysisStatus status;
    private String errorMessage;

    // Analysis results
    private List<String> summaries;
    private List<String> sources;
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
    private String linkedinAnalysis;

    // File information
    private String uploadedFileName;
    private String uploadedFileId;

    public enum AnalysisStatus {
        PENDING, COMPLETED, FAILED
    }

    // Constructors
    public UserAnalysis() {
        this.analysisDate = LocalDateTime.now();
        this.status = AnalysisStatus.PENDING;
    }

    public UserAnalysis(String userId, String companyName) {
        this();
        this.userId = userId;
        this.companyName = companyName;
    }

    // Nested classes for complex data structures
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

    // Main class Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public LocalDateTime getAnalysisDate() {
        return analysisDate;
    }

    public void setAnalysisDate(LocalDateTime analysisDate) {
        this.analysisDate = analysisDate;
    }

    public AnalysisStatus getStatus() {
        return status;
    }

    public void setStatus(AnalysisStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<String> getSummaries() {
        return summaries;
    }

    public void setSummaries(List<String> summaries) {
        this.summaries = summaries;
    }

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
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

    public String getLinkedinAnalysis() {
        return linkedinAnalysis;
    }

    public void setLinkedinAnalysis(String linkedinAnalysis) {
        this.linkedinAnalysis = linkedinAnalysis;
    }

    public String getUploadedFileName() {
        return uploadedFileName;
    }

    public void setUploadedFileName(String uploadedFileName) {
        this.uploadedFileName = uploadedFileName;
    }

    public String getUploadedFileId() {
        return uploadedFileId;
    }

    public void setUploadedFileId(String uploadedFileId) {
        this.uploadedFileId = uploadedFileId;
    }
}
