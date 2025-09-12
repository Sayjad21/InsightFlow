import React from "react";
import { useDashboardData } from "../hooks/useDashboardData";
import TabNavigation from "../components/Dashboard/TabNavigation";
import ComparisonModal from "../components/Dashboard/ComparisonModal";
import AnalysisTab from "../components/Dashboard/AnalysisTab";
import ComparisonTab from "../components/Dashboard/ComparisonTab";
import Layout from "../components/Layout";

const Dashboard: React.FC = () => {
  const dashboardData = useDashboardData();

  const {
    activeTab,
    setActiveTab,
    userAnalyses,
    isLoading,
    error,
    comparisonResults,
    selectedComparison,
    setSelectedComparison,
  } = dashboardData;

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading your dashboard...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
            Error: {error}
          </div>
        </div>
      </div>
    );
  }

  return (
    <Layout>
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
            analysisCount={userAnalyses.length}
            comparisonCount={comparisonResults.length}
          />

          {/* Tab Content */}
          {activeTab === "analysis" ? (
            <AnalysisTab {...dashboardData} />
          ) : (
            <ComparisonTab
              comparisonResults={comparisonResults}
              expandedComparison={dashboardData.expandedComparison}
              setExpandedComparison={dashboardData.setExpandedComparison}
              setSelectedComparison={dashboardData.setSelectedComparison}
            />
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
