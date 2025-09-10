import { useState } from "react";
import { ApiService } from "../services/api.js";
import { useAuth } from "../contexts/AuthContext.js";
import type { AnalysisResult } from "../types";

export const useAnalysis = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { isAuthenticated, handleAuthError } = useAuth();

  const analyzeCompany = async (
    companyName: string,
    file?: File,
    saveToHistory?: boolean
  ): Promise<AnalysisResult | null> => {
    setIsLoading(true);
    setError(null);

    try {
      // Automatically save to history if user is authenticated and not explicitly disabled
      const shouldSaveToHistory =
        saveToHistory !== undefined ? saveToHistory : isAuthenticated;

      const result = await ApiService.analyzeCompany(
        companyName,
        file,
        shouldSaveToHistory
      );

      return result;
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : "Analysis failed";

      // Check if it's a 401 authentication error
      if (
        errorMessage.includes("401") ||
        errorMessage.includes("Unauthorized")
      ) {
        console.log("Authentication error during analysis, logging out user");
        handleAuthError();
      }

      setError(errorMessage);
      console.error("Analysis error:", err);
      return null;
    } finally {
      setIsLoading(false);
    }
  };

  const generateCompanyFile = async (
    companyName: string,
    file?: File
  ): Promise<boolean> => {
    setIsLoading(true);
    setError(null);

    try {
      await ApiService.generateCompanyFile(companyName, file);
      return true;
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : "File generation failed";
      setError(errorMessage);
      console.error("File generation error:", err);
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  return {
    analyzeCompany,
    generateCompanyFile,
    isLoading,
    error,
    clearError: () => setError(null),
  };
};
