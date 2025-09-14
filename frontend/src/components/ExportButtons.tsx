import React from "react";
import { Download, FileText, FileImage, File } from "lucide-react";
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
  const handleTxtExport = () => {
    exportToTxt(analysisResult);
  };

  const handleMarkdownExport = () => {
    exportToMarkdown(analysisResult);
  };

  const handlePdfExport = async () => {
    await exportToPdf(analysisResult);
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
          className="inline-flex items-center px-3 py-2 text-xs font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-colors"
          title="Export as plain text file"
        >
          <FileText className="w-4 h-4 mr-1.5" />
          TXT
        </button>

        <button
          onClick={handleMarkdownExport}
          className="inline-flex items-center px-3 py-2 text-xs font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-colors"
          title="Export as Markdown file with formatting"
        >
          <File className="w-4 h-4 mr-1.5" />
          Markdown
        </button>

        <button
          onClick={handlePdfExport}
          className="inline-flex items-center px-3 py-2 text-xs font-medium text-white bg-indigo-600 border border-transparent rounded-md hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 transition-colors"
          title="Export as PDF with professional formatting"
        >
          <FileImage className="w-4 h-4 mr-1.5" />
          PDF
        </button>
      </div>
      <p className="text-xs text-gray-500 mt-2">
        Choose your preferred format to download the complete analysis report.
      </p>
    </div>
  );
};

export default ExportButtons;
