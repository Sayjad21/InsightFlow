import React, { useRef } from "react";
import { Loader2 } from "lucide-react";
import { useDashboardData } from "../hooks/useDashboardData";
import TabNavigation from "../components/Dashboard/TabNavigation";
import ComparisonModal from "../components/Dashboard/ComparisonModal";
import AnalysisTab from "../components/Dashboard/AnalysisTab";
import ComparisonTab from "../components/Dashboard/ComparisonTab";
import ReportsTab from "../components/Dashboard/ReportsTab";
import InsightsTab from "../components/Dashboard/InsightsTab";
import TrendsTab from "../components/Dashboard/TrendsTab";
import SentimentAnalysisTab from "../components/Dashboard/SentimentAnalysisTab";
import Layout, { type LayoutRef } from "../components/Layout";

const Dashboard: React.FC = () => {
  const layoutRef = useRef<LayoutRef>(null);
  const dashboardData = useDashboardData();

  const {
    activeTab,
    setActiveTab,
    isLoading,
    error,
    comparisonResults,
    selectedComparison,
    setSelectedComparison,
    totalAnalyses,
    comparisonCurrentPage,
    setComparisonCurrentPage,
    comparisonTotalPages,
    totalComparisons,
  } = dashboardData;

  // Function to refresh profile data after successful operations
  const handleProfileRefresh = () => {
    layoutRef.current?.refreshProfile();
  };

  return (
    <Layout ref={layoutRef}>
      <div className="bg-white rounded-2xl shadow-lg">
        <div className="p-6">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">
                Welcome back!
              </h1>
              <p className="text-gray-600">
                Manage your business analyses and comparisons
              </p>
            </div>
          </div>

          {/* Tab Navigation */}
          <TabNavigation
            activeTab={activeTab}
            onTabChange={setActiveTab}
            analysisCount={totalAnalyses || 0}
            comparisonCount={totalComparisons || 0}
          />

          {/* Show loading or error state only in content area */}
          {isLoading ? (
            <div className="text-center py-12">
              <Loader2 className="h-8 w-8 animate-spin text-blue-600 mx-auto mb-4" />
              <p className="text-gray-600">Loading your dashboard...</p>
            </div>
          ) : error ? (
            <div className="text-center py-12">
              <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg inline-block">
                Error: {error}
              </div>
            </div>
          ) : /* Tab Content */
          activeTab === "analysis" ? (
            <AnalysisTab
              {...dashboardData}
              onProfileRefresh={handleProfileRefresh}
            />
          ) : activeTab === "comparison" ? (
            <ComparisonTab
              comparisonResults={comparisonResults}
              comparisonCurrentPage={comparisonCurrentPage}
              setComparisonCurrentPage={setComparisonCurrentPage}
              comparisonTotalPages={comparisonTotalPages}
              totalComparisons={totalComparisons}
              expandedComparison={dashboardData.expandedComparison}
              setExpandedComparison={dashboardData.setExpandedComparison}
              setSelectedComparison={dashboardData.setSelectedComparison}
            />
          ) : activeTab === "reports" ? (
            <ReportsTab />
          ) : activeTab === "insights" ? (
            <InsightsTab />
          ) : activeTab === "trends" ? (
            <TrendsTab />
          ) : activeTab === "sentiment_analysis" ? (
            <SentimentAnalysisTab />
          ) : (
            <AnalysisTab {...dashboardData} />
          )}
        </div>
      </div>

      {/* Comparison Modal */}
      {selectedComparison && (
        <ComparisonModal
          selectedComparison={selectedComparison}
          onClose={() => setSelectedComparison(null)}
        />
      )}
    </Layout>
  );
};

export default Dashboard;
