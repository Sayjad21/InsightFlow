package com.insightflow.controllers;

import com.insightflow.models.User;
import com.insightflow.models.UserAnalysis;
import com.insightflow.models.UserAnalysis.SwotLists;
import com.insightflow.models.UserAnalysis.PestelLists;
import com.insightflow.models.UserAnalysis.PorterForces;
import com.insightflow.models.UserAnalysis.BcgProduct;
import com.insightflow.models.UserAnalysis.McKinsey7s;
import com.insightflow.services.UserService;
import com.insightflow.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173" })
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody Map<String, String> request) {
        try {
            String firstName = request.getOrDefault("firstName", "");
            String lastName = request.getOrDefault("lastName", "");
            String email = request.get("email");
            String password = request.get("password");
            String username = request.getOrDefault("username", email); // Use email as username if not provided

            String token = userService.signup(username, password, firstName, lastName, email);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("message", "User created successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String email = request.get("email");
            String password = request.get("password");

            String token;
            if (email != null && !email.isEmpty()) {
                token = userService.loginWithEmail(email, password);
            } else {
                token = userService.login(username, password);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("message", "Login successful");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/user/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(@RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            String username = jwtUtil.extractUsername(jwtToken);

            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isEmpty()) {
                // Try to find by email if username lookup fails
                userOpt = userService.findByEmail(username);
            }

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                Map<String, Object> response = new HashMap<>();
                response.put("id", user.getId());
                response.put("firstName", user.getFirstName());
                response.put("lastName", user.getLastName());
                response.put("email", user.getEmail());
                response.put("avatar", user.getAvatar());
                response.put("role", user.getRole());
                response.put("createdAt", user.getCreatedAt());
                response.put("lastLogin", user.getLastLogin());

                // Add analysis statistics
                long totalAnalyses = userService.getUserAnalysisCount(user.getId());
                long successfulAnalyses = userService.getUserSuccessfulAnalysisCount(user.getId());
                response.put("totalAnalyses", totalAnalyses);
                response.put("successfulAnalyses", successfulAnalyses);

                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid token");
            return ResponseEntity.status(401).body(error);
        }
    }

    @GetMapping("/user/analyses")
    public ResponseEntity<Map<String, Object>> getUserAnalyses(@RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            String username = jwtUtil.extractUsername(jwtToken);

            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isEmpty()) {
                userOpt = userService.findByEmail(username);
            }

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                List<UserAnalysis> analyses = userService.getUserAnalyses(user.getId());

                Map<String, Object> response = new HashMap<>();
                response.put("analyses", analyses);
                response.put("total", analyses.size());

                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid token");
            return ResponseEntity.status(401).body(error);
        }
    }

    @PostMapping("/user/analyses")
    public ResponseEntity<Map<String, Object>> saveUserAnalysis(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> analysisData) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            String username = jwtUtil.extractUsername(jwtToken);

            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isEmpty()) {
                userOpt = userService.findByEmail(username);
            }

            if (userOpt.isPresent()) {
                User user = userOpt.get();

                // Debug: Log the received analysis data
                System.out.println("=== ANALYSIS DATA RECEIVED ===");
                System.out.println("Company Name: " + analysisData.get("companyName"));
                System.out.println("Available keys: " + analysisData.keySet());
                for (Map.Entry<String, Object> entry : analysisData.entrySet()) {
                    System.out.println(entry.getKey() + " -> "
                            + (entry.getValue() != null
                                    ? entry.getValue().getClass().getSimpleName() + ": "
                                            + entry.getValue().toString().substring(0,
                                                    Math.min(100, entry.getValue().toString().length()))
                                    : "null"));
                }
                System.out.println("========================");

                // Create UserAnalysis from request data
                UserAnalysis analysis = new UserAnalysis(user.getId(), (String) analysisData.get("companyName"));

                // Set analysis results - using safe casting with instanceof checks
                // Handle both snake_case (from frontend) and camelCase (fallback)
                Object summariesObj = analysisData.get("summaries");
                if (summariesObj instanceof List<?>) {
                    @SuppressWarnings("unchecked")
                    List<String> summaries = (List<String>) summariesObj;
                    analysis.setSummaries(summaries);
                }

                Object sourcesObj = analysisData.get("sources");
                if (sourcesObj instanceof List<?>) {
                    @SuppressWarnings("unchecked")
                    List<String> sources = (List<String>) sourcesObj;
                    analysis.setSources(sources);
                }

                // Handle strategy_recommendations (snake_case from frontend)
                Object strategyObj = analysisData.get("strategy_recommendations");
                if (strategyObj == null) {
                    strategyObj = analysisData.get("strategyRecommendations"); // fallback to camelCase
                }
                if (strategyObj instanceof String) {
                    analysis.setStrategyRecommendations((String) strategyObj);
                }

                // Handle linkedin_analysis (snake_case from frontend)
                Object linkedinObj = analysisData.get("linkedin_analysis");
                if (linkedinObj == null) {
                    linkedinObj = analysisData.get("linkedinAnalysis"); // fallback to camelCase
                }
                if (linkedinObj instanceof String) {
                    analysis.setLinkedinAnalysis((String) linkedinObj);
                }

                if (analysisData.get("uploadedFileName") instanceof String) {
                    analysis.setUploadedFileName((String) analysisData.get("uploadedFileName"));
                }

                // Set SWOT data - handle swot_lists (snake_case from frontend)
                Object swotListsObj = analysisData.get("swot_lists");
                if (swotListsObj == null) {
                    swotListsObj = analysisData.get("swotLists"); // fallback to camelCase
                }
                if (swotListsObj instanceof Map<?, ?>) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> swotData = (Map<String, Object>) swotListsObj;
                    SwotLists swot = new SwotLists();

                    Object strengthsObj = swotData.get("strengths");
                    if (strengthsObj instanceof List<?>) {
                        @SuppressWarnings("unchecked")
                        List<String> strengths = (List<String>) strengthsObj;
                        swot.setStrengths(strengths);
                    }

                    Object weaknessesObj = swotData.get("weaknesses");
                    if (weaknessesObj instanceof List<?>) {
                        @SuppressWarnings("unchecked")
                        List<String> weaknesses = (List<String>) weaknessesObj;
                        swot.setWeaknesses(weaknesses);
                    }

                    Object opportunitiesObj = swotData.get("opportunities");
                    if (opportunitiesObj instanceof List<?>) {
                        @SuppressWarnings("unchecked")
                        List<String> opportunities = (List<String>) opportunitiesObj;
                        swot.setOpportunities(opportunities);
                    }

                    Object threatsObj = swotData.get("threats");
                    if (threatsObj instanceof List<?>) {
                        @SuppressWarnings("unchecked")
                        List<String> threats = (List<String>) threatsObj;
                        swot.setThreats(threats);
                    }

                    analysis.setSwotLists(swot);
                }

                // Set PESTEL data - handle pestel_lists (snake_case from frontend)
                Object pestelListsObj = analysisData.get("pestel_lists");
                if (pestelListsObj == null) {
                    pestelListsObj = analysisData.get("pestelLists"); // fallback to camelCase
                }
                if (pestelListsObj instanceof Map<?, ?>) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> pestelData = (Map<String, Object>) pestelListsObj;
                    PestelLists pestel = new PestelLists();

                    Object politicalObj = pestelData.get("political");
                    if (politicalObj instanceof List<?>) {
                        @SuppressWarnings("unchecked")
                        List<String> political = (List<String>) politicalObj;
                        pestel.setPolitical(political);
                    }

                    Object economicObj = pestelData.get("economic");
                    if (economicObj instanceof List<?>) {
                        @SuppressWarnings("unchecked")
                        List<String> economic = (List<String>) economicObj;
                        pestel.setEconomic(economic);
                    }

                    Object socialObj = pestelData.get("social");
                    if (socialObj instanceof List<?>) {
                        @SuppressWarnings("unchecked")
                        List<String> social = (List<String>) socialObj;
                        pestel.setSocial(social);
                    }

                    Object technologicalObj = pestelData.get("technological");
                    if (technologicalObj instanceof List<?>) {
                        @SuppressWarnings("unchecked")
                        List<String> technological = (List<String>) technologicalObj;
                        pestel.setTechnological(technological);
                    }

                    Object environmentalObj = pestelData.get("environmental");
                    if (environmentalObj instanceof List<?>) {
                        @SuppressWarnings("unchecked")
                        List<String> environmental = (List<String>) environmentalObj;
                        pestel.setEnvironmental(environmental);
                    }

                    Object legalObj = pestelData.get("legal");
                    if (legalObj instanceof List<?>) {
                        @SuppressWarnings("unchecked")
                        List<String> legal = (List<String>) legalObj;
                        pestel.setLegal(legal);
                    }

                    analysis.setPestelLists(pestel);
                }

                // Set images - handle snake_case field names
                Object swotImageObj = analysisData.get("swot_image");
                if (swotImageObj == null)
                    swotImageObj = analysisData.get("swotImage");
                if (swotImageObj instanceof String) {
                    analysis.setSwotImage((String) swotImageObj);
                }

                Object pestelImageObj = analysisData.get("pestel_image");
                if (pestelImageObj == null)
                    pestelImageObj = analysisData.get("pestelImage");
                if (pestelImageObj instanceof String) {
                    analysis.setPestelImage((String) pestelImageObj);
                }

                Object porterImageObj = analysisData.get("porter_image");
                if (porterImageObj == null)
                    porterImageObj = analysisData.get("porterImage");
                if (porterImageObj instanceof String) {
                    analysis.setPorterImage((String) porterImageObj);
                }

                Object bcgImageObj = analysisData.get("bcg_image");
                if (bcgImageObj == null)
                    bcgImageObj = analysisData.get("bcgImage");
                if (bcgImageObj instanceof String) {
                    analysis.setBcgImage((String) bcgImageObj);
                }

                Object mckinseyImageObj = analysisData.get("mckinsey_image");
                if (mckinseyImageObj == null)
                    mckinseyImageObj = analysisData.get("mckinseyImage");
                if (mckinseyImageObj instanceof String) {
                    analysis.setMckinseyImage((String) mckinseyImageObj);
                }

                // Set Porter Forces data - handle porter_forces (snake_case from frontend)
                Object porterForcesObj = analysisData.get("porter_forces");
                if (porterForcesObj == null) {
                    porterForcesObj = analysisData.get("porterForces"); // fallback to camelCase
                }
                if (porterForcesObj instanceof Map<?, ?>) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> porterData = (Map<String, Object>) porterForcesObj;
                    PorterForces porter = new PorterForces();

                    Object rivalryObj = porterData.get("rivalry");
                    if (rivalryObj instanceof List<?>) {
                        @SuppressWarnings("unchecked")
                        List<String> rivalry = (List<String>) rivalryObj;
                        porter.setRivalry(rivalry);
                    }

                    // Handle new_entrants (snake_case from frontend)
                    Object newEntrantsObj = porterData.get("new_entrants");
                    if (newEntrantsObj == null) {
                        newEntrantsObj = porterData.get("newEntrants"); // fallback to camelCase
                    }
                    if (newEntrantsObj instanceof List<?>) {
                        @SuppressWarnings("unchecked")
                        List<String> newEntrants = (List<String>) newEntrantsObj;
                        porter.setNewEntrants(newEntrants);
                    }

                    Object substitutesObj = porterData.get("substitutes");
                    if (substitutesObj instanceof List<?>) {
                        @SuppressWarnings("unchecked")
                        List<String> substitutes = (List<String>) substitutesObj;
                        porter.setSubstitutes(substitutes);
                    }

                    // Handle buyer_power (snake_case from frontend)
                    Object buyerPowerObj = porterData.get("buyer_power");
                    if (buyerPowerObj == null) {
                        buyerPowerObj = porterData.get("buyerPower"); // fallback to camelCase
                    }
                    if (buyerPowerObj instanceof List<?>) {
                        @SuppressWarnings("unchecked")
                        List<String> buyerPower = (List<String>) buyerPowerObj;
                        porter.setBuyerPower(buyerPower);
                    }

                    // Handle supplier_power (snake_case from frontend)
                    Object supplierPowerObj = porterData.get("supplier_power");
                    if (supplierPowerObj == null) {
                        supplierPowerObj = porterData.get("supplierPower"); // fallback to camelCase
                    }
                    if (supplierPowerObj instanceof List<?>) {
                        @SuppressWarnings("unchecked")
                        List<String> supplierPower = (List<String>) supplierPowerObj;
                        porter.setSupplierPower(supplierPower);
                    }

                    analysis.setPorterForces(porter);
                }

                // Set BCG Matrix data - handle bcg_matrix (snake_case from frontend)
                Object bcgMatrixObj = analysisData.get("bcg_matrix");
                if (bcgMatrixObj == null) {
                    bcgMatrixObj = analysisData.get("bcgMatrix"); // fallback to camelCase
                }
                if (bcgMatrixObj instanceof Map<?, ?>) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> bcgData = (Map<String, Object>) bcgMatrixObj;
                    Map<String, BcgProduct> bcgMatrix = new HashMap<>();

                    for (Map.Entry<String, Object> entry : bcgData.entrySet()) {
                        if (entry.getValue() instanceof Map<?, ?>) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> productData = (Map<String, Object>) entry.getValue();

                            BcgProduct product = new BcgProduct();

                            // Handle market_share (snake_case from frontend)
                            Object marketShareObj = productData.get("market_share");
                            if (marketShareObj == null) {
                                marketShareObj = productData.get("marketShare"); // fallback to camelCase
                            }
                            if (marketShareObj instanceof Number) {
                                product.setMarketShare(((Number) marketShareObj).doubleValue());
                            }

                            // Handle growth_rate (snake_case from frontend)
                            Object growthRateObj = productData.get("growth_rate");
                            if (growthRateObj == null) {
                                growthRateObj = productData.get("growthRate"); // fallback to camelCase
                            }
                            if (growthRateObj instanceof Number) {
                                product.setGrowthRate(((Number) growthRateObj).doubleValue());
                            }

                            bcgMatrix.put(entry.getKey(), product);
                        }
                    }

                    analysis.setBcgMatrix(bcgMatrix);
                }

                // Set McKinsey 7S data - handle mckinsey_7s (snake_case from frontend)
                Object mckinsey7sObj = analysisData.get("mckinsey_7s");
                if (mckinsey7sObj == null) {
                    mckinsey7sObj = analysisData.get("mckinsey7s"); // fallback to camelCase
                }
                if (mckinsey7sObj instanceof Map<?, ?>) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> mckinseyData = (Map<String, Object>) mckinsey7sObj;
                    McKinsey7s mckinsey = new McKinsey7s();

                    if (mckinseyData.get("strategy") instanceof String) {
                        mckinsey.setStrategy((String) mckinseyData.get("strategy"));
                    }
                    if (mckinseyData.get("structure") instanceof String) {
                        mckinsey.setStructure((String) mckinseyData.get("structure"));
                    }
                    if (mckinseyData.get("systems") instanceof String) {
                        mckinsey.setSystems((String) mckinseyData.get("systems"));
                    }
                    if (mckinseyData.get("style") instanceof String) {
                        mckinsey.setStyle((String) mckinseyData.get("style"));
                    }
                    if (mckinseyData.get("staff") instanceof String) {
                        mckinsey.setStaff((String) mckinseyData.get("staff"));
                    }
                    if (mckinseyData.get("skills") instanceof String) {
                        mckinsey.setSkills((String) mckinseyData.get("skills"));
                    }

                    // Handle shared_values (snake_case from frontend)
                    Object sharedValuesObj = mckinseyData.get("shared_values");
                    if (sharedValuesObj == null) {
                        sharedValuesObj = mckinseyData.get("sharedValues"); // fallback to camelCase
                    }
                    if (sharedValuesObj instanceof String) {
                        mckinsey.setSharedValues((String) sharedValuesObj);
                    }

                    analysis.setMckinsey7s(mckinsey);
                } // Set status as completed
                analysis.setStatus(UserAnalysis.AnalysisStatus.COMPLETED);

                // Save the analysis
                UserAnalysis savedAnalysis = userService.saveAnalysis(analysis);

                Map<String, Object> response = new HashMap<>();
                response.put("message", "Analysis saved successfully");
                response.put("analysisId", savedAnalysis.getId());
                response.put("companyName", savedAnalysis.getCompanyName());
                response.put("analysisDate", savedAnalysis.getAnalysisDate());

                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.status(404).body(error);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to save analysis: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}