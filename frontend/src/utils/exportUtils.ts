import type { AnalysisResult } from "../types";

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

// Generate TXT export
export function exportToTxt(analysisResult: AnalysisResult): void {
  const content = generateTextContent(analysisResult);
  const blob = new Blob([content], { type: "text/plain" });
  downloadFile(blob, `${analysisResult.company_name}_analysis.txt`);
}

// Generate Markdown export
export function exportToMarkdown(analysisResult: AnalysisResult): void {
  const content = generateMarkdownContent(analysisResult);
  const blob = new Blob([content], { type: "text/markdown" });
  downloadFile(blob, `${analysisResult.company_name}_analysis.md`);
}

// Generate PDF export with images and formatted content
export async function exportToPdf(
  analysisResult: AnalysisResult
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
        const imageHeight = Math.min(maxHeight, 80); // Reasonable height for PDF
        checkPageBreak(imageHeight + 20);

        // Add image title
        pdf.setFontSize(12);
        pdf.setFont(undefined, "bold");
        pdf.text(title, 20, yPosition);
        yPosition += 15;

        // Calculate image position to center it
        const imageX = (pageWidth - maxWidth) / 2;

        // Add the image
        pdf.addImage(
          `data:image/png;base64,${base64Data}`,
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
    pdf.setFontSize(20);
    pdf.setFont(undefined, "bold");
    pdf.text(`Business Analysis Report`, 20, yPosition);
    yPosition += 15;

    pdf.setFontSize(16);
    pdf.text(`${analysisResult.company_name}`, 20, yPosition);
    yPosition += 10;

    pdf.setFontSize(10);
    pdf.setFont(undefined, "normal");
    pdf.text(`Generated on: ${new Date().toLocaleDateString()}`, 20, yPosition);
    yPosition += 20;

    // Table of Contents
    addText("TABLE OF CONTENTS", 14, "bold");
    let tocIndex = 1;
    if (analysisResult.summaries?.length)
      addText(`${tocIndex++}. Competitive Intelligence`);
    if (analysisResult.strategy_recommendations)
      addText(`${tocIndex++}. Differentiation Strategy`);
    if (analysisResult.linkedin_analysis)
      addText(`${tocIndex++}. LinkedIn Intelligence`);
    if (analysisResult.swot_lists) addText(`${tocIndex++}. SWOT Analysis`);
    if (analysisResult.porter_forces)
      addText(`${tocIndex++}. Porter's Five Forces`);
    if (analysisResult.bcg_matrix) addText(`${tocIndex++}. BCG Matrix`);
    if (analysisResult.mckinsey_7s) addText(`${tocIndex++}. McKinsey 7S Model`);

    yPosition += 10;

    // 1. Competitive Intelligence
    if (analysisResult.summaries && analysisResult.summaries.length > 0) {
      addText("1. COMPETITIVE INTELLIGENCE", 14, "bold");
      analysisResult.summaries.forEach((summary, index) => {
        addText(`Analysis ${index + 1}:`, 12, "bold");
        addText(cleanHtmlContent(summary));
        if (analysisResult.sources && analysisResult.sources[index]) {
          addText(`Source: ${analysisResult.sources[index]}`, 9);
        }
      });
    }

    // 2. Differentiation Strategy
    if (analysisResult.strategy_recommendations) {
      addText("2. DIFFERENTIATION STRATEGY", 14, "bold");
      addText(cleanHtmlContent(analysisResult.strategy_recommendations));
    }

    // 3. LinkedIn Intelligence
    if (analysisResult.linkedin_analysis) {
      addText("3. LINKEDIN INTELLIGENCE", 14, "bold");
      addText(cleanHtmlContent(analysisResult.linkedin_analysis));
    }

    // 4. SWOT Analysis
    if (analysisResult.swot_lists) {
      addText("4. SWOT ANALYSIS", 14, "bold");

      addText("STRENGTHS:", 12, "bold");
      analysisResult.swot_lists.strengths.forEach((item) =>
        addText(`• ${item}`)
      );

      addText("WEAKNESSES:", 12, "bold");
      analysisResult.swot_lists.weaknesses.forEach((item) =>
        addText(`• ${item}`)
      );

      addText("OPPORTUNITIES:", 12, "bold");
      analysisResult.swot_lists.opportunities.forEach((item) =>
        addText(`• ${item}`)
      );

      addText("THREATS:", 12, "bold");
      analysisResult.swot_lists.threats.forEach((item) => addText(`• ${item}`));

      // Add SWOT visualization if available
      if (analysisResult.swot_image) {
        addImage(analysisResult.swot_image, "SWOT Analysis Visualization");
      }
    }

    // 5. Porter's Five Forces
    if (analysisResult.porter_forces) {
      addText("5. PORTER'S FIVE FORCES", 14, "bold");

      Object.entries(analysisResult.porter_forces).forEach(([force, items]) => {
        addText(`${force.toUpperCase().replace("_", " ")}:`, 12, "bold");
        items.forEach((item) => addText(`• ${item}`));
      });

      // Add Porter's Five Forces visualization if available
      if (analysisResult.porter_image) {
        addImage(
          analysisResult.porter_image,
          "Porter's Five Forces Visualization"
        );
      }
    }

    // 6. BCG Matrix
    if (analysisResult.bcg_matrix) {
      addText("6. BCG MATRIX", 14, "bold");

      Object.entries(analysisResult.bcg_matrix).forEach(([product, values]) => {
        addText(`${product}:`, 12, "bold");
        addText(`  Market Share: ${values.market_share}`);
        addText(`  Growth Rate: ${values.growth_rate}%`);
      });

      // Add BCG Matrix visualization if available
      if (analysisResult.bcg_image) {
        addImage(analysisResult.bcg_image, "BCG Matrix Visualization");
      }
    }

    // 7. McKinsey 7S
    if (analysisResult.mckinsey_7s) {
      addText("7. MCKINSEY 7S MODEL", 14, "bold");

      Object.entries(analysisResult.mckinsey_7s).forEach(([element, value]) => {
        addText(`${element.toUpperCase().replace("_", " ")}: ${value}`);
      });

      // Add McKinsey 7S visualization if available
      if (analysisResult.mckinsey_image) {
        addImage(
          analysisResult.mckinsey_image,
          "McKinsey 7S Model Visualization"
        );
      }
    }

    // Add PESTEL Analysis if available
    if (analysisResult.pestel_image) {
      addText("PESTEL ANALYSIS", 14, "bold");
      addImage(analysisResult.pestel_image, "PESTEL Analysis Visualization");
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

    pdf.save(`${analysisResult.company_name}_analysis.pdf`);
  } catch (error) {
    console.error("Failed to generate PDF:", error);
    // Fallback to text export
    exportToTxt(analysisResult);
  }
}

// Generate text content for exports
function generateTextContent(analysisResult: AnalysisResult): string {
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
  }

  // Porter's Five Forces
  if (analysisResult.porter_forces) {
    sections.push("5. PORTER'S FIVE FORCES");
    sections.push("-".repeat(50));
    Object.entries(analysisResult.porter_forces).forEach(([force, items]) => {
      sections.push(`${force.toUpperCase().replace("_", " ")}:`);
      items.forEach((item) => sections.push(`• ${item}`));
      sections.push("");
    });
  }

  // BCG Matrix
  if (analysisResult.bcg_matrix) {
    sections.push("6. BCG MATRIX");
    sections.push("-".repeat(50));
    Object.entries(analysisResult.bcg_matrix).forEach(([product, values]) => {
      sections.push(`${product}:`);
      sections.push(`  Market Share: ${values.market_share}`);
      sections.push(`  Growth Rate: ${values.growth_rate}%`);
      sections.push("");
    });
  }

  // McKinsey 7S
  if (analysisResult.mckinsey_7s) {
    sections.push("7. MCKINSEY 7S MODEL");
    sections.push("-".repeat(50));
    Object.entries(analysisResult.mckinsey_7s).forEach(([element, value]) => {
      sections.push(`${element.toUpperCase().replace("_", " ")}: ${value}`);
    });
    sections.push("");
  }

  sections.push("=".repeat(80));
  sections.push("END OF REPORT");
  sections.push("=".repeat(80));

  return sections.join("\n");
}

// Generate Markdown content
function generateMarkdownContent(analysisResult: AnalysisResult): string {
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

  sections.push("---");
  sections.push(
    "*This report was generated by InsightFlow - AI-Powered Competitive Intelligence*"
  );

  return sections.join("\n");
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
