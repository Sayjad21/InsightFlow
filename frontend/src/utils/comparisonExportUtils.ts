import type { ComparisonResult } from "../types";

// Utility function to clean HTML content for text export
function cleanHtmlContent(html: string): string {
  if (!html) return "";
  return html
    .replace(/<br\s*\/?>/gi, "\n")
    .replace(/<\/p>/gi, "\n\n")
    .replace(/<p[^>]*>/gi, "")
    .replace(/<strong[^>]*>(.*?)<\/strong>/gi, "**$1**")
    .replace(/<em[^>]*>(.*?)<\/em>/gi, "*$1*")
    .replace(/<h([1-6])[^>]*>(.*?)<\/h[1-6]>/gi, (_, level, text) => {
      const prefix = "#".repeat(parseInt(level));
      return `${prefix} ${text}\n`;
    })
    .replace(/<[^>]+>/g, "") // Remove any remaining HTML tags
    .replace(/&nbsp;/g, " ")
    .replace(/&amp;/g, "&")
    .replace(/&lt;/g, "<")
    .replace(/&gt;/g, ">")
    .replace(/\n{3,}/g, "\n\n") // Replace multiple newlines with double newlines
    .trim();
}

// Helper function to extract company names from comparison data
function getCompanyNames(comparison: ComparisonResult): string[] {
  // First try the companyNames field if it exists and has data
  if (comparison.companyNames && comparison.companyNames.length > 0) {
    return comparison.companyNames;
  }

  // Otherwise extract from analyses array
  if (comparison.analyses && comparison.analyses.length > 0) {
    return comparison.analyses
      .map((analysis) => analysis.companyName)
      .filter((name) => name && name.trim() !== "");
  }

  // Fallback: try to infer from number of metrics/analyses
  const count = Math.max(
    comparison.metrics?.length || 0,
    comparison.analyses?.length || 0
  );

  if (count > 0) {
    return Array.from({ length: count }, (_, i) => `Company ${i + 1}`);
  }

  return ["Unknown Companies"];
}

// Format date helper
function formatDate(dateString: string | undefined): string {
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
}

// Download file helper
function downloadFile(blob: Blob, filename: string): void {
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
}

// Generate TXT export for comparison
export function exportComparisonToTxt(comparison: ComparisonResult): void {
  const content = generateComparisonTextContent(comparison);
  const companyNames = getCompanyNames(comparison);
  const filename = `${companyNames
    .join("_vs_")
    .replace(/[^a-zA-Z0-9_]/g, "")}_comparison_report.txt`;
  const blob = new Blob([content], { type: "text/plain" });
  downloadFile(blob, filename);
}

// Generate Markdown export for comparison
export function exportComparisonToMarkdown(comparison: ComparisonResult): void {
  const content = generateComparisonMarkdownContent(comparison);
  const companyNames = getCompanyNames(comparison);
  const filename = `${companyNames
    .join("_vs_")
    .replace(/[^a-zA-Z0-9_]/g, "")}_comparison_report.md`;
  const blob = new Blob([content], { type: "text/markdown" });
  downloadFile(blob, filename);
}

// Generate HTML export for comparison
export function exportComparisonToHtml(comparison: ComparisonResult): void {
  const markdownContent = generateComparisonMarkdownContent(comparison);

  // Convert markdown to HTML
  let htmlContent = markdownContent
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
  const companyNames = getCompanyNames(comparison);
  const fullHtml = `
<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <title>${companyNames.join(" vs ")} - Comparison Report</title>
    <style>
      body { font-family: Arial, sans-serif; line-height: 1.6; margin: 40px; }
      h1 { color: #333; border-bottom: 2px solid #333; }
      h2 { color: #666; border-bottom: 1px solid #666; }
      h3, h4 { color: #888; }
      li { margin: 5px 0; }
      img { display: block; margin: 20px auto; }
      p { margin: 10px 0; }
      .metric-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin: 20px 0; }
      .metric-card { border: 1px solid #ddd; padding: 15px; border-radius: 8px; }
      .benchmark-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 15px; margin: 20px 0; }
      .benchmark-card { border: 1px solid #ddd; padding: 10px; border-radius: 6px; text-align: center; }
    </style>
  </head>
  <body>
    ${htmlContent}
  </body>
</html>
  `;

  const filename = `${companyNames
    .join("_vs_")
    .replace(/[^a-zA-Z0-9_]/g, "")}_comparison_report.html`;
  const blob = new Blob([fullHtml], { type: "text/html" });
  downloadFile(blob, filename);
}

// Generate PDF export with proper formatting and images
export async function exportComparisonToPdf(
  comparison: ComparisonResult
): Promise<void> {
  try {
    // Dynamic import to keep bundle size smaller
    const jsPDF = (await import("jspdf")).default;

    const pdf = new jsPDF();
    let yPosition = 20;
    const lineHeight = 7;
    const pageHeight = pdf.internal.pageSize.height - 20;
    const pageWidth = pdf.internal.pageSize.width;

    // Helper function to add a new page if needed
    const checkPageBreak = (requiredHeight: number) => {
      if (yPosition + requiredHeight > pageHeight) {
        pdf.addPage();
        yPosition = 20;
      }
    };

    // Helper function to add text with proper formatting
    const addText = (text: string, fontSize = 10, fontStyle = "normal") => {
      if (!text) return;

      checkPageBreak(lineHeight * 2);
      pdf.setFontSize(fontSize);
      pdf.setFont(undefined, fontStyle);

      const wrappedLines = pdf.splitTextToSize(text, 170);
      if (Array.isArray(wrappedLines)) {
        for (const wrappedLine of wrappedLines) {
          checkPageBreak(lineHeight);
          pdf.text(wrappedLine, 20, yPosition);
          yPosition += lineHeight;
        }
      } else {
        pdf.text(wrappedLines, 20, yPosition);
        yPosition += lineHeight;
      }
      yPosition += 3; // Extra spacing after text blocks
    };

    // Helper function to add images
    const addImage = (
      base64Data: string,
      title: string,
      maxWidth = 150,
      maxHeight = 100
    ) => {
      try {
        const imageHeight = Math.min(maxHeight, 80);
        checkPageBreak(imageHeight + 20);

        // Add image title
        pdf.setFontSize(12);
        pdf.setFont(undefined, "bold");
        pdf.text(title, 20, yPosition);
        yPosition += 15;

        // Calculate image position to center it
        const imageX = (pageWidth - maxWidth) / 2;

        // Ensure base64 data has proper format
        const imageData = base64Data.startsWith("data:")
          ? base64Data
          : `data:image/png;base64,${base64Data}`;

        // Add the image
        pdf.addImage(
          imageData,
          "PNG",
          imageX,
          yPosition,
          maxWidth,
          imageHeight
        );
        yPosition += imageHeight + 15;
      } catch (error) {
        console.error(`Failed to add image ${title}:`, error);
        // Continue without the image
      }
    };

    // Title Page
    const companyNames = getCompanyNames(comparison);
    pdf.setFontSize(20);
    pdf.setFont(undefined, "bold");
    pdf.text("Company Comparison Report", 20, yPosition);
    yPosition += 15;

    pdf.setFontSize(16);
    pdf.text(companyNames.join(" vs "), 20, yPosition);
    yPosition += 10;

    pdf.setFontSize(10);
    pdf.setFont(undefined, "normal");
    pdf.text(
      `Generated on: ${formatDate(comparison.comparisonDate)}`,
      20,
      yPosition
    );
    pdf.text(
      `Comparison Type: ${comparison.comparisonType || "standard"}`,
      20,
      yPosition + 7
    );
    yPosition += 20;

    // Table of Contents
    addText("TABLE OF CONTENTS", 14, "bold");
    let tocIndex = 1;
    if (comparison.benchmarks) addText(`${tocIndex++}. Industry Benchmarks`);
    if (comparison.metrics?.length)
      addText(`${tocIndex++}. Company Performance Metrics`);
    if (comparison.insights?.length) addText(`${tocIndex++}. Key Insights`);
    if (comparison.investmentRecommendations)
      addText(`${tocIndex++}. Investment Recommendations`);
    if (comparison.radarChart || comparison.barGraph || comparison.scatterPlot)
      addText(`${tocIndex++}. Visual Analytics`);
    if (comparison.analyses?.length)
      addText(`${tocIndex++}. Individual Company Analysis`);
    yPosition += 10;

    // 1. Industry Benchmarks
    if (comparison.benchmarks) {
      addText("1. INDUSTRY BENCHMARKS", 14, "bold");
      addText(
        `Average Market Share: ${
          comparison.benchmarks.avgMarketShare != null
            ? (comparison.benchmarks.avgMarketShare * 100).toFixed(1) + "%"
            : "N/A"
        }`
      );
      addText(
        `Average Growth Rate: ${
          comparison.benchmarks.avgGrowthRate != null
            ? comparison.benchmarks.avgGrowthRate.toFixed(1) + "%"
            : "N/A"
        }`
      );
      addText(
        `Average Risk Rating: ${
          comparison.benchmarks.avgRiskRating != null
            ? comparison.benchmarks.avgRiskRating.toFixed(1) + "/10"
            : "N/A"
        }`
      );
      addText(
        `Average Sentiment Score: ${
          comparison.benchmarks.avgSentimentScore != null
            ? comparison.benchmarks.avgSentimentScore.toFixed(0)
            : "N/A"
        }`
      );
      yPosition += 5;
    }

    // 2. Company Performance Metrics
    if (comparison.metrics && comparison.metrics.length > 0) {
      addText("2. COMPANY PERFORMANCE METRICS", 14, "bold");
      const companyNames = getCompanyNames(comparison);
      comparison.metrics.forEach((metric, index) => {
        const companyName = companyNames[index] || `Company ${index + 1}`;
        addText(`${companyName}:`, 12, "bold");
        addText(
          `  Market Share: ${
            metric.marketShare != null
              ? (metric.marketShare * 100).toFixed(1) + "%"
              : "N/A"
          }`
        );
        addText(
          `  Growth Rate: ${
            metric.growthRate != null
              ? metric.growthRate.toFixed(1) + "%"
              : "N/A"
          }`
        );
        addText(
          `  Risk Rating: ${
            metric.riskRating != null
              ? metric.riskRating.toFixed(1) + "/10"
              : "N/A"
          }`
        );
        addText(
          `  Sentiment Score: ${
            metric.sentimentScore != null
              ? metric.sentimentScore.toFixed(0)
              : "N/A"
          }`
        );
        yPosition += 3;
      });
    }

    // 3. Key Insights
    if (comparison.insights && comparison.insights.length > 0) {
      addText("3. KEY INSIGHTS", 14, "bold");
      comparison.insights.forEach((insight, index) => {
        addText(`${index + 1}. ${insight}`);
      });
      yPosition += 5;
    }

    // 4. Investment Recommendations
    if (comparison.investmentRecommendations) {
      addText("4. INVESTMENT RECOMMENDATIONS", 14, "bold");
      addText(cleanHtmlContent(comparison.investmentRecommendations));
      yPosition += 5;
    }

    // 5. Visual Analytics
    if (
      comparison.radarChart ||
      comparison.barGraph ||
      comparison.scatterPlot
    ) {
      addText("5. VISUAL ANALYTICS", 14, "bold");

      if (comparison.radarChart) {
        addImage(comparison.radarChart, "Radar Chart Analysis", 140, 90);
      }

      if (comparison.barGraph) {
        addImage(comparison.barGraph, "Performance Comparison", 140, 90);
      }

      if (comparison.scatterPlot) {
        addImage(comparison.scatterPlot, "Risk vs Growth Analysis", 140, 90);
      }
    }

    // 6. Individual Company Analysis
    if (comparison.analyses && comparison.analyses.length > 0) {
      addText("6. INDIVIDUAL COMPANY ANALYSIS", 14, "bold");

      comparison.analyses.forEach((analysis, index) => {
        const companyName = analysis.companyName || `Company ${index + 1}`;
        addText(`${companyName}`, 13, "bold");

        // SWOT Analysis
        if (analysis.swotLists) {
          addText("SWOT ANALYSIS:", 12, "bold");

          if (analysis.swotLists.strengths?.length) {
            addText(
              `Strengths (${analysis.swotLists.strengths.length}):`,
              11,
              "bold"
            );
            analysis.swotLists.strengths.forEach((strength) =>
              addText(`• ${strength}`)
            );
          }

          if (analysis.swotLists.weaknesses?.length) {
            addText(
              `Weaknesses (${analysis.swotLists.weaknesses.length}):`,
              11,
              "bold"
            );
            analysis.swotLists.weaknesses.forEach((weakness) =>
              addText(`• ${weakness}`)
            );
          }

          if (analysis.swotLists.opportunities?.length) {
            addText(
              `Opportunities (${analysis.swotLists.opportunities.length}):`,
              11,
              "bold"
            );
            analysis.swotLists.opportunities.forEach((opportunity) =>
              addText(`• ${opportunity}`)
            );
          }

          if (analysis.swotLists.threats?.length) {
            addText(
              `Threats (${analysis.swotLists.threats.length}):`,
              11,
              "bold"
            );
            analysis.swotLists.threats.forEach((threat) =>
              addText(`• ${threat}`)
            );
          }

          yPosition += 5;
        }

        // PESTEL Analysis
        if (analysis.pestelLists) {
          addText("PESTEL ANALYSIS:", 12, "bold");

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
              addText(`${label} (${items.length}):`, 11, "bold");
              items.forEach((item) => addText(`• ${item}`));
            }
          });

          yPosition += 5;
        }

        // Strategy Recommendations
        if (analysis.strategyRecommendations) {
          addText("STRATEGY RECOMMENDATIONS:", 12, "bold");
          addText(cleanHtmlContent(analysis.strategyRecommendations));
          yPosition += 5;
        }

        // Company Summaries
        if (analysis.summaries?.length) {
          addText("COMPANY SUMMARY:", 12, "bold");
          analysis.summaries.forEach((summary, idx) => {
            addText(`${idx + 1}. ${cleanHtmlContent(summary)}`);
          });
          yPosition += 5;
        }

        yPosition += 10; // Space between companies
      });
    }

    // Footer
    checkPageBreak(30);
    pdf.setFontSize(8);
    pdf.setFont(undefined, "normal");
    pdf.text(
      "Generated by InsightFlow - AI-Powered Competitive Intelligence",
      20,
      yPosition
    );

    // Save the PDF
    const filename = `${companyNames
      .join("_vs_")
      .replace(/[^a-zA-Z0-9\s]/g, "")
      .replace(/\s+/g, "_")}_comparison_report.pdf`;
    pdf.save(filename);
  } catch (error) {
    console.error("Failed to generate PDF:", error);
    // Fallback to text export
    exportComparisonToTxt(comparison);
  }
}

// Generate text content for comparison exports
function generateComparisonTextContent(comparison: ComparisonResult): string {
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
    sections.push(
      `Average Market Share: ${
        comparison.benchmarks.avgMarketShare != null
          ? (comparison.benchmarks.avgMarketShare * 100).toFixed(1) + "%"
          : "N/A"
      }`
    );
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
      sections.push(
        `  Market Share: ${
          metric.marketShare != null
            ? (metric.marketShare * 100).toFixed(1) + "%"
            : "N/A"
        }`
      );
      sections.push(
        `  Growth Rate: ${
          metric.growthRate != null ? metric.growthRate.toFixed(1) + "%" : "N/A"
        }`
      );
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
        sections.push("COMPANY SUMMARY:");
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

// Generate Markdown content for comparison
function generateComparisonMarkdownContent(
  comparison: ComparisonResult
): string {
  const sections: string[] = [];

  // Header
  const companyNames = getCompanyNames(comparison);
  sections.push(`# ${companyNames.join(" vs ")} - Comparison Report`);
  sections.push(`**Analysis Date:** ${formatDate(comparison.comparisonDate)}`);
  sections.push(
    `**Comparison Type:** ${comparison.comparisonType || "standard"}`
  );
  sections.push(`**Companies Analyzed:** ${companyNames.join(", ")}`);
  sections.push("");

  // Industry Benchmarks
  if (comparison.benchmarks) {
    sections.push(`## Industry Benchmarks`);
    sections.push(
      `- **Average Market Share:** ${
        comparison.benchmarks.avgMarketShare != null
          ? (comparison.benchmarks.avgMarketShare * 100).toFixed(1) + "%"
          : "N/A"
      }`
    );
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
      sections.push(
        `- **Market Share:** ${
          metric.marketShare != null
            ? (metric.marketShare * 100).toFixed(1) + "%"
            : "N/A"
        }`
      );
      sections.push(
        `- **Growth Rate:** ${
          metric.growthRate != null ? metric.growthRate.toFixed(1) + "%" : "N/A"
        }`
      );
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
      sections.push(
        `![Radar Chart](data:image/png;base64,${comparison.radarChart})`
      );
      sections.push("");
    }

    if (comparison.barGraph) {
      sections.push(`### Performance Comparison`);
      sections.push(
        `![Bar Chart](data:image/png;base64,${comparison.barGraph})`
      );
      sections.push("");
    }

    if (comparison.scatterPlot) {
      sections.push(`### Risk vs Growth Analysis`);
      sections.push(
        `![Scatter Plot](data:image/png;base64,${comparison.scatterPlot})`
      );
      sections.push("");
    }
  }

  // Individual Company Analysis
  if (comparison.analyses && comparison.analyses.length > 0) {
    sections.push(`## Individual Company Analysis`);

    comparison.analyses.forEach((analysis, index) => {
      const companyName = analysis.companyName || `Company ${index + 1}`;
      sections.push(`### ${companyName}`);

      // SWOT Analysis
      if (analysis.swotLists) {
        sections.push(`#### SWOT Analysis`);

        if (analysis.swotLists.strengths?.length) {
          sections.push(
            `**Strengths (${analysis.swotLists.strengths.length}):**`
          );
          analysis.swotLists.strengths.forEach((strength) =>
            sections.push(`- ${strength}`)
          );
          sections.push("");
        }

        if (analysis.swotLists.weaknesses?.length) {
          sections.push(
            `**Weaknesses (${analysis.swotLists.weaknesses.length}):**`
          );
          analysis.swotLists.weaknesses.forEach((weakness) =>
            sections.push(`- ${weakness}`)
          );
          sections.push("");
        }

        if (analysis.swotLists.opportunities?.length) {
          sections.push(
            `**Opportunities (${analysis.swotLists.opportunities.length}):**`
          );
          analysis.swotLists.opportunities.forEach((opportunity) =>
            sections.push(`- ${opportunity}`)
          );
          sections.push("");
        }

        if (analysis.swotLists.threats?.length) {
          sections.push(`**Threats (${analysis.swotLists.threats.length}):**`);
          analysis.swotLists.threats.forEach((threat) =>
            sections.push(`- ${threat}`)
          );
          sections.push("");
        }
      }

      // PESTEL Analysis
      if (analysis.pestelLists) {
        sections.push(`#### PESTEL Analysis`);

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
            sections.push(`**${label} (${items.length}):**`);
            items.forEach((item) => sections.push(`- ${item}`));
            sections.push("");
          }
        });
      }

      // Strategy Recommendations
      if (analysis.strategyRecommendations) {
        sections.push(`#### Strategy Recommendations`);
        sections.push(
          analysis.strategyRecommendations
            .replace(/\*\*(.*?)\*\*/g, "**$1**")
            .replace(/\n\n/g, "\n\n")
            .replace(/\n/g, "\n")
        );
        sections.push("");
      }

      // Company Summaries
      if (analysis.summaries?.length) {
        sections.push(`#### Company Summary`);
        analysis.summaries.forEach((summary, idx) => {
          sections.push(`${idx + 1}. ${summary}`);
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
