import type { ComparisonResult } from "../../types";
import { formatDate } from "../helpers/dateFormatter";
import { getCompanyNames } from "../helpers/comparisonCompanyNames";

// Generate Markdown content for comparison
export function generateComparisonMarkdownContent(
  comparison: ComparisonResult
): string {
  const sections: string[] = [];

  // Header
  const companyNames = getCompanyNames(comparison);
  sections.push(`# ${companyNames.join(" vs ")} - Comparison Report`);
  sections.push("");
  sections.push(`**Analysis Date:** ${formatDate(comparison.comparisonDate)}`);
  sections.push("");
  sections.push(
    `**Comparison Type:** ${comparison.comparisonType || "standard"}`
  );
  sections.push("");
  sections.push(`**Companies Analyzed:** ${companyNames.join(", ")}`);
  sections.push("");

  // Industry Benchmarks
  if (comparison.benchmarks) {
    sections.push(`## Industry Benchmarks`);

    // Handle average market share
    let avgMarketShareText = "N/A";
    if (comparison.benchmarks.avgMarketShare != null) {
      const avgMarketShare = Number(comparison.benchmarks.avgMarketShare);
      if (avgMarketShare <= 1) {
        // Assume it's a decimal (0.1 = 10%)
        avgMarketShareText = (avgMarketShare * 100).toFixed(1) + "%";
      } else {
        // Assume it's already a percentage (10 = 10%)
        avgMarketShareText = avgMarketShare.toFixed(1) + "%";
      }
    }

    sections.push(`- **Average Market Share:** ${avgMarketShareText}`);

    sections.push(
      `- **Average Growth Rate:** ${
        comparison.benchmarks.avgGrowthRate != null
          ? comparison.benchmarks.avgGrowthRate.toFixed(1) + "%"
          : "N/A"
      }`
    );
    sections.push(
      `- **Average Risk Rating:** ${
        comparison.benchmarks.avgRiskRating != null
          ? comparison.benchmarks.avgRiskRating.toFixed(1) + "/10"
          : "N/A"
      }`
    );
    sections.push(
      `- **Average Sentiment Score:** ${
        comparison.benchmarks.avgSentimentScore != null
          ? comparison.benchmarks.avgSentimentScore.toFixed(0)
          : "N/A"
      }`
    );
    sections.push("");
  }

  // Company Performance Metrics
  if (comparison.metrics && comparison.metrics.length > 0) {
    sections.push(`## Company Performance Metrics`);
    const companyNames = getCompanyNames(comparison);
    comparison.metrics.forEach((metric, index) => {
      const companyName = companyNames[index] || `Company ${index + 1}`;
      sections.push(`### ${companyName}`);

      // Handle market share - check if it's already a percentage or decimal
      let marketShareText = "N/A";
      if (metric.marketShare != null) {
        const marketShare = Number(metric.marketShare);
        if (marketShare <= 1) {
          // Assume it's a decimal (0.1 = 10%)
          marketShareText = (marketShare * 100).toFixed(1) + "%";
        } else {
          // Assume it's already a percentage (10 = 10%)
          marketShareText = marketShare.toFixed(1) + "%";
        }
      }

      sections.push(`- **Market Share:** ${marketShareText}`);

      // Handle growth rate
      let growthRateText = "N/A";
      if (metric.growthRate != null) {
        const growthRate = Number(metric.growthRate);
        growthRateText = growthRate.toFixed(1) + "%";
      }

      sections.push(`- **Growth Rate:** ${growthRateText}`);
      sections.push(
        `- **Risk Rating:** ${
          metric.riskRating != null
            ? metric.riskRating.toFixed(1) + "/10"
            : "N/A"
        }`
      );
      sections.push(
        `- **Sentiment Score:** ${
          metric.sentimentScore != null
            ? metric.sentimentScore.toFixed(0)
            : "N/A"
        }`
      );
      sections.push("");
    });
  }

  // Key Insights
  if (comparison.insights && comparison.insights.length > 0) {
    sections.push(`## Key Insights`);
    comparison.insights.forEach((insight, index) => {
      sections.push(`${index + 1}. ${insight}`);
    });
    sections.push("");
  }

  // Investment Recommendations
  if (comparison.investmentRecommendations) {
    sections.push(`## Investment Recommendations`);
    sections.push(
      comparison.investmentRecommendations
        .replace(/\*\*(.*?)\*\*/g, "**$1**")
        .replace(/\n\n/g, "\n\n")
        .replace(/\n/g, "\n")
    );
    sections.push("");
  }

  // Visual Analytics
  if (comparison.radarChart || comparison.barGraph || comparison.scatterPlot) {
    sections.push(`## Visual Analytics`);

    if (comparison.radarChart) {
      sections.push(`### Radar Chart Analysis`);
      sections.push(`![Radar Chart](${comparison.radarChart})`);
      sections.push("");
    }

    if (comparison.barGraph) {
      sections.push(`### Performance Comparison`);
      sections.push(`![Bar Chart](${comparison.barGraph})`);
      sections.push("");
    }

    if (comparison.scatterPlot) {
      sections.push(`### Risk vs Growth Analysis`);
      sections.push(`![Scatter Plot](${comparison.scatterPlot})`);
      sections.push("");
    }
  }

  // Individual Company Analysis
  if (comparison.analyses && comparison.analyses.length > 0) {
    sections.push(`## Individual Company Analysis`);

    // If multiple companies, show SWOT images side by side
    const hasSwotImages = comparison.analyses.some(
      (analysis) => analysis.swotImage
    );
    if (hasSwotImages && comparison.analyses.length > 1) {
      sections.push(`### SWOT Analysis Visualizations`);
      sections.push("");

      // Create a side-by-side layout for SWOT images
      let imageRow = "| ";
      comparison.analyses.forEach((analysis) => {
        imageRow += `${analysis.companyName || "Company"} |`;
      });
      sections.push(imageRow);

      imageRow = "|";
      for (let i = 0; i < comparison.analyses.length; i++) {
        imageRow += "---|";
      }
      sections.push(imageRow);

      imageRow = "|";
      comparison.analyses.forEach((analysis) => {
        if (analysis.swotImage) {
          imageRow += ` ![SWOT](${analysis.swotImage}) |`;
        } else {
          imageRow += ` *No SWOT visualization available* |`;
        }
      });
      sections.push(imageRow);
      sections.push("");
    }

    const hasPestelImages = comparison.analyses.some(
      (analysis) => analysis.pestelImage
    );
    if (hasPestelImages && comparison.analyses.length > 1) {
      sections.push(`### PESTEL Analysis Visualizations`);
      sections.push("");

      // Create a side-by-side layout for Pestel images
      let imageRow = "| ";
      comparison.analyses.forEach((analysis) => {
        imageRow += `${analysis.companyName || "Company"} |`;
      });
      sections.push(imageRow);

      imageRow = "|";
      for (let i = 0; i < comparison.analyses.length; i++) {
        imageRow += "---|";
      }
      sections.push(imageRow);

      imageRow = "|";
      comparison.analyses.forEach((analysis) => {
        if (analysis.pestelImage) {
          imageRow += ` ![PESTEL](${analysis.pestelImage}) |`;
        } else {
          imageRow += ` *No PESTEL visualization available* |`;
        }
      });
      sections.push(imageRow);
      sections.push("");
    }

    const hasPorterImages = comparison.analyses.some(
      (analysis) => analysis.porterImage
    );
    if (hasPorterImages && comparison.analyses.length > 1) {
      sections.push(`### PORTER'S FIVE FORCES Analysis Visualizations`);
      sections.push("");

      // Create a side-by-side layout for PORTER images
      let imageRow = "| ";
      comparison.analyses.forEach((analysis) => {
        imageRow += `${analysis.companyName || "Company"} |`;
      });
      sections.push(imageRow);

      imageRow = "|";
      for (let i = 0; i < comparison.analyses.length; i++) {
        imageRow += "---|";
      }
      sections.push(imageRow);

      imageRow = "|";
      comparison.analyses.forEach((analysis) => {
        if (analysis.porterImage) {
          imageRow += ` ![PORTER](${analysis.porterImage}) |`;
        } else {
          imageRow += ` *No PORTER visualization available* |`;
        }
      });
      sections.push(imageRow);
      sections.push("");
    }

    const hasBCGImages = comparison.analyses.some(
      (analysis) => analysis.bcgImage
    );
    if (hasBCGImages && comparison.analyses.length > 1) {
      sections.push(`### BCG Analysis Visualizations`);
      sections.push("");

      // Create a side-by-side layout for BCG images
      let imageRow = "| ";
      comparison.analyses.forEach((analysis) => {
        imageRow += `${analysis.companyName || "Company"} |`;
      });
      sections.push(imageRow);

      imageRow = "|";
      for (let i = 0; i < comparison.analyses.length; i++) {
        imageRow += "---|";
      }
      sections.push(imageRow);

      imageRow = "|";
      comparison.analyses.forEach((analysis) => {
        if (analysis.bcgImage) {
          imageRow += ` ![BCG](${analysis.bcgImage}) |`;
        } else {
          imageRow += ` *No BCG visualization available* |`;
        }
      });
      sections.push(imageRow);
      sections.push("");
    }

    const hasMckinseyImages = comparison.analyses.some(
      (analysis) => analysis.mckinseyImage
    );
    if (hasMckinseyImages && comparison.analyses.length > 1) {
      sections.push(`### Mckinsey7s Analysis Visualizations`);
      sections.push("");

      // Create a side-by-side layout for Mckinsey7s images
      let imageRow = "| ";
      comparison.analyses.forEach((analysis) => {
        imageRow += `${analysis.companyName || "Company"} |`;
      });
      sections.push(imageRow);

      imageRow = "|";
      for (let i = 0; i < comparison.analyses.length; i++) {
        imageRow += "---|";
      }
      sections.push(imageRow);

      imageRow = "|";
      comparison.analyses.forEach((analysis) => {
        if (analysis.mckinseyImage) {
          imageRow += ` ![Mckinsey7s](${analysis.mckinseyImage}) |`;
        } else {
          imageRow += ` *No Mckinsey7s visualization available* |`;
        }
      });
      sections.push(imageRow);
      sections.push("");
    }

    comparison.analyses.forEach((analysis, index) => {
      const companyName = analysis.companyName || `Company ${index + 1}`;
      sections.push(`### ${companyName}`);

      // Analysis Sources
      if (analysis.sources && analysis.sources.length > 0) {
        sections.push(`#### Analysis Sources (${analysis.sources.length})`);
        analysis.sources.forEach((source, idx) => {
          sections.push(`${idx + 1}. [${source}](${source})`);
        });
        sections.push("");
      }

      // LinkedIn Analysis
      if (analysis.linkedinAnalysis) {
        sections.push(`#### LinkedIn-Based Analysis`);
        sections.push(
          analysis.linkedinAnalysis
            .replace(/<br\s*\/?>/gi, "\n")
            .replace(/<[^>]+>/g, "")
            .replace(/&nbsp;/g, " ")
            .replace(/&amp;/g, "&")
            .replace(/&lt;/g, "<")
            .replace(/&gt;/g, ">")
        );
        sections.push("");
      }

      // Strategy Recommendations
      if (analysis.strategyRecommendations) {
        sections.push(`#### Strategy Recommendations`);
        sections.push(
          analysis.strategyRecommendations
            .replace(/\*\*(.*?)\*\*/g, "**$1**")
            .replace(/<br\s*\/?>/gi, "\n")
            .replace(/<[^>]+>/g, "")
            .replace(/&nbsp;/g, " ")
            .replace(/&amp;/g, "&")
            .replace(/&lt;/g, "<")
            .replace(/&gt;/g, ">")
        );
        sections.push("");
      }

      // Company Summaries
      if (analysis.summaries?.length) {
        sections.push(`#### Detailed Analysis Summaries`);
        analysis.summaries.forEach((summary, idx) => {
          sections.push(
            `${idx + 1}. ${summary
              .replace(/<[^>]+>/g, "")
              .replace(/&nbsp;/g, " ")}`
          );
        });
        sections.push("");
      }
    });
  }

  sections.push("---");
  sections.push(
    "*This report was generated by InsightFlow - AI-Powered Competitive Intelligence*"
  );

  return sections.join("\n");
}
