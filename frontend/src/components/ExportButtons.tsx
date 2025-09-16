import React, { useState } from "react";
import { Download, FileText, FileDown, Loader2 } from "lucide-react";
import {
  exportToTxt,
  exportToMarkdown,
  exportToPdf,
} from "../utils/analysisExport/analysisExportUtils";
import type { AnalysisResult } from "../types";

interface ExportButtonsProps {
  analysisResult: AnalysisResult;
}

const ExportButtons: React.FC<ExportButtonsProps> = ({ analysisResult }) => {
  const [exportingType, setExportingType] = useState<
    "txt" | "markdown" | "pdf" | null
  >(null);

  const handleTxtExport = async () => {
    try {
      setExportingType("txt");
      exportToTxt(analysisResult);
    } catch (error) {
      console.error("Export to TXT failed:", error);
    } finally {
      setExportingType(null);
    }
  };

  const handleMarkdownExport = async () => {
    try {
      setExportingType("markdown");
      exportToMarkdown(analysisResult);
    } catch (error) {
      console.error("Export to Markdown failed:", error);
    } finally {
      setExportingType(null);
    }
  };

  const handlePdfExport = async () => {
    try {
      setExportingType("pdf");
      await exportToPdf(analysisResult);
    } catch (error) {
      console.error("Export to PDF failed:", error);
    } finally {
      setExportingType(null);
    }
  };

  return (
    <div className="border-t pt-4 mt-4">
      <h4 className="text-sm font-semibold text-gray-700 mb-3 flex items-center">
        <Download className="w-4 h-4 mr-2" />
        Export Analysis
      </h4>
      <div className="flex flex-wrap gap-2">
        <button
          onClick={handleTxtExport}
          disabled={exportingType === "txt"}
          className="p-2 text-gray-500 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
          title="Export as TXT"
        >
          {exportingType === "txt" ? (
            <Loader2 className="h-4 w-4 animate-spin" />
          ) : (
            <FileText className="h-4 w-4" />
          )}
        </button>

        <button
          onClick={handleMarkdownExport}
          disabled={exportingType === "markdown"}
          className="p-2 text-gray-500 hover:text-green-600 hover:bg-green-50 rounded-lg transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
          title="Export as Markdown"
        >
          {exportingType === "markdown" ? (
            <Loader2 className="h-4 w-4 animate-spin" />
          ) : (
            <FileDown className="h-4 w-4" />
          )}
        </button>

        <button
          onClick={handlePdfExport}
          disabled={exportingType === "pdf"}
          className="p-2 text-gray-500 hover:text-red-600 hover:bg-red-50 rounded-lg transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
          title="Export as PDF"
        >
          {exportingType === "pdf" ? (
            <Loader2 className="h-4 w-4 animate-spin" />
          ) : (
            <Download className="h-4 w-4" />
          )}
        </button>
      </div>
      <p className="text-xs text-gray-500 mt-2">
        Choose your preferred format to download the complete analysis report.
      </p>
    </div>
  );
};

export default ExportButtons;
