import type { ComparisonResult } from "../../types";

// Helper function to extract company names from comparison data
export function getCompanyNames(comparison: ComparisonResult): string[] {
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