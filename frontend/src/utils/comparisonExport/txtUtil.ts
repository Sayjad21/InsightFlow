import type { ComparisonResult } from "../../types";
import { cleanHtmlContent } from "../helpers/htmlCleanup";
import { formatDate } from "../helpers/dateFormatter";
import { getCompanyNames } from "../helpers/comparisonCompanyNames";


// Generate text content for comparison exports
export function generateComparisonTextContent(comparison: ComparisonResult): string {
  const sections: string[] = [];

  // Header
  const companyNames = getCompanyNames(comparison);
  sections.push("=".repeat(80));
  sections.push(
    `COMPANY COMPARISON REPORT: ${companyNames.join(" vs ").toUpperCase()}`
  );
  sections.push(`Analysis Date: ${formatDate(comparison.comparisonDate)}`);
  sections.push(`Comparison Type: ${comparison.comparisonType || "standard"}`);
  sections.push(`Companies Analyzed: ${companyNames.join(", ")}`);
  sections.push("=".repeat(80));
  sections.push("");

  // Industry Benchmarks
  if (comparison.benchmarks) {
    sections.push("1. INDUSTRY BENCHMARKS");
    sections.push("-".repeat(50));

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

    sections.push(`Average Market Share: ${avgMarketShareText}`);

    sections.push(
      `Average Growth Rate: ${
        comparison.benchmarks.avgGrowthRate != null
          ? comparison.benchmarks.avgGrowthRate.toFixed(1) + "%"
          : "N/A"
      }`
    );
    sections.push(
      `Average Risk Rating: ${
        comparison.benchmarks.avgRiskRating != null
          ? comparison.benchmarks.avgRiskRating.toFixed(1) + "/10"
          : "N/A"
      }`
    );
    sections.push(
      `Average Sentiment Score: ${
        comparison.benchmarks.avgSentimentScore != null
          ? comparison.benchmarks.avgSentimentScore.toFixed(0)
          : "N/A"
      }`
    );
    sections.push("");
  }

  // Company Performance Metrics
  if (comparison.metrics && comparison.metrics.length > 0) {
    sections.push("2. COMPANY PERFORMANCE METRICS");
    sections.push("-".repeat(50));
    const companyNames = getCompanyNames(comparison);
    comparison.metrics.forEach((metric, index) => {
      const companyName = companyNames[index] || `Company ${index + 1}`;
      sections.push(`${companyName}:`);

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

      sections.push(`  Market Share: ${marketShareText}`);

      // Handle growth rate
      let growthRateText = "N/A";
      if (metric.growthRate != null) {
        const growthRate = Number(metric.growthRate);
        growthRateText = growthRate.toFixed(1) + "%";
      }

      sections.push(`  Growth Rate: ${growthRateText}`);
      sections.push(
        `  Risk Rating: ${
          metric.riskRating != null
            ? metric.riskRating.toFixed(1) + "/10"
            : "N/A"
        }`
      );
      sections.push(
        `  Sentiment Score: ${
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
    sections.push("3. KEY INSIGHTS");
    sections.push("-".repeat(50));
    comparison.insights.forEach((insight, index) => {
      sections.push(`${index + 1}. ${insight}`);
    });
    sections.push("");
  }

  // Investment Recommendations
  if (comparison.investmentRecommendations) {
    sections.push("4. INVESTMENT RECOMMENDATIONS");
    sections.push("-".repeat(50));
    sections.push(cleanHtmlContent(comparison.investmentRecommendations));
    sections.push("");
  }

  // Individual Company Analysis
  if (comparison.analyses && comparison.analyses.length > 0) {
    sections.push("5. INDIVIDUAL COMPANY ANALYSIS");
    sections.push("-".repeat(50));

    comparison.analyses.forEach((analysis, index) => {
      const companyName = analysis.companyName || `Company ${index + 1}`;
      sections.push(`${companyName.toUpperCase()}`);
      sections.push("-".repeat(30));

      // SWOT Analysis
      if (analysis.swotLists) {
        sections.push("SWOT ANALYSIS:");

        if (analysis.swotLists.strengths?.length) {
          sections.push(
            `\nStrengths (${analysis.swotLists.strengths.length}):`
          );
          analysis.swotLists.strengths.forEach((strength) =>
            sections.push(`• ${strength}`)
          );
        }

        if (analysis.swotLists.weaknesses?.length) {
          sections.push(
            `\nWeaknesses (${analysis.swotLists.weaknesses.length}):`
          );
          analysis.swotLists.weaknesses.forEach((weakness) =>
            sections.push(`• ${weakness}`)
          );
        }

        if (analysis.swotLists.opportunities?.length) {
          sections.push(
            `\nOpportunities (${analysis.swotLists.opportunities.length}):`
          );
          analysis.swotLists.opportunities.forEach((opportunity) =>
            sections.push(`• ${opportunity}`)
          );
        }

        if (analysis.swotLists.threats?.length) {
          sections.push(`\nThreats (${analysis.swotLists.threats.length}):`);
          analysis.swotLists.threats.forEach((threat) =>
            sections.push(`• ${threat}`)
          );
        }

        // Add SWOT image reference
        if (analysis.swotImage) {
          sections.push(
            `\n[SWOT Analysis Visualization: ${analysis.swotImage}]`
          );
        }

        sections.push("");
      }

      // PESTEL Analysis
      if (analysis.pestelLists) {
        sections.push("PESTEL ANALYSIS:");

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
            sections.push(`\n${label} (${items.length}):`);
            items.forEach((item) => sections.push(`• ${item}`));
          }
        });

        // Add PESTEL image reference
        if (analysis.pestelImage) {
          sections.push(
            `\n[PESTEL Analysis Visualization: ${analysis.pestelImage}]`
          );
        }

        sections.push("");
      }

      // Porter's Five Forces Analysis
      if (analysis.porterForces) {
        sections.push("PORTER'S FIVE FORCES ANALYSIS:");

        const porterCategories = [
          { key: "rivalry", label: "Competitive Rivalry" },
          { key: "newEntrants", label: "Threat of New Entrants" },
          { key: "substitutes", label: "Threat of Substitutes" },
          { key: "buyerPower", label: "Bargaining Power of Buyers" },
          { key: "supplierPower", label: "Bargaining Power of Suppliers" },
        ];

        porterCategories.forEach(({ key, label }) => {
          const items =
            analysis.porterForces?.[key as keyof typeof analysis.porterForces];
          if (items && items.length > 0) {
            sections.push(`\n${label} (${items.length}):`);
            items.forEach((item) => sections.push(`• ${item}`));
          }
        });

        // Add Porter's image reference
        if (analysis.porterImage) {
          sections.push(
            `\n[Porter's Five Forces Visualization: ${analysis.porterImage}]`
          );
        }

        sections.push("");
      }

      // BCG Matrix Analysis
      if (analysis.bcgMatrix) {
        sections.push("BCG MATRIX ANALYSIS:");
        sections.push("");

        Object.entries(analysis.bcgMatrix).forEach(([product, data]) => {
          sections.push(`${product}:`);
          sections.push(`  Market Share: ${data.marketShare}%`);
          sections.push(`  Growth Rate: ${data.growthRate}%`);
          sections.push("");
        });

        // Add BCG image reference
        if (analysis.bcgImage) {
          sections.push(`[BCG Matrix Visualization: ${analysis.bcgImage}]`);
        }

        sections.push("");
      }

      // McKinsey 7S Framework
      if (analysis.mckinsey7s) {
        sections.push("MCKINSEY 7S FRAMEWORK:");
        sections.push("");

        const mckinsey7sLabels = {
          strategy: "Strategy",
          structure: "Structure",
          systems: "Systems",
          style: "Style",
          staff: "Staff",
          skills: "Skills",
          sharedValues: "Shared Values",
        };

        Object.entries(analysis.mckinsey7s).forEach(([key, value]) => {
          const label =
            mckinsey7sLabels[key as keyof typeof mckinsey7sLabels] || key;
          sections.push(`${label}: ${value}`);
        });

        // Add McKinsey image reference
        if (analysis.mckinseyImage) {
          sections.push(
            `\n[McKinsey 7S Framework Visualization: ${analysis.mckinseyImage}]`
          );
        }

        sections.push("");
      }

      // Analysis Sources
      if (analysis.sources && analysis.sources.length > 0) {
        sections.push(`ANALYSIS SOURCES (${analysis.sources.length}):`);
        analysis.sources.forEach((source, idx) => {
          sections.push(`${idx + 1}. ${source}`);
        });
        sections.push("");
      }

      // LinkedIn Analysis
      if (analysis.linkedinAnalysis) {
        sections.push("LINKEDIN-BASED ANALYSIS:");
        sections.push(cleanHtmlContent(analysis.linkedinAnalysis));
        sections.push("");
      }

      // Strategy Recommendations
      if (analysis.strategyRecommendations) {
        sections.push("STRATEGY RECOMMENDATIONS:");
        sections.push(cleanHtmlContent(analysis.strategyRecommendations));
        sections.push("");
      }

      // Company Summaries
      if (analysis.summaries?.length) {
        sections.push("DETAILED ANALYSIS SUMMARIES:");
        analysis.summaries.forEach((summary, idx) => {
          sections.push(`${idx + 1}. ${cleanHtmlContent(summary)}`);
        });
        sections.push("");
      }

      sections.push(""); // Extra space between companies
    });
  }

  sections.push("=".repeat(80));
  sections.push("END OF REPORT");
  sections.push("=".repeat(80));

  return sections.join("\n");
}
