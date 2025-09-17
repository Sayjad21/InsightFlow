import React, { useState } from "react";
import {
  Building2,
  CheckCircle,
  Clock,
  AlertCircle,
  Eye,
  ChevronDown,
  ChevronUp,
  GitCompare,
  Plus,
  X,
  Loader2,
  TrendingUp,
  Upload,
  FileText,
} from "lucide-react";
import type { UserAnalysis } from "../../types";
import type { UserProfileResponse } from "../../services/api";
import { ApiService } from "../../services/api";
import AnalysisDisplay from "../AnalysisDisplay";
import AnalysisModal from "../AnalysisModal";
import ExportButtons from "../ExportButtons";
import Pagination from "../common/Pagination";

interface AnalysisTabProps {
  userAnalyses: UserAnalysis[];
  userProfile: UserProfileResponse | null;
  analysisCurrentPage: number;
  setAnalysisCurrentPage: (page: number) => void;
  analysisTotalPages: number;
  expandedAnalysis: string | null;
  setExpandedAnalysis: (id: string | null) => void;
  selectedAnalysis: UserAnalysis | null;
  setSelectedAnalysis: (analysis: UserAnalysis | null) => void;
  comparisonMode: boolean;
  setComparisonMode: (mode: boolean) => void;
  selectedAnalysisIds: string[];
  setSelectedAnalysisIds: React.Dispatch<React.SetStateAction<string[]>>;
  newCompanyNames: string[];
  setNewCompanyNames: React.Dispatch<React.SetStateAction<string[]>>;
  newCompanyFiles: (File | null)[];
  setNewCompanyFiles: React.Dispatch<React.SetStateAction<(File | null)[]>>;
  saveNewAnalyses: boolean;
  setSaveNewAnalyses: (save: boolean) => void;
  isComparing: boolean;
  setIsComparing: (comparing: boolean) => void;
  refreshAnalyses: () => void;
  refreshComparisons: () => void;
  onProfileRefresh?: () => void;
}

const AnalysisTab: React.FC<AnalysisTabProps> = ({
  userAnalyses,
  analysisCurrentPage,
  setAnalysisCurrentPage,
  analysisTotalPages,
  expandedAnalysis,
  setExpandedAnalysis,
  selectedAnalysis,
  setSelectedAnalysis,
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
  isComparing,
  setIsComparing,
  refreshAnalyses,
  refreshComparisons,
  onProfileRefresh,
}) => {
  const [comparisonError, setComparisonError] = useState<string | null>(null);

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "COMPLETED":
        return <CheckCircle className="h-5 w-5 text-green-600" />;
      case "PENDING":
        return <Clock className="h-5 w-5 text-yellow-600" />;
      case "FAILED":
        return <AlertCircle className="h-5 w-5 text-red-600" />;
      default:
        return <AlertCircle className="h-5 w-5 text-gray-600" />;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case "COMPLETED":
        return "bg-green-50 text-green-700 border-green-200";
      case "PENDING":
        return "bg-yellow-50 text-yellow-700 border-yellow-200";
      case "FAILED":
        return "bg-red-50 text-red-700 border-red-200";
      default:
        return "bg-gray-50 text-gray-700 border-gray-200";
    }
  };

  const toggleComparisonMode = () => {
    setComparisonMode(!comparisonMode);
    if (!comparisonMode) {
      // Reset comparison state when entering comparison mode
      setSelectedAnalysisIds([]);
      setNewCompanyNames([""]);
      setNewCompanyFiles([null]);
      setSaveNewAnalyses(false);
      setComparisonError(null);
    }
  };

  const handleAnalysisSelection = (analysisId: string) => {
    setSelectedAnalysisIds((prev: string[]) =>
      prev.includes(analysisId)
        ? prev.filter((id: string) => id !== analysisId)
        : [...prev, analysisId]
    );
  };

  const handleAnalysisClick = (analysis: UserAnalysis) => {
    if (comparisonMode) return;

    if (analysis.status === "COMPLETED") {
      if (expandedAnalysis === analysis.id) {
        setExpandedAnalysis(null);
      } else {
        setExpandedAnalysis(analysis.id);

        // Scroll to the analysis item after a short delay to allow for expansion
        setTimeout(() => {
          const analysisElement = document.getElementById(
            `analysis-${analysis.id}`
          );
          if (analysisElement) {
            const elementTop = analysisElement.offsetTop;
            const offset = 100; // Add some offset from the top
            window.scrollTo({
              top: elementTop - offset,
              behavior: "smooth",
            });
          }
        }, 300); // Wait for the expansion animation
      }
    }
  };

  const handleCompanyNameChange = (index: number, value: string) => {
    setNewCompanyNames((prev: string[]) => {
      const updated = [...prev];
      updated[index] = value;
      return updated;
    });
  };

  const handleCompanyFileChange = (index: number, file: File | null) => {
    // Update state
    setNewCompanyFiles((prev) => {
      const updated = [...prev];
      updated[index] = file;
      return updated;
    });

    // Reset input if file removed
    if (!file) {
      const input = document.getElementById(
        `file-${index}`
      ) as HTMLInputElement;
      if (input) input.value = "";
    }
  };

  const addCompanyField = () => {
    if (newCompanyNames.length < 5) {
      setNewCompanyNames((prev: string[]) => [...prev, ""]);
      setNewCompanyFiles((prev: (File | null)[]) => [...prev, null]);
    }
  };

  const removeCompanyField = (index: number) => {
    if (newCompanyNames.length > 1) {
      setNewCompanyNames((prev: string[]) =>
        prev.filter((_: string, i: number) => i !== index)
      );
      setNewCompanyFiles((prev: (File | null)[]) =>
        prev.filter((_: File | null, i: number) => i !== index)
      );
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
      if (selectedAnalysisIds.length > 0 && validCompanyNames.length === 0) {
        // Compare existing analyses only
        await ApiService.compareExistingAnalyses(selectedAnalysisIds);
      } else {
        // Mixed or new company comparison - collect files for companies with names
        const filesToSend: (File | null)[] = [];
        for (let i = 0; i < validCompanyNames.length; i++) {
          // Find the corresponding file for this company name
          const companyName = validCompanyNames[i];
          const nameIndex = newCompanyNames.findIndex(
            (name, idx) =>
              name.trim() === companyName && idx < newCompanyFiles.length
          );
          filesToSend.push(
            nameIndex >= 0 ? newCompanyFiles[nameIndex] || null : null
          );
        }

        await ApiService.compareEnhanced({
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
          files: filesToSend.length > 0 ? filesToSend : undefined,
        });
      }

      // Reset comparison state and refresh data
      setComparisonMode(false);
      setSelectedAnalysisIds([]);
      setNewCompanyNames([""]);
      setNewCompanyFiles([null]);
      setSaveNewAnalyses(false);

      // Refresh both analyses and comparisons
      refreshAnalyses();
      refreshComparisons();

      // Refresh profile statistics to update total comparisons
      if (onProfileRefresh) {
        onProfileRefresh();
      }
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

  // Helper function to create a result object for ExportButtons
  const createAnalysisResult = (analysis: UserAnalysis) => {
    const porterForces = analysis.porterForces
      ? {
          rivalry: analysis.porterForces.rivalry || [],
          new_entrants: analysis.porterForces.newEntrants || [],
          substitutes: analysis.porterForces.substitutes || [],
          buyer_power: analysis.porterForces.buyerPower || [],
          supplier_power: analysis.porterForces.supplierPower || [],
        }
      : {
          rivalry: [],
          new_entrants: [],
          substitutes: [],
          buyer_power: [],
          supplier_power: [],
        };

    // Transform BCG matrix from camelCase to snake_case
    const bcgMatrix: {
      [key: string]: { market_share: number; growth_rate: number };
    } = {};
    if (analysis.bcgMatrix) {
      Object.entries(analysis.bcgMatrix).forEach(([key, value]) => {
        if (
          value &&
          typeof value === "object" &&
          "marketShare" in value &&
          "growthRate" in value
        ) {
          bcgMatrix[key] = {
            market_share: value.marketShare,
            growth_rate: value.growthRate,
          };
        }
      });
    }

    // Transform McKinsey 7S from camelCase to snake_case
    const mckinsey7s = analysis.mckinsey7s
      ? {
          strategy: analysis.mckinsey7s.strategy || "",
          structure: analysis.mckinsey7s.structure || "",
          systems: analysis.mckinsey7s.systems || "",
          shared_values: analysis.mckinsey7s.sharedValues || "",
          style: analysis.mckinsey7s.style || "",
          staff: analysis.mckinsey7s.staff || "",
          skills: analysis.mckinsey7s.skills || "",
        }
      : {
          strategy: "",
          structure: "",
          systems: "",
          shared_values: "",
          style: "",
          staff: "",
          skills: "",
        };

    return {
      company_name: analysis.companyName,
      summaries: analysis.summaries || [],
      sources: analysis.sources || [],
      strategy_recommendations: analysis.strategyRecommendations || "",
      swot_lists: analysis.swotLists || {
        strengths: [],
        weaknesses: [],
        opportunities: [],
        threats: [],
      },
      swot_image: analysis.swotImage || "",
      pestel_lists: analysis.pestelLists || {
        political: [],
        economic: [],
        social: [],
        technological: [],
        environmental: [],
        legal: [],
      },
      pestel_image: analysis.pestelImage || "",
      porter_forces: porterForces,
      porter_image: analysis.porterImage || "",
      bcg_matrix: bcgMatrix,
      bcg_image: analysis.bcgImage || "",
      mckinsey_7s: mckinsey7s,
      mckinsey_image: analysis.mckinseyImage || "",
      ansoff_matrix: {
        market_penetration: "",
        market_development: "",
        product_development: "",
        diversification: "",
      },
      ansoff_image: "",
      value_chain: {
        primary_activities: [],
        support_activities: [],
      },
      value_chain_image: "",
      balanced_scorecard: {
        financial: [],
        customer: [],
        internal: [],
        learning: [],
      },
      balanced_scorecard_image: "",
      linkedin_analysis: analysis.linkedinAnalysis || "",
    };
  };

  return (
    <>
      {/* Header */}
      <div className="bg-gradient-to-r from-green-50 to-teal-50 border border-blue-200 rounded-xl p-6 mb-8">
        <div className="flex items-center justify-center mb-4">
          <div className="bg-blue-100 p-3 rounded-full mr-4">
            <Building2 className="h-8 w-8 text-green-600" />
          </div>
          <div>
            <h2 className="text-xl font-semibold text-teal-900">
              Company Analysis
            </h2>
            <p className="text-blue-700">
              Create comprehensive business analysis reports and compare
              companies.
            </p>
          </div>
        </div>
        <div className="text-center">
          <p className="text-gray-600 mb-4">
            Generate detailed insights using SWOT, PESTEL, Porter's Forces, and
            other strategic frameworks.
          </p>
          <div className="flex items-center justify-center text-sm text-gray-500">
            <Clock className="h-4 w-4 mr-2" />
            Updated 2025
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
            <div className="bg-purple-50 border border-purple-200 rounded-lg p-4">
              <h4 className="text-sm font-semibold text-purple-900 mb-2">
                How to Compare:
              </h4>
              <ul className="text-sm text-purple-800 space-y-1">
                <li>• Select 2-5 completed analyses from your history below</li>
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
              <p className="text-sm text-gray-600 mb-4">
                Upload files (TXT) to provide additional context for analysis
              </p>
              <div className="space-y-3">
                {newCompanyNames.map((name, index) => (
                  <div key={index} className="flex items-center space-x-3">
                    <div className="flex-1">
                      <input
                        type="text"
                        placeholder={`Company ${index + 1} name`}
                        value={name}
                        onChange={(e) =>
                          handleCompanyNameChange(index, e.target.value)
                        }
                        className="w-full px-4 py-2 border border-purple-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent text-purple-900 placeholder-purple-400"
                      />
                      {/* File upload section - only show when company name is entered */}
                      {name.trim() && (
                        <div className="mt-2 flex items-center space-x-2">
                          <input
                            type="file"
                            id={`file-${index}`}
                            accept=".pdf,.doc,.docx,.txt,.csv,.xlsx,.xls"
                            onChange={(e) => {
                              const file = e.target.files?.[0] || null;
                              handleCompanyFileChange(index, file);
                            }}
                            className="hidden"
                          />
                          <label
                            htmlFor={`file-${index}`}
                            className="flex items-center px-3 py-1 text-sm text-purple-600 bg-purple-50 border border-purple-200 rounded-lg cursor-pointer hover:bg-purple-100 transition-colors"
                          >
                            <Upload className="h-4 w-4 mr-1" />
                            Upload File
                          </label>
                          {newCompanyFiles[index] && (
                            <div className="flex items-center space-x-1 text-sm text-gray-600">
                              <FileText className="h-4 w-4" />
                              <span className="truncate max-w-32">
                                {newCompanyFiles[index]?.name}
                              </span>
                              <button
                                onClick={() =>
                                  handleCompanyFileChange(index, null)
                                }
                                className="text-red-500 hover:text-red-700"
                              >
                                <X className="h-3 w-3" />
                              </button>
                            </div>
                          )}
                        </div>
                      )}
                    </div>
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
                    setNewCompanyFiles([null]);
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
      {/* <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-200">
          <div className="flex items-center">
            <div className="p-2 bg-blue-100 rounded-lg">
              <Building2 className="h-6 w-6 text-blue-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Total</p>
              <p className="text-2xl font-bold text-gray-900">
                {userProfile?.totalAnalyses || 0}
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
                {userProfile?.successfulAnalyses || 0}
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
                {userAnalyses.filter((a) => a.status === "PENDING").length}
              </p>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-200">
          <div className="flex items-center">
            <div className="p-2 bg-red-100 rounded-lg">
              <AlertCircle className="h-6 w-6 text-red-600" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Failed</p>
              <p className="text-2xl font-bold text-gray-900">
                {userAnalyses.filter((a) => a.status === "FAILED").length}
              </p>
            </div>
          </div>
        </div>
      </div> */}

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
            <div
              key={analysis.id}
              id={`analysis-${analysis.id}`}
              className="p-6"
            >
              <div
                className={`flex items-center justify-between transition-all duration-200 ${
                  !comparisonMode && analysis.status === "COMPLETED"
                    ? "cursor-pointer hover:bg-gray-50"
                    : ""
                }`}
                onClick={() => handleAnalysisClick(analysis)}
              >
                <div className="flex items-center space-x-4">
                  {/* Comparison Mode Checkbox */}
                  {comparisonMode && analysis.status === "COMPLETED" && (
                    <div className="relative">
                      <input
                        type="checkbox"
                        id={`checkbox-${analysis.id}`}
                        checked={selectedAnalysisIds.includes(analysis.id)}
                        onChange={() => handleAnalysisSelection(analysis.id)}
                        onClick={(e) => e.stopPropagation()}
                        className="peer sr-only"
                      />
                      <label
                        htmlFor={`checkbox-${analysis.id}`}
                        className="relative flex items-center justify-center w-5 h-5 bg-white border-2 border-gray-300 rounded cursor-pointer transition-all duration-200 peer-checked:bg-gradient-to-r peer-checked:from-purple-600 peer-checked:to-blue-600 peer-checked:border-purple-600 hover:border-purple-400 peer-focus:ring-2 peer-focus:ring-purple-500 peer-focus:ring-opacity-50"
                        onClick={(e) => e.stopPropagation()}
                      >
                        {selectedAnalysisIds.includes(analysis.id) && (
                          <CheckCircle className="w-3 h-3 text-white" />
                        )}
                      </label>
                    </div>
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
                    {analysis.status}
                  </span>
                  <div className="flex items-center space-x-2">
                    {analysis.status === "COMPLETED" && (
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          setSelectedAnalysis(analysis);
                        }}
                        className="p-2 text-gray-400 hover:text-blue-600 transition-colors duration-200"
                        title="View Details"
                      >
                        <Eye className="h-4 w-4" />
                      </button>
                    )}
                    {!comparisonMode && (
                      <>
                        {expandedAnalysis === analysis.id ? (
                          <ChevronUp className="h-5 w-5 text-gray-400" />
                        ) : (
                          <ChevronDown className="h-5 w-5 text-gray-400" />
                        )}
                      </>
                    )}
                  </div>
                </div>
              </div>

              {/* Expanded Analysis Details */}
              {expandedAnalysis === analysis.id &&
                analysis.status === "COMPLETED" &&
                analysis.summaries && (
                  <div className="mt-6 p-6 bg-gradient-to-br from-white/90 via-gray-50/50 to-purple-50/30 rounded-2xl border border-gray-200/50 shadow-lg backdrop-blur-sm">
                    <div className="flex items-center justify-between mb-6">
                      <h4 className="text-xl font-bold text-gray-900">
                        Analysis Results
                      </h4>
                      <ExportButtons
                        analysisResult={createAnalysisResult(analysis)}
                      />
                    </div>
                    <AnalysisDisplay
                      analysisResult={createAnalysisResult(analysis)}
                    />
                  </div>
                )}
            </div>
          ))}

          {/* Pagination Controls */}
          {userAnalyses.length > 0 && analysisTotalPages > 1 && (
            <div className="px-6 py-4 border-t border-gray-200">
              <Pagination
                currentPage={analysisCurrentPage}
                totalPages={analysisTotalPages}
                onPageChange={setAnalysisCurrentPage}
              />
            </div>
          )}

          {userAnalyses.length === 0 && (
            <div className="text-center py-12">
              <Building2 className="h-12 w-12 text-gray-400 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">
                No analyses yet
              </h3>
              <p className="text-gray-500">
                Your analysis history will appear here once you start analyzing
                companies.
              </p>
            </div>
          )}
        </div>
      </div>

      {/* Analysis Detail Modal */}
      {selectedAnalysis && (
        <AnalysisModal
          analysisResult={createAnalysisResult(selectedAnalysis)}
          companyName={selectedAnalysis.companyName}
          onClose={() => setSelectedAnalysis(null)}
        />
      )}
    </>
  );
};

export default AnalysisTab;
