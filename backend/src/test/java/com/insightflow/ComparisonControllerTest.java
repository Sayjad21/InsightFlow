package com.insightflow;

import com.insightflow.controllers.ComparisonController;
import com.insightflow.dto.ComparisonRequest;
import com.insightflow.models.User;
import com.insightflow.models.UserAnalysis;
import com.insightflow.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ComparisonControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private RagService ragService;

    @Mock
    private AnalysisService analysisService;

    @Mock
    private ScrapingService scrapingService;

    @Mock
    private ComparisonVisualizationService visualizationService;

    @Mock
    private ComparisonService comparisonService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ComparisonController comparisonController;

    private UserAnalysis mockUserAnalysis;
    private ComparisonRequest mockRequest;

    @BeforeEach
    void setUp() {
        // Mock authentication with lenient stubbing since not all tests need it
        lenient().when(authentication.getName()).thenReturn("testuser");

        // Mock user resolution - the controller now needs to resolve username to user
        // ID
        User mockUser = new User();
        mockUser.setId("testuser"); // For test purposes, use same ID as username
        mockUser.setUsername("testuser");
        mockUser.setEmail("testuser@example.com");

        // Mock the user service methods for user resolution
        lenient().when(userService.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        lenient().when(userService.findByEmail("testuser")).thenReturn(Optional.of(mockUser));

        // Create mock UserAnalysis
        mockUserAnalysis = new UserAnalysis();
        mockUserAnalysis.setId("analysis123");
        mockUserAnalysis.setUserId("testuser");
        mockUserAnalysis.setCompanyName("Test Company");
        mockUserAnalysis.setStatus(UserAnalysis.AnalysisStatus.COMPLETED);
        mockUserAnalysis.setAnalysisDate(LocalDateTime.now());
        mockUserAnalysis.setSummaries(Arrays.asList("Summary 1", "Summary 2"));
        mockUserAnalysis.setSources(Arrays.asList("Source 1", "Source 2"));
        mockUserAnalysis.setStrategyRecommendations("Test recommendations");

        // Set up SWOT
        UserAnalysis.SwotLists swot = new UserAnalysis.SwotLists();
        swot.setStrengths(Arrays.asList("Strong brand"));
        swot.setWeaknesses(Arrays.asList("High costs"));
        swot.setOpportunities(Arrays.asList("Market expansion"));
        swot.setThreats(Arrays.asList("Competition"));
        mockUserAnalysis.setSwotLists(swot);

        // Set up Porter Forces
        UserAnalysis.PorterForces porter = new UserAnalysis.PorterForces();
        porter.setRivalry(Arrays.asList("High rivalry"));
        porter.setNewEntrants(Arrays.asList("Low barriers"));
        porter.setSubstitutes(Arrays.asList("Few substitutes"));
        porter.setBuyerPower(Arrays.asList("Medium power"));
        porter.setSupplierPower(Arrays.asList("Low power"));
        mockUserAnalysis.setPorterForces(porter);

        // Set up BCG Matrix
        Map<String, UserAnalysis.BcgProduct> bcg = new HashMap<>();
        bcg.put("Product A", new UserAnalysis.BcgProduct(0.8, 0.6));
        mockUserAnalysis.setBcgMatrix(bcg);

        // Set up McKinsey 7S
        UserAnalysis.McKinsey7s mckinsey = new UserAnalysis.McKinsey7s();
        mckinsey.setStrategy("Test strategy");
        mckinsey.setStructure("Test structure");
        mckinsey.setSystems("Test systems");
        mckinsey.setStyle("Test style");
        mckinsey.setStaff("Test staff");
        mckinsey.setSkills("Test skills");
        mckinsey.setSharedValues("Test values");
        mockUserAnalysis.setMckinsey7s(mckinsey);

        mockUserAnalysis.setLinkedinAnalysis("LinkedIn analysis data");

        // Create mock comparison request
        mockRequest = new ComparisonRequest();
        mockRequest.setAnalysisIds(Arrays.asList("analysis123", "analysis456"));
        mockRequest.setComparisonType("existing");
    }

    @Test
    void testGetUserAnalyses() {
        // Arrange
        List<UserAnalysis> analyses = Arrays.asList(mockUserAnalysis);
        when(userService.getUserAnalyses("testuser")).thenReturn(analyses);

        // Act
        ResponseEntity<List<Map<String, Object>>> response = comparisonController.getUserAnalyses(authentication);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());

        Map<String, Object> analysisInfo = response.getBody().get(0);
        assertEquals("analysis123", analysisInfo.get("id"));
        assertEquals("Test Company", analysisInfo.get("companyName"));
        assertNotNull(analysisInfo.get("analysisDate"));
        assertEquals(false, analysisInfo.get("hasFile"));
    }

    @Test
    void testGetAnalysisById_Success() {
        // Arrange
        when(userService.getAnalysisById("analysis123")).thenReturn(Optional.of(mockUserAnalysis));

        // Act
        ResponseEntity<Map<String, Object>> response = comparisonController.getAnalysisById("analysis123",
                authentication);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Test Company", response.getBody().get("company_name"));
        assertEquals("analysis123", response.getBody().get("id"));
        assertEquals(UserAnalysis.AnalysisStatus.COMPLETED, response.getBody().get("status"));
    }

    @Test
    void testGetAnalysisById_NotFound() {
        // Arrange
        when(userService.getAnalysisById("nonexistent")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Map<String, Object>> response = comparisonController.getAnalysisById("nonexistent",
                authentication);

        // Assert
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testGetAnalysisById_UnauthorizedAccess() {
        // Arrange
        mockUserAnalysis.setUserId("differentuser");
        when(userService.getAnalysisById("analysis123")).thenReturn(Optional.of(mockUserAnalysis));

        // Act
        ResponseEntity<Map<String, Object>> response = comparisonController.getAnalysisById("analysis123",
                authentication);

        // Assert
        assertEquals(403, response.getStatusCode().value());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("Access denied", response.getBody().get("error"));
    }

    @Test
    void testCompareExistingAnalyses_Success() {
        // Arrange
        UserAnalysis secondAnalysis = new UserAnalysis();
        secondAnalysis.setId("analysis456");
        secondAnalysis.setUserId("testuser");
        secondAnalysis.setCompanyName("Second Company");
        secondAnalysis.setStatus(UserAnalysis.AnalysisStatus.COMPLETED);
        secondAnalysis.setSummaries(Arrays.asList("Summary A", "Summary B"));

        when(userService.getAnalysisById("analysis123")).thenReturn(Optional.of(mockUserAnalysis));
        when(userService.getAnalysisById("analysis456")).thenReturn(Optional.of(secondAnalysis));

        Map<String, Object> mockComparisonData = new HashMap<>();
        mockComparisonData.put("benchmarks", new HashMap<>());
        mockComparisonData.put("metrics", new HashMap<>());
        mockComparisonData.put("insights", "Test insights");

        when(comparisonService.computeComparison(anyList())).thenReturn(mockComparisonData);
        when(visualizationService.generateRadarChart(any())).thenReturn("radar_chart_data");
        when(visualizationService.generateBarGraph(any())).thenReturn("bar_graph_data");
        when(visualizationService.generateScatterPlot(any())).thenReturn("scatter_plot_data");

        Map<String, List<String>> request = new HashMap<>();
        request.put("analysisIds", Arrays.asList("analysis123", "analysis456"));

        // Act
        ResponseEntity<Map<String, Object>> response = comparisonController.compareExistingAnalyses(request,
                authentication);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().get("requested_by"));
        assertEquals("existing_analyses", response.getBody().get("comparison_type"));
        assertTrue(response.getBody().containsKey("analyses"));
        assertTrue(response.getBody().containsKey("radar_chart"));
        assertTrue(response.getBody().containsKey("bar_graph"));
        assertTrue(response.getBody().containsKey("scatter_plot"));
    }

    @Test
    void testCompareExistingAnalyses_InvalidRequest() {
        // Arrange
        Map<String, List<String>> request = new HashMap<>();
        request.put("analysisIds", Arrays.asList("only_one"));

        // Act
        ResponseEntity<Map<String, Object>> response = comparisonController.compareExistingAnalyses(request,
                authentication);

        // Assert
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().containsKey("error"));
        assertTrue(response.getBody().get("error").toString().contains("Provide 2 to 5 analysis IDs"));
    }

    @Test
    void testCompareEnhanced_MixedRequest() {
        // Arrange
        ComparisonRequest request = new ComparisonRequest();
        request.setAnalysisIds(Arrays.asList("analysis123"));
        request.setCompanyNames(Arrays.asList("New Company"));
        request.setComparisonType("mixed");

        when(userService.getAnalysisById("analysis123")).thenReturn(Optional.of(mockUserAnalysis));

        // Mock services for new company analysis
        Map<String, Object> ragResult = new HashMap<>();
        ragResult.put("summaries", Arrays.asList("New summary"));
        ragResult.put("sources", Arrays.asList("New source"));
        ragResult.put("strategy_recommendations", "New recommendations");

        when(ragService.analyzeCompetitor(null, "New Company")).thenReturn(ragResult);
        when(analysisService.generateSwot("New Company")).thenReturn(new HashMap<>());
        when(analysisService.generatePestel("New Company")).thenReturn(new HashMap<>());
        when(analysisService.generatePorterForces("New Company")).thenReturn(new HashMap<>());
        when(analysisService.generateBcgMatrix("New Company")).thenReturn(new HashMap<>());
        when(analysisService.generateMckinsey7s("New Company")).thenReturn(new HashMap<>());
        when(scrapingService.getLinkedInAnalysis("New Company")).thenReturn("LinkedIn data");

        Map<String, Object> mockComparisonData = new HashMap<>();
        mockComparisonData.put("benchmarks", new HashMap<>());
        mockComparisonData.put("metrics", new HashMap<>());
        mockComparisonData.put("insights", "Mixed insights");

        when(comparisonService.computeComparison(anyList())).thenReturn(mockComparisonData);
        when(visualizationService.generateRadarChart(any())).thenReturn("radar_chart_data");
        when(visualizationService.generateBarGraph(any())).thenReturn("bar_graph_data");
        when(visualizationService.generateScatterPlot(any())).thenReturn("scatter_plot_data");

        // Act
        ResponseEntity<Map<String, Object>> response = comparisonController.compareEnhanced(request, authentication);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("mixed", response.getBody().get("comparison_type"));
        assertEquals(2, response.getBody().get("total_items"));
        assertEquals(1, response.getBody().get("existing_analyses"));
        assertEquals(1, response.getBody().get("new_analyses"));
    }

    @Test
    void testComparisonRequest_Validation() {
        // Test valid request
        ComparisonRequest validRequest = new ComparisonRequest();
        validRequest.setAnalysisIds(Arrays.asList("id1", "id2"));
        assertTrue(validRequest.isValid());
        assertEquals(2, validRequest.getTotalItemsCount());

        // Test invalid request (too few items)
        ComparisonRequest invalidRequest = new ComparisonRequest();
        invalidRequest.setAnalysisIds(Arrays.asList("id1"));
        assertFalse(invalidRequest.isValid());

        // Test invalid request (too many items)
        ComparisonRequest tooManyRequest = new ComparisonRequest();
        tooManyRequest.setAnalysisIds(Arrays.asList("id1", "id2", "id3", "id4", "id5", "id6"));
        assertFalse(tooManyRequest.isValid());
    }
}