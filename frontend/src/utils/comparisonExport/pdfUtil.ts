import { PdfExporter } from "../helpers/pdfFunctions";
import type { ComparisonResult } from "../../types";
import { cleanHtmlContent } from "../helpers/htmlCleanup";
import { getCompanyNames } from "../helpers/comparisonCompanyNames";

export async function generateComparisonPdfContents(
  pdf: PdfExporter,
  comparison: ComparisonResult
) {
  // Table of Contents
  pdf.addText("TABLE OF CONTENTS", 14, "bold");
  let tocIndex = 1;
  if (comparison.benchmarks) pdf.addText(`${tocIndex++}. Industry Benchmarks`);
  if (comparison.metrics?.length)
    pdf.addText(`${tocIndex++}. Company Performance Metrics`);
  if (comparison.insights?.length) pdf.addText(`${tocIndex++}. Key Insights`);
  if (comparison.investmentRecommendations)
    pdf.addText(`${tocIndex++}. Investment Recommendations`);
  if (comparison.radarChart || comparison.barGraph || comparison.scatterPlot)
    pdf.addText(`${tocIndex++}. Visual Analytics`);
  if (comparison.analyses?.length)
    pdf.addText(`${tocIndex++}. Individual Company Analysis`);
  pdf.yPosition += 10;

  // 1. Industry Benchmarks
  if (comparison.benchmarks) {
    pdf.addText("1. INDUSTRY BENCHMARKS", 14, "bold");

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

    pdf.addText(`Average Market Share: ${avgMarketShareText}`);

    pdf.addText(
      `Average Growth Rate: ${
        comparison.benchmarks.avgGrowthRate != null
          ? comparison.benchmarks.avgGrowthRate.toFixed(1) + "%"
          : "N/A"
      }`
    );
    pdf.addText(
      `Average Risk Rating: ${
        comparison.benchmarks.avgRiskRating != null
          ? comparison.benchmarks.avgRiskRating.toFixed(1) + "/10"
          : "N/A"
      }`
    );
    pdf.addText(
      `Average Sentiment Score: ${
        comparison.benchmarks.avgSentimentScore != null
          ? comparison.benchmarks.avgSentimentScore.toFixed(0)
          : "N/A"
      }`
    );
    pdf.yPosition += 5;
  }

  // 2. Company Performance Metrics
  if (comparison.metrics && comparison.metrics.length > 0) {
    pdf.addText("2. COMPANY PERFORMANCE METRICS", 14, "bold");
    const companyNames = getCompanyNames(comparison);
    comparison.metrics.forEach((metric, index) => {
      const companyName = companyNames[index] || `Company ${index + 1}`;
      pdf.addText(`${companyName}:`, 12, "bold");

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

      pdf.addText(`  Market Share: ${marketShareText}`);

      // Handle growth rate - check if it needs to be treated differently
      let growthRateText = "N/A";
      if (metric.growthRate != null) {
        const growthRate = Number(metric.growthRate);
        growthRateText = growthRate.toFixed(1) + "%";
      }

      pdf.addText(`  Growth Rate: ${growthRateText}`);
      pdf.addText(
        `  Risk Rating: ${
          metric.riskRating != null
            ? metric.riskRating.toFixed(1) + "/10"
            : "N/A"
        }`
      );
      pdf.addText(
        `  Sentiment Score: ${
          metric.sentimentScore != null
            ? metric.sentimentScore.toFixed(0)
            : "N/A"
        }`
      );
      pdf.yPosition += 3;
    });
  }

  // 3. Key Insights
  if (comparison.insights && comparison.insights.length > 0) {
    pdf.addText("3. KEY INSIGHTS", 14, "bold");
    comparison.insights.forEach((insight, index) => {
      pdf.addText(`${index + 1}. ${insight}`);
    });
    pdf.yPosition += 5;
  }

  // 4. Investment Recommendations
  if (comparison.investmentRecommendations) {
    pdf.addText("4. INVESTMENT RECOMMENDATIONS", 14, "bold");
    pdf.addMarkdown(cleanHtmlContent(comparison.investmentRecommendations));
    pdf.yPosition += 5;
  }

  // 5. Visual Analytics
  if (comparison.radarChart || comparison.barGraph || comparison.scatterPlot) {
    pdf.addText("5. VISUAL ANALYTICS", 14, "bold");

    if (comparison.radarChart) {
      await pdf.addImage(
        comparison.radarChart,
        "Radar Chart Analysis",
        140,
        90
      );
    }

    if (comparison.barGraph) {
      await pdf.addImage(
        comparison.barGraph,
        "Performance Comparison",
        140,
        90
      );
    }

    if (comparison.scatterPlot) {
      await pdf.addImage(
        comparison.scatterPlot,
        "Risk vs Growth Analysis",
        140,
        90
      );
    }
  }

  // 6. Individual Company Analysis
  if (comparison.analyses && comparison.analyses.length > 0) {
    pdf.addText("6. INDIVIDUAL COMPANY ANALYSIS", 14, "bold");

    // First, add all visualization sections as in markdown (side-by-side tables approach)

    // SWOT Analysis Visualizations
    const hasSwotImages = comparison.analyses.some(
      (analysis) => analysis.swotImage
    );
    if (hasSwotImages && comparison.analyses.length > 1) {
      // Estimate required height: title + one image row
      const imageHeight = 70;
      const imageSpacing = 15;
      const titleHeight = 15; // approx height of the title
      const totalNeededHeight = titleHeight + imageHeight + imageSpacing;
      pdf.checkPageBreak(totalNeededHeight);
      pdf.addText("SWOT ANALYSIS VISUALIZATIONS", 13, "bold");

      const swotImages = comparison.analyses
        .filter((analysis) => analysis.swotImage)
        .map((analysis) => ({
          url: analysis.swotImage!,
          title: `SWOT - ${analysis.companyName || "Company"}`,
        }));

      if (swotImages.length > 0) {
        await pdf.addImagesInPairs(swotImages, 70, 90);
      }
      pdf.yPosition += 10;
    }

    // PESTEL Analysis Visualizations
    const hasPestelImages = comparison.analyses.some(
      (analysis) => analysis.pestelImage
    );
    if (hasPestelImages && comparison.analyses.length > 1) {
      // Estimate required height: title + one image row
      const imageHeight = 70;
      const imageSpacing = 15;
      const titleHeight = 15; // approx height of the title
      const totalNeededHeight = titleHeight + imageHeight + imageSpacing;
      pdf.checkPageBreak(totalNeededHeight);

      pdf.addText("PESTEL ANALYSIS VISUALIZATIONS", 13, "bold");

      const pestelImages = comparison.analyses
        .filter((analysis) => analysis.pestelImage)
        .map((analysis) => ({
          url: analysis.pestelImage!,
          title: `PESTEL - ${analysis.companyName || "Company"}`,
        }));

      if (pestelImages.length > 0) {
        await pdf.addImagesInPairs(pestelImages, 70, 90);
      }
      pdf.yPosition += 10;
    }

    // Porter's Five Forces Analysis Visualizations
    const hasPorterImages = comparison.analyses.some(
      (analysis) => analysis.porterImage
    );
    if (hasPorterImages && comparison.analyses.length > 1) {
      // Estimate required height: title + one image row
      const imageHeight = 60;
      const imageSpacing = 15;
      const titleHeight = 15; // approx height of the title
      const totalNeededHeight = titleHeight + imageHeight + imageSpacing;
      pdf.checkPageBreak(totalNeededHeight);
      pdf.addText("PORTER'S FIVE FORCES ANALYSIS VISUALIZATIONS", 13, "bold");

      const porterImages = comparison.analyses
        .filter((analysis) => analysis.porterImage)
        .map((analysis) => ({
          url: analysis.porterImage!,
          title: `Porter's - ${analysis.companyName || "Company"}`,
        }));

      if (porterImages.length > 0) {
        await pdf.addImagesInPairs(porterImages, 70, 90);
      }
      pdf.yPosition += 10;
    }

    // BCG Analysis Visualizations
    const hasBCGImages = comparison.analyses.some(
      (analysis) => analysis.bcgImage
    );
    if (hasBCGImages && comparison.analyses.length > 1) {
      // Estimate required height: title + one image row
      const imageHeight = 50;
      const imageSpacing = 15;
      const titleHeight = 15; // approx height of the title
      const totalNeededHeight = titleHeight + imageHeight + imageSpacing;
      pdf.checkPageBreak(totalNeededHeight);

      // Now add the title
      pdf.addText("BCG ANALYSIS VISUALIZATIONS", 13, "bold");

      // Add images
      const bcgImages = comparison.analyses
        .filter((analysis) => analysis.bcgImage)
        .map((analysis) => ({
          url: analysis.bcgImage!,
          title: `BCG - ${analysis.companyName || "Company"}`,
        }));

      if (bcgImages.length > 0) {
        await pdf.addImagesInPairs(bcgImages, 70, 90);
      }

      pdf.yPosition += 10;
    }

    // McKinsey 7S Analysis Visualizations
    const hasMckinseyImages = comparison.analyses.some(
      (analysis) => analysis.mckinseyImage
    );
    if (hasMckinseyImages && comparison.analyses.length > 1) {
      // Estimate required height: title + one image row
      const imageHeight = 70;
      const imageSpacing = 15;
      const titleHeight = 15; // approx height of the title
      const totalNeededHeight = titleHeight + imageHeight + imageSpacing;
      pdf.checkPageBreak(totalNeededHeight);

      pdf.addText("MCKINSEY7S ANALYSIS VISUALIZATIONS", 13, "bold");

      const mckinseyImages = comparison.analyses
        .filter((analysis) => analysis.mckinseyImage)
        .map((analysis) => ({
          url: analysis.mckinseyImage!,
          title: `Mckinsey7s - ${analysis.companyName || "Company"}`,
        }));

      if (mckinseyImages.length > 0) {
        await pdf.addImagesInPairs(mckinseyImages, 70, 90);
      }
      pdf.yPosition += 10;
    }

    // Now add individual company details for each company (as in markdown)
    comparison.analyses.forEach((analysis, index) => {
      const companyName = analysis.companyName || `Company ${index + 1}`;
      pdf.addText(`${companyName}`, 13, "bold");

      // Analysis Sources
      if (analysis.sources && analysis.sources.length > 0) {
        pdf.addText(
          `Analysis Sources (${analysis.sources.length})`,
          12,
          "bold"
        );
        analysis.sources.forEach((source, idx) => {
          pdf.pdf.setTextColor(0, 0, 255);
          pdf.addText(`${idx + 1}. ${source}`, 10);
          pdf.pdf.setTextColor(0, 0, 0); // reset back to black for next text
        });
        pdf.yPosition += 5;
      }

      // LinkedIn Analysis
      if (analysis.linkedinAnalysis) {
        pdf.addText("LinkedIn-Based Analysis", 12, "bold");
        pdf.addMarkdown(cleanHtmlContent(analysis.linkedinAnalysis));
        pdf.yPosition += 5;
      }

      // Strategy Recommendations
      if (analysis.strategyRecommendations) {
        pdf.addText("Strategy Recommendations", 12, "bold");
        pdf.addMarkdown(cleanHtmlContent(analysis.strategyRecommendations));
        pdf.yPosition += 5;
      }

      // Company Summaries
      if (analysis.summaries?.length) {
        pdf.addText("Detailed Analysis Summaries", 12, "bold");
        analysis.summaries.forEach((summary, idx) => {
          pdf.addMarkdown(`${idx + 1}. ${cleanHtmlContent(summary)}`);
        });
        pdf.yPosition += 5;
      }

      pdf.yPosition += 10; // Extra spacing between companies
    });
  }
}
