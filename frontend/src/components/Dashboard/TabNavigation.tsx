import React from "react";
import { BarChart3, GitCompare, TrendingUp } from "lucide-react";

interface TabNavigationProps {
  activeTab:
    | "analysis"
    | "comparison"
    | "reports"
    | "insights"
    | "trends"
    | "sentiment_analysis";
  onTabChange: (
    tab:
      | "analysis"
      | "comparison"
      | "reports"
      | "insights"
      | "trends"
      | "sentiment_analysis"
  ) => void;
  analysisCount: number;
  comparisonCount: number;
}

const TabNavigation: React.FC<TabNavigationProps> = ({
  activeTab,
  onTabChange,
  analysisCount,
  comparisonCount,
}) => {
  return (
    <div className="border-b border-gray-200 mb-6">
      <nav className="-mb-px flex space-x-8">
        <button
          onClick={() => onTabChange("analysis")}
          className={`group inline-flex items-center py-4 px-1 border-b-2 font-medium text-sm cursor-pointer ${
            activeTab === "analysis"
              ? "border-green-500 text-green-600"
              : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
          }`}
        >
          <BarChart3 className="mr-2 h-5 w-5" />
          Analysis
          <span
            className={`ml-2 py-0.5 px-2 rounded-full text-xs ${
              activeTab === "analysis"
                ? "bg-green-100 text-green-600"
                : "bg-gray-100 text-gray-500"
            }`}
          >
            {analysisCount}
          </span>
        </button>
        <button
          onClick={() => onTabChange("comparison")}
          className={`group inline-flex items-center py-4 px-1 border-b-2 font-medium text-sm cursor-pointer ${
            activeTab === "comparison"
              ? "border-purple-500 text-purple-600"
              : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
          }`}
        >
          <GitCompare className="mr-2 h-5 w-5" />
          Comparison
          <span
            className={`ml-2 py-0.5 px-2 rounded-full text-xs ${
              activeTab === "comparison"
                ? "bg-purple-100 text-purple-600"
                : "bg-gray-100 text-gray-500"
            }`}
          >
            {comparisonCount}
          </span>
        </button>
        {/* <button
          onClick={() => onTabChange("reports")}
          className={`group inline-flex items-center py-4 px-1 border-b-2 font-medium text-sm ${
            activeTab === "reports"
              ? "border-purple-500 text-purple-600"
              : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
          }`}
        >
          <PieChart className="mr-2 h-5 w-5" />
          Reports
          <span
            className={`ml-2 py-0.5 px-2 rounded-full text-xs ${
              activeTab === "reports"
                ? "bg-purple-100 text-purple-600"
                : "bg-gray-100 text-gray-500"
            }`}
          >
            0
          </span>
        </button> */}
        {/* <button
          onClick={() => onTabChange("insights")}
          className={`group inline-flex items-center py-4 px-1 border-b-2 font-medium text-sm ${
            activeTab === "insights"
              ? "border-purple-500 text-purple-600"
              : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
          }`}
        >
          <Target className="mr-2 h-5 w-5" />
          Insights
          <span
            className={`ml-2 py-0.5 px-2 rounded-full text-xs ${
              activeTab === "insights"
                ? "bg-purple-100 text-purple-600"
                : "bg-gray-100 text-gray-500"
            }`}
          >
            0
          </span>
        </button> */}
        {/* <button
          onClick={() => onTabChange("trends")}
          className={`group inline-flex items-center py-4 px-1 border-b-2 font-medium text-sm ${
            activeTab === "trends"
              ? "border-purple-500 text-purple-600"
              : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
          }`}
        >
          <TrendingUp className="mr-2 h-5 w-5" />
          Trends
          <span
            className={`ml-2 py-0.5 px-2 rounded-full text-xs ${
              activeTab === "trends"
                ? "bg-purple-100 text-purple-600"
                : "bg-gray-100 text-gray-500"
            }`}
          >
            0
          </span>
        </button> */}
        <button
          onClick={() => onTabChange("sentiment_analysis")}
          className={`group inline-flex items-center py-4 px-1 border-b-2 font-medium text-sm cursor-pointer ${
            activeTab === "sentiment_analysis"
              ? "border-blue-500 text-blue-600"
              : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
          }`}
        >
          <TrendingUp className="mr-2 h-5 w-5" />
          Sentiments
        </button>
      </nav>
    </div>
  );
};

export default TabNavigation;
