import React from "react";
import { X, Download } from "lucide-react";
import type { ComparisonResult } from "../../types";

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

  // Dedicated comparison report export method
  const createComparisonExportData = (
    comparison: ComparisonResult,
    format: "md" | "txt" | "pdf"
  ) => {
    const companyNames =
      comparison.companyNames?.join(" vs ") || "Company Comparison";
    const includeImages = format === "pdf";

    let content = "";

    // Header
    content += `# ${companyNames} - Comparison Report\n\n`;
    content += `**Analysis Date:** ${formatDate(comparison.comparisonDate)}\n`;
    content += `**Comparison Type:** ${
      comparison.comparisonType || "standard"
    }\n`;
    content += `**Companies Analyzed:** ${
      comparison.companyNames?.join(", ") || "N/A"
    }\n\n`;

    // Industry Benchmarks
    if (comparison.benchmarks) {
      content += `## Industry Benchmarks\n\n`;
      content += `- **Average Market Share:** ${
        comparison.benchmarks.avgMarketShare != null
          ? (comparison.benchmarks.avgMarketShare * 100).toFixed(1) + "%"
          : "N/A"
      }\n`;
      content += `- **Average Growth Rate:** ${
        comparison.benchmarks.avgGrowthRate != null
          ? comparison.benchmarks.avgGrowthRate.toFixed(1) + "%"
          : "N/A"
      }\n`;
      content += `- **Average Risk Rating:** ${
        comparison.benchmarks.avgRiskRating != null
          ? comparison.benchmarks.avgRiskRating.toFixed(1) + "/10"
          : "N/A"
      }\n`;
      content += `- **Average Sentiment Score:** ${
        comparison.benchmarks.avgSentimentScore != null
          ? comparison.benchmarks.avgSentimentScore.toFixed(0)
          : "N/A"
      }\n\n`;
    }

    // Company Performance Metrics
    if (comparison.metrics && comparison.metrics.length > 0) {
      content += `## Company Performance Metrics\n\n`;
      comparison.metrics.forEach((metric, index) => {
        const companyName =
          comparison.companyNames?.[index] || `Company ${index + 1}`;
        content += `### ${companyName}\n`;
        content += `- **Market Share:** ${
          metric.marketShare != null
            ? (metric.marketShare * 100).toFixed(1) + "%"
            : "N/A"
        }\n`;
        content += `- **Growth Rate:** ${
          metric.growthRate != null ? metric.growthRate.toFixed(1) + "%" : "N/A"
        }\n`;
        content += `- **Risk Rating:** ${
          metric.riskRating != null
            ? metric.riskRating.toFixed(1) + "/10"
            : "N/A"
        }\n`;
        content += `- **Sentiment Score:** ${
          metric.sentimentScore != null
            ? metric.sentimentScore.toFixed(0)
            : "N/A"
        }\n\n`;
      });
    }

    // Key Insights
    if (comparison.insights && comparison.insights.length > 0) {
      content += `## Key Insights\n\n`;
      comparison.insights.forEach((insight, index) => {
        content += `${index + 1}. ${insight}\n`;
      });
      content += "\n";
    }

    // Investment Recommendations
    if (comparison.investmentRecommendations) {
      content += `## Investment Recommendations\n\n`;
      content +=
        comparison.investmentRecommendations
          .replace(/\*\*(.*?)\*\*/g, "**$1**")
          .replace(/\n\n/g, "\n\n")
          .replace(/\n/g, "\n") + "\n\n";
    }

    // Visual Analytics (only for PDF)
    if (includeImages) {
      if (comparison.radarChart) {
        content += `## Radar Chart Analysis\n\n`;
        content += `![Radar Chart](data:image/png;base64,${comparison.radarChart})\n\n`;
      }
      if (comparison.barGraph) {
        content += `## Performance Comparison\n\n`;
        content += `![Bar Chart](data:image/png;base64,${comparison.barGraph})\n\n`;
      }
      if (comparison.scatterPlot) {
        content += `## Risk vs Growth Analysis\n\n`;
        content += `![Scatter Plot](data:image/png;base64,${comparison.scatterPlot})\n\n`;
      }
    }

    // Individual Company Analysis
    if (comparison.analyses && comparison.analyses.length > 0) {
      content += `## Individual Company Analysis\n\n`;
      comparison.analyses.forEach((analysis, index) => {
        const companyName = analysis.companyName || `Company ${index + 1}`;
        content += `### ${companyName}\n\n`;

        // SWOT Analysis
        if (analysis.swotLists) {
          content += `#### SWOT Analysis\n\n`;

          if (
            analysis.swotLists.strengths &&
            analysis.swotLists.strengths.length > 0
          ) {
            content += `**Strengths (${analysis.swotLists.strengths.length}):**\n`;
            analysis.swotLists.strengths.forEach((strength) => {
              content += `- ${strength}\n`;
            });
            content += "\n";
          }

          if (
            analysis.swotLists.weaknesses &&
            analysis.swotLists.weaknesses.length > 0
          ) {
            content += `**Weaknesses (${analysis.swotLists.weaknesses.length}):**\n`;
            analysis.swotLists.weaknesses.forEach((weakness) => {
              content += `- ${weakness}\n`;
            });
            content += "\n";
          }

          if (
            analysis.swotLists.opportunities &&
            analysis.swotLists.opportunities.length > 0
          ) {
            content += `**Opportunities (${analysis.swotLists.opportunities.length}):**\n`;
            analysis.swotLists.opportunities.forEach((opportunity) => {
              content += `- ${opportunity}\n`;
            });
            content += "\n";
          }

          if (
            analysis.swotLists.threats &&
            analysis.swotLists.threats.length > 0
          ) {
            content += `**Threats (${analysis.swotLists.threats.length}):**\n`;
            analysis.swotLists.threats.forEach((threat) => {
              content += `- ${threat}\n`;
            });
            content += "\n";
          }
        }

        // PESTEL Analysis
        if (analysis.pestelLists) {
          content += `#### PESTEL Analysis\n\n`;

          const pestelCategories = [
            { key: "political", label: "Political" },
            { key: "economic", label: "Economic" },
            { key: "social", label: "Social" },
            { key: "technological", label: "Technological" },
            { key: "environmental", label: "Environmental" },
            { key: "legal", label: "Legal" },
          ];

          pestelCategories.forEach(({ key, label }) => {
            const items =
              analysis.pestelLists?.[key as keyof typeof analysis.pestelLists];
            if (items && items.length > 0) {
              content += `**${label} (${items.length}):**\n`;
              items.forEach((item) => {
                content += `- ${item}\n`;
              });
              content += "\n";
            }
          });
        }

        // Strategy Recommendations
        if (analysis.strategyRecommendations) {
          content += `#### Strategy Recommendations\n\n`;
          content +=
            analysis.strategyRecommendations
              .replace(/\*\*(.*?)\*\*/g, "**$1**")
              .replace(/\n\n/g, "\n\n")
              .replace(/\n/g, "\n") + "\n\n";
        }

        // Company Summaries
        if (analysis.summaries && analysis.summaries.length > 0) {
          content += `#### Company Summary\n\n`;
          analysis.summaries.forEach((summary, idx) => {
            content += `${idx + 1}. ${summary}\n`;
          });
          content += "\n";
        }
      });
    }

    return {
      filename: `${companyNames
        .replace(/[^a-zA-Z0-9\s]/g, "")
        .replace(/\s+/g, "_")}_comparison_report`,
      content: content,
      type: "comparison_report",
    };
  };

  // Custom export handlers for comparison reports
  const handleExportMarkdown = () => {
    const exportData = createComparisonExportData(selectedComparison, "md");
    const blob = new Blob([exportData.content], { type: "text/markdown" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `${exportData.filename}.md`;
    a.click();
    URL.revokeObjectURL(url);
  };

  const handleExportText = () => {
    const exportData = createComparisonExportData(selectedComparison, "txt");
    const blob = new Blob([exportData.content], { type: "text/plain" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `${exportData.filename}.txt`;
    a.click();
    URL.revokeObjectURL(url);
  };

  const handleExportPDF = () => {
    try{
        const exportData = createComparisonExportData(selectedComparison, "pdf");
        
    }
    catch(error){
        console.error("Export failed:", error);
    }
  };

  const handleExportHTML = () => {
    try {
      const exportData = createComparisonExportData(selectedComparison, "pdf");

      // Convert markdown to HTML
      let htmlContent = exportData.content
        .replace(/^# (.*$)/gim, "<h1>$1</h1>")
        .replace(/^## (.*$)/gim, "<h2>$1</h2>")
        .replace(/^### (.*$)/gim, "<h3>$1</h3>")
        .replace(/^#### (.*$)/gim, "<h4>$1</h4>")
        .replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>")
        .replace(/\*(.*?)\*/g, "<em>$1</em>")
        .replace(/^- (.*$)/gim, "<li>$1</li>")
        .replace(/^\d+\. (.*$)/gim, "<li>$1</li>")
        .replace(/\n\n/g, "</p><p>")
        .replace(
          /!\[([^\]]*)\]\(([^)]+)\)/g,
          '<img src="$2" alt="$1" style="max-width:100%; height:auto; margin: 20px 0;" />'
        )
        .replace(/^/, "<p>")
        .replace(/$/, "</p>");

      // Wrap in proper HTML structure
      const fullHtml = `
        <!DOCTYPE html>
        <html>
          <head>
            <meta charset="utf-8">
            <title>${exportData.filename}</title>
            <style>
              body { font-family: Arial, sans-serif; line-height: 1.6; margin: 40px; }
              h1 { color: #333; border-bottom: 2px solid #333; }
              h2 { color: #666; border-bottom: 1px solid #666; }
              h3, h4 { color: #888; }
              li { margin: 5px 0; }
              img { display: block; margin: 20px auto; }
              p { margin: 10px 0; }
            </style>
          </head>
          <body>
            ${htmlContent}
          </body>
        </html>
      `;

      const blob = new Blob([fullHtml], { type: "text/html" });
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `${exportData.filename}.html`;
      a.click();
      URL.revokeObjectURL(url);
    } catch (error) {
      console.error("Export failed:", error);
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
                      const companyName =
                        selectedComparison.companyNames?.[index] ||
                        `Company ${index + 1}`;
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
                              <div className="bg-green-500/10 border border-green-500/30 rounded-lg p-4">
                                <h6 className="font-medium text-green-300 mb-2 flex items-center">
                                  <span className="w-3 h-3 bg-green-400 rounded-full mr-2"></span>
                                  Economic (
                                  {analysis.pestelLists.economic?.length || 0})
                                </h6>
                                {analysis.pestelLists.economic &&
                                analysis.pestelLists.economic.length > 0 ? (
                                  <ul className="space-y-1 text-sm text-green-200">
                                    {analysis.pestelLists.economic.map(
                                      (item, idx) => (
                                        <li
                                          key={idx}
                                          className="flex items-start"
                                        >
                                          <span className="text-green-400 mr-2">
                                            •
                                          </span>
                                          <span>{item}</span>
                                        </li>
                                      )
                                    )}
                                  </ul>
                                ) : (
                                  <p className="text-sm text-green-400 italic">
                                    No economic factors identified
                                  </p>
                                )}
                              </div>

                              {/* Social */}
                              <div className="bg-blue-500/10 border border-blue-500/30 rounded-lg p-4">
                                <h6 className="font-medium text-blue-300 mb-2 flex items-center">
                                  <span className="w-3 h-3 bg-blue-400 rounded-full mr-2"></span>
                                  Social (
                                  {analysis.pestelLists.social?.length || 0})
                                </h6>
                                {analysis.pestelLists.social &&
                                analysis.pestelLists.social.length > 0 ? (
                                  <ul className="space-y-1 text-sm text-blue-200">
                                    {analysis.pestelLists.social.map(
                                      (item, idx) => (
                                        <li
                                          key={idx}
                                          className="flex items-start"
                                        >
                                          <span className="text-blue-400 mr-2">
                                            •
                                          </span>
                                          <span>{item}</span>
                                        </li>
                                      )
                                    )}
                                  </ul>
                                ) : (
                                  <p className="text-sm text-blue-400 italic">
                                    No social factors identified
                                  </p>
                                )}
                              </div>

                              {/* Technological */}
                              <div className="bg-purple-500/10 border border-purple-500/30 rounded-lg p-4">
                                <h6 className="font-medium text-purple-300 mb-2 flex items-center">
                                  <span className="w-3 h-3 bg-purple-400 rounded-full mr-2"></span>
                                  Technological (
                                  {analysis.pestelLists.technological?.length ||
                                    0}
                                  )
                                </h6>
                                {analysis.pestelLists.technological &&
                                analysis.pestelLists.technological.length >
                                  0 ? (
                                  <ul className="space-y-1 text-sm text-purple-200">
                                    {analysis.pestelLists.technological.map(
                                      (item, idx) => (
                                        <li
                                          key={idx}
                                          className="flex items-start"
                                        >
                                          <span className="text-purple-400 mr-2">
                                            •
                                          </span>
                                          <span>{item}</span>
                                        </li>
                                      )
                                    )}
                                  </ul>
                                ) : (
                                  <p className="text-sm text-purple-400 italic">
                                    No technological factors identified
                                  </p>
                                )}
                              </div>

                              {/* Environmental */}
                              <div className="bg-emerald-500/10 border border-emerald-500/30 rounded-lg p-4">
                                <h6 className="font-medium text-emerald-300 mb-2 flex items-center">
                                  <span className="w-3 h-3 bg-emerald-400 rounded-full mr-2"></span>
                                  Environmental (
                                  {analysis.pestelLists.environmental?.length ||
                                    0}
                                  )
                                </h6>
                                {analysis.pestelLists.environmental &&
                                analysis.pestelLists.environmental.length >
                                  0 ? (
                                  <ul className="space-y-1 text-sm text-emerald-200">
                                    {analysis.pestelLists.environmental.map(
                                      (item, idx) => (
                                        <li
                                          key={idx}
                                          className="flex items-start"
                                        >
                                          <span className="text-emerald-400 mr-2">
                                            •
                                          </span>
                                          <span>{item}</span>
                                        </li>
                                      )
                                    )}
                                  </ul>
                                ) : (
                                  <p className="text-sm text-emerald-400 italic">
                                    No environmental factors identified
                                  </p>
                                )}
                              </div>

                              {/* Legal */}
                              <div className="bg-orange-500/10 border border-orange-500/30 rounded-lg p-4">
                                <h6 className="font-medium text-orange-300 mb-2 flex items-center">
                                  <span className="w-3 h-3 bg-orange-400 rounded-full mr-2"></span>
                                  Legal (
                                  {analysis.pestelLists.legal?.length || 0})
                                </h6>
                                {analysis.pestelLists.legal &&
                                analysis.pestelLists.legal.length > 0 ? (
                                  <ul className="space-y-1 text-sm text-orange-200">
                                    {analysis.pestelLists.legal.map(
                                      (item, idx) => (
                                        <li
                                          key={idx}
                                          className="flex items-start"
                                        >
                                          <span className="text-orange-400 mr-2">
                                            •
                                          </span>
                                          <span>{item}</span>
                                        </li>
                                      )
                                    )}
                                  </ul>
                                ) : (
                                  <p className="text-sm text-orange-400 italic">
                                    No legal factors identified
                                  </p>
                                )}
                              </div>
                            </div>
                          </div>
                        )}

                        {/* Strategy Recommendations */}
                        {analysis.strategyRecommendations && (
                          <div className="mb-6">
                            <h5 className="font-medium text-yellow-400 mb-3">
                              Strategy Recommendations
                            </h5>
                            <div className="bg-yellow-500/10 border border-yellow-500/30 rounded-lg p-4">
                              <div
                                className="text-yellow-200 prose prose-sm max-w-none"
                                dangerouslySetInnerHTML={{
                                  __html: analysis.strategyRecommendations
                                    .replace(
                                      /\*\*(.*?)\*\*/g,
                                      "<strong class='text-yellow-300'>$1</strong>"
                                    )
                                    .replace(
                                      /\*(.*?)\*/g,
                                      "<em class='text-yellow-200'>$1</em>"
                                    )
                                    .replace(
                                      /\n\n/g,
                                      '</p><p class="mb-4 text-yellow-200">'
                                    )
                                    .replace(/\n/g, "<br/>")
                                    .replace(
                                      /^/,
                                      '<p class="mb-4 text-yellow-200">'
                                    )
                                    .replace(/$/, "</p>"),
                                }}
                              />
                            </div>
                          </div>
                        )}

                        {/* Company Summaries */}
                        {analysis.summaries &&
                          analysis.summaries.length > 0 && (
                            <div className="mb-6">
                              <h5 className="font-medium text-indigo-400 mb-3">
                                Company Summary
                              </h5>
                              <div className="bg-indigo-500/10 border border-indigo-500/30 rounded-lg p-4">
                                <div className="space-y-2">
                                  {analysis.summaries.map((summary, idx) => (
                                    <div
                                      key={idx}
                                      className="flex items-start text-indigo-200"
                                    >
                                      <span className="text-indigo-400 mr-2 font-bold">
                                        {idx + 1}.
                                      </span>
                                      <span>{summary}</span>
                                    </div>
                                  ))}
                                </div>
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
                >
                  <Download className="h-4 w-4 mr-2" />
                  Export as Markdown (.md)
                </button>
                <button
                  onClick={handleExportText}
                  className="flex items-center px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg transition-colors"
                >
                  <Download className="h-4 w-4 mr-2" />
                  Export as Text (.txt)
                </button>
                <button
                  onClick={handleExportHTML}
                  className="flex items-center px-4 py-2 bg-purple-600 hover:bg-purple-700 text-white rounded-lg transition-colors"
                >
                  <Download className="h-4 w-4 mr-2" />
                  Export as HTML (with images)
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
