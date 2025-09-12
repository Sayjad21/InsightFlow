import { useState, useEffect } from "react";
import { useLocation } from "react-router-dom";
import { ApiService, type UserProfileResponse } from "../services/api";
import type { UserAnalysis, ComparisonResult } from "../types";

export const useDashboardData = () => {
  const location = useLocation();

  // Tab state - get from URL parameter
  const [activeTab, setActiveTab] = useState<"analysis" | "comparison">(() => {
    const params = new URLSearchParams(location.search);
    const tab = params.get("tab");
    return tab === "comparison" ? "comparison" : "analysis";
  });

  // User and profile state
  const [userProfile, setUserProfile] = useState<UserProfileResponse | null>(
    null
  );
  const [userAnalyses, setUserAnalyses] = useState<UserAnalysis[]>([]);
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
  const [saveNewAnalyses, setSaveNewAnalyses] = useState(false);
  const [comparisonResult, setComparisonResult] = useState<any>(null);
  const [isComparing, setIsComparing] = useState(false);

  // Update URL when tab changes
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    if (activeTab === "comparison") {
      params.set("tab", "comparison");
    } else {
      params.delete("tab");
    }
    const newUrl = params.toString()
      ? `?${params.toString()}`
      : location.pathname;
    window.history.replaceState({}, "", newUrl);
  }, [activeTab, location.pathname]);

  // Fetch user data and analyses
  const fetchUserData = async () => {
    try {
      setIsLoading(true);
      setError(null);

      const [profileData, analysesData] = await Promise.all([
        ApiService.getUserProfile(),
        ApiService.getUserAnalyses(),
      ]);

      setUserProfile(profileData);
      setUserAnalyses(analysesData.analyses || []);
    } catch (err) {
      console.error("Failed to fetch user data:", err);
      setError(err instanceof Error ? err.message : "Failed to load data");
    } finally {
      setIsLoading(false);
    }
  };

  // Fetch comparison results
  const fetchComparisonResults = async () => {
    if (activeTab === "comparison") {
      try {
        setIsLoadingComparisons(true);
        const results = await ApiService.getSavedComparisons();
        setComparisonResults(results);
        setComparisonError(null);
      } catch (err) {
        console.error("Failed to fetch comparison results:", err);
        setComparisonError(
          err instanceof Error
            ? err.message
            : "Failed to load comparison results"
        );
      } finally {
        setIsLoadingComparisons(false);
      }
    }
  };

  // Initial data fetch
  useEffect(() => {
    fetchUserData();
  }, []);

  // Fetch comparison results when switching to comparison tab
  useEffect(() => {
    fetchComparisonResults();
  }, [activeTab]);

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
    saveNewAnalyses,
    setSaveNewAnalyses,
    comparisonResult,
    setComparisonResult,
    isComparing,
    setIsComparing,
  };
};
