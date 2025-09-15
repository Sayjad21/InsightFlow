import React from "react";
import { TrendingUp, Clock } from "lucide-react";

const TrendsTab: React.FC = () => {
  return (
    <div className="space-y-6">
      {/* Coming Soon Banner */}
      <div className="bg-gradient-to-r from-orange-50 to-red-50 border border-orange-200 rounded-xl p-6">
        <div className="flex items-center justify-center mb-4">
          <div className="bg-orange-100 p-3 rounded-full mr-4">
            <TrendingUp className="h-8 w-8 text-orange-600" />
          </div>
          <div>
            <h2 className="text-xl font-semibold text-orange-900">
              Market Trends & Forecasting
            </h2>
            <p className="text-orange-700">Coming Soon</p>
          </div>
        </div>
        <div className="text-center">
          <p className="text-gray-600 mb-4">
            Market trends analysis and forecasting features are coming soon!
          </p>
          <div className="flex items-center justify-center text-sm text-gray-500">
            <Clock className="h-4 w-4 mr-2" />
            Expected Q3 2025
          </div>
        </div>
      </div>

      {/* Preview of Future Features */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <div className="bg-gray-100 h-32 rounded-lg mb-4 flex items-center justify-center">
            <TrendingUp className="h-8 w-8 text-gray-400" />
          </div>
          <h3 className="font-semibold text-gray-900 mb-2">Industry Trends</h3>
          <p className="text-sm text-gray-600">
            Track and analyze industry-wide trends and developments
          </p>
        </div>

        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <div className="bg-gray-100 h-32 rounded-lg mb-4 flex items-center justify-center">
            <TrendingUp className="h-8 w-8 text-gray-400" />
          </div>
          <h3 className="font-semibold text-gray-900 mb-2">
            Competitive Landscape
          </h3>
          <p className="text-sm text-gray-600">
            Monitor changes in competitive positioning over time
          </p>
        </div>

        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <div className="bg-gray-100 h-32 rounded-lg mb-4 flex items-center justify-center">
            <TrendingUp className="h-8 w-8 text-gray-400" />
          </div>
          <h3 className="font-semibold text-gray-900 mb-2">
            Market Forecasting
          </h3>
          <p className="text-sm text-gray-600">
            Predict future market conditions and opportunities
          </p>
        </div>
      </div>
    </div>
  );
};

export default TrendsTab;
