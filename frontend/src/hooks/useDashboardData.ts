import { useState, useEffect, useCallback } from "react";
import { useLocation } from "react-router-dom";
import { ApiService, type UserProfileResponse } from "../services/api";
import type { UserAnalysis, ComparisonResult } from "../types";

export const useDashboardData = () => {
  const location = useLocation();

  // Tab state - get from URL parameter
  const [activeTab, setActiveTab] = useState<
    | "analysis"
    | "comparison"
    | "reports"
    | "insights"
    | "trends"
    | "sentiment_analysis"
  >(() => {
    const params = new URLSearchParams(location.search);
    const tab = params.get("tab");
    if (tab === "comparison") return "comparison";
    if (tab === "reports") return "reports";
    if (tab === "insights") return "insights";
    if (tab === "trends") return "trends";
    if (tab === "sentiment_analysis") return "sentiment_analysis";
    return "analysis";
  });

  // User and profile state
  const [userProfile, setUserProfile] = useState<UserProfileResponse | null>(
    null
  );
  const [userAnalyses, setUserAnalyses] = useState<UserAnalysis[]>([]);
  const [analysisCurrentPage, setAnalysisCurrentPage] = useState(1);
  const [analysisTotalPages, setAnalysisTotalPages] = useState(1);
  const [totalAnalyses, setTotalAnalyses] = useState(0);

  // Comparison pagination state
  const [comparisonCurrentPage, setComparisonCurrentPage] = useState(1);
  const [comparisonTotalPages, setComparisonTotalPages] = useState(1);
  const [totalComparisons, setTotalComparisons] = useState(0);

  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Analysis state
  const [expandedAnalysis, setExpandedAnalysis] = useState<string | null>(null);
  const [selectedAnalysis, setSelectedAnalysis] = useState<UserAnalysis | null>(
    null
  );

  // Comparison state
  const [comparisonResults, setComparisonResults] = useState<
    ComparisonResult[]
  >([]);
  const [expandedComparison, setExpandedComparison] = useState<string | null>(
    null
  );
  const [selectedComparison, setSelectedComparison] =
    useState<ComparisonResult | null>(null);
  const [isLoadingComparisons, setIsLoadingComparisons] = useState(false);
  const [comparisonError, setComparisonError] = useState<string | null>(null);

  // Comparison mode state
  const [comparisonMode, setComparisonMode] = useState(false);
  const [selectedAnalysisIds, setSelectedAnalysisIds] = useState<string[]>([]);
  const [newCompanyNames, setNewCompanyNames] = useState<string[]>([""]);
  const [newCompanyFiles, setNewCompanyFiles] = useState<(File | null)[]>([
    null,
  ]);
  const [saveNewAnalyses, setSaveNewAnalyses] = useState(false);
  const [comparisonResult, setComparisonResult] = useState<any>(null);
  const [isComparing, setIsComparing] = useState(false);

  // Update URL when tab changes
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    if (activeTab === "comparison") {
      params.set("tab", "comparison");
    } else if (activeTab === "reports") {
      params.set("tab", "reports");
    } else if (activeTab === "insights") {
      params.set("tab", "insights");
    } else if (activeTab === "trends") {
      params.set("tab", "trends");
    } else if (activeTab === "sentiment_analysis") {
      params.set("tab", "sentiment_analysis");
    } else {
      params.delete("tab");
    }
    const newUrl = params.toString()
      ? `?${params.toString()}`
      : location.pathname;
    window.history.replaceState({}, "", newUrl);
  }, [activeTab, location.pathname]);

  // Fetch user data and analyses
  const fetchUserData = useCallback(async () => {
    try {
      setIsLoading(true);
      setError(null);

      console.log("Fetching analyses for page:", analysisCurrentPage);

      const [profileData, analysesData] = await Promise.all([
        ApiService.getUserProfile(),
        ApiService.getUserAnalyses(analysisCurrentPage - 1, 10), // Use pagination
      ]);

      console.log("Analysis response:", analysesData);

      setUserProfile(profileData);
      setUserAnalyses(analysesData.analyses || []);
      setAnalysisTotalPages(analysesData.totalPages);
      setTotalAnalyses(analysesData.total);
    } catch (err) {
      console.error("Failed to fetch user data:", err);
      setError(err instanceof Error ? err.message : "Failed to load data");
    } finally {
      setIsLoading(false);
    }
  }, [analysisCurrentPage]);

  // Fetch comparison results
  const fetchComparisonResults = useCallback(async () => {
    try {
      setIsLoadingComparisons(true);

      console.log("Fetching comparisons for page:", comparisonCurrentPage);

      const response = await ApiService.getSavedComparisons(
        comparisonCurrentPage - 1,
        10
      );

      console.log("Comparison response:", response);

      // Handle paginated response
      if (response && typeof response === "object" && "content" in response) {
        // Handle PageableResponse structure
        console.log("Using paginated response structure");
        setComparisonResults(response.content || []);
        setTotalComparisons(response.totalElements || 0);
        setComparisonTotalPages(response.totalPages || 1);
      } else if (Array.isArray(response)) {
        // Handle legacy array response (fallback)
        console.log("Using legacy array response");
        setComparisonResults(response);
        setTotalComparisons(response.length);
        setComparisonTotalPages(Math.ceil(response.length / 10));
      } else {
        console.log("No comparison data received");
        setComparisonResults([]);
        setTotalComparisons(0);
        setComparisonTotalPages(1);
      }

      setComparisonError(null);
    } catch (err) {
      console.error("Failed to fetch comparison results:", err);
      setComparisonError(
        err instanceof Error ? err.message : "Failed to load comparison results"
      );
    } finally {
      setIsLoadingComparisons(false);
    }
  }, [comparisonCurrentPage]);

  // Initial data fetch
  useEffect(() => {
    fetchUserData();
    fetchComparisonResults();
  }, []);

  // Fetch analyses when page changes
  useEffect(() => {
    fetchUserData();
  }, [fetchUserData]);

  // Fetch comparisons when page changes
  useEffect(() => {
    fetchComparisonResults();
  }, [fetchComparisonResults]);

  // No longer need to fetch on tab change since we load on initial render
  // useEffect(() => {
  //   fetchComparisonResults();
  // }, [activeTab]);

  // Utility functions
  const refreshAnalyses = () => {
    fetchUserData();
  };

  const refreshComparisons = () => {
    fetchComparisonResults();
  };

  return {
    // Tab state
    activeTab,
    setActiveTab,

    // User data
    userProfile,
    userAnalyses,
    isLoading,
    error,
    refreshAnalyses,

    // Analysis pagination
    analysisCurrentPage,
    setAnalysisCurrentPage,
    analysisTotalPages,
    totalAnalyses,

    // Analysis state
    expandedAnalysis,
    setExpandedAnalysis,
    selectedAnalysis,
    setSelectedAnalysis,

    // Comparison data
    comparisonResults,
    isLoadingComparisons,
    comparisonError,
    refreshComparisons,

    // Comparison pagination
    comparisonCurrentPage,
    setComparisonCurrentPage,
    comparisonTotalPages,
    totalComparisons,

    // Comparison state
    expandedComparison,
    setExpandedComparison,
    selectedComparison,
    setSelectedComparison,

    // Comparison mode
    comparisonMode,
    setComparisonMode,
    selectedAnalysisIds,
    setSelectedAnalysisIds,
    newCompanyNames,
    setNewCompanyNames,
    newCompanyFiles,
    setNewCompanyFiles,
    saveNewAnalyses,
    setSaveNewAnalyses,
    comparisonResult,
    setComparisonResult,
    isComparing,
    setIsComparing,
  };
};
