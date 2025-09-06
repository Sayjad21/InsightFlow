import type { AnalysisResult } from "../types";

const API_BASE_URL = "http://localhost:8000";

export class ApiService {
  /**
   * Analyze a company using the competitive analysis endpoint
   * @param companyName - Name of the company to analyze
   * @param file - Optional file upload for additional context
   * @returns Promise with analysis results
   */
  static async analyzeCompany(
    companyName: string,
    file?: File
  ): Promise<AnalysisResult> {
    try {
      const formData = new FormData();
      formData.append("company_name", companyName);

      if (file) {
        formData.append("file", file);
      }

      const response = await fetch(`${API_BASE_URL}/api/analyze`, {
        method: "POST",
        body: formData,
        // Don't set Content-Type for FormData - let browser set it with boundary
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`API Error: ${response.status} - ${errorText}`);
      }

      const data = await response.json();
      return data as AnalysisResult;
    } catch (error) {
      console.error("API Error:", error);
      if (error instanceof TypeError && error.message.includes("fetch")) {
        throw new Error(
          "Network error: Unable to connect to server. Make sure the backend is running on http://localhost:8080"
        );
      }
      throw error;
    }
  }

  /**
   * Test RAG query endpoint
   * @param query - The query string
   * @param context - Context for the query
   * @returns Promise with RAG results
   */
  static async queryRAG(query: string, context: string): Promise<any> {
    const response = await fetch(`${API_BASE_URL}/api/rag/analyze`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        query,
        companyName: context, // Based on your RagController
      }),
    });

    if (!response.ok) {
      throw new Error(
        `RAG API Error: ${response.status} - ${response.statusText}`
      );
    }

    return response.json();
  }

  /**
   * Generate and download company analysis text file
   * @param companyName - Name of the company to analyze
   * @param file - Optional file for differentiation analysis
   * @returns Promise that resolves when download starts
   */
  static async generateCompanyFile(
    companyName: string,
    file?: File
  ): Promise<void> {
    try {
      const formData = new FormData();
      formData.append("company_name", companyName);

      if (file) {
        formData.append("file", file);
      }

      const response = await fetch(
        `${API_BASE_URL}/api/generate-company-file`,
        {
          method: "POST",
          body: formData,
          // Don't set Content-Type for FormData - let browser set it with boundary
        }
      );

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`API Error: ${response.status} - ${errorText}`);
      }

      // Get the file content as blob
      const blob = await response.blob();

      // Create download link
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;

      // Get filename from response headers or create one
      const contentDisposition = response.headers.get("content-disposition");
      let filename = `${companyName.replace(
        /[^a-zA-Z0-9-_]/g,
        "_"
      )}_analysis.txt`;
      if (contentDisposition) {
        const filenameMatch = contentDisposition.match(
          /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/
        );
        if (filenameMatch && filenameMatch[1]) {
          filename = filenameMatch[1].replace(/['"]/g, "");
        }
      }

      link.download = filename;

      // Trigger download
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);

      // Clean up
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error("File Generation Error:", error);
      if (error instanceof TypeError && error.message.includes("fetch")) {
        throw new Error(
          "Network error: Unable to connect to server. Make sure the backend is running on http://localhost:8080"
        );
      }
      throw error;
    }
  }
}
