import React from "react";
import { X } from "lucide-react";
import type { ComparisonResult } from "../../types";
import ExportButtons from "../ExportButtons";

interface ComparisonModalProps {
  selectedComparison: ComparisonResult;
  onClose: () => void;
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

  // Helper function to create AnalysisResult for export
  const createExportData = (comparison: ComparisonResult) => {
    const companyNames =
      comparison.companyNames?.join(" vs ") || "Company Comparison";

    return {
      company_name: companyNames,
      summaries: [
        `Comparison Report: ${companyNames}`,
        ...(comparison.insights || []),
      ],
      sources: [],
      strategy_recommendations: comparison.investmentRecommendations || "",
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
        shared_values: "",
        style: "",
        staff: "",
        skills: "",
      },
      mckinsey_image: "",
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
    };
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-black/70 backdrop-blur-sm"
        onClick={onClose}
      ></div>

      {/* Modal */}
      <div className="bg-purple-800/30 backdrop-blur-sm rounded-2xl w-full max-w-6xl max-h-[90vh] overflow-auto relative z-10 shadow-xl shadow-blue-900/20">
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
            Full Comparison Report -{" "}
            {selectedComparison.companyNames?.join(" vs ") ||
              "Company Comparison"}
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
                      const companyNames =
                        selectedComparison.companyNames ||
                        selectedComparison.analyses?.map(
                          (a: any) => a.companyName
                        ) ||
                        (selectedComparison.analyses?.length > 0
                          ? selectedComparison.analyses.map(
                              (_: any, index: number) => `Company ${index + 1}`
                            )
                          : []);

                      return companyNames.length > 0 ? (
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
                      {selectedComparison.benchmarks?.avgMarketShare != null
                        ? (
                            selectedComparison.benchmarks.avgMarketShare * 100
                          ).toFixed(1)
                        : "N/A"}
                      {selectedComparison.benchmarks?.avgMarketShare != null &&
                        "%"}
                    </p>
                    <p className="text-xs text-blue-300">Industry Average</p>
                  </div>
                  <div className="bg-green-500/10 border border-green-500/30 rounded-lg p-4 text-center">
                    <h4 className="font-medium text-green-300 mb-1">
                      Growth Rate
                    </h4>
                    <p className="text-2xl font-bold text-green-400">
                      {selectedComparison.benchmarks?.avgGrowthRate != null
                        ? selectedComparison.benchmarks.avgGrowthRate.toFixed(1)
                        : "N/A"}
                      {selectedComparison.benchmarks?.avgGrowthRate != null &&
                        "%"}
                    </p>
                    <p className="text-xs text-green-300">Industry Average</p>
                  </div>
                  <div className="bg-orange-500/10 border border-orange-500/30 rounded-lg p-4 text-center">
                    <h4 className="font-medium text-orange-300 mb-1">
                      Risk Rating
                    </h4>
                    <p className="text-2xl font-bold text-orange-400">
                      {selectedComparison.benchmarks?.avgRiskRating != null
                        ? selectedComparison.benchmarks.avgRiskRating.toFixed(1)
                        : "N/A"}
                      {selectedComparison.benchmarks?.avgRiskRating != null &&
                        "/10"}
                    </p>
                    <p className="text-xs text-orange-300">Industry Average</p>
                  </div>
                  <div className="bg-purple-500/10 border border-purple-500/30 rounded-lg p-4 text-center">
                    <h4 className="font-medium text-purple-300 mb-1">
                      Sentiment
                    </h4>
                    <p className="text-2xl font-bold text-purple-400">
                      {selectedComparison.benchmarks?.avgSentimentScore != null
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

            {/* Performance Metrics */}
            {selectedComparison.metrics &&
              selectedComparison.metrics.length > 0 && (
                <div className="bg-white rounded-xl border border-gray-200 p-6">
                  <h3 className="text-lg font-semibold text-gray-900 mb-4">
                    Company Performance Metrics
                  </h3>
                  <div className="grid grid-cols-1 gap-4">
                    {selectedComparison.metrics.map((metric, index) => {
                      const companyName =
                        selectedComparison.companyNames?.[index] ||
                        selectedComparison.analyses?.[index]?.companyName ||
                        `Company ${index + 1}`;
                      return (
                        <div key={index} className="bg-gray-50 rounded-lg p-4">
                          <h4 className="font-medium text-gray-900 mb-3">
                            {companyName}
                          </h4>
                          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                            <div className="text-center">
                              <p className="text-sm text-gray-600">
                                Sentiment Score
                              </p>
                              <p className="text-xl font-bold text-purple-600">
                                {metric.sentimentScore != null
                                  ? metric.sentimentScore.toFixed(0)
                                  : "N/A"}
                              </p>
                            </div>
                            <div className="text-center">
                              <p className="text-sm text-gray-600">
                                Growth Rate
                              </p>
                              <p className="text-xl font-bold text-green-600">
                                {metric.growthRate != null
                                  ? metric.growthRate.toFixed(1)
                                  : "N/A"}
                                {metric.growthRate != null && "%"}
                              </p>
                            </div>
                            <div className="text-center">
                              <p className="text-sm text-gray-600">
                                Risk Rating
                              </p>
                              <p className="text-xl font-bold text-orange-600">
                                {metric.riskRating != null
                                  ? metric.riskRating.toFixed(1)
                                  : "N/A"}
                                {metric.riskRating != null && "/10"}
                              </p>
                            </div>
                            <div className="text-center">
                              <p className="text-sm text-gray-600">
                                Market Share
                              </p>
                              <p className="text-xl font-bold text-blue-600">
                                {metric.marketShare != null
                                  ? (metric.marketShare * 100).toFixed(1)
                                  : "N/A"}
                                {metric.marketShare != null && "%"}
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
                        selectedComparison.radarChart?.startsWith("data:")
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
                        selectedComparison.barGraph?.startsWith("data:")
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
                        selectedComparison.scatterPlot?.startsWith("data:")
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
                        <div className="flex items-center justify-between mb-4">
                          <h4 className="font-medium text-gray-300 text-lg">
                            {analysis.companyName || `Company ${index + 1}`}
                          </h4>
                          {analysis.analysisId && (
                            <span className="text-xs text-gray-400">
                              ID: {analysis.analysisId}
                            </span>
                          )}
                        </div>

                        {/* SWOT Analysis - Full Details */}
                        {analysis.swotLists && (
                          <div className="mb-6">
                            <h5 className="font-medium text-green-400 mb-3">
                              SWOT Analysis
                            </h5>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                              {/* Strengths */}
                              <div className="bg-green-50 rounded-lg p-4">
                                <h6 className="font-medium text-green-800 mb-2 flex items-center">
                                  <span className="w-3 h-3 bg-green-500 rounded-full mr-2"></span>
                                  Strengths (
                                  {analysis.swotLists.strengths?.length || 0})
                                </h6>
                                {analysis.swotLists.strengths &&
                                analysis.swotLists.strengths.length > 0 ? (
                                  <ul className="space-y-1 text-sm text-green-700">
                                    {analysis.swotLists.strengths.map(
                                      (strength, idx) => (
                                        <li
                                          key={idx}
                                          className="flex items-start"
                                        >
                                          <span className="text-green-500 mr-2">
                                            •
                                          </span>
                                          <span>{strength}</span>
                                        </li>
                                      )
                                    )}
                                  </ul>
                                ) : (
                                  <p className="text-sm text-green-600 italic">
                                    No strengths identified
                                  </p>
                                )}
                              </div>

                              {/* Weaknesses */}
                              <div className="bg-red-50 rounded-lg p-4">
                                <h6 className="font-medium text-red-800 mb-2 flex items-center">
                                  <span className="w-3 h-3 bg-red-500 rounded-full mr-2"></span>
                                  Weaknesses (
                                  {analysis.swotLists.weaknesses?.length || 0})
                                </h6>
                                {analysis.swotLists.weaknesses &&
                                analysis.swotLists.weaknesses.length > 0 ? (
                                  <ul className="space-y-1 text-sm text-red-700">
                                    {analysis.swotLists.weaknesses.map(
                                      (weakness, idx) => (
                                        <li
                                          key={idx}
                                          className="flex items-start"
                                        >
                                          <span className="text-red-500 mr-2">
                                            •
                                          </span>
                                          <span>{weakness}</span>
                                        </li>
                                      )
                                    )}
                                  </ul>
                                ) : (
                                  <p className="text-sm text-red-600 italic">
                                    No weaknesses identified
                                  </p>
                                )}
                              </div>

                              {/* Opportunities */}
                              <div className="bg-blue-50 rounded-lg p-4">
                                <h6 className="font-medium text-blue-800 mb-2 flex items-center">
                                  <span className="w-3 h-3 bg-blue-500 rounded-full mr-2"></span>
                                  Opportunities (
                                  {analysis.swotLists.opportunities?.length ||
                                    0}
                                  )
                                </h6>
                                {analysis.swotLists.opportunities &&
                                analysis.swotLists.opportunities.length > 0 ? (
                                  <ul className="space-y-1 text-sm text-blue-700">
                                    {analysis.swotLists.opportunities.map(
                                      (opportunity, idx) => (
                                        <li
                                          key={idx}
                                          className="flex items-start"
                                        >
                                          <span className="text-blue-500 mr-2">
                                            •
                                          </span>
                                          <span>{opportunity}</span>
                                        </li>
                                      )
                                    )}
                                  </ul>
                                ) : (
                                  <p className="text-sm text-blue-600 italic">
                                    No opportunities identified
                                  </p>
                                )}
                              </div>

                              {/* Threats */}
                              <div className="bg-orange-50 rounded-lg p-4">
                                <h6 className="font-medium text-orange-800 mb-2 flex items-center">
                                  <span className="w-3 h-3 bg-orange-500 rounded-full mr-2"></span>
                                  Threats (
                                  {analysis.swotLists.threats?.length || 0})
                                </h6>
                                {analysis.swotLists.threats &&
                                analysis.swotLists.threats.length > 0 ? (
                                  <ul className="space-y-1 text-sm text-orange-700">
                                    {analysis.swotLists.threats.map(
                                      (threat, idx) => (
                                        <li
                                          key={idx}
                                          className="flex items-start"
                                        >
                                          <span className="text-orange-500 mr-2">
                                            •
                                          </span>
                                          <span>{threat}</span>
                                        </li>
                                      )
                                    )}
                                  </ul>
                                ) : (
                                  <p className="text-sm text-orange-600 italic">
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
                            <h5 className="font-medium text-gray-800 mb-3">
                              PESTEL Analysis
                            </h5>
                            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                              {/* Political */}
                              <div className="bg-gray-50 rounded-lg p-4">
                                <h6 className="font-medium text-gray-800 mb-2 flex items-center">
                                  <span className="w-3 h-3 bg-gray-600 rounded-full mr-2"></span>
                                  Political (
                                  {analysis.pestelLists.political?.length || 0})
                                </h6>
                                {analysis.pestelLists.political &&
                                analysis.pestelLists.political.length > 0 ? (
                                  <ul className="space-y-1 text-sm text-gray-700">
                                    {analysis.pestelLists.political.map(
                                      (item, idx) => (
                                        <li
                                          key={idx}
                                          className="flex items-start"
                                        >
                                          <span className="text-gray-500 mr-2">
                                            •
                                          </span>
                                          <span>{item}</span>
                                        </li>
                                      )
                                    )}
                                  </ul>
                                ) : (
                                  <p className="text-sm text-gray-600 italic">
                                    No political factors identified
                                  </p>
                                )}
                              </div>

                              {/* Economic */}
                              <div className="bg-gray-50 rounded-lg p-4">
                                <h6 className="font-medium text-gray-800 mb-2 flex items-center">
                                  <span className="w-3 h-3 bg-gray-600 rounded-full mr-2"></span>
                                  Economic (
                                  {analysis.pestelLists.economic?.length || 0})
                                </h6>
                                {analysis.pestelLists.economic &&
                                analysis.pestelLists.economic.length > 0 ? (
                                  <ul className="space-y-1 text-sm text-gray-700">
                                    {analysis.pestelLists.economic.map(
                                      (item, idx) => (
                                        <li
                                          key={idx}
                                          className="flex items-start"
                                        >
                                          <span className="text-gray-500 mr-2">
                                            •
                                          </span>
                                          <span>{item}</span>
                                        </li>
                                      )
                                    )}
                                  </ul>
                                ) : (
                                  <p className="text-sm text-gray-600 italic">
                                    No economic factors identified
                                  </p>
                                )}
                              </div>

                              {/* Social */}
                              <div className="bg-gray-50 rounded-lg p-4">
                                <h6 className="font-medium text-gray-800 mb-2 flex items-center">
                                  <span className="w-3 h-3 bg-gray-600 rounded-full mr-2"></span>
                                  Social (
                                  {analysis.pestelLists.social?.length || 0})
                                </h6>
                                {analysis.pestelLists.social &&
                                analysis.pestelLists.social.length > 0 ? (
                                  <ul className="space-y-1 text-sm text-gray-700">
                                    {analysis.pestelLists.social.map(
                                      (item, idx) => (
                                        <li
                                          key={idx}
                                          className="flex items-start"
                                        >
                                          <span className="text-gray-500 mr-2">
                                            •
                                          </span>
                                          <span>{item}</span>
                                        </li>
                                      )
                                    )}
                                  </ul>
                                ) : (
                                  <p className="text-sm text-gray-600 italic">
                                    No social factors identified
                                  </p>
                                )}
                              </div>

                              {/* Technological */}
                              <div className="bg-gray-50 rounded-lg p-4">
                                <h6 className="font-medium text-gray-800 mb-2 flex items-center">
                                  <span className="w-3 h-3 bg-gray-600 rounded-full mr-2"></span>
                                  Technology (
                                  {analysis.pestelLists.technological?.length ||
                                    0}
                                  )
                                </h6>
                                {analysis.pestelLists.technological &&
                                analysis.pestelLists.technological.length >
                                  0 ? (
                                  <ul className="space-y-1 text-sm text-gray-700">
                                    {analysis.pestelLists.technological.map(
                                      (item, idx) => (
                                        <li
                                          key={idx}
                                          className="flex items-start"
                                        >
                                          <span className="text-gray-500 mr-2">
                                            •
                                          </span>
                                          <span>{item}</span>
                                        </li>
                                      )
                                    )}
                                  </ul>
                                ) : (
                                  <p className="text-sm text-gray-600 italic">
                                    No technological factors identified
                                  </p>
                                )}
                              </div>

                              {/* Environmental */}
                              <div className="bg-gray-50 rounded-lg p-4">
                                <h6 className="font-medium text-gray-800 mb-2 flex items-center">
                                  <span className="w-3 h-3 bg-gray-600 rounded-full mr-2"></span>
                                  Environment (
                                  {analysis.pestelLists.environmental?.length ||
                                    0}
                                  )
                                </h6>
                                {analysis.pestelLists.environmental &&
                                analysis.pestelLists.environmental.length >
                                  0 ? (
                                  <ul className="space-y-1 text-sm text-gray-700">
                                    {analysis.pestelLists.environmental.map(
                                      (item, idx) => (
                                        <li
                                          key={idx}
                                          className="flex items-start"
                                        >
                                          <span className="text-gray-500 mr-2">
                                            •
                                          </span>
                                          <span>{item}</span>
                                        </li>
                                      )
                                    )}
                                  </ul>
                                ) : (
                                  <p className="text-sm text-gray-600 italic">
                                    No environmental factors identified
                                  </p>
                                )}
                              </div>

                              {/* Legal */}
                              <div className="bg-gray-50 rounded-lg p-4">
                                <h6 className="font-medium text-gray-800 mb-2 flex items-center">
                                  <span className="w-3 h-3 bg-gray-600 rounded-full mr-2"></span>
                                  Legal (
                                  {analysis.pestelLists.legal?.length || 0})
                                </h6>
                                {analysis.pestelLists.legal &&
                                analysis.pestelLists.legal.length > 0 ? (
                                  <ul className="space-y-1 text-sm text-gray-700">
                                    {analysis.pestelLists.legal.map(
                                      (item, idx) => (
                                        <li
                                          key={idx}
                                          className="flex items-start"
                                        >
                                          <span className="text-gray-500 mr-2">
                                            •
                                          </span>
                                          <span>{item}</span>
                                        </li>
                                      )
                                    )}
                                  </ul>
                                ) : (
                                  <p className="text-sm text-gray-600 italic">
                                    No legal factors identified
                                  </p>
                                )}
                              </div>
                            </div>
                          </div>
                        )}

                        {/* Strategy Recommendations - Full Details */}
                        {analysis.strategyRecommendations && (
                          <div className="mb-6">
                            <h5 className="font-medium text-gray-800 mb-3">
                              Strategic Recommendations
                            </h5>
                            <div className="bg-blue-50 border-l-4 border-blue-400 rounded-r-lg p-4">
                              <div
                                className="text-sm text-gray-700 leading-relaxed"
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
                              <h5 className="font-medium text-gray-800 mb-3">
                                Detailed Analysis Summaries (
                                {analysis.summaries.length})
                              </h5>
                              <div className="space-y-4">
                                {analysis.summaries.map((summary, idx) => (
                                  <div
                                    key={idx}
                                    className="bg-gray-50 rounded-lg p-4"
                                  >
                                    <div className="flex items-start">
                                      <span className="flex-shrink-0 w-6 h-6 bg-gray-500 text-white rounded-full flex items-center justify-center text-xs font-medium mr-3 mt-0.5">
                                        {idx + 1}
                                      </span>
                                      <div className="flex-1">
                                        <div
                                          className="text-sm text-gray-700 leading-relaxed"
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

            {/* Export Functionality */}
            <div className="bg-white/5 rounded-xl p-6">
              <ExportButtons
                analysisResult={createExportData(selectedComparison)}
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ComparisonModal;
