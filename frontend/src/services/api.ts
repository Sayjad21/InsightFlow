import type { AnalysisResult, UserAnalysis } from "../types";

const API_BASE_URL = "http://localhost:8000";

// Log the API base URL for debugging
console.log("API Base URL:", API_BASE_URL);

// Authentication and User interfaces
export interface SignupRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  message: string;
}

export interface UserProfileResponse {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  avatar: string;
  role: string;
  createdAt: string;
  lastLogin: string;
  totalAnalyses: number;
  successfulAnalyses: number;
}

export interface UserAnalysesResponse {
  analyses: UserAnalysis[];
  total: number;
}

export class ApiService {
  // Server connectivity test
  static async testConnection(): Promise<boolean> {
    try {
      console.log("Testing connection to:", `${API_BASE_URL}/api/signup`);
      const response = await fetch(`${API_BASE_URL}/api/signup`, {
        method: "OPTIONS", // Use OPTIONS to test CORS
        headers: {
          Origin: window.location.origin,
          "Access-Control-Request-Method": "POST",
          "Access-Control-Request-Headers": "Content-Type",
        },
      });
      console.log("Connection test response:", response.status);
      return response.status < 400;
    } catch (error) {
      console.error("Connection test failed:", error);
      return false;
    }
  }
  // Token management
  private static getAuthToken(): string | null {
    return localStorage.getItem("authToken");
  }

  private static setAuthToken(token: string): void {
    console.log("Setting auth token:", token ? "Token received" : "No token");
    localStorage.setItem("authToken", token);
    console.log(
      "Token saved to localStorage:",
      localStorage.getItem("authToken") ? "Success" : "Failed"
    );
  }

  private static removeAuthToken(): void {
    console.log("Removing auth token");
    localStorage.removeItem("authToken");
  }

  private static getAuthHeaders(): HeadersInit {
    const token = this.getAuthToken();
    console.log("Getting auth headers, token:", token ? "Present" : "Missing");
    const headers = {
      "Content-Type": "application/json",
      ...(token && { Authorization: `Bearer ${token}` }),
    };
    console.log("Auth headers:", headers);
    return headers;
  }

  // Authentication methods
  /**
   * Sign up a new user
   */
  static async signup(userData: SignupRequest): Promise<AuthResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/api/signup`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(userData),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || `Signup failed: ${response.status}`);
      }

      const data: AuthResponse = await response.json();
      console.log("Signup response data:", data);
      console.log("Token from signup:", data.token ? "Received" : "Missing");
      this.setAuthToken(data.token);
      return data;
    } catch (error) {
      console.error("Signup Error:", error);
      if (
        error instanceof TypeError &&
        error.message.includes("NetworkError")
      ) {
        throw new Error(
          "Unable to connect to server. Please ensure the backend is running on http://localhost:8000"
        );
      }
      throw error;
    }
  }

  /**
   * Login user
   */
  static async login(credentials: LoginRequest): Promise<AuthResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/api/login`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(credentials),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || `Login failed: ${response.status}`);
      }

      const data: AuthResponse = await response.json();
      console.log("Login response data:", data);
      console.log("Token from login:", data.token ? "Received" : "Missing");
      this.setAuthToken(data.token);
      return data;
    } catch (error) {
      console.error("Login Error:", error);
      if (
        error instanceof TypeError &&
        error.message.includes("NetworkError")
      ) {
        throw new Error(
          "Unable to connect to server. Please ensure the backend is running on http://localhost:8000"
        );
      }
      throw error;
    }
  }

  /**
   * Logout user (clear local token)
   */
  static logout(): void {
    this.removeAuthToken();
  }

  /**
   * Check if user is authenticated
   */
  static isAuthenticated(): boolean {
    const token = this.getAuthToken();
    console.log("Checking authentication, token exists:", token ? "Yes" : "No");
    if (!token) return false;

    try {
      // Basic check - decode JWT payload to check expiration
      const payload = JSON.parse(atob(token.split(".")[1]));
      const currentTime = Date.now() / 1000;
      const isValid = payload.exp > currentTime;
      console.log("Token payload:", payload);
      console.log("Token expires at:", new Date(payload.exp * 1000));
      console.log("Current time:", new Date());
      console.log("Token is valid:", isValid);

      if (!isValid) {
        console.log("Token expired, removing from storage");
        this.removeAuthToken();
      }

      return isValid;
    } catch (error) {
      console.log("Token validation error:", error);
      console.log("Removing invalid token from storage");
      this.removeAuthToken();
      return false;
    }
  }

  /**
   * Clear all authentication data - useful when JWT secret changes
   */
  static clearAuthData(): void {
    console.log("Clearing all authentication data");
    this.removeAuthToken();
  } // Debug method - call this in browser console to check token status
  static debugAuth(): void {
    const token = this.getAuthToken();
    console.log("=== AUTH DEBUG ===");
    console.log("Token in localStorage:", token);
    console.log("Is authenticated:", this.isAuthenticated());
    console.log("Auth headers:", this.getAuthHeaders());
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split(".")[1]));
        console.log("Token payload:", payload);
        console.log("Token subject (username):", payload.sub);
        console.log("Token expires:", new Date(payload.exp * 1000));
      } catch (error) {
        console.log("Error decoding token:", error);
      }
    }
    console.log("================");
  }

  // User profile methods
  /**
   * Get current user profile
   */
  static async getUserProfile(): Promise<UserProfileResponse> {
    try {
      const token = this.getAuthToken();
      console.log(
        "Getting user profile with token:",
        token ? "Present" : "Missing"
      );
      console.log("Making request to:", `${API_BASE_URL}/api/user/profile`);

      const headers = this.getAuthHeaders();
      console.log("Request headers:", headers);

      const response = await fetch(`${API_BASE_URL}/api/user/profile`, {
        method: "GET",
        headers: headers,
      });

      console.log("Profile response status:", response.status);
      console.log("Profile response ok:", response.ok);

      if (!response.ok) {
        if (response.status === 401) {
          this.removeAuthToken();
          throw new Error("Authentication expired. Please login again.");
        }
        const errorData = await response
          .json()
          .catch(() => ({ error: "Unknown error" }));
        throw new Error(
          errorData.error || `Failed to get profile: ${response.status}`
        );
      }

      return await response.json();
    } catch (error) {
      console.error("Get Profile Error:", error);

      if (error instanceof Error) {
        console.error("Error details:", {
          name: error.name,
          message: error.message,
          stack: error.stack,
        });
      }

      if (
        error instanceof TypeError &&
        error.message.includes("NetworkError")
      ) {
        throw new Error(
          "Unable to connect to server. Please ensure the backend is running on http://localhost:8000 and CORS is properly configured."
        );
      }
      throw error;
    }
  }

  /**
   * Get user's analysis history
   */
  static async getUserAnalyses(): Promise<UserAnalysesResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/api/user/analyses`, {
        method: "GET",
        headers: this.getAuthHeaders(),
      });

      if (!response.ok) {
        if (response.status === 401) {
          this.removeAuthToken();
          throw new Error("Authentication expired. Please login again.");
        }
        const errorData = await response.json();
        throw new Error(
          errorData.error || `Failed to get analyses: ${response.status}`
        );
      }

      return await response.json();
    } catch (error) {
      console.error("Get Analyses Error:", error);
      throw error;
    }
  }

  // Analysis methods
  /**
   * Save analysis result for authenticated user
   */
  static async saveAnalysis(analysisData: {
    companyName: string;
    analysisResult: AnalysisResult;
    uploadedFileName?: string;
  }): Promise<UserAnalysis> {
    try {
      const response = await fetch(`${API_BASE_URL}/api/user/analyses`, {
        method: "POST",
        headers: this.getAuthHeaders(),
        body: JSON.stringify({
          companyName: analysisData.companyName,
          ...analysisData.analysisResult,
          uploadedFileName: analysisData.uploadedFileName,
        }),
      });

      if (!response.ok) {
        if (response.status === 401) {
          console.log("401 Unauthorized - clearing auth data and logging out");
          this.removeAuthToken();
          throw new Error("Authentication expired. Please login again.");
        }
        const errorData = await response
          .json()
          .catch(() => ({ error: "Unknown error" }));
        throw new Error(
          errorData.error || `Failed to save analysis: ${response.status}`
        );
      }

      return await response.json();
    } catch (error) {
      console.error("Save Analysis Error:", error);
      throw error;
    }
  }

  /**
   * Analyze a company using the competitive analysis endpoint
   * @param companyName - Name of the company to analyze
   * @param file - Optional file upload for additional context
   * @param saveToHistory - Whether to save to user's analysis history (requires auth)
   * @returns Promise with analysis results
   */
  static async analyzeCompany(
    companyName: string,
    file?: File,
    saveToHistory: boolean = false
  ): Promise<AnalysisResult> {
    try {
      const formData = new FormData();
      formData.append("company_name", companyName);

      if (file) {
        formData.append("file", file);
      }

      // Add auth headers if user is authenticated (always include token if available)
      const headers: HeadersInit = {};
      if (this.isAuthenticated()) {
        const authToken = this.getAuthToken();
        console.log(
          "Adding auth token to analyze request:",
          authToken ? "Present" : "Missing"
        );
        if (authToken) {
          headers.Authorization = `Bearer ${authToken}`;
        }
      } else {
        console.log(
          "User not authenticated, no token added to analyze request"
        );
      }

      console.log("Analyze request headers:", headers);

      const response = await fetch(`${API_BASE_URL}/api/analyze`, {
        method: "POST",
        body: formData,
        headers,
        // Don't set Content-Type for FormData - let browser set it with boundary
      });

      console.log("Analyze response status:", response.status);

      if (!response.ok) {
        const errorText = await response.text();
        console.error("Analyze request failed:", {
          status: response.status,
          statusText: response.statusText,
          error: errorText,
          headers: Object.fromEntries(response.headers.entries()),
        });

        if (response.status === 401) {
          console.log("401 Unauthorized in analyze - clearing auth data");
          this.removeAuthToken();
          throw new Error("Authentication expired. Please login again.");
        }

        throw new Error(`API Error: ${response.status} - ${errorText}`);
      }

      const data = await response.json();

      // Save to user history if requested and authenticated
      if (saveToHistory && this.isAuthenticated()) {
        try {
          await this.saveAnalysis({
            companyName,
            analysisResult: data,
            uploadedFileName: file?.name,
          });
        } catch (saveError) {
          console.warn("Failed to save analysis to history:", saveError);
          // Don't fail the whole operation if saving fails
        }
      }

      return data as AnalysisResult;
    } catch (error) {
      console.error("API Error:", error);
      if (error instanceof TypeError && error.message.includes("fetch")) {
        throw new Error(
          "Network error: Unable to connect to server. Make sure the backend is running on http://localhost:8000"
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
    const headers: HeadersInit = {
      "Content-Type": "application/json",
    };

    // Add auth headers if user is authenticated
    if (this.isAuthenticated()) {
      const authToken = this.getAuthToken();
      if (authToken) {
        headers.Authorization = `Bearer ${authToken}`;
      }
    }

    const response = await fetch(`${API_BASE_URL}/api/rag/analyze`, {
      method: "POST",
      headers,
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

      // Add auth headers if user is authenticated
      const headers: HeadersInit = {};
      if (this.isAuthenticated()) {
        const authToken = this.getAuthToken();
        if (authToken) {
          headers.Authorization = `Bearer ${authToken}`;
        }
      }

      const response = await fetch(
        `${API_BASE_URL}/api/generate-company-file`,
        {
          method: "POST",
          body: formData,
          headers,
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
          "Network error: Unable to connect to server. Make sure the backend is running on http://localhost:8000"
        );
      }
      throw error;
    }
  }
}

// Make ApiService available globally for debugging
if (typeof window !== "undefined") {
  (window as any).ApiService = ApiService;
  console.log(
    "ApiService is now available globally. Use ApiService.debugAuth() to check token status."
  );
}
