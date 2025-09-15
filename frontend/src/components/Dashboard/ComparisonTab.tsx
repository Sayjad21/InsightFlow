import React from "react";
import {
  BarChart3,
  ChevronDown,
  ChevronUp,
  Eye,
  GitCompare,
  Calendar,
  TrendingUp,
  Building2,
  Target,
  Shield,
} from "lucide-react";
import type { ComparisonResult } from "../../types";
import { ApiService } from "../../services/api";
import Pagination from "../common/Pagination";

interface ComparisonTabProps {
  comparisonResults: ComparisonResult[];
  comparisonCurrentPage: number;
  setComparisonCurrentPage: (page: number) => void;
  comparisonTotalPages: number;
  totalComparisons: number;
  expandedComparison?: string | null;
  setExpandedComparison?: (id: string | null) => void;
  setSelectedComparison?: (comparison: ComparisonResult | null) => void;
}

const ComparisonTab: React.FC<ComparisonTabProps> = ({
  comparisonResults,
  comparisonCurrentPage,
  setComparisonCurrentPage,
  comparisonTotalPages,
  totalComparisons,
  expandedComparison = null,
  setExpandedComparison = () => {},
  setSelectedComparison = () => {},
}) => {
  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const handleComparisonClick = (comparison: ComparisonResult) => {
    if (expandedComparison === comparison.id) {
      setExpandedComparison(null);
    } else {
      setExpandedComparison(comparison.id);
    }
  };

  const handleViewFullReport = async (
    comparison: ComparisonResult,
    e: React.MouseEvent
  ) => {
    e.stopPropagation();
    try {
      console.log("Fetching full comparison data for ID:", comparison.id);
      const fullComparisonData = await ApiService.getSavedComparison(
        comparison.id
      );
      console.log("Full comparison data:", fullComparisonData);
      setSelectedComparison(fullComparisonData);
    } catch (error) {
      console.error("Failed to fetch full comparison data:", error);
      // Fallback to summary data
      setSelectedComparison(comparison);
    }
  };

  const renderComparisonInsights = (comparison: ComparisonResult) => {
    if (!comparison.insights && !comparison.investmentRecommendations)
      return null;

    // Extract key insights from comparison results
    const insights = [];

    if (comparison.insights && comparison.insights.length > 0) {
      insights.push({
        type: "Key Insights",
        icon: <TrendingUp className="h-4 w-4" />,
        content: comparison.insights.slice(0, 2).join(". "),
        color: "blue",
      });
    }

    if (comparison.investmentRecommendations) {
      insights.push({
        type: "Investment Recommendations",
        icon: <Target className="h-4 w-4" />,
        content: comparison.investmentRecommendations.slice(0, 200) + "...",
        color: "green",
      });
    }

    if (comparison.benchmarks) {
      insights.push({
        type: "Risk Assessment",
        icon: <Shield className="h-4 w-4" />,
        content: `Average Risk Rating: ${
          comparison.benchmarks.avgRiskRating?.toFixed(1) || "N/A"
        }/10`,
        color: "orange",
      });
    }

    return insights;
  };

  const getInsightColorClass = (color: string) => {
    switch (color) {
      case "blue":
        return "bg-blue-50 border-blue-200 text-blue-800";
      case "purple":
        return "bg-purple-50 border-purple-200 text-purple-800";
      case "green":
        return "bg-green-50 border-green-200 text-green-800";
      case "orange":
        return "bg-orange-50 border-orange-200 text-orange-800";
      case "red":
        return "bg-red-50 border-red-200 text-red-800";
      default:
        return "bg-gray-50 border-gray-200 text-gray-800";
    }
  };

  return (
    <>
      {/* Comparison History */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200">
        <div className="px-6 py-4 border-b border-gray-200">
          <div className="flex items-center">
            <BarChart3 className="h-6 w-6 text-purple-600 mr-3" />
            <div>
              <h3 className="text-lg font-semibold text-gray-900">
                Comparison History
              </h3>
              <p className="text-sm text-gray-600 mt-1">
                View your saved company comparison reports
              </p>
            </div>
          </div>
        </div>

        <div className="divide-y divide-gray-200">
          {comparisonResults.map((comparison) => (
            <div key={comparison.id} className="p-6">
              <div
                className="flex items-center justify-between cursor-pointer hover:bg-gray-50 transition-all duration-200"
                onClick={() => handleComparisonClick(comparison)}
              >
                <div className="flex items-center space-x-4">
                  <div className="p-2 bg-purple-100 rounded-lg">
                    <GitCompare className="h-5 w-5 text-purple-600" />
                  </div>
                  <div>
                    <h4 className="text-lg font-medium text-gray-900">
                      {comparison.companyNames?.join(" vs ") ||
                        "Company Comparison"}
                    </h4>
                    <div className="flex items-center space-x-4 mt-1">
                      <p className="text-sm text-gray-500 flex items-center">
                        <Calendar className="h-4 w-4 mr-1" />
                        {formatDate(comparison.comparisonDate)}
                      </p>
                      <p className="text-sm text-gray-500 flex items-center">
                        <Building2 className="h-4 w-4 mr-1" />
                        {comparison.companyNames?.length ||
                          comparison.analyses?.length ||
                          0}{" "}
                        companies
                      </p>
                    </div>
                  </div>
                </div>

                <div className="flex items-center space-x-2">
                  <button
                    onClick={(e) => handleViewFullReport(comparison, e)}
                    className="p-2 text-gray-400 hover:text-blue-600 transition-colors duration-200"
                    title="View Full Report"
                  >
                    <Eye className="h-4 w-4" />
                  </button>
                  {expandedComparison === comparison.id ? (
                    <ChevronUp className="h-5 w-5 text-gray-400" />
                  ) : (
                    <ChevronDown className="h-5 w-5 text-gray-400" />
                  )}
                </div>
              </div>

              {/* Expanded Comparison Preview */}
              {expandedComparison === comparison.id && (
                <div className="mt-6 p-6 bg-gradient-to-br from-white/90 via-gray-50/50 to-purple-50/30 rounded-2xl border border-gray-200/50 shadow-lg backdrop-blur-sm">
                  <div className="flex items-center justify-between mb-6">
                    <h4 className="text-xl font-bold text-gray-900">
                      Comparison Insights
                    </h4>
                    <button
                      onClick={(e) => handleViewFullReport(comparison, e)}
                      className="px-4 py-2 bg-gradient-to-r from-purple-600 to-blue-600 text-white rounded-lg hover:from-purple-700 hover:to-blue-700 transition-all duration-200 text-sm font-medium"
                    >
                      View Full Report
                    </button>
                  </div>

                  {/* Company Overview */}
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-6">
                    {(
                      comparison.companyNames ||
                      comparison.analyses?.map((a) => a.companyName) ||
                      []
                    ).map((company: string, index: number) => (
                      <div
                        key={index}
                        className="bg-white rounded-lg p-4 border border-gray-200"
                      >
                        <div className="flex items-center">
                          <div className="p-2 bg-blue-100 rounded-lg">
                            <Building2 className="h-4 w-4 text-blue-600" />
                          </div>
                          <div className="ml-3">
                            <h5 className="font-medium text-gray-900">
                              {company}
                            </h5>
                            <p className="text-xs text-gray-500">
                              Analysis #{index + 1}
                            </p>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>

                  {/* Quick Insights */}
                  {renderComparisonInsights(comparison) && (
                    <div className="space-y-4">
                      <h5 className="font-semibold text-gray-900 mb-3">
                        Key Insights
                      </h5>
                      {renderComparisonInsights(comparison)?.map(
                        (insight, index) => (
                          <div
                            key={index}
                            className={`p-4 rounded-lg border ${getInsightColorClass(
                              insight.color
                            )}`}
                          >
                            <div className="flex items-start space-x-2">
                              <div className="mt-0.5">{insight.icon}</div>
                              <div>
                                <h6 className="font-medium mb-1">
                                  {insight.type}
                                </h6>
                                <p className="text-sm leading-relaxed">
                                  {insight.content.length > 200
                                    ? `${insight.content.substring(0, 200)}...`
                                    : insight.content}
                                </p>
                              </div>
                            </div>
                          </div>
                        )
                      )}
                    </div>
                  )}
                </div>
              )}
            </div>
          ))}

          {/* Pagination Controls */}
          {comparisonResults.length > 0 && comparisonTotalPages > 1 && (
            <div className="px-6 py-4 border-t border-gray-200">
              <Pagination
                currentPage={comparisonCurrentPage}
                totalPages={comparisonTotalPages}
                onPageChange={setComparisonCurrentPage}
              />
            </div>
          )}

          {comparisonResults.length === 0 && (
            <div className="text-center py-12">
              <BarChart3 className="h-12 w-12 text-gray-400 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">
                No comparisons yet
              </h3>
              <p className="text-gray-500 mb-4">
                Your comparison reports will appear here once you start
                comparing companies.
              </p>
              <p className="text-sm text-gray-400">
                Use the Analysis tab to create your first comparison!
              </p>
            </div>
          )}
        </div>
      </div>
    </>
  );
};

export default ComparisonTab;
