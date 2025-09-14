import React from "react";
import { Target, Clock } from "lucide-react";

const InsightsTab: React.FC = () => {
  return (
    <div className="space-y-6">
      {/* Coming Soon Banner */}
      <div className="bg-gradient-to-r from-green-50 to-teal-50 border border-green-200 rounded-xl p-6">
        <div className="flex items-center justify-center mb-4">
          <div className="bg-green-100 p-3 rounded-full mr-4">
            <Target className="h-8 w-8 text-green-600" />
          </div>
          <div>
            <h2 className="text-xl font-semibold text-green-900">
              AI-Powered Insights
            </h2>
            <p className="text-green-700">Coming Soon</p>
          </div>
        </div>
        <div className="text-center">
          <p className="text-gray-600 mb-4">
            Advanced AI-powered insights and recommendations are coming soon!
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
            <Target className="h-8 w-8 text-gray-400" />
          </div>
          <h3 className="font-semibold text-gray-900 mb-2">
            Smart Recommendations
          </h3>
          <p className="text-sm text-gray-600">
            AI-powered strategic recommendations based on your data
          </p>
        </div>

        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <div className="bg-gray-100 h-32 rounded-lg mb-4 flex items-center justify-center">
            <Target className="h-8 w-8 text-gray-400" />
          </div>
          <h3 className="font-semibold text-gray-900 mb-2">
            Pattern Detection
          </h3>
          <p className="text-sm text-gray-600">
            Automatically detect patterns and anomalies in your analysis data
          </p>
        </div>

        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <div className="bg-gray-100 h-32 rounded-lg mb-4 flex items-center justify-center">
            <Target className="h-8 w-8 text-gray-400" />
          </div>
          <h3 className="font-semibold text-gray-900 mb-2">
            Predictive Analytics
          </h3>
          <p className="text-sm text-gray-600">
            Predict future trends based on historical analysis data
          </p>
        </div>
      </div>
    </div>
  );
};

export default InsightsTab;
