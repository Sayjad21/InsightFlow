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
} from "lucide-react";
import { useAuth } from "../contexts/AuthContext";
import { ApiService, type UserProfileResponse } from "../services/api.js";
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
                  className={`flex items-center justify-between cursor-pointer transition-all duration-200 ${
                    analysis.status === "COMPLETED" ? "hover:bg-gray-50" : ""
                  }`}
                  onClick={() => handleAnalysisClick(analysis)}
                >
                  <div className="flex items-center space-x-4">
                    {getStatusIcon(analysis.status)}
                    <div>
                      <h4 className="text-lg font-medium text-gray-900">
                        {analysis.companyName}
                      </h4>
                      <p className="text-sm text-gray-500">
                        {formatDate(analysis.analysisDate)}
                      </p>
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

                    {analysis.status === "COMPLETED" && (
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
                {expandedAnalysis === analysis.id &&
                  analysis.status === "COMPLETED" &&
                  (() => {
                    const analysisData = getAnalysisData(analysis);
                    if (!analysisData) return null;

                    return (
                      <div className="mt-6 p-6 bg-gradient-to-br from-white/90 via-gray-50/50 to-blue-50/30 rounded-2xl border border-gray-200/50 shadow-lg backdrop-blur-sm">
                        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
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
                          analysisData.porterImage ||
                          analysisData.bcgImage ||
                          analysisData.mckinseyImage) && (
                          <div className="mt-6">
                            <h5 className="text-lg font-semibold text-gray-900 mb-4">
                              Analysis Charts
                            </h5>
                            <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
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
