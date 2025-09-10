import React, { useState, useEffect } from "react";
import { ApiService } from "../services/api.js";

const DiagnosticPage: React.FC = () => {
  const [connectionStatus, setConnectionStatus] = useState<
    "testing" | "success" | "failed"
  >("testing");
  const [errorMessage, setErrorMessage] = useState<string>("");
  const [testResults, setTestResults] = useState<any[]>([]);

  useEffect(() => {
    runDiagnostics();
  }, []);

  const runDiagnostics = async () => {
    const results: any[] = [];

    // Test 1: Basic connection
    try {
      results.push({ test: "Connection Test", status: "testing" });
      setTestResults([...results]);

      const isConnected = await ApiService.testConnection();
      results[results.length - 1] = {
        test: "Connection Test",
        status: isConnected ? "success" : "failed",
        message: isConnected ? "Server is reachable" : "Cannot reach server",
      };
      setTestResults([...results]);

      if (isConnected) {
        setConnectionStatus("success");
      } else {
        setConnectionStatus("failed");
        setErrorMessage("Backend server is not reachable");
      }
    } catch (error) {
      results[results.length - 1] = {
        test: "Connection Test",
        status: "failed",
        message: error instanceof Error ? error.message : "Unknown error",
      };
      setTestResults([...results]);
      setConnectionStatus("failed");
      setErrorMessage(error instanceof Error ? error.message : "Unknown error");
    }

    // Test 2: Try a simple API call
    try {
      results.push({ test: "API Test (Signup endpoint)", status: "testing" });
      setTestResults([...results]);

      const response = await fetch("http://localhost:8000/api/signup", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          firstName: "test",
          lastName: "test",
          email: "test@test.com",
          password: "test123",
        }),
      });

      results[results.length - 1] = {
        test: "API Test (Signup endpoint)",
        status: response.status < 500 ? "success" : "failed",
        message: `HTTP ${response.status} - ${response.statusText}`,
      };
      setTestResults([...results]);
    } catch (error) {
      results[results.length - 1] = {
        test: "API Test (Signup endpoint)",
        status: "failed",
        message: error instanceof Error ? error.message : "Network error",
      };
      setTestResults([...results]);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case "success":
        return "text-green-600 bg-green-50";
      case "failed":
        return "text-red-600 bg-red-50";
      case "testing":
        return "text-yellow-600 bg-yellow-50";
      default:
        return "text-gray-600 bg-gray-50";
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4">
      <div className="max-w-2xl mx-auto">
        <div className="bg-white rounded-lg shadow-md p-6">
          <h1 className="text-2xl font-bold mb-6">
            Backend Connection Diagnostics
          </h1>

          <div className="mb-6">
            <h2 className="text-lg font-semibold mb-2">Server Status</h2>
            <div
              className={`p-3 rounded-md ${getStatusColor(connectionStatus)}`}
            >
              {connectionStatus === "testing" && "Testing connection..."}
              {connectionStatus === "success" && "✅ Server is responding"}
              {connectionStatus === "failed" && "❌ Server connection failed"}
            </div>
            {errorMessage && (
              <div className="mt-2 p-3 bg-red-50 border border-red-200 rounded-md">
                <p className="text-red-700 text-sm">{errorMessage}</p>
              </div>
            )}
          </div>

          <div className="mb-6">
            <h2 className="text-lg font-semibold mb-2">Test Results</h2>
            <div className="space-y-2">
              {testResults.map((result, index) => (
                <div
                  key={index}
                  className={`p-3 rounded-md ${getStatusColor(result.status)}`}
                >
                  <div className="flex justify-between items-center">
                    <span className="font-medium">{result.test}</span>
                    <span className="text-sm">
                      {result.status === "testing" && "⏳"}
                      {result.status === "success" && "✅"}
                      {result.status === "failed" && "❌"}
                    </span>
                  </div>
                  {result.message && (
                    <p className="text-sm mt-1">{result.message}</p>
                  )}
                </div>
              ))}
            </div>
          </div>

          <div className="bg-blue-50 border border-blue-200 rounded-md p-4">
            <h3 className="font-semibold text-blue-900 mb-2">
              Troubleshooting Steps:
            </h3>
            <ol className="list-decimal list-inside text-blue-800 text-sm space-y-1">
              <li>Ensure the backend server is running on port 8000</li>
              <li>Check if MongoDB is connected and accessible</li>
              <li>Verify CORS configuration allows your frontend origin</li>
              <li>Check browser console for detailed error messages</li>
              <li>
                Try accessing http://localhost:8000/api/signup directly in
                browser
              </li>
            </ol>
          </div>

          <div className="mt-4 flex space-x-2">
            <button
              onClick={runDiagnostics}
              className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
            >
              Run Tests Again
            </button>
            <a
              href="http://localhost:8000/api/signup"
              target="_blank"
              rel="noopener noreferrer"
              className="px-4 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700"
            >
              Test Backend URL
            </a>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DiagnosticPage;
