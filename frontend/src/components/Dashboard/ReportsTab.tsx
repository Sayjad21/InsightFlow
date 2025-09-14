import React from "react";
import { PieChart, Clock } from "lucide-react";

const ReportsTab: React.FC = () => {
  return (
    <div className="space-y-6">
      {/* Coming Soon Banner */}
      <div className="bg-gradient-to-r from-purple-50 to-blue-50 border border-purple-200 rounded-xl p-6">
        <div className="flex items-center justify-center mb-4">
          <div className="bg-purple-100 p-3 rounded-full mr-4">
            <PieChart className="h-8 w-8 text-purple-600" />
          </div>
          <div>
            <h2 className="text-xl font-semibold text-purple-900">
              Reports & Analytics
            </h2>
            <p className="text-purple-700">Coming Soon</p>
          </div>
        </div>
        <div className="text-center">
          <p className="text-gray-600 mb-4">
            Advanced reporting and analytics features are coming soon!
          </p>
          <div className="flex items-center justify-center text-sm text-gray-500">
            <Clock className="h-4 w-4 mr-2" />
            Expected Q2 2025
          </div>
        </div>
      </div>

      {/* Preview of Future Features */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <div className="bg-gray-100 h-32 rounded-lg mb-4 flex items-center justify-center">
            <PieChart className="h-8 w-8 text-gray-400" />
          </div>
          <h3 className="font-semibold text-gray-900 mb-2">
            Executive Summaries
          </h3>
          <p className="text-sm text-gray-600">
            Generate comprehensive executive reports from your analyses
          </p>
        </div>

        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <div className="bg-gray-100 h-32 rounded-lg mb-4 flex items-center justify-center">
            <PieChart className="h-8 w-8 text-gray-400" />
          </div>
          <h3 className="font-semibold text-gray-900 mb-2">
            Performance Dashboards
          </h3>
          <p className="text-sm text-gray-600">
            Interactive dashboards with real-time analytics
          </p>
        </div>

        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <div className="bg-gray-100 h-32 rounded-lg mb-4 flex items-center justify-center">
            <PieChart className="h-8 w-8 text-gray-400" />
          </div>
          <h3 className="font-semibold text-gray-900 mb-2">Custom Reports</h3>
          <p className="text-sm text-gray-600">
            Build custom reports tailored to your specific needs
          </p>
        </div>
      </div>
    </div>
  );
};

export default ReportsTab;
