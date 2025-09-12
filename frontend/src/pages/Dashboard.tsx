import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import {
  User,
  LogOut,
  Calendar,
  Building2,
  CheckCircle,
  Clock,
  XCircle,
  ChevronDown,
  ChevronUp,
  Eye,
  Download,
  BarChart3,
  Target,
  Users2,
  Settings,
  Home as HomeIcon,
  AlertCircle,
  Loader2,
  FileText,
  Plus,
  X,
  TrendingUp,
  GitCompare,
} from "lucide-react";
import { useAuth } from "../contexts/AuthContext";
import { ApiService, type UserProfileResponse } from "../services/api";
import type { UserAnalysis } from "../types";
import AnalysisDisplay from "../components/AnalysisDisplay";
import ExportButtons from "../components/ExportButtons";
import {
  exportToPdf,
  exportToMarkdown,
  exportToTxt,
} from "../utils/exportUtils";

const Dashboard: React.FC = () => {
  const { user, logout } = useAuth();
  const [expandedAnalysis, setExpandedAnalysis] = useState<string | null>(null);
  const [selectedAnalysis, setSelectedAnalysis] = useState<UserAnalysis | null>(
    null
  );
  const [userAnalyses, setUserAnalyses] = useState<UserAnalysis[]>([]);
  const [userStats, setUserStats] = useState<UserProfileResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Comparison state
  const [comparisonMode, setComparisonMode] = useState(false);
  const [selectedAnalysisIds, setSelectedAnalysisIds] = useState<string[]>([]);
  const [newCompanyNames, setNewCompanyNames] = useState<string[]>([""]);
  const [saveNewAnalyses, setSaveNewAnalyses] = useState(false);
  const [comparisonResult, setComparisonResult] = useState<any>(null);
  const [isComparing, setIsComparing] = useState(false);
  const [comparisonError, setComparisonError] = useState<string | null>(null);

  // Fetch user data and analyses on mount
  useEffect(() => {
    const fetchUserData = async () => {
      try {
        setIsLoading(true);
        const [profileData, analysesData] = await Promise.all([
          ApiService.getUserProfile(),
          ApiService.getUserAnalyses(),
        ]);

        setUserStats(profileData);
        setUserAnalyses(analysesData.analyses);
        setError(null);
      } catch (err) {
        console.error("Failed to fetch user data:", err);
        setError(err instanceof Error ? err.message : "Failed to load data");
      } finally {
        setIsLoading(false);
      }
    };

    if (user) {
      fetchUserData();
    }
  }, [user]);

  // Handle click outside to close expanded view
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (expandedAnalysis && event.target instanceof Element) {
        const modalContent = document.querySelector(".expanded-analysis-modal");
        if (modalContent && !modalContent.contains(event.target)) {
          setExpandedAnalysis(null);
        }
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [expandedAnalysis]);

  if (!user) return null;

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-indigo-50 to-purple-50 flex items-center justify-center">
        <div className="text-center">
          <Loader2 className="h-8 w-8 animate-spin text-indigo-600 mx-auto mb-4" />
          <p className="text-gray-600">Loading dashboard...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-indigo-50 to-purple-50 flex items-center justify-center">
        <div className="text-center">
          <AlertCircle className="h-12 w-12 text-red-500 mx-auto mb-4" />
          <h2 className="text-xl font-semibold text-gray-900 mb-2">
            Failed to Load Dashboard
          </h2>
          <p className="text-gray-600 mb-4">{error}</p>
          <button
            onClick={() => window.location.reload()}
            className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  const completedAnalyses = userAnalyses.filter(
    (analysis) => analysis.status === "COMPLETED"
  );
  const pendingAnalyses = userAnalyses.filter(
    (analysis) => analysis.status === "PENDING"
  );
  const failedAnalyses = userAnalyses.filter(
    (analysis) => analysis.status === "FAILED"
  );

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "completed":
        return <CheckCircle className="h-5 w-5 text-green-500" />;
      case "pending":
        return <Clock className="h-5 w-5 text-yellow-500" />;
      case "failed":
        return <XCircle className="h-5 w-5 text-red-500" />;
      default:
        return null;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case "completed":
        return "bg-green-100 text-green-800 border-green-200";
      case "pending":
        return "bg-yellow-100 text-yellow-800 border-yellow-200";
      case "failed":
        return "bg-red-100 text-red-800 border-red-200";
      default:
        return "bg-gray-100 text-gray-800 border-gray-200";
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const handleAnalysisClick = (analysis: UserAnalysis) => {
    console.log("Status:", analysis.status);
    if (analysis.status === "COMPLETED") {
      const isExpanded = expandedAnalysis === analysis.id;
      setExpandedAnalysis(isExpanded ? null : analysis.id);
    }
  };

  const handleViewFullReport = (analysis: UserAnalysis) => {
    setSelectedAnalysis(analysis);
  };

  // Helper function to get analysis data (supports both new and legacy formats)
  const getAnalysisData = (analysis: UserAnalysis) => {
    // New format: data stored directly in UserAnalysis
    if (analysis.summaries || analysis.swotLists) {
      return {
        summaries: analysis.summaries || [],
        sources: analysis.sources || [],
        strategyRecommendations: analysis.strategyRecommendations || "",
        swotLists: analysis.swotLists || {
          strengths: [],
          weaknesses: [],
          opportunities: [],
          threats: [],
        },
        swotImage: analysis.swotImage || "",
        pestelLists: analysis.pestelLists || {
          political: [],
          economic: [],
          social: [],
          technological: [],
          environmental: [],
          legal: [],
        },
        pestelImage: analysis.pestelImage || "",
        porterForces: analysis.porterForces || {
          rivalry: [],
          newEntrants: [],
          substitutes: [],
          buyerPower: [],
          supplierPower: [],
        },
        porterImage: analysis.porterImage || "",
        bcgMatrix: analysis.bcgMatrix || {},
        bcgImage: analysis.bcgImage || "",
        mckinsey7s: analysis.mckinsey7s || {
          strategy: "",
          structure: "",
          systems: "",
          style: "",
          staff: "",
          skills: "",
          sharedValues: "",
        },
        mckinseyImage: analysis.mckinseyImage || "",
        linkedinAnalysis: analysis.linkedinAnalysis || "",
      };
    }

    // Legacy format: data stored in result object
    if (analysis.result) {
      return {
        summaries: analysis.result.summaries || [],
        sources: analysis.result.sources || [],
        strategyRecommendations: analysis.result.strategy_recommendations || "",
        swotLists: analysis.result.swot_lists || {
          strengths: [],
          weaknesses: [],
          opportunities: [],
          threats: [],
        },
        swotImage: analysis.result.swot_image || "",
        pestelLists: analysis.result.pestel_lists || {
          political: [],
          economic: [],
          social: [],
          technological: [],
          environmental: [],
          legal: [],
        },
        pestelImage: analysis.result.pestel_image || "",
        porterForces: analysis.result.porter_forces || {
          rivalry: [],
          new_entrants: [],
          substitutes: [],
          buyer_power: [],
          supplier_power: [],
        },
        porterImage: analysis.result.porter_image || "",
        bcgMatrix: analysis.result.bcg_matrix || {},
        bcgImage: analysis.result.bcg_image || "",
        mckinsey7s: analysis.result.mckinsey_7s || {
          strategy: "",
          structure: "",
          systems: "",
          style: "",
          staff: "",
          skills: "",
          shared_values: "",
        },
        mckinseyImage: analysis.result.mckinsey_image || "",
        linkedinAnalysis: analysis.result.linkedin_analysis || "",
      };
    }

    return null;
  };

  const handleExportPDF = async (analysis: UserAnalysis) => {
    const transformedData = transformAnalysisForDisplay(analysis);
    if (transformedData) {
      try {
        await exportToPdf(transformedData);
      } catch (error) {
        console.error("Error exporting PDF:", error);
      }
    }
  };

  const handleExportMarkdown = (analysis: UserAnalysis) => {
    const transformedData = transformAnalysisForDisplay(analysis);
    if (transformedData) {
      try {
        exportToMarkdown(transformedData);
      } catch (error) {
        console.error("Error exporting Markdown:", error);
      }
    }
  };

  const handleExportTxt = (analysis: UserAnalysis) => {
    const transformedData = transformAnalysisForDisplay(analysis);
    if (transformedData) {
      try {
        exportToTxt(transformedData);
      } catch (error) {
        console.error("Error exporting TXT:", error);
      }
    }
  };

  // Comparison handlers
  const handleAnalysisSelection = (analysisId: string) => {
    setSelectedAnalysisIds((prev) => {
      if (prev.includes(analysisId)) {
        return prev.filter((id) => id !== analysisId);
      } else {
        return [...prev, analysisId];
      }
    });
  };

  const handleCompanyNameChange = (index: number, value: string) => {
    setNewCompanyNames((prev) => {
      const updated = [...prev];
      updated[index] = value;
      return updated;
    });
  };

  const addCompanyField = () => {
    if (newCompanyNames.length < 5) {
      setNewCompanyNames((prev) => [...prev, ""]);
    }
  };

  const removeCompanyField = (index: number) => {
    if (newCompanyNames.length > 1) {
      setNewCompanyNames((prev) => prev.filter((_, i) => i !== index));
    }
  };

  const handleCompare = async () => {
    const validCompanyNames = newCompanyNames.filter(
      (name) => name.trim() !== ""
    );
    const totalSelections =
      selectedAnalysisIds.length + validCompanyNames.length;

    if (totalSelections < 2) {
      setComparisonError(
        "Please select at least 2 companies/analyses to compare"
      );
      return;
    }

    if (totalSelections > 5) {
      setComparisonError(
        "Maximum 5 companies/analyses can be compared at once"
      );
      return;
    }

    setIsComparing(true);
    setComparisonError(null);

    try {
      let result;

      if (selectedAnalysisIds.length > 0 && validCompanyNames.length === 0) {
        // Compare existing analyses only
        result = await ApiService.compareExistingAnalyses(selectedAnalysisIds);
      } else {
        // Mixed or new company comparison
        result = await ApiService.compareEnhanced({
          analysisIds:
            selectedAnalysisIds.length > 0 ? selectedAnalysisIds : undefined,
          companyNames:
            validCompanyNames.length > 0 ? validCompanyNames : undefined,
          comparisonType:
            selectedAnalysisIds.length > 0 && validCompanyNames.length > 0
              ? "mixed"
              : selectedAnalysisIds.length > 0
              ? "existing_analyses"
              : "new_analysis",
          saveNewAnalyses: saveNewAnalyses && validCompanyNames.length > 0,
        });
      }

      setComparisonResult(result);
      // Don't set comparisonMode to true since results modal will show
    } catch (error) {
      console.error("Comparison failed:", error);
      const errorMessage =
        error instanceof Error ? error.message : "Comparison failed";
      setComparisonError(
        errorMessage.includes("fetch")
          ? "Unable to connect to server. Please check your connection."
          : errorMessage
      );
    } finally {
      setIsComparing(false);
    }
  };

  const resetComparison = async () => {
    setComparisonMode(false);
    setSelectedAnalysisIds([]);
    setNewCompanyNames([""]);
    setSaveNewAnalyses(false);
    setComparisonResult(null);
    setComparisonError(null);

    // If new analyses were saved, refresh the analyses list
    if (
      comparisonResult?.saved_analysis_ids &&
      comparisonResult.saved_analysis_ids.length > 0
    ) {
      try {
        const analysesData = await ApiService.getUserAnalyses();
        setUserAnalyses(analysesData.analyses);
      } catch (error) {
        console.error("Failed to refresh analyses:", error);
      }
    }
  };

  const toggleComparisonMode = () => {
    if (comparisonMode) {
      resetComparison();
    } else {
      setComparisonMode(true);
    }
  };

  // Helper function to transform analysis data for AnalysisDisplay component
  const transformAnalysisForDisplay = (analysis: UserAnalysis) => {
    const analysisData = getAnalysisData(analysis);
    if (!analysisData) return null;

    // Transform porter forces format
    const porterForces = analysisData.porterForces;
    const transformedPorter =
      "newEntrants" in porterForces
        ? {
            rivalry: porterForces.rivalry,
            new_entrants: porterForces.newEntrants,
            substitutes: porterForces.substitutes,
            buyer_power: porterForces.buyerPower,
            supplier_power: porterForces.supplierPower,
          }
        : porterForces;

    // Transform BCG matrix format
    const bcgMatrix = analysisData.bcgMatrix;
    const transformedBcg: {
      [key: string]: { market_share: number; growth_rate: number };
    } = {};
    Object.entries(bcgMatrix).forEach(([key, value]) => {
      if (
        value &&
        typeof value === "object" &&
        "marketShare" in value &&
        "growthRate" in value
      ) {
        transformedBcg[key] = {
          market_share: (value as any).marketShare,
          growth_rate: (value as any).growthRate,
        };
      } else {
        transformedBcg[key] = value as any;
      }
    });

    // Transform McKinsey 7S format
    const mckinsey7s = analysisData.mckinsey7s;
    const transformedMckinsey =
      "sharedValues" in mckinsey7s
        ? {
            strategy: mckinsey7s.strategy,
            structure: mckinsey7s.structure,
            systems: mckinsey7s.systems,
            style: mckinsey7s.style,
            staff: mckinsey7s.staff,
            skills: mckinsey7s.skills,
            shared_values: mckinsey7s.sharedValues,
          }
        : mckinsey7s;

    return {
      company_name: analysis.companyName,
      summaries: analysisData.summaries,
      sources: analysisData.sources,
      strategy_recommendations: analysisData.strategyRecommendations,
      swot_lists: analysisData.swotLists,
      swot_image: analysisData.swotImage,
      pestel_lists: analysisData.pestelLists,
      pestel_image: analysisData.pestelImage,
      porter_forces: transformedPorter,
      porter_image: analysisData.porterImage,
      bcg_matrix: transformedBcg,
      bcg_image: analysisData.bcgImage,
      mckinsey_7s: transformedMckinsey,
      mckinsey_image: analysisData.mckinseyImage,
      linkedin_analysis: analysisData.linkedinAnalysis,
    };
  };

  const closeModal = () => {
    setSelectedAnalysis(null);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <div className="flex items-center">
              <Building2 className="h-8 w-8 text-blue-600 mr-3" />
              <h1 className="text-xl font-semibold text-gray-900">
                InsightFlow Dashboard
              </h1>
            </div>

            <div className="flex items-center space-x-4">
              <Link
                to="/home"
                className="flex items-center px-3 py-2 text-sm font-medium text-gray-700 hover:text-gray-900 transition-colors duration-200"
              >
                <HomeIcon className="h-4 w-4 mr-2" />
                Analysis Tools
              </Link>
              <Link
                to="/settings"
                className="p-2 text-gray-400 hover:text-gray-600 transition-colors duration-200"
              >
                <Settings className="h-5 w-5" />
              </Link>
              <button
                onClick={logout}
                className="flex items-center px-3 py-2 text-sm font-medium text-gray-700 hover:text-gray-900 transition-colors duration-200"
              >
                <LogOut className="h-4 w-4 mr-2" />
                Logout
              </button>
            </div>
          </div>
        </div>
      </header>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* User Profile Section */}
        <div className="bg-gradient-to-r from-blue-600 to-purple-700 rounded-2xl p-8 mb-8 text-white">
          <div className="flex items-center space-x-6">
            <div className="relative">
              <img
                src={user.avatar}
                alt={`${user.firstName} ${user.lastName}`}
                className="h-20 w-20 rounded-full border-4 border-white/20"
              />
              <div className="absolute -bottom-2 -right-2 bg-green-500 h-6 w-6 rounded-full border-2 border-white"></div>
            </div>
            <div>
              <h2 className="text-2xl font-bold mb-2">
                Welcome back, {user.firstName}!
              </h2>
              <p className="text-blue-100 mb-4">{user.email}</p>
              <div className="flex items-center space-x-4 text-sm">
                <div className="flex items-center">
                  <User className="h-4 w-4 mr-1" />
                  Member since {new Date(user.createdAt).toLocaleDateString()}
                </div>
                <div className="flex items-center">
                  <Calendar className="h-4 w-4 mr-1" />
                  Last login: {formatDate(user.lastLogin)}
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Comparison Controls Section */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 mb-8">
          <div className="px-6 py-4 border-b border-gray-200">
            <div className="flex items-center justify-between">
              <div className="flex items-center">
                <GitCompare className="h-6 w-6 text-purple-600 mr-3" />
                <div>
                  <h3 className="text-lg font-semibold text-gray-900">
                    Compare Companies
                  </h3>
                  <p className="text-sm text-gray-600 mt-1">
                    Compare existing analyses and/or analyze new companies
                  </p>
                </div>
              </div>
              <button
                onClick={toggleComparisonMode}
                className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors duration-200 ${
                  comparisonMode
                    ? "bg-red-100 text-red-700 hover:bg-red-200"
                    : "bg-purple-100 text-purple-700 hover:bg-purple-200"
                }`}
              >
                {comparisonMode ? "Cancel Comparison" : "Start Comparison"}
              </button>
            </div>
          </div>

          {comparisonMode && (
            <div className="p-6 space-y-6">
              {/* Instructions */}
              <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                <h4 className="text-sm font-semibold text-blue-900 mb-2">
                  How to Compare:
                </h4>
                <ul className="text-sm text-blue-800 space-y-1">
                  <li>
                    • Select 2-5 completed analyses from your history below
                  </li>
                  <li>• Or add new company names for fresh analysis</li>
                  <li>• Or mix both existing and new companies</li>
                  <li>
                    • Check "Save new analyses" to add them to your dashboard
                  </li>
                </ul>
              </div>
              {/* New Company Analysis Section */}
              <div className="space-y-4">
                <h4 className="text-md font-semibold text-gray-900 flex items-center">
                  <Plus className="h-4 w-4 mr-2 text-green-600" />
                  Add New Companies for Analysis
                </h4>
                <div className="space-y-3">
                  {newCompanyNames.map((name, index) => (
                    <div key={index} className="flex items-center space-x-3">
                      <input
                        type="text"
                        placeholder={`Company ${index + 1} name`}
                        value={name}
                        onChange={(e) =>
                          handleCompanyNameChange(index, e.target.value)
                        }
                        className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
                      />
                      {index === newCompanyNames.length - 1 &&
                        newCompanyNames.length < 5 && (
                          <button
                            onClick={addCompanyField}
                            className="p-2 text-green-600 hover:bg-green-50 rounded-lg transition-colors"
                          >
                            <Plus className="h-4 w-4" />
                          </button>
                        )}
                      {newCompanyNames.length > 1 && (
                        <button
                          onClick={() => removeCompanyField(index)}
                          className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                        >
                          <X className="h-4 w-4" />
                        </button>
                      )}
                    </div>
                  ))}
                </div>

                {/* Save to Database Checkbox */}
                {newCompanyNames.some((name) => name.trim() !== "") && (
                  <div className="flex items-center space-x-2">
                    <input
                      type="checkbox"
                      id="saveNewAnalyses"
                      checked={saveNewAnalyses}
                      onChange={(e) => setSaveNewAnalyses(e.target.checked)}
                      className="rounded border-gray-300 text-purple-600 focus:ring-purple-500"
                    />
                    <label
                      htmlFor="saveNewAnalyses"
                      className="text-sm text-gray-600"
                    >
                      Save new analyses to my dashboard
                    </label>
                  </div>
                )}
              </div>

              {/* Compare Button and Status */}
              <div className="flex items-center justify-between pt-4 border-t border-gray-200">
                <div className="flex items-center space-x-4">
                  <span className="text-sm text-gray-600">
                    Selected: {selectedAnalysisIds.length} existing +{" "}
                    {newCompanyNames.filter((n) => n.trim()).length} new
                  </span>
                  {comparisonError && (
                    <span className="text-sm text-red-600">
                      {comparisonError}
                    </span>
                  )}
                </div>
                <div className="flex items-center space-x-3">
                  <button
                    onClick={() => {
                      setSelectedAnalysisIds([]);
                      setNewCompanyNames([""]);
                      setSaveNewAnalyses(false);
                      setComparisonError(null);
                    }}
                    className="px-4 py-2 text-gray-600 hover:text-gray-800 transition-colors text-sm"
                  >
                    Clear All
                  </button>
                  <button
                    onClick={handleCompare}
                    disabled={
                      isComparing ||
                      selectedAnalysisIds.length +
                        newCompanyNames.filter((n) => n.trim()).length <
                        2
                    }
                    className="flex items-center px-6 py-2 bg-gradient-to-r from-purple-600 to-blue-600 text-white rounded-lg hover:from-purple-700 hover:to-blue-700 disabled:from-gray-400 disabled:to-gray-400 disabled:cursor-not-allowed transition-all duration-200"
                  >
                    {isComparing ? (
                      <>
                        <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                        Comparing...
                      </>
                    ) : (
                      <>
                        <TrendingUp className="h-4 w-4 mr-2" />
                        Compare
                      </>
                    )}
                  </button>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-200">
            <div className="flex items-center">
              <div className="p-2 bg-blue-100 rounded-lg">
                <Building2 className="h-6 w-6 text-blue-600" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Total</p>
                <p className="text-2xl font-bold text-gray-900">
                  {userStats?.totalAnalyses || 0}
                </p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-200">
            <div className="flex items-center">
              <div className="p-2 bg-green-100 rounded-lg">
                <CheckCircle className="h-6 w-6 text-green-600" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Completed</p>
                <p className="text-2xl font-bold text-gray-900">
                  {completedAnalyses.length}
                </p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-200">
            <div className="flex items-center">
              <div className="p-2 bg-yellow-100 rounded-lg">
                <Clock className="h-6 w-6 text-yellow-600" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Pending</p>
                <p className="text-2xl font-bold text-gray-900">
                  {pendingAnalyses.length}
                </p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-200">
            <div className="flex items-center">
              <div className="p-2 bg-red-100 rounded-lg">
                <XCircle className="h-6 w-6 text-red-600" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Failed</p>
                <p className="text-2xl font-bold text-gray-900">
                  {failedAnalyses.length}
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Analysis History */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200">
          <div className="px-6 py-4 border-b border-gray-200">
            <h3 className="text-lg font-semibold text-gray-900">
              Analysis History
            </h3>
            <p className="text-sm text-gray-600 mt-1">
              View and manage your company analysis reports
            </p>
          </div>

          <div className="divide-y divide-gray-200">
            {userAnalyses.map((analysis) => (
              <div key={analysis.id} className="p-6">
                <div
                  className={`flex items-center justify-between transition-all duration-200 ${
                    !comparisonMode && analysis.status === "COMPLETED"
                      ? "cursor-pointer hover:bg-gray-50"
                      : ""
                  }`}
                  onClick={
                    !comparisonMode
                      ? () => handleAnalysisClick(analysis)
                      : undefined
                  }
                >
                  <div className="flex items-center space-x-4">
                    {/* Comparison Mode Checkbox */}
                    {comparisonMode && analysis.status === "COMPLETED" && (
                      <input
                        type="checkbox"
                        checked={selectedAnalysisIds.includes(analysis.id)}
                        onChange={() => handleAnalysisSelection(analysis.id)}
                        onClick={(e) => e.stopPropagation()}
                        className="rounded border-gray-300 text-purple-600 focus:ring-purple-500 h-4 w-4"
                      />
                    )}
                    {getStatusIcon(analysis.status)}
                    <div>
                      <h4 className="text-lg font-medium text-gray-900">
                        {analysis.companyName}
                      </h4>
                      <p className="text-sm text-gray-500">
                        {formatDate(analysis.analysisDate)}
                      </p>
                      {comparisonMode && analysis.status !== "COMPLETED" && (
                        <p className="text-xs text-gray-400">
                          (Only completed analyses can be compared)
                        </p>
                      )}
                    </div>
                  </div>

                  <div className="flex items-center space-x-4">
                    <span
                      className={`px-3 py-1 rounded-full text-xs font-medium border ${getStatusColor(
                        analysis.status
                      )}`}
                    >
                      {analysis.status.charAt(0).toUpperCase() +
                        analysis.status.slice(1)}
                    </span>

                    {!comparisonMode && analysis.status === "COMPLETED" && (
                      <div className="flex items-center space-x-2">
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            handleExportPDF(analysis);
                          }}
                          className="p-2 text-gray-400 hover:text-green-600 transition-colors duration-200"
                          title="Download PDF"
                        >
                          <Download className="h-4 w-4" />
                        </button>
                        {expandedAnalysis === analysis.id ? (
                          <ChevronUp className="h-5 w-5 text-gray-400" />
                        ) : (
                          <ChevronDown className="h-5 w-5 text-gray-400" />
                        )}
                      </div>
                    )}
                  </div>
                </div>

                {/* Elaborate Inline Expansion */}
                {!comparisonMode &&
                  expandedAnalysis === analysis.id &&
                  analysis.status === "COMPLETED" &&
                  (() => {
                    const analysisData = getAnalysisData(analysis);
                    if (!analysisData) return null;

                    return (
                      <div className="mt-6 p-6 bg-gradient-to-br from-white/90 via-gray-50/50 to-blue-50/30 rounded-2xl border border-gray-200/50 shadow-lg backdrop-blur-sm">
                        <div className="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-4 gap-6">
                          {/* SWOT Analysis Card */}
                          <div className="bg-gradient-to-br from-blue-50/80 to-indigo-100/80 backdrop-blur-sm rounded-2xl p-6 border border-blue-200/50 shadow-lg">
                            <div className="flex items-center mb-4">
                              <div className="p-2 bg-blue-500/90 rounded-lg shadow-sm">
                                <BarChart3 className="h-5 w-5 text-white" />
                              </div>
                              <h5 className="text-lg font-semibold text-gray-900 ml-3">
                                SWOT Analysis
                              </h5>
                            </div>
                            <div className="space-y-3">
                              <div className="grid grid-cols-2 gap-3">
                                <div className="bg-green-50/90 backdrop-blur-sm rounded-xl p-3 border border-green-200/50 shadow-sm">
                                  <h6 className="font-medium text-green-800 text-sm">
                                    Strengths
                                  </h6>
                                  <p className="text-xs text-green-600 mt-1">
                                    {analysisData.swotLists.strengths.length}{" "}
                                    items
                                  </p>
                                </div>
                                <div className="bg-red-50/90 backdrop-blur-sm rounded-xl p-3 border border-red-200/50 shadow-sm">
                                  <h6 className="font-medium text-red-800 text-sm">
                                    Weaknesses
                                  </h6>
                                  <p className="text-xs text-red-600 mt-1">
                                    {analysisData.swotLists.weaknesses.length}{" "}
                                    items
                                  </p>
                                </div>
                              </div>
                              <div className="grid grid-cols-2 gap-3">
                                <div className="bg-blue-50/90 backdrop-blur-sm rounded-xl p-3 border border-blue-200/50 shadow-sm">
                                  <h6 className="font-medium text-blue-800 text-sm">
                                    Opportunities
                                  </h6>
                                  <p className="text-xs text-blue-600 mt-1">
                                    {
                                      analysisData.swotLists.opportunities
                                        .length
                                    }{" "}
                                    items
                                  </p>
                                </div>
                                <div className="bg-orange-50/90 backdrop-blur-sm rounded-xl p-3 border border-orange-200/50 shadow-sm">
                                  <h6 className="font-medium text-orange-800 text-sm">
                                    Threats
                                  </h6>
                                  <p className="text-xs text-orange-600 mt-1">
                                    {analysisData.swotLists.threats.length}{" "}
                                    items
                                  </p>
                                </div>
                              </div>
                            </div>
                          </div>

                          {/* PESTEL Analysis Card */}
                          <div className="bg-gradient-to-br from-teal-50/80 to-cyan-100/80 backdrop-blur-sm rounded-2xl p-6 border border-teal-200/50 shadow-lg">
                            <div className="flex items-center mb-4">
                              <div className="p-2 bg-teal-500/90 rounded-lg shadow-sm">
                                <Target className="h-5 w-5 text-white" />
                              </div>
                              <h5 className="text-lg font-semibold text-gray-900 ml-3">
                                PESTEL Analysis
                              </h5>
                            </div>
                            <div className="space-y-2">
                              <div className="grid grid-cols-2 gap-2">
                                <div className="bg-white/70 backdrop-blur-sm rounded-xl p-2 border border-teal-100/50 shadow-sm">
                                  <h6 className="font-medium text-teal-800 text-sm">
                                    Political
                                  </h6>
                                  <p className="text-xs text-teal-600 mt-1">
                                    {analysisData.pestelLists.political.length}{" "}
                                    items
                                  </p>
                                </div>
                                <div className="bg-white/70 backdrop-blur-sm rounded-xl p-2 border border-teal-100/50 shadow-sm">
                                  <h6 className="font-medium text-teal-800 text-sm">
                                    Economic
                                  </h6>
                                  <p className="text-xs text-teal-600 mt-1">
                                    {analysisData.pestelLists.economic.length}{" "}
                                    items
                                  </p>
                                </div>
                              </div>
                              <div className="grid grid-cols-2 gap-2">
                                <div className="bg-white/70 backdrop-blur-sm rounded-xl p-2 border border-teal-100/50 shadow-sm">
                                  <h6 className="font-medium text-teal-800 text-sm">
                                    Social
                                  </h6>
                                  <p className="text-xs text-teal-600 mt-1">
                                    {analysisData.pestelLists.social.length}{" "}
                                    items
                                  </p>
                                </div>
                                <div className="bg-white/70 backdrop-blur-sm rounded-xl p-2 border border-teal-100/50 shadow-sm">
                                  <h6 className="font-medium text-teal-800 text-sm">
                                    Technological
                                  </h6>
                                  <p className="text-xs text-teal-600 mt-1">
                                    {
                                      analysisData.pestelLists.technological
                                        .length
                                    }{" "}
                                    items
                                  </p>
                                </div>
                              </div>
                              <div className="grid grid-cols-2 gap-2">
                                <div className="bg-white/70 backdrop-blur-sm rounded-xl p-2 border border-teal-100/50 shadow-sm">
                                  <h6 className="font-medium text-teal-800 text-sm">
                                    Environmental
                                  </h6>
                                  <p className="text-xs text-teal-600 mt-1">
                                    {
                                      analysisData.pestelLists.environmental
                                        .length
                                    }{" "}
                                    items
                                  </p>
                                </div>
                                <div className="bg-white/70 backdrop-blur-sm rounded-xl p-2 border border-teal-100/50 shadow-sm">
                                  <h6 className="font-medium text-teal-800 text-sm">
                                    Legal
                                  </h6>
                                  <p className="text-xs text-teal-600 mt-1">
                                    {analysisData.pestelLists.legal.length}{" "}
                                    items
                                  </p>
                                </div>
                              </div>
                            </div>
                          </div>

                          {/* Porter's Five Forces Card */}
                          <div className="bg-gradient-to-br from-purple-50/80 to-pink-100/80 backdrop-blur-sm rounded-2xl p-6 border border-purple-200/50 shadow-lg">
                            <div className="flex items-center mb-4">
                              <div className="p-2 bg-purple-500/90 rounded-lg shadow-sm">
                                <Target className="h-5 w-5 text-white" />
                              </div>
                              <h5 className="text-lg font-semibold text-gray-900 ml-3">
                                Porter's Forces
                              </h5>
                            </div>
                            <div className="space-y-2">
                              {Object.entries(analysisData.porterForces).map(
                                ([force, items]) => (
                                  <div
                                    key={force}
                                    className="bg-white/70 backdrop-blur-sm rounded-xl p-2 border border-purple-100/50 shadow-sm"
                                  >
                                    <p className="text-sm font-medium text-purple-800 capitalize">
                                      {force
                                        .replace(/([A-Z])/g, " $1")
                                        .toLowerCase()
                                        .replace(/^./, (str) =>
                                          str.toUpperCase()
                                        )}
                                    </p>
                                    <p className="text-xs text-purple-600">
                                      {Array.isArray(items) ? items.length : 0}{" "}
                                      factors
                                    </p>
                                  </div>
                                )
                              )}
                            </div>
                          </div>

                          {/* McKinsey 7S Card */}
                          <div className="bg-gradient-to-br from-green-50/80 to-emerald-100/80 backdrop-blur-sm rounded-2xl p-6 border border-green-200/50 shadow-lg">
                            <div className="flex items-center mb-4">
                              <div className="p-2 bg-green-500/90 rounded-lg shadow-sm">
                                <Users2 className="h-5 w-5 text-white" />
                              </div>
                              <h5 className="text-lg font-semibold text-gray-900 ml-3">
                                McKinsey 7S
                              </h5>
                            </div>
                            <div className="space-y-2">
                              {Object.entries(analysisData.mckinsey7s).map(
                                ([element, value]) => (
                                  <div
                                    key={element}
                                    className="bg-white/70 backdrop-blur-sm rounded-xl p-2 border border-green-100/50 shadow-sm"
                                  >
                                    <p className="text-sm font-medium text-green-800 capitalize">
                                      {element
                                        .replace(/([A-Z])/g, " $1")
                                        .toLowerCase()
                                        .replace(/^./, (str) =>
                                          str.toUpperCase()
                                        )}
                                    </p>
                                    <p className="text-xs text-green-600 truncate">
                                      {value || "Not available"}
                                    </p>
                                  </div>
                                )
                              )}
                            </div>
                          </div>
                        </div>

                        {/* Summary and Strategy Section */}
                        <div className="mt-6 space-y-6">
                          {/* Summaries */}
                          <div className="bg-gradient-to-r from-gray-50/90 to-blue-50/90 backdrop-blur-sm rounded-2xl p-6 border border-gray-200/50 shadow-lg">
                            <h5 className="text-lg font-semibold text-gray-900 mb-4">
                              Key Insights
                            </h5>
                            <div className="space-y-3">
                              {analysisData.summaries
                                .slice(0, 3)
                                .map((summary, index) => (
                                  <div
                                    key={index}
                                    className="bg-white/80 backdrop-blur-sm rounded-xl p-4 border border-gray-100/50 shadow-sm"
                                  >
                                    <div
                                      className="text-sm text-gray-700 prose prose-sm max-w-none"
                                      dangerouslySetInnerHTML={{
                                        __html: summary,
                                      }}
                                    />
                                  </div>
                                ))}
                              {analysisData.summaries.length > 3 && (
                                <p className="text-xs text-gray-500 text-center">
                                  +{analysisData.summaries.length - 3} more
                                  insights
                                </p>
                              )}
                            </div>
                          </div>

                          {/* Strategy Recommendations */}
                          <div className="bg-gradient-to-r from-yellow-50/90 to-orange-50/90 backdrop-blur-sm rounded-2xl p-6 border border-yellow-200/50 shadow-lg">
                            <h5 className="text-lg font-semibold text-gray-900 mb-4">
                              Strategy Recommendations
                            </h5>
                            <div className="bg-white/80 backdrop-blur-sm rounded-xl p-4 border border-yellow-100/50 shadow-sm">
                              <div
                                className="text-sm text-gray-700 prose prose-sm max-w-none"
                                dangerouslySetInnerHTML={{
                                  __html:
                                    analysisData.strategyRecommendations ||
                                    "No strategy recommendations available",
                                }}
                              />
                            </div>
                          </div>
                        </div>

                        {/* Analysis Images */}
                        {(analysisData.swotImage ||
                          analysisData.pestelImage ||
                          analysisData.porterImage ||
                          analysisData.bcgImage ||
                          analysisData.mckinseyImage) && (
                          <div className="mt-6">
                            <h5 className="text-lg font-semibold text-gray-900 mb-4">
                              Analysis Charts
                            </h5>
                            <div className="grid grid-cols-2 lg:grid-cols-5 gap-4">
                              {analysisData.swotImage && (
                                <div className="bg-white/80 backdrop-blur-sm rounded-xl p-3 border border-gray-200/50 shadow-lg">
                                  <img
                                    src={
                                      analysisData.swotImage.startsWith("data:")
                                        ? analysisData.swotImage
                                        : `data:image/png;base64,${analysisData.swotImage}`
                                    }
                                    alt="SWOT Analysis"
                                    className="w-full h-32 object-contain rounded-lg"
                                    onError={(e) => {
                                      console.error(
                                        "Failed to load SWOT image"
                                      );
                                      e.currentTarget.style.display = "none";
                                    }}
                                  />
                                  <p className="text-xs text-gray-600 mt-2 text-center">
                                    SWOT Analysis
                                  </p>
                                </div>
                              )}
                              {analysisData.pestelImage && (
                                <div className="bg-white/80 backdrop-blur-sm rounded-xl p-3 border border-gray-200/50 shadow-lg">
                                  <img
                                    src={
                                      analysisData.pestelImage.startsWith(
                                        "data:"
                                      )
                                        ? analysisData.pestelImage
                                        : `data:image/png;base64,${analysisData.pestelImage}`
                                    }
                                    alt="PESTEL Analysis"
                                    className="w-full h-32 object-contain rounded-lg"
                                    onError={(e) => {
                                      console.error(
                                        "Failed to load PESTEL image"
                                      );
                                      e.currentTarget.style.display = "none";
                                    }}
                                  />
                                  <p className="text-xs text-gray-600 mt-2 text-center">
                                    PESTEL Analysis
                                  </p>
                                </div>
                              )}
                              {analysisData.porterImage && (
                                <div className="bg-white/80 backdrop-blur-sm rounded-xl p-3 border border-gray-200/50 shadow-lg">
                                  <img
                                    src={
                                      analysisData.porterImage.startsWith(
                                        "data:"
                                      )
                                        ? analysisData.porterImage
                                        : `data:image/png;base64,${analysisData.porterImage}`
                                    }
                                    alt="Porter's Five Forces"
                                    className="w-full h-32 object-contain rounded-lg"
                                    onError={(e) => {
                                      console.error(
                                        "Failed to load Porter image"
                                      );
                                      e.currentTarget.style.display = "none";
                                    }}
                                  />
                                  <p className="text-xs text-gray-600 mt-2 text-center">
                                    Porter's Forces
                                  </p>
                                </div>
                              )}
                              {analysisData.bcgImage && (
                                <div className="bg-white/80 backdrop-blur-sm rounded-xl p-3 border border-gray-200/50 shadow-lg">
                                  <img
                                    src={
                                      analysisData.bcgImage.startsWith("data:")
                                        ? analysisData.bcgImage
                                        : `data:image/png;base64,${analysisData.bcgImage}`
                                    }
                                    alt="BCG Matrix"
                                    className="w-full h-32 object-contain rounded-lg"
                                    onError={(e) => {
                                      console.error("Failed to load BCG image");
                                      e.currentTarget.style.display = "none";
                                    }}
                                  />
                                  <p className="text-xs text-gray-600 mt-2 text-center">
                                    BCG Matrix
                                  </p>
                                </div>
                              )}
                              {analysisData.mckinseyImage && (
                                <div className="bg-white/80 backdrop-blur-sm rounded-xl p-3 border border-gray-200/50 shadow-lg">
                                  <img
                                    src={
                                      analysisData.mckinseyImage.startsWith(
                                        "data:"
                                      )
                                        ? analysisData.mckinseyImage
                                        : `data:image/png;base64,${analysisData.mckinseyImage}`
                                    }
                                    alt="McKinsey 7S"
                                    className="w-full h-32 object-contain rounded-lg"
                                    onError={(e) => {
                                      console.error(
                                        "Failed to load McKinsey image"
                                      );
                                      e.currentTarget.style.display = "none";
                                    }}
                                  />
                                  <p className="text-xs text-gray-600 mt-2 text-center">
                                    McKinsey 7S
                                  </p>
                                </div>
                              )}
                            </div>
                          </div>
                        )}

                        {/* LinkedIn Analysis */}
                        {analysisData.linkedinAnalysis && (
                          <div className="mt-6">
                            <div className="bg-gradient-to-r from-blue-50/90 to-indigo-50/90 backdrop-blur-sm rounded-2xl p-6 border border-blue-200/50 shadow-lg">
                              <h5 className="text-lg font-semibold text-gray-900 mb-4">
                                LinkedIn Analysis
                              </h5>
                              <div className="bg-white/80 backdrop-blur-sm rounded-xl p-4 border border-blue-100/50 shadow-sm">
                                <div
                                  className="text-sm text-gray-700 prose prose-sm max-w-none"
                                  dangerouslySetInnerHTML={{
                                    __html: analysisData.linkedinAnalysis,
                                  }}
                                />
                              </div>
                            </div>
                          </div>
                        )}

                        {/* Action Buttons */}
                        <div className="mt-8 pt-6 border-t border-gray-200/50">
                          <div className="flex flex-wrap gap-3 justify-center">
                            <button
                              onClick={() => handleViewFullReport(analysis)}
                              className="flex items-center px-6 py-3 bg-gradient-to-r from-blue-600 to-blue-700 text-white rounded-xl hover:from-blue-700 hover:to-blue-800 transition-all duration-200 shadow-lg backdrop-blur-sm"
                            >
                              <Eye className="h-4 w-4 mr-2" />
                              View Full Report
                            </button>

                            {/* Export Buttons Group */}
                            <div className="flex gap-2">
                              <button
                                onClick={() => handleExportPDF(analysis)}
                                className="flex items-center px-4 py-3 bg-gradient-to-r from-green-600 to-green-700 text-white rounded-xl hover:from-green-700 hover:to-green-800 transition-all duration-200 text-sm shadow-lg backdrop-blur-sm"
                              >
                                <Download className="h-4 w-4 mr-1" />
                                PDF
                              </button>
                              <button
                                onClick={() => handleExportMarkdown(analysis)}
                                className="flex items-center px-4 py-3 bg-gradient-to-r from-orange-600 to-orange-700 text-white rounded-xl hover:from-orange-700 hover:to-orange-800 transition-all duration-200 text-sm shadow-lg backdrop-blur-sm"
                              >
                                <FileText className="h-4 w-4 mr-1" />
                                MD
                              </button>
                              <button
                                onClick={() => handleExportTxt(analysis)}
                                className="flex items-center px-4 py-3 bg-gradient-to-r from-gray-600 to-gray-700 text-white rounded-xl hover:from-gray-700 hover:to-gray-800 transition-all duration-200 text-sm shadow-lg backdrop-blur-sm"
                              >
                                <FileText className="h-4 w-4 mr-1" />
                                TXT
                              </button>
                            </div>
                          </div>
                        </div>
                      </div>
                    );
                  })()}
              </div>
            ))}
          </div>

          {userAnalyses.length === 0 && (
            <div className="text-center py-12">
              <Building2 className="h-12 w-12 text-gray-400 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">
                No analyses yet
              </h3>
              <p className="text-gray-500">
                Start by analyzing your first company!
              </p>
            </div>
          )}
        </div>
      </div>

      {/* Comparison Results */}
      {comparisonResult && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-2xl max-w-7xl max-h-[90vh] overflow-y-auto w-full">
            <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between">
              <h2 className="text-xl font-semibold text-gray-900">
                Company Comparison Report
              </h2>
              <button
                onClick={resetComparison}
                className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
              >
                <X className="h-5 w-5 text-gray-500" />
              </button>
            </div>

            <div className="p-6 space-y-8">
              {/* Summary Overview */}
              <div className="bg-gradient-to-r from-purple-50 to-blue-50 rounded-xl p-6 border border-purple-200">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">
                  Summary Overview
                </h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-center">
                  <div className="bg-white rounded-lg p-4">
                    <div className="text-2xl font-bold text-purple-600">
                      {comparisonResult.analyses?.length || 0}
                    </div>
                    <div className="text-sm text-gray-600">Companies</div>
                  </div>
                  <div className="bg-white rounded-lg p-4">
                    <div className="text-2xl font-bold text-blue-600">
                      {comparisonResult.comparison_type
                        ?.replace("_", " ")
                        .toUpperCase() || "N/A"}
                    </div>
                    <div className="text-sm text-gray-600">Type</div>
                  </div>
                  <div className="bg-white rounded-lg p-4">
                    <div className="text-2xl font-bold text-green-600">
                      {comparisonResult.existing_analyses || 0}
                    </div>
                    <div className="text-sm text-gray-600">Existing</div>
                  </div>
                  <div className="bg-white rounded-lg p-4">
                    <div className="text-2xl font-bold text-orange-600">
                      {comparisonResult.new_analyses || 0}
                    </div>
                    <div className="text-sm text-gray-600">New</div>
                  </div>
                </div>
              </div>
              {/* Benchmarks & Metrics */}
              {comparisonResult.metrics && comparisonResult.analyses && (
                <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
                  <div className="px-6 py-4 border-b border-gray-200 bg-gray-50">
                    <h3 className="text-lg font-semibold text-gray-900">
                      Benchmarks & Metrics
                    </h3>
                  </div>
                  <div className="p-6 overflow-x-auto">
                    <table className="min-w-full">
                      <thead>
                        <tr className="border-b border-gray-200">
                          <th className="text-left py-3 px-4 font-medium text-gray-900">
                            Metric
                          </th>
                          {comparisonResult.analyses?.map(
                            (analysis: any, index: number) => (
                              <th
                                key={index}
                                className="text-center py-3 px-4 font-medium text-gray-900"
                              >
                                {analysis.company_name}
                              </th>
                            )
                          )}
                          <th className="text-center py-3 px-4 font-medium text-blue-600">
                            Benchmark Avg
                          </th>
                        </tr>
                      </thead>
                      <tbody>
                        {/* Market Share Row */}
                        <tr className="border-b border-gray-100 hover:bg-gray-50">
                          <td className="py-3 px-4 font-medium text-gray-700">
                            Market Share
                          </td>
                          {comparisonResult.metrics.map(
                            (metric: any, index: number) => {
                              const values = comparisonResult.metrics.map(
                                (m: any) => m.market_share
                              );
                              const isHighest =
                                metric.market_share === Math.max(...values);
                              return (
                                <td
                                  key={index}
                                  className={`py-3 px-4 text-center ${
                                    isHighest
                                      ? "bg-green-100 text-green-800 font-semibold"
                                      : "bg-red-100 text-red-600"
                                  }`}
                                >
                                  {metric.market_share?.toFixed(2)}%
                                </td>
                              );
                            }
                          )}
                          <td className="py-3 px-4 text-center text-blue-600 font-medium">
                            {comparisonResult.benchmarks?.avg_market_share?.toFixed(
                              2
                            )}
                            %
                          </td>
                        </tr>
                        {/* Growth Rate Row */}
                        <tr className="border-b border-gray-100 hover:bg-gray-50">
                          <td className="py-3 px-4 font-medium text-gray-700">
                            Growth Rate
                          </td>
                          {comparisonResult.metrics.map(
                            (metric: any, index: number) => {
                              const values = comparisonResult.metrics.map(
                                (m: any) => m.growth_rate
                              );
                              const isHighest =
                                metric.growth_rate === Math.max(...values);
                              return (
                                <td
                                  key={index}
                                  className={`py-3 px-4 text-center ${
                                    isHighest
                                      ? "bg-green-100 text-green-800 font-semibold"
                                      : "text-gray-600"
                                  }`}
                                >
                                  {metric.growth_rate?.toFixed(2)}%
                                </td>
                              );
                            }
                          )}
                          <td className="py-3 px-4 text-center text-blue-600 font-medium">
                            {comparisonResult.benchmarks?.avg_growth_rate?.toFixed(
                              2
                            )}
                            %
                          </td>
                        </tr>
                        {/* Risk Rating Row */}
                        <tr className="border-b border-gray-100 hover:bg-gray-50">
                          <td className="py-3 px-4 font-medium text-gray-700">
                            Risk Rating
                          </td>
                          {comparisonResult.metrics.map(
                            (metric: any, index: number) => {
                              const values = comparisonResult.metrics.map(
                                (m: any) => m.risk_rating
                              );
                              const isLowest =
                                metric.risk_rating === Math.min(...values);
                              return (
                                <td
                                  key={index}
                                  className={`py-3 px-4 text-center ${
                                    isLowest
                                      ? "bg-green-100 text-green-800 font-semibold"
                                      : "text-gray-600"
                                  }`}
                                >
                                  {metric.risk_rating?.toFixed(1)}/10
                                </td>
                              );
                            }
                          )}
                          <td className="py-3 px-4 text-center text-blue-600 font-medium">
                            {comparisonResult.benchmarks?.avg_risk_rating?.toFixed(
                              1
                            )}
                            /10
                          </td>
                        </tr>
                        {/* Sentiment Score Row */}
                        <tr className="border-b border-gray-100 hover:bg-gray-50">
                          <td className="py-3 px-4 font-medium text-gray-700">
                            Sentiment Score
                          </td>
                          {comparisonResult.metrics.map(
                            (metric: any, index: number) => {
                              const values = comparisonResult.metrics.map(
                                (m: any) => m.sentiment_score
                              );
                              const isHighest =
                                metric.sentiment_score === Math.max(...values);
                              return (
                                <td
                                  key={index}
                                  className={`py-3 px-4 text-center ${
                                    isHighest
                                      ? "bg-green-100 text-green-800 font-semibold"
                                      : "text-gray-600"
                                  }`}
                                >
                                  {metric.sentiment_score?.toFixed(1)}/100
                                </td>
                              );
                            }
                          )}
                          <td className="py-3 px-4 text-center text-blue-600 font-medium">
                            {comparisonResult.benchmarks?.avg_sentiment_score?.toFixed(
                              1
                            )}
                            /100
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </div>
              )}{" "}
              {/* Visualizations */}
              {(comparisonResult.radar_chart ||
                comparisonResult.bar_graph ||
                comparisonResult.scatter_plot) && (
                <div className="bg-white rounded-xl border border-gray-200">
                  <div className="px-6 py-4 border-b border-gray-200 bg-gray-50">
                    <h3 className="text-lg font-semibold text-gray-900">
                      Visual Analysis
                    </h3>
                  </div>
                  <div className="p-6 grid grid-cols-1 md:grid-cols-3 gap-6">
                    {comparisonResult.radar_chart && (
                      <div className="text-center">
                        <h4 className="font-medium text-gray-900 mb-3">
                          Radar Chart
                        </h4>
                        <img
                          src={`data:image/png;base64,${comparisonResult.radar_chart}`}
                          alt="Radar Chart"
                          className="w-full h-48 object-contain rounded-lg border border-gray-200"
                        />
                      </div>
                    )}
                    {comparisonResult.bar_graph && (
                      <div className="text-center">
                        <h4 className="font-medium text-gray-900 mb-3">
                          Bar Graph
                        </h4>
                        <img
                          src={`data:image/png;base64,${comparisonResult.bar_graph}`}
                          alt="Bar Graph"
                          className="w-full h-48 object-contain rounded-lg border border-gray-200"
                        />
                      </div>
                    )}
                    {comparisonResult.scatter_plot && (
                      <div className="text-center">
                        <h4 className="font-medium text-gray-900 mb-3">
                          Scatter Plot
                        </h4>
                        <img
                          src={`data:image/png;base64,${comparisonResult.scatter_plot}`}
                          alt="Scatter Plot"
                          className="w-full h-48 object-contain rounded-lg border border-gray-200"
                        />
                      </div>
                    )}
                  </div>
                </div>
              )}
              {/* Insights */}
              {comparisonResult.insights && (
                <div className="bg-gradient-to-r from-blue-50 to-indigo-50 rounded-xl border border-blue-200">
                  <div className="px-6 py-4 border-b border-blue-200">
                    <h3 className="text-lg font-semibold text-gray-900">
                      Key Insights
                    </h3>
                  </div>
                  <div className="p-6">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      {(Array.isArray(comparisonResult.insights)
                        ? comparisonResult.insights
                        : [comparisonResult.insights]
                      ).map((insight: string, index: number) => (
                        <div
                          key={index}
                          className="bg-white rounded-lg p-4 border border-blue-100"
                        >
                          <p className="text-sm text-gray-700">{insight}</p>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              )}
              {/* Investment Recommendations */}
              {comparisonResult.investment_recommendations && (
                <div className="bg-gradient-to-r from-green-50 to-emerald-50 rounded-xl border border-green-200">
                  <div className="px-6 py-4 border-b border-green-200">
                    <h3 className="text-lg font-semibold text-gray-900">
                      Investment Recommendations
                    </h3>
                  </div>
                  <div className="p-6">
                    <div className="bg-white rounded-lg p-6 border border-green-100">
                      <div
                        className="prose prose-sm max-w-none text-gray-700"
                        dangerouslySetInnerHTML={{
                          __html:
                            comparisonResult.investment_recommendations.replace(
                              /\n/g,
                              "<br>"
                            ),
                        }}
                      />
                    </div>
                  </div>
                </div>
              )}
              {/* Close Button */}
              <div className="flex justify-between items-center pt-6 border-t border-gray-200">
                {comparisonResult.saved_analysis_ids &&
                  comparisonResult.saved_analysis_ids.length > 0 && (
                    <div className="text-sm text-green-600">
                      ✓ {comparisonResult.saved_analysis_ids.length} new
                      analysis(es) saved to your dashboard
                    </div>
                  )}
                <button
                  onClick={resetComparison}
                  className="px-6 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 transition-colors ml-auto"
                >
                  Close Report
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Full Report Modal */}
      {selectedAnalysis && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-2xl max-w-6xl max-h-[90vh] overflow-y-auto w-full">
            <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between">
              <h2 className="text-xl font-semibold text-gray-900">
                Full Analysis Report - {selectedAnalysis.companyName}
              </h2>
              <button
                onClick={closeModal}
                className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
              >
                <XCircle className="h-5 w-5 text-gray-500" />
              </button>
            </div>

            <div className="p-6">
              {(() => {
                const transformedData =
                  transformAnalysisForDisplay(selectedAnalysis);
                if (!transformedData) return <p>No analysis data available</p>;

                return (
                  <AnalysisDisplay
                    analysisResult={transformedData}
                    companyName={selectedAnalysis.companyName}
                  />
                );
              })()}

              <div className="mt-8 pt-6 border-t border-gray-200">
                <ExportButtons
                  analysisResult={
                    transformAnalysisForDisplay(selectedAnalysis) || {
                      company_name: selectedAnalysis.companyName,
                      summaries: [],
                      sources: [],
                      strategy_recommendations: "",
                      swot_lists: {
                        strengths: [],
                        weaknesses: [],
                        opportunities: [],
                        threats: [],
                      },
                      swot_image: "",
                      pestel_lists: {
                        political: [],
                        economic: [],
                        social: [],
                        technological: [],
                        environmental: [],
                        legal: [],
                      },
                      pestel_image: "",
                      porter_forces: {
                        rivalry: [],
                        new_entrants: [],
                        substitutes: [],
                        buyer_power: [],
                        supplier_power: [],
                      },
                      porter_image: "",
                      bcg_matrix: {},
                      bcg_image: "",
                      mckinsey_7s: {
                        strategy: "",
                        structure: "",
                        systems: "",
                        style: "",
                        staff: "",
                        skills: "",
                        shared_values: "",
                      },
                      mckinsey_image: "",
                      linkedin_analysis: undefined,
                    }
                  }
                />
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Dashboard;
