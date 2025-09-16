import React, { useState, useEffect } from "react";
import {
  Plus,
  TrendingUp,
  BarChart3,
  AlertCircle,
  Loader2,
  Clock,
} from "lucide-react";
import { ApiService } from "../../services/api";
import type {
  SentimentTrendResponse,
  SentimentComparisonResponse,
} from "../../services/api";

interface SentimentAnalysisTabProps {
  className?: string;
}

const SentimentAnalysisTab: React.FC<SentimentAnalysisTabProps> = ({
  className = "",
}) => {
  // State management
  const [companies, setCompanies] = useState<string[]>([]);
  const [selectedCompanies, setSelectedCompanies] = useState<string[]>([]);
  const [analysisMode, setAnalysisMode] = useState<"single" | "comparison">(
    "single"
  );
  const [loading, setLoading] = useState(false);
  const [companiesLoading, setCompaniesLoading] = useState(true);
  const [newCompany, setNewCompany] = useState("");
  const [showAddCompany, setShowAddCompany] = useState(false);
  const [addingCompany, setAddingCompany] = useState(false);

  // Selected analysis period in days
  const [selectedDays, setSelectedDays] = useState<number>(30);

  // Sources: "news", "social", or "both"
  const [selectedSources, setSelectedSources] = useState<
    "news" | "social" | "both"
  >("both");

  // Results state
  const [trendResult, setTrendResult] = useState<SentimentTrendResponse | null>(
    null
  );
  const [comparisonResult, setComparisonResult] =
    useState<SentimentComparisonResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  // Load companies on component mount
  useEffect(() => {
    loadCompanies();
  }, []);

  const loadCompanies = async () => {
    try {
      setCompaniesLoading(true);
      const response = await ApiService.getCompanies();
      setCompanies(response.companies);
    } catch (error) {
      console.error("Failed to load companies:", error);
      setError("Failed to load companies. Please try again.");
    } finally {
      setCompaniesLoading(false);
    }
  };

  const handleAddCompany = async () => {
    if (!newCompany.trim()) return;

    try {
      setAddingCompany(true);
      //   const request: AddCompanyRequest = { company: newCompany.trim() };
      await ApiService.addCompany(newCompany.trim());

      // Reload companies list
      await loadCompanies();

      setNewCompany("");
      setShowAddCompany(false);
    } catch (error) {
      console.error("Failed to add company:", error);
      setError("Failed to add company. Please try again.");
    } finally {
      setAddingCompany(false);
    }
  };

  const handleCompanySelection = (company: string) => {
    if (analysisMode === "single") {
      // Toggle in single mode
      if (selectedCompanies.includes(company)) {
        setSelectedCompanies([]); // unselect if already selected
      } else {
        setSelectedCompanies([company]); // only one allowed
      }
    } else {
      // Comparison mode → allow multiple, toggle if clicked again
      if (selectedCompanies.includes(company)) {
        setSelectedCompanies(selectedCompanies.filter((c) => c !== company));
      } else {
        setSelectedCompanies([...selectedCompanies, company]);
      }
    }
  };

  const handleAnalysis = async () => {
    if (selectedCompanies.length === 0) {
      setError("Please select at least one company.");
      return;
    }

    try {
      setLoading(true);
      setError(null);
      setTrendResult(null);
      setComparisonResult(null);

      if (analysisMode === "single" && selectedCompanies.length > 0) {
        const sourcesParam =
          selectedSources === "both" ? "news,social" : selectedSources;
        const result = await ApiService.getSentimentTrend(
          selectedCompanies[0],
          selectedDays,
          sourcesParam
        );
        setTrendResult(result);
      } else if (
        analysisMode === "comparison" &&
        selectedCompanies.length > 1
      ) {
        const sourcesParam =
          selectedSources === "both" ? "news,social" : selectedSources;
        const result = await ApiService.getSentimentComparison(
          selectedCompanies,
          selectedDays,
          sourcesParam
        );
        setComparisonResult(result);
      } else {
        setError(
          analysisMode === "comparison"
            ? "Please select at least 2 companies for comparison."
            : "Please select a company for analysis."
        );
      }
    } catch (error) {
      console.error("Analysis failed:", error);
      setError("Analysis failed. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const renderInsufficientDataError = (message: string) => (
    <div className="mt-6 bg-gradient-to-br from-yellow-50 to-amber-50 border-l-4 border-yellow-400 rounded-lg p-6 shadow-sm">
      <div className="flex items-start">
        <div className="flex-shrink-0">
          <AlertCircle className="w-6 h-6 text-yellow-500" />
        </div>
        <div className="ml-4">
          <h3 className="text-lg font-semibold text-yellow-800 mb-2">
            Insufficient Data Available
          </h3>
          <p className="text-yellow-700 leading-relaxed">{message}</p>
          <div className="mt-3 p-3 bg-yellow-100 rounded-md">
            <p className="text-sm text-yellow-800">
              💡 <strong>Tip:</strong> Try selecting a longer time period or
              different companies with more recent activity.
            </p>
          </div>
        </div>
      </div>
    </div>
  );

  const renderChart = (
    chartUrl?: string,
    chartBase64?: string,
    chartType?: string
  ) => {
    if (!chartUrl && !chartBase64) return null;

    const imageSrc =
      chartType === "url" && chartUrl
        ? chartUrl
        : chartBase64
        ? `data:image/png;base64,${chartBase64}`
        : null;

    if (!imageSrc) return null;

    return (
      <div className="mt-6">
        <h4 className="text-2xl font-semibold text-gray-900 mb-4">
          Sentiment Analysis Chart
        </h4>
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
          <img
            src={imageSrc}
            alt="Sentiment Analysis Chart"
            className="w-full h-auto rounded-lg shadow-sm"
            onError={(e) => {
              console.error("Failed to load chart image");
              e.currentTarget.style.display = "none";
            }}
          />
        </div>
      </div>
    );
  };

  return (
    <div className={`bg-white rounded-lg shadow-sm border p-6 ${className}`}>
      {/* Header */}
      <div className="bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-200 rounded-xl p-6">
        <div className="flex items-center justify-center mb-4">
          <div className="bg-blue-100 p-3 rounded-full mr-4">
            <TrendingUp className="h-8 w-8 text-blue-600" />
          </div>
          <div>
            <h2 className="text-xl font-semibold text-blue-900">
              Sentiment Analysis
            </h2>
            <p className="text-blue-700">
              Analyze sentiment trends for companies based on LinkedIn data.
            </p>
          </div>
        </div>
        <div className="text-center">
          <p className="text-gray-600 mb-4  ">
            Get insights into public perception and sentiment patterns over
            time.
          </p>
          <div className="flex items-center justify-center text-sm text-gray-500">
            <Clock className="h-4 w-4 mr-2" />
            Updated 2025
          </div>
        </div>
      </div>

      {/* Analysis Mode Selection */}
      <div className="mb-6 mt-6">
        <label className="block text-sm font-medium text-gray-700 mb-3">
          Analysis Mode
        </label>
        <div className="flex space-x-4">
          <button
            onClick={() => {
              setAnalysisMode("single");
              setSelectedCompanies(selectedCompanies.slice(0, 1));
            }}
            className={`flex items-center px-4 py-2 rounded-lg border transition-colors ${
              analysisMode === "single"
                ? "bg-blue-50 border-blue-200 text-blue-700"
                : "bg-white border-gray-200 text-gray-700 hover:bg-gray-50"
            }`}
          >
            <TrendingUp className="w-4 h-4 mr-2" />
            Single Company Trend
          </button>
          <button
            onClick={() => setAnalysisMode("comparison")}
            className={`flex items-center px-4 py-2 rounded-lg border transition-colors ${
              analysisMode === "comparison"
                ? "bg-blue-50 border-blue-200 text-blue-700"
                : "bg-white border-gray-200 text-gray-700 hover:bg-gray-50"
            }`}
          >
            <BarChart3 className="w-4 h-4 mr-2" />
            Multi-Company Comparison
          </button>
        </div>
      </div>

      {/* Company Selection */}
      <div className="mb-6">
        <div className="flex items-center justify-between mb-3">
          <label className="block text-sm font-medium text-gray-700">
            Select {analysisMode === "single" ? "Company" : "Companies"}
            {analysisMode === "comparison" && (
              <span className="text-xs text-gray-500 ml-1">(minimum 2)</span>
            )}
          </label>
          <button
            onClick={() => setShowAddCompany(!showAddCompany)}
            className="flex items-center text-sm text-blue-600 hover:text-blue-700"
          >
            <Plus className="w-4 h-4 mr-1" />
            Add Company
          </button>
        </div>

        {/* Add Company Form */}
        {showAddCompany && (
          <div className="mb-4 p-4 bg-gray-50 rounded-lg">
            <div className="flex space-x-2">
              <input
                type="text"
                value={newCompany}
                onChange={(e) => setNewCompany(e.target.value)}
                placeholder="Enter company name"
                className="flex-1 px-3 py-2 border text-gray-900 border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                onKeyDown={(e) => {
                  if (e.key === "Enter") {
                    handleAddCompany();
                  }
                }}
              />
              <button
                onClick={handleAddCompany}
                disabled={addingCompany || !newCompany.trim()}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed flex items-center"
              >
                {addingCompany ? (
                  <Loader2 className="w-4 h-4 animate-spin" />
                ) : (
                  <Plus className="w-4 h-4" />
                )}
              </button>
            </div>
          </div>
        )}

        {/* Company Grid */}
        {companiesLoading ? (
          <div className="flex items-center justify-center py-12 bg-gradient-to-br from-blue-50 to-indigo-50 rounded-xl border border-blue-100">
            <div className="text-center">
              <Loader2 className="w-8 h-8 animate-spin text-blue-600 mx-auto mb-3" />
              <span className="text-blue-700 font-medium">
                Loading companies...
              </span>
              <p className="text-sm text-blue-600 mt-1">
                Fetching available companies from our database
              </p>
            </div>
          </div>
        ) : companies.length === 0 ? (
          <div className="text-center py-12 bg-gradient-to-br from-gray-50 to-slate-50 rounded-xl border border-gray-200">
            <div className="text-gray-500">
              <Plus className="w-12 h-12 mx-auto mb-4 text-gray-400" />
              <p className="text-lg font-medium mb-2">No companies available</p>
              <p className="text-sm">
                Add a company above to get started with sentiment analysis.
              </p>
            </div>
          </div>
        ) : (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-3">
            {companies.map((company) => (
              <button
                key={company}
                onClick={() => handleCompanySelection(company)}
                className={`px-4 py-3 text-sm rounded-xl border-2 font-medium transition-all duration-300 ease-out transform
                ${
                  selectedCompanies.includes(company)
                    ? "bg-gradient-to-r from-blue-500 to-blue-600 border-blue-500 text-white shadow-lg scale-105 hover:scale-110"
                    : "bg-white border-gray-200 text-gray-700 hover:border-blue-300 hover:bg-blue-50 hover:shadow-md hover:scale-105"
                }`}
              >
                {company}
              </button>
            ))}
          </div>
        )}
      </div>

      {/* Period Selection */}
      <div className="mb-6">
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Select Period
        </label>
        <div className="flex flex-wrap gap-2">
          {[15, 30, 60, 180, 365].map((days) => (
            <button
              key={days}
              onClick={() => setSelectedDays(days)}
              className={`px-4 py-2 text-sm rounded-xl border-2 font-medium transition-all duration-300 ease-out transform
                ${
                  selectedDays === days
                    ? "bg-gradient-to-r from-blue-500 to-blue-600 border-blue-500 text-white shadow-md scale-105 hover:scale-110"
                    : "bg-white border-gray-200 text-gray-700 hover:border-blue-300 hover:bg-blue-50 hover:shadow-sm hover:scale-105"
                }`}
            >
              {days === 15
                ? "15 Days"
                : days === 30
                ? "1 Month"
                : days === 60
                ? "2 Months"
                : days === 180
                ? "6 Months"
                : "1 Year"}
            </button>
          ))}
        </div>
      </div>

      {/* Sources Selection */}
      <div className="mb-6">
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Select Sources
        </label>
        <div className="flex space-x-2">
          <button
            onClick={() => setSelectedSources("news")}
            className={`px-4 py-2 text-sm rounded-xl border-2 font-medium transition-all duration-300 ease-out transform
            ${
              selectedSources === "news"
                ? "bg-gradient-to-r from-blue-500 to-blue-600 border-blue-500 text-white shadow-md scale-105 hover:scale-110"
                : "bg-white border-gray-200 text-gray-700 hover:border-blue-300 hover:bg-blue-50 hover:shadow-sm hover:scale-105"
            }`}
          >
            News
          </button>
          <button
            onClick={() => setSelectedSources("social")}
            className={`px-4 py-2 text-sm rounded-xl border-2 font-medium transition-all duration-300 ease-out transform 
            ${
              selectedSources === "social"
                ? "bg-gradient-to-r from-blue-500 to-blue-600 border-blue-500 text-white shadow-md scale-105 hover:scale-110"
                : "bg-white border-gray-200 text-gray-700 hover:border-blue-300 hover:bg-blue-50 hover:shadow-sm hover:scale-105"
            }`}
          >
            Social
          </button>
          <button
            onClick={() => setSelectedSources("both")}
            className={`px-4 py-2 text-sm rounded-xl border-2 font-medium transition-all duration-300 ease-out transform 
            ${
              selectedSources === "both"
                ? "bg-gradient-to-r from-blue-500 to-blue-600 border-blue-500 text-white shadow-md scale-105 hover:scale-110"
                : "bg-white border-gray-200 text-gray-700 hover:border-blue-300 hover:bg-blue-50 hover:shadow-sm hover:scale-105"
            }`}
          >
            Both
          </button>
        </div>
      </div>

      {/* Analysis Button */}
      <div className="mb-6">
        <button
          onClick={handleAnalysis}
          disabled={
            loading ||
            selectedCompanies.length === 0 ||
            (analysisMode === "comparison" && selectedCompanies.length < 2)
          }
          className={`group relative px-8 py-4 rounded-xl flex items-center font-semibold text-lg shadow-lg transition-all duration-300 transform hover:-translate-y-0.5 overflow-hidden`}
        >
          {/* Background layer for smooth transition */}
          <span
            className={`absolute inset-0 transition-all duration-300 ${
              loading
                ? "bg-blue-600"
                : (analysisMode === "comparison" &&
                    selectedCompanies.length < 2) ||
                  selectedCompanies.length === 0
                ? "bg-gray-400"
                : "bg-gradient-to-r from-blue-600 to-blue-700 group-hover:from-blue-700 group-hover:to-blue-800"
            } rounded-xl`}
          ></span>

          {/* Button content */}
          <span className="relative flex items-center justify-center w-full">
            {loading ? (
              <>
                <Loader2 className="w-5 h-5 animate-spin mr-3" />
                Analyzing Sentiment...
              </>
            ) : (
              <>
                {analysisMode === "single" ? (
                  <TrendingUp className="w-5 h-5 mr-3 transition-transform group-hover:scale-110" />
                ) : (
                  <BarChart3 className="w-5 h-5 mr-3 transition-transform group-hover:scale-110" />
                )}
                Run {analysisMode === "single" ? "Trend" : "Comparison"}{" "}
                Analysis
              </>
            )}
          </span>
        </button>

        {/* Helper text */}
        <div className="mt-2 text-sm text-gray-600">
          {selectedCompanies.length === 0 ? (
            <span className="flex items-center">
              <AlertCircle className="w-4 h-4 mr-1 text-orange-500" />
              Please select at least one company to analyze
            </span>
          ) : analysisMode === "comparison" && selectedCompanies.length < 2 ? (
            <span className="flex items-center">
              <AlertCircle className="w-4 h-4 mr-1 text-orange-500" />
              Select at least 2 companies for comparison analysis
            </span>
          ) : (
            <span className="flex items-center text-green-600">
              ✓ Ready to analyze {selectedCompanies.length}{" "}
              {selectedCompanies.length === 1 ? "company" : "companies"}
            </span>
          )}
        </div>
      </div>

      {/* Error Display */}
      {error && (
        <div className="mb-6 bg-gradient-to-br from-red-50 to-rose-50 border-l-4 border-red-400 rounded-lg p-6 shadow-sm">
          <div className="flex items-start">
            <div className="flex-shrink-0">
              <AlertCircle className="w-6 h-6 text-red-500" />
            </div>
            <div className="ml-4">
              <h3 className="text-lg font-semibold text-red-800 mb-2">
                Analysis Error
              </h3>
              <p className="text-red-700 leading-relaxed">{error}</p>
              <div className="mt-3 p-3 bg-red-100 rounded-md">
                <p className="text-sm text-red-800">
                  🔄 Please try again or contact support if the issue persists.
                </p>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Results Display */}
      {trendResult && (
        <div>
          {trendResult.status === "insufficient_data" ? (
            renderInsufficientDataError(
              trendResult.message ||
                "Not enough data available for this company."
            )
          ) : trendResult.status === "success" ? (
            <div className="bg-gradient-to-br from-blue-50 to-indigo-50 rounded-xl p-6 border border-blue-100">
              <div className="flex items-center mb-4">
                <TrendingUp className="w-6 h-6 text-blue-600 mr-3" />
                <h3 className="text-xl font-bold text-gray-900">
                  Sentiment Trend for {trendResult.company}
                </h3>
              </div>

              {/* Analysis Summary */}
              {trendResult.analysis && (
                <div className="mt-8 mb-7">
                  <h4 className="text-blue-900 font-semibold text-2xl mb-4">
                    Analysis Summary
                  </h4>

                  {/* Key Metrics */}
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-center">
                    {trendResult.analysis.overall_positive_percentage !==
                      undefined && (
                      <div className="bg-green-50 rounded-lg p-4 border border-green-100">
                        <div className="text-2xl font-bold text-green-700">
                          {trendResult.analysis.overall_positive_percentage.toFixed(
                            1
                          )}
                          %
                        </div>
                        <div className="text-sm text-green-600 font-medium">
                          Overall Positive
                        </div>
                      </div>
                    )}

                    {trendResult.analysis.overall_negative_percentage !==
                      undefined && (
                      <div className="bg-red-50 rounded-lg p-4 border border-red-100">
                        <div className="text-2xl font-bold text-red-700">
                          {trendResult.analysis.overall_negative_percentage.toFixed(
                            1
                          )}
                          %
                        </div>
                        <div className="text-sm text-red-600 font-medium">
                          Overall Negative
                        </div>
                      </div>
                    )}

                    {trendResult.analysis.overall_neutral_percentage !==
                      undefined && (
                      <div className="bg-gray-50 rounded-lg p-4 border border-gray-100">
                        <div className="text-2xl font-bold text-gray-700">
                          {trendResult.analysis.overall_neutral_percentage.toFixed(
                            1
                          )}
                          %
                        </div>
                        <div className="text-sm text-gray-600 font-medium">
                          Overall Neutral
                        </div>
                      </div>
                    )}

                    {trendResult.analysis.total_posts !== undefined && (
                      <div className="bg-blue-50 rounded-lg p-4 border border-blue-100">
                        <div className="text-2xl font-bold text-blue-700">
                          {trendResult.analysis.total_posts}
                        </div>
                        <div className="text-sm text-blue-600 font-medium">
                          Total Posts
                        </div>
                      </div>
                    )}
                  </div>

                  {/* Overall Trends */}
                  {trendResult.analysis.overall_trends && (
                    <div className="mt-4 grid grid-cols-1 md:grid-cols-3 gap-4 text-center">
                      {["average_score", "volatility", "slope"].map((key) => {
                        const val = trendResult.analysis.overall_trends[key];
                        const display =
                          val !== undefined
                            ? +val.toFixed(2) !== 0
                              ? val.toFixed(2)
                              : val.toExponential(2)
                            : "—";
                        const color =
                          key === "average_score"
                            ? "green"
                            : key === "volatility"
                            ? "purple"
                            : "blue";

                        return (
                          <div key={key} className="text-center">
                            <span className={`font-semibold text-gray-700`}>
                              {key.charAt(0).toUpperCase() + key.slice(1)}:
                            </span>
                            <span className={`ml-1 text-${color}-600`}>
                              {display}
                            </span>
                          </div>
                        );
                      })}
                    </div>
                  )}

                  {/* Time Period & Data Points */}
                  <div className="mt-4 grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
                    <div className="text-center">
                      <span className="font-semibold text-gray-700">
                        Data Points:
                      </span>
                      <span className="ml-1 text-gray-600">
                        {trendResult.analysis.data_point_count}
                      </span>
                    </div>

                    <div className="text-center">
                      <span className="font-semibold text-gray-700">
                        Period:
                      </span>
                      <span className="ml-1 text-gray-600">
                        {trendResult.days} days
                      </span>
                    </div>

                    {trendResult.sources && (
                      <div className="text-center">
                        <span className="font-semibold text-gray-700">
                          Sources:
                        </span>
                        <span className="ml-1 text-gray-600">
                          {trendResult.sources.join(", ")}
                        </span>
                      </div>
                    )}
                  </div>
                </div>
              )}

              {/* Chart */}
              {renderChart(
                trendResult.chart_url,
                trendResult.chart,
                trendResult.chart_type
              )}
            </div>
          ) : null}
        </div>
      )}

      {comparisonResult && (
        <div>
          {comparisonResult.status === "insufficient_data" ? (
            renderInsufficientDataError(
              comparisonResult.message ||
                "Not enough data available for comparison."
            )
          ) : comparisonResult.status === "success" ? (
            <div className="bg-gradient-to-br from-purple-50 to-pink-50 rounded-xl p-6 border border-purple-100">
              <div className="flex items-center mb-4">
                <BarChart3 className="w-6 h-6 text-purple-600 mr-3" />
                <h3 className="text-xl font-bold text-gray-900">
                  Sentiment Comparison
                </h3>
              </div>

              {/* Companies Summary */}
              {comparisonResult.companies_data && (
                <div className="overflow-x-auto">
                  <table className="w-full text-sm border-collapse">
                    <thead>
                      <tr className="border-b-2 border-gray-200">
                        <th className="text-left py-3 px-4 font-semibold text-gray-700">
                          Company
                        </th>
                        <th className="text-center py-3 px-4 font-semibold text-green-600">
                          Avg Score
                        </th>
                        <th className="text-center py-3 px-4 font-semibold text-purple-600">
                          Volatility
                        </th>
                        <th className="text-center py-3 px-4 font-semibold text-blue-600">
                          Trend (Slope)
                        </th>
                        <th className="text-center py-3 px-4 font-semibold text-gray-600">
                          Data Points
                        </th>
                      </tr>
                    </thead>
                    <tbody>
                      {Object.entries(comparisonResult.companies_data).map(
                        ([company, data]: [string, any]) => (
                          <tr
                            key={company}
                            className="border-b border-gray-100 hover:bg-gray-50 transition-colors"
                          >
                            <td className="py-3 px-4 font-medium text-gray-900">
                              {data.company_name || company}
                            </td>
                            <td className="text-center py-3 px-4">
                              <span className="inline-block bg-green-100 text-green-800 px-2 py-1 rounded-full font-medium">
                                {data.overall_trends?.average_score !==
                                undefined
                                  ? +data.overall_trends.average_score.toFixed(
                                      2
                                    ) !== 0
                                    ? data.overall_trends.average_score.toFixed(
                                        2
                                      )
                                    : data.overall_trends.average_score.toExponential(
                                        2
                                      )
                                  : "—"}
                              </span>
                            </td>

                            <td className="text-center py-3 px-4">
                              <span className="inline-block bg-purple-100 text-purple-800 px-2 py-1 rounded-full font-medium">
                                {data.overall_trends?.volatility !== undefined
                                  ? +data.overall_trends.volatility.toFixed(
                                      2
                                    ) !== 0
                                    ? data.overall_trends.volatility.toFixed(2)
                                    : data.overall_trends.volatility.toExponential(
                                        2
                                      )
                                  : "—"}
                              </span>
                            </td>

                            <td className="text-center py-3 px-4">
                              <span className="inline-block bg-blue-100 text-blue-800 px-2 py-1 rounded-full font-medium">
                                {data.overall_trends?.slope !== undefined
                                  ? +data.overall_trends.slope.toFixed(2) !== 0
                                    ? data.overall_trends.slope.toFixed(2)
                                    : data.overall_trends.slope.toExponential(2)
                                  : "—"}
                              </span>
                            </td>

                            <td className="text-center py-3 px-4">
                              <span className="inline-block bg-gray-100 text-gray-800 px-2 py-1 rounded-full font-medium">
                                {data.data_point_count || 0}
                              </span>
                            </td>
                          </tr>
                        )
                      )}
                    </tbody>
                  </table>
                </div>
              )}

              {renderChart(
                comparisonResult.chart_url,
                comparisonResult.chart,
                comparisonResult.chart_type
              )}
            </div>
          ) : null}
        </div>
      )}
    </div>
  );
};

export default SentimentAnalysisTab;
