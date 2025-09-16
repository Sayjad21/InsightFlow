import type { AnalysisResult } from "../../types";
import { cleanHtmlContent } from "../helpers/htmlCleanup";

// Generate Markdown content
export function generateMarkdownContent(
  analysisResult: AnalysisResult
): string {
  const sections: string[] = [];

  // Header
  sections.push(`# Business Analysis Report: ${analysisResult.company_name}`);
  sections.push(`*Generated on: ${new Date().toLocaleDateString()}*`);
  sections.push("");

  // Table of Contents
  sections.push("## Table of Contents");
  let tocIndex = 1;
  if (analysisResult.summaries?.length)
    sections.push(
      `${tocIndex++}. [Competitive Intelligence](#competitive-intelligence)`
    );
  if (analysisResult.strategy_recommendations)
    sections.push(
      `${tocIndex++}. [Differentiation Strategy](#differentiation-strategy)`
    );
  if (analysisResult.linkedin_analysis)
    sections.push(
      `${tocIndex++}. [LinkedIn Intelligence](#linkedin-intelligence)`
    );
  if (analysisResult.swot_lists)
    sections.push(`${tocIndex++}. [SWOT Analysis](#swot-analysis)`);
  if (analysisResult.pestel_lists)
    sections.push(`${tocIndex++}. [PESTEL Analysis](#pestel-analysis)`);
  if (analysisResult.porter_forces)
    sections.push(
      `${tocIndex++}. [Porter's Five Forces](#porters-five-forces)`
    );
  if (analysisResult.bcg_matrix)
    sections.push(`${tocIndex++}. [BCG Matrix](#bcg-matrix)`);
  if (analysisResult.mckinsey_7s)
    sections.push(`${tocIndex++}. [McKinsey 7S Model](#mckinsey-7s-model)`);
  sections.push("");

  // Competitive Intelligence
  if (analysisResult.summaries && analysisResult.summaries.length > 0) {
    sections.push("## Competitive Intelligence");
    sections.push('<a id="competitive-intelligence"></a>');
    analysisResult.summaries.forEach((summary, index) => {
      sections.push(`### Analysis ${index + 1}`);
      sections.push(
        cleanHtmlContent(summary).replace(/\*\*(.*?)\*\*/g, "**$1**")
      );
      if (analysisResult.sources && analysisResult.sources[index]) {
        sections.push(
          `\n**Source:** [${analysisResult.sources[index]}](${analysisResult.sources[index]})`
        );
      }
      sections.push("");
    });
  }

  // Differentiation Strategy
  if (analysisResult.strategy_recommendations) {
    sections.push("## Differentiation Strategy");
    sections.push('<a id="differentiation-strategy"></a>');
    sections.push(
      cleanHtmlContent(analysisResult.strategy_recommendations).replace(
        /\*\*(.*?)\*\*/g,
        "**$1**"
      )
    );
    sections.push("");
  }

  // LinkedIn Intelligence
  if (analysisResult.linkedin_analysis) {
    sections.push("## LinkedIn Intelligence");
    sections.push('<a id="linkedin-intelligence"></a>');
    sections.push(
      cleanHtmlContent(analysisResult.linkedin_analysis).replace(
        /\*\*(.*?)\*\*/g,
        "**$1**"
      )
    );
    sections.push("");
  }

  // SWOT Analysis
  if (analysisResult.swot_lists) {
    sections.push("## SWOT Analysis");
    sections.push('<a id="swot-analysis"></a>');
    sections.push("### Strengths");
    analysisResult.swot_lists.strengths.forEach((item) =>
      sections.push(`- ${item}`)
    );
    sections.push("\n### Weaknesses");
    analysisResult.swot_lists.weaknesses.forEach((item) =>
      sections.push(`- ${item}`)
    );
    sections.push("\n### Opportunities");
    analysisResult.swot_lists.opportunities.forEach((item) =>
      sections.push(`- ${item}`)
    );
    sections.push("\n### Threats");
    analysisResult.swot_lists.threats.forEach((item) =>
      sections.push(`- ${item}`)
    );
    sections.push("");
  }

  // SWOT Visualization
  if (analysisResult.swot_image) {
    sections.push("### SWOT Analysis Visualization");
    sections.push(`![SWOT Analysis](${analysisResult.swot_image})`);
    sections.push("");
  }

  // PESTEL Analysis
  if (analysisResult.pestel_lists) {
    sections.push("## PESTEL Analysis");
    sections.push('<a id="pestel-analysis"></a>');
    sections.push("### Political");
    analysisResult.pestel_lists.political.forEach((item) =>
      sections.push(`- ${item}`)
    );
    sections.push("\n### Economic");
    analysisResult.pestel_lists.economic.forEach((item) =>
      sections.push(`- ${item}`)
    );
    sections.push("\n### Social");
    analysisResult.pestel_lists.social.forEach((item) =>
      sections.push(`- ${item}`)
    );
    sections.push("\n### Technological");
    analysisResult.pestel_lists.technological.forEach((item) =>
      sections.push(`- ${item}`)
    );
    sections.push("\n### Environmental");
    analysisResult.pestel_lists.environmental.forEach((item) =>
      sections.push(`- ${item}`)
    );
    sections.push("\n### Legal");
    analysisResult.pestel_lists.legal.forEach((item) =>
      sections.push(`- ${item}`)
    );
    sections.push("");
  }

  // PESTEL Visualization
  if (analysisResult.pestel_image) {
    sections.push("### PESTEL Analysis Visualization");
    sections.push(`![PESTEL Analysis](${analysisResult.pestel_image})`);
    sections.push("");
  }

  // Porter's Five Forces
  if (analysisResult.porter_forces) {
    sections.push("## Porter's Five Forces");
    sections.push('<a id="porters-five-forces"></a>');
    Object.entries(analysisResult.porter_forces).forEach(([force, items]) => {
      sections.push(
        `### ${force
          .replace("_", " ")
          .replace(/\b\w/g, (l) => l.toUpperCase())}`
      );
      items.forEach((item) => sections.push(`- ${item}`));
      sections.push("");
    });
  }

  // Porter Visualization
  if (analysisResult.porter_image) {
    sections.push("### Porter's Five Forces Visualization");
    sections.push(`![Porter's Five Forces](${analysisResult.porter_image})`);
    sections.push("");
  }

  // BCG Matrix
  if (analysisResult.bcg_matrix) {
    sections.push("## BCG Matrix");
    sections.push('<a id="bcg-matrix"></a>');
    sections.push("| Product/Service | Market Share | Growth Rate |");
    sections.push("|----------------|--------------|-------------|");
    Object.entries(analysisResult.bcg_matrix).forEach(([product, values]) => {
      sections.push(
        `| ${product} | ${values.market_share} | ${values.growth_rate}% |`
      );
    });
    sections.push("");
  }

  // BCG Visualization
  if (analysisResult.bcg_image) {
    sections.push("### BCG Matrix Visualization");
    sections.push(`![BCG Matrix](${analysisResult.bcg_image})`);
    sections.push("");
  }

  // McKinsey 7S
  if (analysisResult.mckinsey_7s) {
    sections.push("## McKinsey 7S Model");
    sections.push('<a id="mckinsey-7s-model"></a>');
    Object.entries(analysisResult.mckinsey_7s).forEach(([element, value]) => {
      sections.push(
        `### ${element
          .replace("_", " ")
          .replace(/\b\w/g, (l) => l.toUpperCase())}`
      );
      sections.push(value);
      sections.push("");
    });
  }

  // McKinsey 7S Visualization
  if (analysisResult.mckinsey_image) {
    sections.push("### McKinsey 7S Model Visualization");
    sections.push(`![McKinsey 7S Model](${analysisResult.mckinsey_image})`);
    sections.push("");
  }

  sections.push("---");
  sections.push(
    "*This report was generated by InsightFlow - AI-Powered Competitive Intelligence*"
  );

  return sections.join("\n");
}
