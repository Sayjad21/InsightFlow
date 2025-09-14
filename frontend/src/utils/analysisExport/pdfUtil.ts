import type { PdfExporter } from "../helpers/pdfFunctions";
import type { AnalysisResult } from "../../types";
import { cleanHtmlContent } from "../helpers/htmlCleanup";

export async function generatePdfContents(
  pdf: PdfExporter,
  analysisResult: AnalysisResult
) {
  // Table of Contents
  pdf.addText("TABLE OF CONTENTS", 14, "bold");

  let tocIndex = 1;
  if (analysisResult.summaries?.length)
    pdf.addText(`${tocIndex++}. Competitive Intelligence`);
  if (analysisResult.strategy_recommendations)
    pdf.addText(`${tocIndex++}. Differentiation Strategy`);
  if (analysisResult.linkedin_analysis)
    pdf.addText(`${tocIndex++}. LinkedIn Intelligence`);
  if (analysisResult.swot_lists) pdf.addText(`${tocIndex++}. SWOT Analysis`);
  if (analysisResult.porter_forces)
    pdf.addText(`${tocIndex++}. Porter's Five Forces`);
  if (analysisResult.bcg_matrix) pdf.addText(`${tocIndex++}. BCG Matrix`);
  if (analysisResult.mckinsey_7s)
    pdf.addText(`${tocIndex++}. McKinsey 7S Model`);

  // 1. Competitive Intelligence
  if (analysisResult.summaries?.length) {
    pdf.addText("1. COMPETITIVE INTELLIGENCE", 14, "bold");
    analysisResult.summaries.forEach((s, i) => {
      pdf.addText(`Analysis ${i + 1}:`, 12, "bold");
      pdf.addMarkdown(cleanHtmlContent(s));
      if (analysisResult.sources?.[i]) {
        pdf.pdf.setTextColor(0, 0, 255); // blue color
        pdf.addText(`Source: ${analysisResult.sources[i]}`, 9);
        pdf.pdf.setTextColor(0, 0, 0); // reset back to black for next text
      }
    });
  }

  // 2. Differentiation Strategy
  if (analysisResult.strategy_recommendations) {
    pdf.addText("2. DIFFERENTIATION STRATEGY", 14, "bold");
    pdf.addMarkdown(cleanHtmlContent(analysisResult.strategy_recommendations));
  }

  // 3. LinkedIn Intelligence
  if (analysisResult.linkedin_analysis) {
    pdf.addText("3. LINKEDIN INTELLIGENCE", 14, "bold");
    pdf.addMarkdown(cleanHtmlContent(analysisResult.linkedin_analysis));
  }

  // 4. SWOT Analysis
  if (analysisResult.swot_lists) {
    // pdf.addText("4. SWOT ANALYSIS", 14, "bold");

    // pdf.addText("STRENGTHS:", 12, "bold");
    // analysisResult.swot_lists.strengths.forEach((item) =>
    //   pdf.addText(`• ${item}`)
    // );

    // pdf.addText("WEAKNESSES:", 12, "bold");
    // analysisResult.swot_lists.weaknesses.forEach((item) =>
    //   pdf.addText(`• ${item}`)
    // );

    // pdf.addText("OPPORTUNITIES:", 12, "bold");
    // analysisResult.swot_lists.opportunities.forEach((item) =>
    //   pdf.addText(`• ${item}`)
    // );

    // pdf.addText("THREATS:", 12, "bold");
    // analysisResult.swot_lists.threats.forEach((item) =>
    //   pdf.addText(`• ${item}`)
    // );

    if (analysisResult.swot_image) {
      await pdf.addImage(analysisResult.swot_image, "4. SWOT ANALYSIS");
    }
  }

  // 5. Porter's Five Forces
  if (analysisResult.porter_forces) {
    // pdf.addText("5. PORTER'S FIVE FORCES", 14, "bold");

    // Object.entries(analysisResult.porter_forces).forEach(([force, items]) => {
    //   pdf.addText(`${force.toUpperCase().replace("_", " ")}:`, 12, "bold");
    //   items.forEach((item) => pdf.addText(`• ${item}`));
    // });

    if (analysisResult.porter_image) {
      await pdf.addImage(
        analysisResult.porter_image,
        "5. PORTER'S FIVE FORCES",
        100,
        150
      );
    }
  }

  // 6. BCG Matrix
  if (analysisResult.bcg_matrix) {
    // pdf.addText("6. BCG MATRIX", 14, "bold");

    // Object.entries(analysisResult.bcg_matrix).forEach(([product, values]) => {
    //   pdf.addText(`${product}:`, 12, "bold");
    //   pdf.addText(`  Market Share: ${values.market_share}`);
    //   pdf.addText(`  Growth Rate: ${values.growth_rate}%`);
    // });

    if (analysisResult.bcg_image) {
      await pdf.addImage(analysisResult.bcg_image, "6. BCG MATRIX", 140, 150);
    }
  }

  // 7. McKinsey 7S
  if (analysisResult.mckinsey_7s) {
    // pdf.addText("7. MCKINSEY 7S MODEL", 14, "bold");

    // Object.entries(analysisResult.mckinsey_7s).forEach(([element, value]) => {
    //   pdf.addText(`${element.toUpperCase().replace("_", " ")}: ${value}`);
    // });

    if (analysisResult.mckinsey_image) {
      await pdf.addImage(
        analysisResult.mckinsey_image,
        "7. MCKINSEY 7S MODEL",
        100,
        100
      );
    }
  }

  // PESTEL Analysis
  if (analysisResult.pestel_image) {
    // pdf.addText("PESTEL ANALYSIS", 14, "bold");
    await pdf.addImage(
      analysisResult.pestel_image,
      "8. PESTEL Analysis Visualization",
      150,
      130
    );
  }
}
