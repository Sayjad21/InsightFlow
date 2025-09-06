import React, { useState } from "react";
import { ApiService } from "../services/api";
import type { AnalysisResult } from "../types";

interface CompanyAnalysisProps {
  isVisible: boolean;
  onAnalysisComplete?: (result: AnalysisResult | null) => void;
}

const CompanyAnalysis: React.FC<CompanyAnalysisProps> = ({
  isVisible,
  onAnalysisComplete,
}) => {
  const [companyName, setCompanyName] = useState("");
  const [file, setFile] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);
  const [fileGenerating, setFileGenerating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!companyName.trim()) return;

    setLoading(true);
    setError(null);

    try {
      const result = await ApiService.analyzeCompany(
        companyName,
        file || undefined
      );
      if (onAnalysisComplete) {
        onAnalysisComplete(result);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "An error occurred");
      if (onAnalysisComplete) {
        onAnalysisComplete(null);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setFile(e.target.files[0]);
    }
  };

  const handleGenerateFile = async () => {
    if (!companyName.trim()) {
      setError("Please enter a company name to generate analysis file");
      return;
    }

    setFileGenerating(true);
    setError(null);

    try {
      await ApiService.generateCompanyFile(companyName, file || undefined);
      // Success message could be added here if needed
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to generate file");
    } finally {
      setFileGenerating(false);
    }
  };

  if (!isVisible) return null;

  return (
    <div className="mt-8 space-y-6">
      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="flex flex-col gap-2">
          <label htmlFor="file-upload" className="text-black/80">
            Upload .txt Document (Optional - enables differentiation analysis)
          </label>
          <div className="relative">
            <input
              id="file-upload"
              type="file"
              onChange={handleFileChange}
              className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
              accept=".txt"
            />
            <div className="px-4 py-2 rounded-lg bg-white/10 text-black border border-white/20 flex items-center">
              <span className="bg-blue-500 text-black px-4 py-2 rounded-lg mr-4 text-sm font-semibold hover:bg-blue-600">
                Choose File
              </span>
              <span className="text-black-400">
                {file
                  ? `${file.name} (Differentiation analysis enabled)`
                  : "No file selected (Basic analysis)"}
              </span>
            </div>
          </div>
        </div>

        <div className="flex gap-4">
          <input
            type="text"
            value={companyName}
            onChange={(e) => setCompanyName(e.target.value)}
            placeholder="Enter company name..."
            className="flex-1 px-4 py-2 rounded-lg bg-white/10 text-black placeholder-black-400 border border-black focus:outline-none focus:border-blue-500"
          />
          <button
            type="submit"
            disabled={loading || fileGenerating}
            className="px-6 py-2 bg-gradient-to-r from-blue-500 to-blue-600 text-black rounded-lg hover:from-blue-600 hover:to-blue-700 disabled:opacity-50"
          >
            {loading ? "Analyzing..." : "Analyze"}
          </button>
        </div>

        {/* Additional Action Buttons */}
        <div className="flex flex-col sm:flex-row gap-4 pt-4 border-t border-white/20">
          <div className="flex-1">
            <h3 className="text-black/80 mb-2 font-medium">
              Generate Company Analysis File
            </h3>
            <p className="text-black/60 text-sm mb-3">
              Create a comprehensive analysis text file that can be uploaded
              later for comparison with other companies.
              {file
                ? " Including differentiation strategies based on uploaded comparison file."
                : " Upload a file above to enable differentiation analysis."}
            </p>
            <button
              type="button"
              onClick={handleGenerateFile}
              disabled={loading || fileGenerating || !companyName.trim()}
              className="px-6 py-2 bg-gradient-to-r from-green-500 to-green-600 text-white rounded-lg hover:from-green-600 hover:to-green-700 disabled:opacity-50 text-sm font-medium"
            >
              {fileGenerating
                ? "Generating..."
                : file
                ? "📄 Generate Differentiation Analysis File"
                : "📄 Generate Basic Analysis File"}
            </button>
          </div>
        </div>
      </form>

      {error && (
        <div className="p-4 bg-red-500/20 border border-red-500 rounded-lg text-red-200">
          {error}
        </div>
      )}
    </div>
  );
};

export default CompanyAnalysis;
