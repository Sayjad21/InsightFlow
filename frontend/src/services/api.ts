import type { AnalysisResult, UserAnalysis } from "../types";

// Get API base URL from environment variables with fallback
const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8000";

// Log the API base URL for debugging
console.log("API Base URL:", API_BASE_URL);
console.log("Environment:", import.meta.env.MODE);

// Authentication and User interfaces
export interface SignupRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  profileImage?: File;
}

export interface SignupFormData {
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
  bio?: string;
  role: string;
  createdAt: string;
  lastLogin: string;
  totalAnalyses: number;
  successfulAnalyses: number;
  totalComparisons: number;
}

export interface UserAnalysesResponse {
  analyses: UserAnalysis[];
  total: number;
  totalPages: number;
  currentPage: number;
  size: number;
}

// Sentiment Analysis interfaces
export interface CompanyListResponse {
  companies: string[];
}

export interface SentimentTrendData {
  date: string;
  positivePercentage: number;
  negativePercentage: number;
  neutralPercentage: number;
  totalPosts: number;
}

export interface SentimentTrendResponse {
  status: "success" | "insufficient_data" | "error";
  error_type?: "insufficient_data" | "INSUFFICIENT_DATA_POINTS";
  message?: string;
  company?: string;
  days?: number;
  sources?: string[];
  chart_url?: string;
  chart?: string;
  chart_type?: "url" | "base64";
  analysis?: any;
  requested_by?: string;
  data_points_found?: number;
  minimum_required?: number;
}

export interface SentimentComparisonResponse {
  status: "success" | "insufficient_data" | "error";
  error_type?: "insufficient_data" | "INSUFFICIENT_DATA_POINTS";
  message?: string;
  companies?: string[]; // ✅ present in backend
  days?: number; // ✅
  sources?: string[]; // ✅
  chart_url?: string; // ✅
  chart?: string; // ✅ (base64 fallback)
  chart_type?: "url" | "base64"; // ✅
  companies_data?: {
    [key: string]: {
      company_name: string;
      time_period_days: number;
      data_point_count: number;
      time_series: {
        date: string;
        sentiment_score: number;
        risk_rating: number;
        source: string;
      }[];
      source_specific_trends?: {
        [source: string]: {
          average_score: number;
          volatility: number;
          slope: number;
        };
      };
      significant_events?: {
        date: string;
        score: number;
        slope_change: number;
        source: string;
        type: string;
      }[];
      analysis_timestamp: string;
      overall_trends: {
        average_score: number;
        volatility: number;
        slope: number;
      };
    };
  };
  requested_by?: string; // ✅
  companies_requested?: string[]; // only in insufficient_data case
  companies_with_insufficient_data?: string[]; // only in insufficient_data case
  total_data_points_found?: number; // only in insufficient_data case
  minimum_required_per_company?: number; // only in insufficient_data case
}

export interface AddCompanyRequest {
  company: string;
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
      // Check if we have a profile image to upload
      if (userData.profileImage) {
        const formData = new FormData();
        formData.append("firstName", userData.firstName);
        formData.append("lastName", userData.lastName);
        formData.append("email", userData.email);
        formData.append("password", userData.password);
        formData.append("profileImage", userData.profileImage);

        const response = await fetch(`${API_BASE_URL}/api/signup-with-image`, {
          method: "POST",
          body: formData,
        });

        if (!response.ok) {
          const errorData = await response.json();
          throw new Error(
            errorData.error || `Signup failed: ${response.status}`
          );
        }

        const data: AuthResponse = await response.json();
        console.log("Signup response data:", data);
        console.log("Token from signup:", data.token ? "Received" : "Missing");
        this.setAuthToken(data.token);
        return data;
      } else {
        // Regular signup without image
        const response = await fetch(`${API_BASE_URL}/api/signup`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            firstName: userData.firstName,
            lastName: userData.lastName,
            email: userData.email,
            password: userData.password,
          }),
        });

        if (!response.ok) {
          const errorData = await response.json();
          throw new Error(
            errorData.error || `Signup failed: ${response.status}`
          );
        }

        const data: AuthResponse = await response.json();
        console.log("Signup response data:", data);
        console.log("Token from signup:", data.token ? "Received" : "Missing");
        this.setAuthToken(data.token);
        return data;
      }
    } catch (error) {
      console.error("Signup Error:", error);
      if (
        error instanceof TypeError &&
        error.message.includes("NetworkError")
      ) {
        throw new Error(
          `Unable to connect to server. Please ensure the backend is running on {API_BASE_URL}`
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
          `Unable to connect to server. Please ensure the backend is running on {API_BASE_URL}`
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
          `Unable to connect to server. Please ensure the backend is running on {API_BASE_URL}`
        );
      }
      throw error;
    }
  }

  /**
   * Update user profile
   */
  static async updateUserProfile(profileData: {
    firstName?: string;
    lastName?: string;
    bio?: string;
    profileImage?: File;
  }): Promise<UserProfileResponse> {
    try {
      const token = this.getAuthToken();
      if (!token) {
        throw new Error("Authentication required");
      }

      const formData = new FormData();

      if (profileData.firstName !== undefined) {
        formData.append("firstName", profileData.firstName);
      }
      if (profileData.lastName !== undefined) {
        formData.append("lastName", profileData.lastName);
      }
      if (profileData.bio !== undefined) {
        formData.append("bio", profileData.bio);
      }
      if (profileData.profileImage) {
        formData.append("profileImage", profileData.profileImage);
      }

      const response = await fetch(`${API_BASE_URL}/api/user/profile/update`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
        },
        body: formData,
      });

      if (!response.ok) {
        if (response.status === 401) {
          this.removeAuthToken();
          throw new Error("Authentication expired. Please login again.");
        }
        const errorData = await response
          .json()
          .catch(() => ({ error: "Unknown error" }));
        throw new Error(
          errorData.error || `Failed to update profile: ${response.status}`
        );
      }

      return await response.json();
    } catch (error) {
      console.error("Update Profile Error:", error);
      if (
        error instanceof TypeError &&
        error.message.includes("NetworkError")
      ) {
        throw new Error(
          `Unable to connect to server. Please ensure the backend is running on {API_BASE_URL}`
        );
      }
      throw error;
    }
  }

  /**
   * Get user's analysis history with pagination
   */
  static async getUserAnalyses(
    page: number = 0,
    size: number = 10
  ): Promise<UserAnalysesResponse> {
    try {
      const response = await fetch(
        `${API_BASE_URL}/api/user/analyses?page=${page}&size=${size}`,
        {
          method: "GET",
          headers: this.getAuthHeaders(),
        }
      );

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

  /**
   * Delete a user analysis
   */
  static async deleteUserAnalysis(analysisId: string): Promise<void> {
    try {
      const token = this.getAuthToken();
      if (!token) {
        throw new Error("Authentication required");
      }

      const response = await fetch(
        `${API_BASE_URL}/api/user/analyses/${analysisId}`,
        {
          method: "DELETE",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) {
        if (response.status === 401) {
          this.removeAuthToken();
          throw new Error("Authentication expired. Please login again.");
        }
        const errorData = await response
          .json()
          .catch(() => ({ error: "Unknown error" }));
        throw new Error(
          errorData.error || `Failed to delete analysis: ${response.status}`
        );
      }
    } catch (error) {
      console.error("Delete Analysis Error:", error);
      if (
        error instanceof TypeError &&
        error.message.includes("NetworkError")
      ) {
        throw new Error(
          `Unable to connect to server. Please ensure the backend is running on {API_BASE_URL}`
        );
      }
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

  // Comparison methods
  /**
   * Get user's available analyses for comparison selection
   */
  static async getComparisonAnalyses(): Promise<any[]> {
    try {
      console.log("Fetching user analyses for comparison");
      const response = await fetch(`${API_BASE_URL}/api/comparison/analyses`, {
        method: "GET",
        headers: this.getAuthHeaders(),
      });

      if (!response.ok) {
        console.error("Failed to fetch comparison analyses:", response.status);
        throw new Error(`Failed to fetch analyses: ${response.status}`);
      }

      const data = await response.json();
      console.log("Comparison analyses fetched successfully:", data);
      return data;
    } catch (error) {
      console.error("Get Comparison Analyses Error:", error);
      throw error;
    }
  }

  /**
   * Compare existing analyses only
   */
  static async compareExistingAnalyses(analysisIds: string[]): Promise<any> {
    try {
      console.log("Comparing existing analyses:", analysisIds);
      const response = await fetch(
        `${API_BASE_URL}/api/comparison/compare-existing`,
        {
          method: "POST",
          headers: this.getAuthHeaders(),
          body: JSON.stringify({
            analysisIds,
            saveResult: true,
          }),
        }
      );

      if (!response.ok) {
        const errorText = await response.text();
        console.error(
          "Failed to compare existing analyses:",
          response.status,
          errorText
        );
        throw new Error(`Comparison failed: ${response.status}`);
      }

      const result = await response.json();
      console.log("Existing analyses comparison result:", result);
      return result;
    } catch (error) {
      console.error("Compare Existing Analyses Error:", error);
      throw error;
    }
  }

  /**
   * Enhanced comparison (supports mixed existing + new companies)
   */
  static async compareEnhanced(request: {
    analysisIds?: string[];
    companyNames?: string[];
    comparisonType?: string;
    saveNewAnalyses?: boolean;
    saveResult?: boolean;
    files?: (File | null)[];
  }): Promise<any> {
    try {
      // Set saveResult to true by default if not specified
      const requestWithDefaults = {
        ...request,
        saveResult:
          request.saveResult !== undefined ? request.saveResult : true,
      };

      console.log("Enhanced comparison request:", requestWithDefaults);

      // Check if we need to send files
      const hasFiles =
        request.files && request.files.some((file) => file !== null);

      if (hasFiles) {
        // Use FormData for file upload
        const formData = new FormData();

        // Add regular parameters
        if (request.analysisIds && request.analysisIds.length > 0) {
          request.analysisIds.forEach((id) =>
            formData.append("analysis_ids", id)
          );
        }

        if (request.companyNames && request.companyNames.length > 0) {
          request.companyNames.forEach((name) =>
            formData.append("company_names", name)
          );
        }

        if (request.saveNewAnalyses) {
          formData.append("save_new_analyses", "true");
        }

        if (requestWithDefaults.saveResult) {
          formData.append("save_result", "true");
        }

        // Add files
        if (request.files) {
          request.files.forEach((file) => {
            if (file) {
              formData.append("files", file);
            }
          });
        }

        const response = await fetch(`${API_BASE_URL}/api/comparison/compare`, {
          method: "POST",
          headers: {
            Authorization: `Bearer ${this.getAuthToken()}`,
            // Don't set Content-Type for FormData - let browser set it with boundary
          },
          body: formData,
        });

        if (!response.ok) {
          const errorText = await response.text();
          console.error(
            "Failed to perform enhanced comparison with files:",
            response.status,
            errorText
          );
          throw new Error(`Comparison failed: ${response.status}`);
        }

        const result = await response.json();
        console.log("Enhanced comparison result:", result);
        return result;
      } else {
        // Use JSON for requests without files (existing logic)
        const response = await fetch(
          `${API_BASE_URL}/api/comparison/compare-enhanced`,
          {
            method: "POST",
            headers: this.getAuthHeaders(),
            body: JSON.stringify(requestWithDefaults),
          }
        );

        if (!response.ok) {
          const errorText = await response.text();
          console.error(
            "Failed to perform enhanced comparison:",
            response.status,
            errorText
          );
          throw new Error(`Comparison failed: ${response.status}`);
        }

        const result = await response.json();
        console.log("Enhanced comparison result:", result);
        return result;
      }
    } catch (error) {
      console.error("Enhanced Comparison Error:", error);
      throw error;
    }
  }

  /**
   * Get user's saved comparison results
   */
  static async getSavedComparisons(
    page: number = 0,
    size: number = 10,
    comparisonType?: string
  ): Promise<any> {
    try {
      console.log("Fetching saved comparisons with pagination:", {
        page,
        size,
      });
      const params = new URLSearchParams();
      params.append("page", page.toString());
      params.append("size", size.toString());
      if (comparisonType) params.append("type", comparisonType);

      const url = `${API_BASE_URL}/api/comparison/saved?${params.toString()}`;
      const response = await fetch(url, {
        method: "GET",
        headers: this.getAuthHeaders(),
      });

      if (!response.ok) {
        console.error("Failed to fetch saved comparisons:", response.status);
        throw new Error(
          `Failed to fetch saved comparisons: ${response.status}`
        );
      }

      const data = await response.json();
      console.log("Saved comparisons fetched successfully:", data);
      return data;
    } catch (error) {
      console.error("Get Saved Comparisons Error:", error);
      throw error;
    }
  }

  /**
   * Get specific saved comparison result by ID
   */
  static async getSavedComparison(comparisonId: string): Promise<any> {
    try {
      console.log("Fetching saved comparison:", comparisonId);
      const response = await fetch(
        `${API_BASE_URL}/api/comparison/saved/${comparisonId}`,
        {
          method: "GET",
          headers: this.getAuthHeaders(),
        }
      );

      if (!response.ok) {
        console.error("Failed to fetch saved comparison:", response.status);
        throw new Error(`Failed to fetch saved comparison: ${response.status}`);
      }

      const data = await response.json();
      console.log("Saved comparison fetched successfully:", data);
      return data;
    } catch (error) {
      console.error("Get Saved Comparison Error:", error);
      throw error;
    }
  }

  /**
   * Delete saved comparison result
   */
  static async deleteSavedComparison(comparisonId: string): Promise<any> {
    try {
      console.log("Deleting saved comparison:", comparisonId);
      const response = await fetch(
        `${API_BASE_URL}/api/comparison/saved/${comparisonId}`,
        {
          method: "DELETE",
          headers: this.getAuthHeaders(),
        }
      );

      if (!response.ok) {
        console.error("Failed to delete saved comparison:", response.status);
        throw new Error(
          `Failed to delete saved comparison: ${response.status}`
        );
      }

      const data = await response.json();
      console.log("Saved comparison deleted successfully:", data);
      return data;
    } catch (error) {
      console.error("Delete Saved Comparison Error:", error);
      throw error;
    }
  }

  // Sentiment Analysis methods
  /**
   * Get list of available companies for sentiment analysis
   */
  static async getCompanies(): Promise<CompanyListResponse> {
    try {
      console.log("Fetching companies list for sentiment analysis");
      const response = await fetch(`${API_BASE_URL}/api/sentiment/companies`, {
        method: "GET",
        headers: this.getAuthHeaders(),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error("Get Companies Error:", error);
      throw error;
    }
  }

  /**
   * Add a new company to the sentiment analysis system
   */
  static async addCompany(company: string): Promise<{ message: string }> {
    try {
      console.log("Adding company:", company);

      const response = await fetch(
        `${API_BASE_URL}/api/sentiment/companies/add/${encodeURIComponent(
          company
        )}`,
        {
          method: "POST",
          headers: this.getAuthHeaders(),
        }
      );

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error("Add Company Error:", error);
      throw error;
    }
  }

  /**
   * Get sentiment trend analysis for a specific company
   */
  static async getSentimentTrend(
    company: string,
    days: number = 30,
    sources: string = "news"
  ): Promise<SentimentTrendResponse> {
    try {
      console.log("Fetching sentiment trend for:", company);
      const params = new URLSearchParams({ days: days.toString() });
      if (sources) params.append("sources", sources); // "news", "social", or "news,social"

      const response = await fetch(
        `${API_BASE_URL}/api/sentiment/${company}/trend/chart?${params.toString()}`,
        {
          method: "GET",
          headers: this.getAuthHeaders(),
        }
      );

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error("Get Sentiment Trend Error:", error);
      throw error;
    }
  }

  /**
   * Get sentiment comparison analysis for multiple companies
   */
  static async getSentimentComparison(
    companies: string[],
    days: number = 30,
    sources?: string
  ): Promise<SentimentComparisonResponse> {
    try {
      console.log("Fetching sentiment comparison for:", companies);
      const params = new URLSearchParams();
      params.append("companies", companies.join(","));
      params.append("days", days.toString());
      if (sources) params.append("sources", sources); // "news", "social", or "news,social"

      const response = await fetch(
        `${API_BASE_URL}/api/sentiment/comparison/chart?${params.toString()}`,
        {
          method: "GET",
          headers: this.getAuthHeaders(),
        }
      );

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error("Get Sentiment Comparison Error:", error);
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
