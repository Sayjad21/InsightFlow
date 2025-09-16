import type { AnalysisResult } from "../../types";
import { cleanHtmlContent } from "../helpers/htmlCleanup";

// Generate text content for exports
export function generateTextContent(analysisResult: AnalysisResult): string {
  const sections: string[] = [];

  // Header
  sections.push("=".repeat(80));
  sections.push(
    `BUSINESS ANALYSIS REPORT: ${analysisResult.company_name.toUpperCase()}`
  );
  sections.push(`Generated on: ${new Date().toLocaleDateString()}`);
  sections.push("=".repeat(80));
  sections.push("");

  // Competitive Monitoring
  if (analysisResult.summaries && analysisResult.summaries.length > 0) {
    sections.push("1. COMPETITIVE INTELLIGENCE");
    sections.push("-".repeat(50));
    analysisResult.summaries.forEach((summary, index) => {
      sections.push(`\nCompetitive Analysis ${index + 1}:`);
      sections.push(cleanHtmlContent(summary));
      if (analysisResult.sources && analysisResult.sources[index]) {
        sections.push(`Source: ${analysisResult.sources[index]}`);
      }
    });
    sections.push("");
  }

  // Differentiation Strategy
  if (analysisResult.strategy_recommendations) {
    sections.push("2. DIFFERENTIATION STRATEGY");
    sections.push("-".repeat(50));
    sections.push(cleanHtmlContent(analysisResult.strategy_recommendations));
    sections.push("");
  }

  // LinkedIn Analysis
  if (analysisResult.linkedin_analysis) {
    sections.push("3. LINKEDIN INTELLIGENCE");
    sections.push("-".repeat(50));
    sections.push(cleanHtmlContent(analysisResult.linkedin_analysis));
    sections.push("");
  }

  // SWOT Analysis
  if (analysisResult.swot_lists) {
    sections.push("4. SWOT ANALYSIS");
    sections.push("-".repeat(50));
    sections.push("STRENGTHS:");
    analysisResult.swot_lists.strengths.forEach((item) =>
      sections.push(`• ${item}`)
    );
    sections.push("\nWEAKNESSES:");
    analysisResult.swot_lists.weaknesses.forEach((item) =>
      sections.push(`• ${item}`)
    );
    sections.push("\nOPPORTUNITIES:");
    analysisResult.swot_lists.opportunities.forEach((item) =>
      sections.push(`• ${item}`)
    );
    sections.push("\nTHREATS:");
    analysisResult.swot_lists.threats.forEach((item) =>
      sections.push(`• ${item}`)
    );
    sections.push("");

    // Add SWOT visualization
    if (analysisResult.swot_image) {
      sections.push(
        `[SWOT Analysis Visualization: ${analysisResult.swot_image}]`
      );
      sections.push("");
    }
  }

  // PESTEL Analysis
  if (analysisResult.pestel_lists) {
    sections.push("5. PESTEL ANALYSIS");
    sections.push("-".repeat(50));
    sections.push("POLITICAL:");
    analysisResult.pestel_lists.political.forEach((item) =>
      sections.push(`• ${item}`)
    );
    sections.push("\nECONOMIC:");
    analysisResult.pestel_lists.economic.forEach((item) =>
      sections.push(`• ${item}`)
    );
    sections.push("\nSOCIAL:");
    analysisResult.pestel_lists.social.forEach((item) =>
      sections.push(`• ${item}`)
    );
    sections.push("\nTECHNOLOGICAL:");
    analysisResult.pestel_lists.technological.forEach((item) =>
      sections.push(`• ${item}`)
    );
    sections.push("\nENVIRONMENTAL:");
    analysisResult.pestel_lists.environmental.forEach((item) =>
      sections.push(`• ${item}`)
    );
    sections.push("\nLEGAL:");
    analysisResult.pestel_lists.legal.forEach((item) =>
      sections.push(`• ${item}`)
    );
    sections.push("");

    // Add PESTEL visualization
    if (analysisResult.pestel_image) {
      sections.push(
        `[PESTEL Analysis Visualization: ${analysisResult.pestel_image}]`
      );
      sections.push("");
    }
  }

  // Porter's Five Forces
  if (analysisResult.porter_forces) {
    sections.push("6. PORTER'S FIVE FORCES");
    sections.push("-".repeat(50));
    Object.entries(analysisResult.porter_forces).forEach(([force, items]) => {
      sections.push(`${force.toUpperCase().replace("_", " ")}:`);
      items.forEach((item) => sections.push(`• ${item}`));
      sections.push("");
    });

    // Add Porter visualization
    if (analysisResult.porter_image) {
      sections.push(
        `[Porter's Five Forces Visualization: ${analysisResult.porter_image}]`
      );
      sections.push("");
    }
  }

  // BCG Matrix
  if (analysisResult.bcg_matrix) {
    sections.push("7. BCG MATRIX");
    sections.push("-".repeat(50));
    Object.entries(analysisResult.bcg_matrix).forEach(([product, values]) => {
      sections.push(`${product}:`);
      sections.push(`  Market Share: ${values.market_share}`);
      sections.push(`  Growth Rate: ${values.growth_rate}%`);
      sections.push("");
    });

    // Add BCG visualization
    if (analysisResult.bcg_image) {
      sections.push(`[BCG Matrix Visualization: ${analysisResult.bcg_image}]`);
      sections.push("");
    }
  }

  // McKinsey 7S
  if (analysisResult.mckinsey_7s) {
    sections.push("8. MCKINSEY 7S MODEL");
    sections.push("-".repeat(50));
    Object.entries(analysisResult.mckinsey_7s).forEach(([element, value]) => {
      sections.push(`${element.toUpperCase().replace("_", " ")}: ${value}`);
    });
    sections.push("");

    // Add McKinsey visualization
    if (analysisResult.mckinsey_image) {
      sections.push(
        `[McKinsey 7S Model Visualization: ${analysisResult.mckinsey_image}]`
      );
      sections.push("");
    }
  }

  sections.push("=".repeat(80));
  sections.push("END OF REPORT");
  sections.push("=".repeat(80));

  return sections.join("\n");
}
