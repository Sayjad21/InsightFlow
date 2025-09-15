import React from "react";
import { X, Download, FileText, FileImage, File } from "lucide-react";
import type { ComparisonResult } from "../../types";
import {
  exportComparisonToTxt,
  exportComparisonToMarkdown,
  exportComparisonToHtml,
  exportComparisonToPdf,
} from "../../utils/comparisonExport/comparisonExportUtils";

interface ComparisonModalProps {
  selectedComparison: ComparisonResult;
  onClose: () => void;
}

// Helper function to get company names consistently
function getCompanyNames(comparison: ComparisonResult): string[] {
  if (comparison.companyNames && comparison.companyNames.length > 0) {
    return comparison.companyNames;
  }
  if (comparison.analyses && comparison.analyses.length > 0) {
    return comparison.analyses
      .map((analysis) => analysis.companyName)
      .filter((name) => name && name.trim() !== "");
  }
  const count = Math.max(
    comparison.metrics?.length || 0,
    comparison.analyses?.length || 0
  );
  if (count > 0) {
    return Array.from({ length: count }, (_, i) => `Company ${i + 1}`);
  }
  return ["Unknown Companies"];
}

const ComparisonModal: React.FC<ComparisonModalProps> = ({
  selectedComparison,
  onClose,
}) => {
  const formatDate = (dateString: string | undefined) => {
    if (!dateString) return "Unknown date";
    try {
      return new Date(dateString).toLocaleDateString("en-US", {
        year: "numeric",
        month: "short",
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      });
    } catch (error) {
      return "Invalid date";
    }
  };

  // Custom export handlers for comparison reports
  const handleExportMarkdown = () => {
    exportComparisonToMarkdown(selectedComparison);
  };

  const handleExportText = () => {
    exportComparisonToTxt(selectedComparison);
  };

  const handleExportPDF = async () => {
    try {
      await exportComparisonToPdf(selectedComparison);
    } catch (error) {
      console.error("Export to PDF failed:", error);
    }
  };

  const handleExportHTML = () => {
    try {
      exportComparisonToHtml(selectedComparison);
    } catch (error) {
      console.error("Export to HTML failed:", error);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-black/70 backdrop-blur-sm"
        onClick={onClose}
      ></div>

      {/* Modal */}
      <div
        className="bg-cyan-800/30 backdrop-blur-sm rounded-2xl w-full max-w-6xl max-h-[90vh] overflow-auto relative z-10 shadow-xl shadow-blue-900/20"
        style={{
          animation: "modalFadeIn 0.3s ease-out forwards",
        }}
      >
        {/* Close button */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-gray-400 hover:text-white p-2 rounded-full hover:bg-white/10 transition-colors z-10"
        >
          <X className="h-5 w-5" />
        </button>

        {/* Content */}
        <div className="p-8">
          <h2 className="text-2xl font-bold text-white mb-6">
            {selectedComparison.companyNames?.join(" vs ") ||
              "Company Comparison"}{" "}
            - Full Report
          </h2>

          <div className="space-y-8">
            {/* Comparison Overview */}
            <div className="bg-white/5 rounded-xl p-6">
              <h3 className="text-xl font-semibold text-blue-400 mb-4">
                Comparison Overview
              </h3>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <div>
                  <h4 className="font-medium text-green-400 mb-3">
                    Companies Analyzed
                  </h4>
                  <div className="flex flex-wrap gap-2">
                    {(() => {
                      const companyNames = getCompanyNames(selectedComparison);
                      return companyNames.length > 0 &&
                        companyNames[0] !== "Unknown Companies" ? (
                        companyNames.map((company: string, index: number) => (
                          <span
                            key={index}
                            className="px-3 py-1 bg-blue-500/20 text-blue-300 rounded-full text-sm border border-blue-500/30"
                          >
                            {company}
                          </span>
                        ))
                      ) : (
                        <span className="px-3 py-1 bg-gray-500/20 text-gray-400 rounded-full text-sm">
                          No company names available
                        </span>
                      );
                    })()}
                  </div>
                </div>
                <div>
                  <h4 className="font-medium text-green-400 mb-3">
                    Comparison Type
                  </h4>
                  <span className="px-3 py-1 bg-purple-500/20 text-purple-300 rounded-full text-sm capitalize border border-purple-500/30">
                    {selectedComparison.comparisonType || "standard"}
                  </span>
                </div>
                <div>
                  <h4 className="font-medium text-green-400 mb-3">
                    Date Created
                  </h4>
                  <span className="text-sm text-gray-300">
                    {formatDate(selectedComparison.comparisonDate)}
                  </span>
                </div>
              </div>
            </div>

            {/* Industry Benchmarks */}
            {selectedComparison.benchmarks && (
              <div className="bg-white/5 rounded-xl p-6">
                <h3 className="text-xl font-semibold text-blue-400 mb-4">
                  Industry Benchmarks
                </h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  <div className="bg-blue-500/10 border border-blue-500/30 rounded-lg p-4 text-center">
                    <h4 className="font-medium text-blue-300 mb-1">
                      Market Share
                    </h4>
                    <p className="text-2xl font-bold text-blue-400">
                      {selectedComparison.benchmarks.avgMarketShare != null
                        ? (
                            selectedComparison.benchmarks.avgMarketShare * 100
                          ).toFixed(1) + "%"
                        : "N/A"}
                    </p>
                    <p className="text-xs text-blue-300">Industry Average</p>
                  </div>
                  <div className="bg-green-500/10 border border-green-500/30 rounded-lg p-4 text-center">
                    <h4 className="font-medium text-green-300 mb-1">
                      Growth Rate
                    </h4>
                    <p className="text-2xl font-bold text-green-400">
                      {selectedComparison.benchmarks.avgGrowthRate != null
                        ? selectedComparison.benchmarks.avgGrowthRate.toFixed(
                            1
                          ) + "%"
                        : "N/A"}
                    </p>
                    <p className="text-xs text-green-300">Industry Average</p>
                  </div>
                  <div className="bg-orange-500/10 border border-orange-500/30 rounded-lg p-4 text-center">
                    <h4 className="font-medium text-orange-300 mb-1">
                      Risk Rating
                    </h4>
                    <p className="text-2xl font-bold text-orange-400">
                      {selectedComparison.benchmarks.avgRiskRating != null
                        ? selectedComparison.benchmarks.avgRiskRating.toFixed(
                            1
                          ) + "/10"
                        : "N/A"}
                    </p>
                    <p className="text-xs text-orange-300">Industry Average</p>
                  </div>
                  <div className="bg-purple-500/10 border border-purple-500/30 rounded-lg p-4 text-center">
                    <h4 className="font-medium text-purple-300 mb-1">
                      Sentiment
                    </h4>
                    <p className="text-2xl font-bold text-purple-400">
                      {selectedComparison.benchmarks.avgSentimentScore != null
                        ? selectedComparison.benchmarks.avgSentimentScore.toFixed(
                            0
                          )
                        : "N/A"}
                    </p>
                    <p className="text-xs text-purple-300">Industry Average</p>
                  </div>
                </div>
              </div>
            )}

            {/* Company Performance Metrics */}
            {selectedComparison.metrics &&
              selectedComparison.metrics.length > 0 && (
                <div className="bg-white/5 rounded-xl p-6">
                  <h3 className="text-xl font-semibold text-blue-400 mb-4">
                    Company Performance Metrics
                  </h3>
                  <div className="grid grid-cols-1 gap-4">
                    {selectedComparison.metrics.map((metric, index) => {
                      const companyNames = getCompanyNames(selectedComparison);
                      const companyName =
                        companyNames[index] || `Company ${index + 1}`;
                      return (
                        <div
                          key={index}
                          className="bg-white/5 border border-gray-500/30 rounded-lg p-4"
                        >
                          <h4 className="font-medium text-gray-300 mb-3">
                            {companyName}
                          </h4>
                          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                            <div className="text-center">
                              <h5 className="text-sm font-medium text-blue-300 mb-1">
                                Market Share
                              </h5>
                              <p className="text-lg font-bold text-blue-400">
                                {metric.marketShare != null
                                  ? (metric.marketShare * 100).toFixed(1) + "%"
                                  : "N/A"}
                              </p>
                            </div>
                            <div className="text-center">
                              <h5 className="text-sm font-medium text-green-300 mb-1">
                                Growth Rate
                              </h5>
                              <p className="text-lg font-bold text-green-400">
                                {metric.growthRate != null
                                  ? metric.growthRate.toFixed(1) + "%"
                                  : "N/A"}
                              </p>
                            </div>
                            <div className="text-center">
                              <h5 className="text-sm font-medium text-orange-300 mb-1">
                                Risk Rating
                              </h5>
                              <p className="text-lg font-bold text-orange-400">
                                {metric.riskRating != null
                                  ? metric.riskRating.toFixed(1) + "/10"
                                  : "N/A"}
                              </p>
                            </div>
                            <div className="text-center">
                              <h5 className="text-sm font-medium text-purple-300 mb-1">
                                Sentiment
                              </h5>
                              <p className="text-lg font-bold text-purple-400">
                                {metric.sentimentScore != null
                                  ? metric.sentimentScore.toFixed(0)
                                  : "N/A"}
                              </p>
                            </div>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              )}

            {/* Key Insights */}
            {selectedComparison.insights &&
              selectedComparison.insights.length > 0 && (
                <div className="bg-white/5 rounded-xl p-6">
                  <h3 className="text-xl font-semibold text-blue-400 mb-4">
                    Key Insights
                  </h3>
                  <div className="space-y-3">
                    {selectedComparison.insights.map((insight, index) => (
                      <div
                        key={index}
                        className="flex items-start bg-blue-500/10 border border-blue-500/30 rounded-lg p-4"
                      >
                        <div className="w-2 h-2 bg-blue-400 rounded-full mt-2 mr-3 flex-shrink-0"></div>
                        <span className="text-gray-300">{insight}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

            {/* Investment Recommendations */}
            {selectedComparison.investmentRecommendations && (
              <div className="bg-white/5 rounded-xl p-6">
                <h3 className="text-xl font-semibold text-blue-400 mb-4">
                  Investment Recommendations
                </h3>
                <div className="bg-green-500/10 border border-green-500/30 rounded-lg p-4">
                  <div
                    className="text-gray-300 prose prose-sm max-w-none"
                    dangerouslySetInnerHTML={{
                      __html: selectedComparison.investmentRecommendations
                        .replace(
                          /\*\*(.*?)\*\*/g,
                          "<strong class='text-green-400'>$1</strong>"
                        )
                        .replace(
                          /\*(.*?)\*/g,
                          "<em class='text-gray-200'>$1</em>"
                        )
                        .replace(/\n\n/g, '</p><p class="mb-4 text-gray-300">')
                        .replace(/\n/g, "<br/>")
                        .replace(/^/, '<p class="mb-4 text-gray-300">')
                        .replace(/$/, "</p>"),
                    }}
                  />
                </div>
              </div>
            )}

            {/* Visual Analytics */}
            <div className="space-y-6">
              {selectedComparison.radarChart && (
                <div className="bg-white/5 rounded-xl p-6">
                  <h3 className="text-xl font-semibold text-blue-400 mb-4">
                    Radar Chart Analysis
                  </h3>
                  <div className="flex justify-center">
                    <img
                      src={
                        selectedComparison.radarChart?.startsWith("data:") ||
                        selectedComparison.radarChart?.startsWith("http")
                          ? selectedComparison.radarChart
                          : `data:image/png;base64,${selectedComparison.radarChart}`
                      }
                      alt="Radar Chart"
                      className="max-w-full h-auto rounded-lg shadow-sm border border-gray-500/30"
                    />
                  </div>
                </div>
              )}

              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {selectedComparison.barGraph && (
                  <div className="bg-white/5 rounded-xl p-6">
                    <h3 className="text-xl font-semibold text-blue-400 mb-4">
                      Performance Comparison
                    </h3>
                    <img
                      src={
                        selectedComparison.barGraph?.startsWith("data:") ||
                        selectedComparison.barGraph?.startsWith("http")
                          ? selectedComparison.barGraph
                          : `data:image/png;base64,${selectedComparison.barGraph}`
                      }
                      alt="Bar Chart"
                      className="w-full h-auto rounded-lg shadow-sm border border-gray-500/30"
                    />
                  </div>
                )}
                {selectedComparison.scatterPlot && (
                  <div className="bg-white/5 rounded-xl p-6">
                    <h3 className="text-xl font-semibold text-blue-400 mb-4">
                      Risk vs Growth Analysis
                    </h3>
                    <img
                      src={
                        selectedComparison.scatterPlot?.startsWith("data:") ||
                        selectedComparison.scatterPlot?.startsWith("http")
                          ? selectedComparison.scatterPlot
                          : `data:image/png;base64,${selectedComparison.scatterPlot}`
                      }
                      alt="Scatter Plot"
                      className="w-full h-auto rounded-lg shadow-sm border border-gray-500/30"
                    />
                  </div>
                )}
              </div>
            </div>

            {/* Individual Company Analysis */}
            {selectedComparison.analyses &&
              selectedComparison.analyses.length > 0 && (
                <div className="bg-white/5 rounded-xl p-6">
                  <h3 className="text-xl font-semibold text-blue-400 mb-4">
                    Individual Company Analysis
                  </h3>
                  <div className="space-y-6">
                    {selectedComparison.analyses.map((analysis, index) => (
                      <div
                        key={index}
                        className="bg-white/5 border border-gray-500/30 rounded-lg p-6"
                      >
                        <h4 className="font-medium text-gray-300 text-lg mb-4">
                          {analysis.companyName || `Company ${index + 1}`}
                        </h4>

                        {/* SWOT Analysis - Full Details */}
                        {analysis.swotLists && (
                          <div className="mb-6">
                            <h5 className="font-medium text-green-400 mb-3">
                              SWOT Analysis
                            </h5>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                              {/* Strengths */}
                              <div className="bg-green-500/10 border border-green-500/30 rounded-lg p-4">
                                <h6 className="font-medium text-green-300 mb-2 flex items-center">
                                  <span className="w-3 h-3 bg-green-400 rounded-full mr-2"></span>
                                  Strengths (
                                  {analysis.swotLists.strengths?.length || 0})
                                </h6>
                                {analysis.swotLists.strengths &&
                                analysis.swotLists.strengths.length > 0 ? (
                                  <ul className="space-y-1 text-sm text-green-200">
                                    {analysis.swotLists.strengths.map(
                                      (strength, idx) => (
                                        <li
                                          key={idx}
                                          className="flex items-start"
                                        >
                                          <span className="text-green-400 mr-2">
                                            •
                                          </span>
                                          <span>{strength}</span>
                                        </li>
                                      )
                                    )}
                                  </ul>
                                ) : (
                                  <p className="text-sm text-green-300 italic">
                                    No strengths identified
                                  </p>
                                )}
                              </div>

                              {/* Weaknesses */}
                              <div className="bg-red-500/10 border border-red-500/30 rounded-lg p-4">
                                <h6 className="font-medium text-red-300 mb-2 flex items-center">
                                  <span className="w-3 h-3 bg-red-400 rounded-full mr-2"></span>
                                  Weaknesses (
                                  {analysis.swotLists.weaknesses?.length || 0})
                                </h6>
                                {analysis.swotLists.weaknesses &&
                                analysis.swotLists.weaknesses.length > 0 ? (
                                  <ul className="space-y-1 text-sm text-red-200">
                                    {analysis.swotLists.weaknesses.map(
                                      (weakness, idx) => (
                                        <li
                                          key={idx}
                                          className="flex items-start"
                                        >
                                          <span className="text-red-400 mr-2">
                                            •
                                          </span>
                                          <span>{weakness}</span>
                                        </li>
                                      )
                                    )}
                                  </ul>
                                ) : (
                                  <p className="text-sm text-red-300 italic">
                                    No weaknesses identified
                                  </p>
                                )}
                              </div>

                              {/* Opportunities */}
                              <div className="bg-blue-500/10 border border-blue-500/30 rounded-lg p-4">
                                <h6 className="font-medium text-blue-300 mb-2 flex items-center">
                                  <span className="w-3 h-3 bg-blue-400 rounded-full mr-2"></span>
                                  Opportunities (
                                  {analysis.swotLists.opportunities?.length ||
                                    0}
                                  )
                                </h6>
                                {analysis.swotLists.opportunities &&
                                analysis.swotLists.opportunities.length > 0 ? (
                                  <ul className="space-y-1 text-sm text-blue-200">
                                    {analysis.swotLists.opportunities.map(
                                      (opportunity, idx) => (
                                        <li
                                          key={idx}
                                          className="flex items-start"
                                        >
                                          <span className="text-blue-400 mr-2">
                                            •
                                          </span>
                                          <span>{opportunity}</span>
                                        </li>
                                      )
                                    )}
                                  </ul>
                                ) : (
                                  <p className="text-sm text-blue-300 italic">
                                    No opportunities identified
                                  </p>
                                )}
                              </div>

                              {/* Threats */}
                              <div className="bg-orange-500/10 border border-orange-500/30 rounded-lg p-4">
                                <h6 className="font-medium text-orange-300 mb-2 flex items-center">
                                  <span className="w-3 h-3 bg-orange-400 rounded-full mr-2"></span>
                                  Threats (
                                  {analysis.swotLists.threats?.length || 0})
                                </h6>
                                {analysis.swotLists.threats &&
                                analysis.swotLists.threats.length > 0 ? (
                                  <ul className="space-y-1 text-sm text-orange-200">
                                    {analysis.swotLists.threats.map(
                                      (threat, idx) => (
                                        <li
                                          key={idx}
                                          className="flex items-start"
                                        >
                                          <span className="text-orange-400 mr-2">
                                            •
                                          </span>
                                          <span>{threat}</span>
                                        </li>
                                      )
                                    )}
                                  </ul>
                                ) : (
                                  <p className="text-sm text-orange-300 italic">
                                    No threats identified
                                  </p>
                                )}
                              </div>
                            </div>
                          </div>
                        )}

                        {/* PESTEL Analysis - Full Details */}
                        {analysis.pestelLists && (
                          <div className="mb-6">
                            <h5 className="font-medium text-blue-400 mb-3">
                              PESTEL Analysis
                            </h5>
                            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                              {/* Political */}
                              <div className="bg-gray-500/10 border border-gray-500/30 rounded-lg p-4">
                                <h6 className="font-medium text-gray-300 mb-2 flex items-center">
                                  <span className="w-3 h-3 bg-gray-400 rounded-full mr-2"></span>
                                  Political (
                                  {analysis.pestelLists.political?.length || 0})
                                </h6>
                                {analysis.pestelLists.political &&
                                analysis.pestelLists.political.length > 0 ? (
                                  <ul className="space-y-1 text-sm text-gray-200">
                                    {analysis.pestelLists.political.map(
                                      (item, idx) => (
                                        <li
                                          key={idx}
                                          className="flex items-start"
                                        >
                                          <span className="text-gray-400 mr-2">
                                            •
                                          </span>
                                          <span>{item}</span>
                                        </li>
                                      )
                                    )}
                                  </ul>
                                ) : (
                                  <p className="text-sm text-gray-400 italic">
                                    No political factors identified
                                  </p>
                                )}
                              </div>

                              {/* Economic */}
                              <div className="bg-gray-500/10 border border-gray-500/30 rounded-lg p-4">
                                <h6 className="font-medium text-gray-300 mb-2 flex items-center">
                                  <span className="w-3 h-3 bg-gray-400 rounded-full mr-2"></span>
                                  Economic (
                                  {analysis.pestelLists.economic?.length || 0})
                                </h6>
                                {analysis.pestelLists.economic &&
                                analysis.pestelLists.economic.length > 0 ? (
                                  <ul className="space-y-1 text-sm text-gray-200">
                                    {analysis.pestelLists.economic.map(
                                      (item, idx) => (
                                        <li
                                          key={idx}
                                          className="flex items-start"
                                        >
                                          <span className="text-gray-400 mr-2">
                                            •
                                          </span>
                                          <span>{item}</span>
                                        </li>
                                      )
                                    )}
                                  </ul>
                                ) : (
                                  <p className="text-sm text-gray-400 italic">
                                    No economic factors identified
                                  </p>
                                )}
                              </div>

                              {/* Social */}
                              <div className="bg-gray-500/10 border border-gray-500/30 rounded-lg p-4">
                                <h6 className="font-medium text-gray-300 mb-2 flex items-center">
                                  <span className="w-3 h-3 bg-gray-400 rounded-full mr-2"></span>
                                  Social (
                                  {analysis.pestelLists.social?.length || 0})
                                </h6>
                                {analysis.pestelLists.social &&
                                analysis.pestelLists.social.length > 0 ? (
                                  <ul className="space-y-1 text-sm text-gray-200">
                                    {analysis.pestelLists.social.map(
                                      (item, idx) => (
                                        <li
                                          key={idx}
                                          className="flex items-start"
                                        >
                                          <span className="text-gray-400 mr-2">
                                            •
                                          </span>
                                          <span>{item}</span>
                                        </li>
                                      )
                                    )}
                                  </ul>
                                ) : (
                                  <p className="text-sm text-gray-400 italic">
                                    No social factors identified
                                  </p>
                                )}
                              </div>

                              {/* Technological */}
                              <div className="bg-gray-500/10 border border-gray-500/30 rounded-lg p-4">
                                <h6 className="font-medium text-gray-300 mb-2 flex items-center">
                                  <span className="w-3 h-3 bg-gray-400 rounded-full mr-2"></span>
                                  Technology (
                                  {analysis.pestelLists.technological?.length ||
                                    0}
                                  )
                                </h6>
                                {analysis.pestelLists.technological &&
                                analysis.pestelLists.technological.length >
                                  0 ? (
                                  <ul className="space-y-1 text-sm text-gray-200">
                                    {analysis.pestelLists.technological.map(
                                      (item, idx) => (
                                        <li
                                          key={idx}
                                          className="flex items-start"
                                        >
                                          <span className="text-gray-400 mr-2">
                                            •
                                          </span>
                                          <span>{item}</span>
                                        </li>
                                      )
                                    )}
                                  </ul>
                                ) : (
                                  <p className="text-sm text-gray-400 italic">
                                    No technological factors identified
                                  </p>
                                )}
                              </div>

                              {/* Environmental */}
                              <div className="bg-gray-500/10 border border-gray-500/30 rounded-lg p-4">
                                <h6 className="font-medium text-gray-300 mb-2 flex items-center">
                                  <span className="w-3 h-3 bg-gray-400 rounded-full mr-2"></span>
                                  Environment (
                                  {analysis.pestelLists.environmental?.length ||
                                    0}
                                  )
                                </h6>
                                {analysis.pestelLists.environmental &&
                                analysis.pestelLists.environmental.length >
                                  0 ? (
                                  <ul className="space-y-1 text-sm text-gray-200">
                                    {analysis.pestelLists.environmental.map(
                                      (item, idx) => (
                                        <li
                                          key={idx}
                                          className="flex items-start"
                                        >
                                          <span className="text-gray-400 mr-2">
                                            •
                                          </span>
                                          <span>{item}</span>
                                        </li>
                                      )
                                    )}
                                  </ul>
                                ) : (
                                  <p className="text-sm text-gray-400 italic">
                                    No environmental factors identified
                                  </p>
                                )}
                              </div>

                              {/* Legal */}
                              <div className="bg-gray-500/10 border border-gray-500/30 rounded-lg p-4">
                                <h6 className="font-medium text-gray-300 mb-2 flex items-center">
                                  <span className="w-3 h-3 bg-gray-400 rounded-full mr-2"></span>
                                  Legal (
                                  {analysis.pestelLists.legal?.length || 0})
                                </h6>
                                {analysis.pestelLists.legal &&
                                analysis.pestelLists.legal.length > 0 ? (
                                  <ul className="space-y-1 text-sm text-gray-200">
                                    {analysis.pestelLists.legal.map(
                                      (item, idx) => (
                                        <li
                                          key={idx}
                                          className="flex items-start"
                                        >
                                          <span className="text-gray-400 mr-2">
                                            •
                                          </span>
                                          <span>{item}</span>
                                        </li>
                                      )
                                    )}
                                  </ul>
                                ) : (
                                  <p className="text-sm text-gray-400 italic">
                                    No legal factors identified
                                  </p>
                                )}
                              </div>
                            </div>
                          </div>
                        )}

                        {/* Visualization Images */}
                        <div className="mb-6">
                          <h5 className="font-medium text-purple-400 mb-3">
                            Strategic Analysis Visualizations
                          </h5>
                          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            {analysis.swotImage && (
                              <div className="bg-white/5 rounded-lg p-4">
                                <h6 className="font-medium text-purple-300 mb-2">
                                  SWOT Analysis
                                </h6>
                                <img
                                  src={analysis.swotImage}
                                  alt={`SWOT Analysis for ${analysis.companyName}`}
                                  className="w-full rounded-lg border border-gray-600/30"
                                  loading="lazy"
                                  onError={(e) => {
                                    const target = e.target as HTMLImageElement;
                                    target.style.display = "none";
                                  }}
                                />
                              </div>
                            )}

                            {analysis.pestelImage && (
                              <div className="bg-white/5 rounded-lg p-4">
                                <h6 className="font-medium text-purple-300 mb-2">
                                  PESTEL Analysis
                                </h6>
                                <img
                                  src={analysis.pestelImage}
                                  alt={`PESTEL Analysis for ${analysis.companyName}`}
                                  className="w-full rounded-lg border border-gray-600/30"
                                  loading="lazy"
                                  onError={(e) => {
                                    const target = e.target as HTMLImageElement;
                                    target.style.display = "none";
                                  }}
                                />
                              </div>
                            )}

                            {analysis.porterImage && (
                              <div className="bg-white/5 rounded-lg p-4">
                                <h6 className="font-medium text-purple-300 mb-2">
                                  Porter's Five Forces
                                </h6>
                                <img
                                  src={analysis.porterImage}
                                  alt={`Porter's Five Forces for ${analysis.companyName}`}
                                  className="w-full rounded-lg border border-gray-600/30"
                                  loading="lazy"
                                  onError={(e) => {
                                    const target = e.target as HTMLImageElement;
                                    target.style.display = "none";
                                  }}
                                />
                              </div>
                            )}

                            {analysis.bcgImage && (
                              <div className="bg-white/5 rounded-lg p-4">
                                <h6 className="font-medium text-purple-300 mb-2">
                                  BCG Matrix
                                </h6>
                                <img
                                  src={analysis.bcgImage}
                                  alt={`BCG Matrix for ${analysis.companyName}`}
                                  className="w-full rounded-lg border border-gray-600/30"
                                  loading="lazy"
                                  onError={(e) => {
                                    const target = e.target as HTMLImageElement;
                                    target.style.display = "none";
                                  }}
                                />
                              </div>
                            )}

                            {analysis.mckinseyImage && (
                              <div className="bg-white/5 rounded-lg p-4">
                                <h6 className="font-medium text-purple-300 mb-2">
                                  McKinsey 7S Framework
                                </h6>
                                <img
                                  src={analysis.mckinseyImage}
                                  alt={`McKinsey 7S for ${analysis.companyName}`}
                                  className="w-full rounded-lg border border-gray-600/30"
                                  loading="lazy"
                                  onError={(e) => {
                                    const target = e.target as HTMLImageElement;
                                    target.style.display = "none";
                                  }}
                                />
                              </div>
                            )}
                          </div>
                        </div>

                        {/* Porter's Five Forces - Full Details */}
                        {analysis.porterForces && (
                          <div className="mb-6">
                            <h5 className="font-medium text-indigo-400 mb-3">
                              Porter's Five Forces Analysis
                            </h5>
                            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                              {/* Competitive Rivalry */}
                              <div className="bg-indigo-500/10 border border-indigo-500/30 rounded-lg p-4">
                                <h6 className="font-medium text-indigo-300 mb-2">
                                  Competitive Rivalry
                                </h6>
                                {analysis.porterForces.rivalry?.length > 0 ? (
                                  <ul className="space-y-1 text-sm text-indigo-200">
                                    {analysis.porterForces.rivalry.map(
                                      (item, idx) => (
                                        <li
                                          key={idx}
                                          className="flex items-start"
                                        >
                                          <span className="text-indigo-400 mr-2">
                                            •
                                          </span>
                                          <span>{item}</span>
                                        </li>
                                      )
                                    )}
                                  </ul>
                                ) : (
                                  <p className="text-sm text-gray-400 italic">
                                    No rivalry factors identified
                                  </p>
                                )}
                              </div>

                              {/* Threat of New Entrants */}
                              <div className="bg-indigo-500/10 border border-indigo-500/30 rounded-lg p-4">
                                <h6 className="font-medium text-indigo-300 mb-2">
                                  New Entrants
                                </h6>
                                {analysis.porterForces.newEntrants?.length >
                                0 ? (
                                  <ul className="space-y-1 text-sm text-indigo-200">
                                    {analysis.porterForces.newEntrants.map(
                                      (item, idx) => (
                                        <li
                                          key={idx}
                                          className="flex items-start"
                                        >
                                          <span className="text-indigo-400 mr-2">
                                            •
                                          </span>
                                          <span>{item}</span>
                                        </li>
                                      )
                                    )}
                                  </ul>
                                ) : (
                                  <p className="text-sm text-gray-400 italic">
                                    No new entrant factors identified
                                  </p>
                                )}
                              </div>

                              {/* Threat of Substitutes */}
                              <div className="bg-indigo-500/10 border border-indigo-500/30 rounded-lg p-4">
                                <h6 className="font-medium text-indigo-300 mb-2">
                                  Substitutes
                                </h6>
                                {analysis.porterForces.substitutes?.length >
                                0 ? (
                                  <ul className="space-y-1 text-sm text-indigo-200">
                                    {analysis.porterForces.substitutes.map(
                                      (item, idx) => (
                                        <li
                                          key={idx}
                                          className="flex items-start"
                                        >
                                          <span className="text-indigo-400 mr-2">
                                            •
                                          </span>
                                          <span>{item}</span>
                                        </li>
                                      )
                                    )}
                                  </ul>
                                ) : (
                                  <p className="text-sm text-gray-400 italic">
                                    No substitute factors identified
                                  </p>
                                )}
                              </div>

                              {/* Buyer Power */}
                              <div className="bg-indigo-500/10 border border-indigo-500/30 rounded-lg p-4">
                                <h6 className="font-medium text-indigo-300 mb-2">
                                  Buyer Power
                                </h6>
                                {analysis.porterForces.buyerPower?.length >
                                0 ? (
                                  <ul className="space-y-1 text-sm text-indigo-200">
                                    {analysis.porterForces.buyerPower.map(
                                      (item, idx) => (
                                        <li
                                          key={idx}
                                          className="flex items-start"
                                        >
                                          <span className="text-indigo-400 mr-2">
                                            •
                                          </span>
                                          <span>{item}</span>
                                        </li>
                                      )
                                    )}
                                  </ul>
                                ) : (
                                  <p className="text-sm text-gray-400 italic">
                                    No buyer power factors identified
                                  </p>
                                )}
                              </div>

                              {/* Supplier Power */}
                              <div className="bg-indigo-500/10 border border-indigo-500/30 rounded-lg p-4">
                                <h6 className="font-medium text-indigo-300 mb-2">
                                  Supplier Power
                                </h6>
                                {analysis.porterForces.supplierPower?.length >
                                0 ? (
                                  <ul className="space-y-1 text-sm text-indigo-200">
                                    {analysis.porterForces.supplierPower.map(
                                      (item, idx) => (
                                        <li
                                          key={idx}
                                          className="flex items-start"
                                        >
                                          <span className="text-indigo-400 mr-2">
                                            •
                                          </span>
                                          <span>{item}</span>
                                        </li>
                                      )
                                    )}
                                  </ul>
                                ) : (
                                  <p className="text-sm text-gray-400 italic">
                                    No supplier power factors identified
                                  </p>
                                )}
                              </div>
                            </div>
                          </div>
                        )}

                        {/* BCG Matrix - Full Details */}
                        {analysis.bcgMatrix && (
                          <div className="mb-6">
                            <h5 className="font-medium text-cyan-400 mb-3">
                              BCG Matrix Analysis
                            </h5>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                              {Object.entries(analysis.bcgMatrix).map(
                                ([product, data]) => (
                                  <div
                                    key={product}
                                    className="bg-cyan-500/10 border border-cyan-500/30 rounded-lg p-4"
                                  >
                                    <h6 className="font-medium text-cyan-300 mb-2">
                                      {product}
                                    </h6>
                                    <div className="space-y-2 text-sm text-cyan-200">
                                      <div className="flex justify-between">
                                        <span>Market Share:</span>
                                        <span>{data.marketShare}%</span>
                                      </div>
                                      <div className="flex justify-between">
                                        <span>Growth Rate:</span>
                                        <span>{data.growthRate}%</span>
                                      </div>
                                    </div>
                                  </div>
                                )
                              )}
                            </div>
                          </div>
                        )}

                        {/* McKinsey 7S Framework - Full Details */}
                        {analysis.mckinsey7s && (
                          <div className="mb-6">
                            <h5 className="font-medium text-teal-400 mb-3">
                              McKinsey 7S Framework
                            </h5>
                            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                              {Object.entries(analysis.mckinsey7s).map(
                                ([dimension, value]) => (
                                  <div
                                    key={dimension}
                                    className="bg-teal-500/10 border border-teal-500/30 rounded-lg p-4"
                                  >
                                    <h6 className="font-medium text-teal-300 mb-2 capitalize">
                                      {dimension === "sharedValues"
                                        ? "Shared Values"
                                        : dimension}
                                    </h6>
                                    <p className="text-sm text-teal-200">
                                      {value}
                                    </p>
                                  </div>
                                )
                              )}
                            </div>
                          </div>
                        )}

                        {/* Sources */}
                        {analysis.sources && analysis.sources.length > 0 && (
                          <div className="mb-6">
                            <h5 className="font-medium text-amber-400 mb-3">
                              Analysis Sources ({analysis.sources.length})
                            </h5>
                            <div className="bg-amber-500/10 border border-amber-500/30 rounded-lg p-4">
                              <ul className="space-y-2 text-sm text-amber-200">
                                {analysis.sources.map((source, idx) => (
                                  <li key={idx} className="flex items-start">
                                    <span className="text-amber-400 mr-2 flex-shrink-0 mt-1">
                                      {idx + 1}.
                                    </span>
                                    <a
                                      href={source}
                                      target="_blank"
                                      rel="noopener noreferrer"
                                      className="text-amber-200 hover:text-amber-100 underline break-all"
                                    >
                                      {source}
                                    </a>
                                  </li>
                                ))}
                              </ul>
                            </div>
                          </div>
                        )}

                        {/* LinkedIn Analysis */}
                        {analysis.linkedinAnalysis && (
                          <div className="mb-6">
                            <h5 className="font-medium text-blue-400 mb-3">
                              LinkedIn-Based Analysis
                            </h5>
                            <div className="bg-blue-500/10 border border-blue-500/30 rounded-lg p-4">
                              <div
                                className="text-sm text-blue-200 leading-relaxed"
                                dangerouslySetInnerHTML={{
                                  __html: analysis.linkedinAnalysis,
                                }}
                              />
                            </div>
                          </div>
                        )}

                        {/* Strategy Recommendations - Full Details */}
                        {analysis.strategyRecommendations && (
                          <div className="mb-6">
                            <h5 className="font-medium text-blue-400 mb-3">
                              Strategic Recommendations
                            </h5>
                            <div className="bg-blue-500/10 border border-blue-500/30 border-l-4 border-l-blue-400 rounded-r-lg p-4">
                              <div
                                className="text-sm text-gray-200 leading-relaxed"
                                dangerouslySetInnerHTML={{
                                  __html: analysis.strategyRecommendations,
                                }}
                              />
                            </div>
                          </div>
                        )}

                        {/* Analysis Summaries - Full Details */}
                        {analysis.summaries &&
                          analysis.summaries.length > 0 && (
                            <div className="mb-6">
                              <h5 className="font-medium text-gray-300 mb-3">
                                Detailed Analysis Summaries (
                                {analysis.summaries.length})
                              </h5>
                              <div className="space-y-4">
                                {analysis.summaries.map((summary, idx) => (
                                  <div
                                    key={idx}
                                    className="bg-gray-500/10 border border-gray-500/30 rounded-lg p-4"
                                  >
                                    <div className="flex items-start">
                                      <span className="flex-shrink-0 w-6 h-6 bg-gray-400 text-gray-900 rounded-full flex items-center justify-center text-xs font-medium mr-3 mt-0.5">
                                        {idx + 1}
                                      </span>
                                      <div className="flex-1">
                                        <div
                                          className="text-sm text-gray-200 leading-relaxed"
                                          dangerouslySetInnerHTML={{
                                            __html: summary,
                                          }}
                                        />
                                      </div>
                                    </div>
                                  </div>
                                ))}
                              </div>
                            </div>
                          )}
                      </div>
                    ))}
                  </div>
                </div>
              )}

            {/* Custom Export Functionality */}
            <div className="bg-white/5 rounded-xl p-6">
              <h3 className="text-xl font-semibold text-blue-400 mb-4">
                Export Comparison Report
              </h3>
              <div className="flex flex-wrap gap-3">
                <button
                  onClick={handleExportMarkdown}
                  className="flex items-center px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors"
                  title="Export as Markdown file with formatting"
                >
                  <File className="h-4 w-4 mr-2" />
                  Markdown
                </button>
                <button
                  onClick={handleExportText}
                  className="flex items-center px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg transition-colors"
                  title="Export as plain text file"
                >
                  <FileText className="h-4 w-4 mr-2" />
                  TXT
                </button>
                <button
                  onClick={handleExportHTML}
                  className="flex items-center px-4 py-2 bg-purple-600 hover:bg-purple-700 text-white rounded-lg transition-colors"
                  title="Export as HTML file with formatting"
                >
                  <Download className="h-4 w-4 mr-2" />
                  HTML
                </button>
                <button
                  onClick={handleExportPDF}
                  className="flex items-center px-4 py-2 bg-purple-600 hover:bg-purple-700 text-white rounded-lg transition-colors"
                  title="Export as PDF with professional formatting"
                >
                  <FileImage className="h-4 w-4 mr-2" />
                  PDF
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ComparisonModal;
