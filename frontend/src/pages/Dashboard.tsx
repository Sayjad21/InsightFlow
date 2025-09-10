import React, { useState } from "react";
import { Link } from "react-router-dom";
import {
  User,
  LogOut,
  Calendar,
  TrendingUp,
  Building2,
  CheckCircle,
  Clock,
  XCircle,
  ChevronDown,
  ChevronUp,
  Eye,
  Download,
  BarChart3,
  Target,
  Users2,
  Settings,
  Home as HomeIcon,
} from "lucide-react";
import { useAuth } from "../contexts/AuthContext";
import { mockUserAnalyses } from "../data/mockData";
import type { UserAnalysis } from "../types";
import AnalysisDisplay from "../components/AnalysisDisplay";
import ExportButtons from "../components/ExportButtons";

const Dashboard: React.FC = () => {
  const { user, logout } = useAuth();
  const [expandedAnalysis, setExpandedAnalysis] = useState<string | null>(null);

  if (!user) return null;

  const completedAnalyses = mockUserAnalyses.filter(
    (analysis) => analysis.status === "completed"
  );
  const pendingAnalyses = mockUserAnalyses.filter(
    (analysis) => analysis.status === "pending"
  );
  const failedAnalyses = mockUserAnalyses.filter(
    (analysis) => analysis.status === "failed"
  );

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "completed":
        return <CheckCircle className="h-5 w-5 text-green-500" />;
      case "pending":
        return <Clock className="h-5 w-5 text-yellow-500" />;
      case "failed":
        return <XCircle className="h-5 w-5 text-red-500" />;
      default:
        return null;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case "completed":
        return "bg-green-100 text-green-800 border-green-200";
      case "pending":
        return "bg-yellow-100 text-yellow-800 border-yellow-200";
      case "failed":
        return "bg-red-100 text-red-800 border-red-200";
      default:
        return "bg-gray-100 text-gray-800 border-gray-200";
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const handleAnalysisClick = (analysis: UserAnalysis) => {
    if (analysis.status === "completed" && analysis.result) {
      const isExpanded = expandedAnalysis === analysis.id;
      setExpandedAnalysis(isExpanded ? null : analysis.id);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <div className="flex items-center">
              <Building2 className="h-8 w-8 text-blue-600 mr-3" />
              <h1 className="text-xl font-semibold text-gray-900">
                InsightFlow Dashboard
              </h1>
            </div>

            <div className="flex items-center space-x-4">
              <Link
                to="/home"
                className="flex items-center px-3 py-2 text-sm font-medium text-gray-700 hover:text-gray-900 transition-colors duration-200"
              >
                <HomeIcon className="h-4 w-4 mr-2" />
                Analysis Tools
              </Link>
              <button className="p-2 text-gray-400 hover:text-gray-600 transition-colors duration-200">
                <Settings className="h-5 w-5" />
              </button>
              <button
                onClick={logout}
                className="flex items-center px-3 py-2 text-sm font-medium text-gray-700 hover:text-gray-900 transition-colors duration-200"
              >
                <LogOut className="h-4 w-4 mr-2" />
                Logout
              </button>
            </div>
          </div>
        </div>
      </header>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* User Profile Section */}
        <div className="bg-gradient-to-r from-blue-600 to-purple-700 rounded-2xl p-8 mb-8 text-white">
          <div className="flex items-center space-x-6">
            <div className="relative">
              <img
                src={user.avatar}
                alt={`${user.firstName} ${user.lastName}`}
                className="h-20 w-20 rounded-full border-4 border-white/20"
              />
              <div className="absolute -bottom-2 -right-2 bg-green-500 h-6 w-6 rounded-full border-2 border-white"></div>
            </div>
            <div>
              <h2 className="text-2xl font-bold mb-2">
                Welcome back, {user.firstName}!
              </h2>
              <p className="text-blue-100 mb-4">{user.email}</p>
              <div className="flex items-center space-x-4 text-sm">
                <div className="flex items-center">
                  <User className="h-4 w-4 mr-1" />
                  Member since {new Date(user.createdAt).toLocaleDateString()}
                </div>
                <div className="flex items-center">
                  <Calendar className="h-4 w-4 mr-1" />
                  Last login: {formatDate(user.lastLogin)}
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-200">
            <div className="flex items-center">
              <div className="p-2 bg-green-100 rounded-lg">
                <CheckCircle className="h-6 w-6 text-green-600" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Completed</p>
                <p className="text-2xl font-bold text-gray-900">
                  {completedAnalyses.length}
                </p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-200">
            <div className="flex items-center">
              <div className="p-2 bg-yellow-100 rounded-lg">
                <Clock className="h-6 w-6 text-yellow-600" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Pending</p>
                <p className="text-2xl font-bold text-gray-900">
                  {pendingAnalyses.length}
                </p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-200">
            <div className="flex items-center">
              <div className="p-2 bg-red-100 rounded-lg">
                <XCircle className="h-6 w-6 text-red-600" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Failed</p>
                <p className="text-2xl font-bold text-gray-900">
                  {failedAnalyses.length}
                </p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-200">
            <div className="flex items-center">
              <div className="p-2 bg-blue-100 rounded-lg">
                <TrendingUp className="h-6 w-6 text-blue-600" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Total</p>
                <p className="text-2xl font-bold text-gray-900">
                  {mockUserAnalyses.length}
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Analysis History */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200">
          <div className="px-6 py-4 border-b border-gray-200">
            <h3 className="text-lg font-semibold text-gray-900">
              Analysis History
            </h3>
            <p className="text-sm text-gray-600 mt-1">
              View and manage your company analysis reports
            </p>
          </div>

          <div className="divide-y divide-gray-200">
            {mockUserAnalyses.map((analysis) => (
              <div key={analysis.id} className="p-6">
                <div
                  className={`flex items-center justify-between cursor-pointer transition-all duration-200 ${
                    analysis.status === "completed" ? "hover:bg-gray-50" : ""
                  }`}
                  onClick={() => handleAnalysisClick(analysis)}
                >
                  <div className="flex items-center space-x-4">
                    {getStatusIcon(analysis.status)}
                    <div>
                      <h4 className="text-lg font-medium text-gray-900">
                        {analysis.companyName}
                      </h4>
                      <p className="text-sm text-gray-500">
                        {formatDate(analysis.analysisDate)}
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center space-x-4">
                    <span
                      className={`px-3 py-1 rounded-full text-xs font-medium border ${getStatusColor(
                        analysis.status
                      )}`}
                    >
                      {analysis.status.charAt(0).toUpperCase() +
                        analysis.status.slice(1)}
                    </span>

                    {analysis.status === "completed" && (
                      <div className="flex items-center space-x-2">
                        <button className="p-2 text-gray-400 hover:text-blue-600 transition-colors duration-200">
                          <Eye className="h-4 w-4" />
                        </button>
                        <button className="p-2 text-gray-400 hover:text-green-600 transition-colors duration-200">
                          <Download className="h-4 w-4" />
                        </button>
                        {expandedAnalysis === analysis.id ? (
                          <ChevronUp className="h-5 w-5 text-gray-400" />
                        ) : (
                          <ChevronDown className="h-5 w-5 text-gray-400" />
                        )}
                      </div>
                    )}
                  </div>
                </div>

                {/* Expanded Analysis Results */}
                {expandedAnalysis === analysis.id && analysis.result && (
                  <div className="mt-6 pt-6 border-t border-gray-200">
                    <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                      {/* SWOT Analysis Card */}
                      <div className="bg-gradient-to-br from-blue-50 to-indigo-100 rounded-xl p-6 border border-blue-200">
                        <div className="flex items-center mb-4">
                          <div className="p-2 bg-blue-500 rounded-lg">
                            <BarChart3 className="h-5 w-5 text-white" />
                          </div>
                          <h5 className="text-lg font-semibold text-gray-900 ml-3">
                            SWOT Analysis
                          </h5>
                        </div>
                        <div className="space-y-3">
                          <div className="grid grid-cols-2 gap-3">
                            <div className="bg-green-50 rounded-lg p-3 border border-green-200">
                              <h6 className="font-medium text-green-800 text-sm">
                                Strengths
                              </h6>
                              <p className="text-xs text-green-600 mt-1">
                                {analysis.result.swot_lists.strengths.length}{" "}
                                items
                              </p>
                            </div>
                            <div className="bg-red-50 rounded-lg p-3 border border-red-200">
                              <h6 className="font-medium text-red-800 text-sm">
                                Weaknesses
                              </h6>
                              <p className="text-xs text-red-600 mt-1">
                                {analysis.result.swot_lists.weaknesses.length}{" "}
                                items
                              </p>
                            </div>
                          </div>
                          <div className="grid grid-cols-2 gap-3">
                            <div className="bg-blue-50 rounded-lg p-3 border border-blue-200">
                              <h6 className="font-medium text-blue-800 text-sm">
                                Opportunities
                              </h6>
                              <p className="text-xs text-blue-600 mt-1">
                                {
                                  analysis.result.swot_lists.opportunities
                                    .length
                                }{" "}
                                items
                              </p>
                            </div>
                            <div className="bg-orange-50 rounded-lg p-3 border border-orange-200">
                              <h6 className="font-medium text-orange-800 text-sm">
                                Threats
                              </h6>
                              <p className="text-xs text-orange-600 mt-1">
                                {analysis.result.swot_lists.threats.length}{" "}
                                items
                              </p>
                            </div>
                          </div>
                        </div>
                      </div>

                      {/* Porter's Five Forces Card */}
                      <div className="bg-gradient-to-br from-purple-50 to-pink-100 rounded-xl p-6 border border-purple-200">
                        <div className="flex items-center mb-4">
                          <div className="p-2 bg-purple-500 rounded-lg">
                            <Target className="h-5 w-5 text-white" />
                          </div>
                          <h5 className="text-lg font-semibold text-gray-900 ml-3">
                            Porter's Forces
                          </h5>
                        </div>
                        <div className="space-y-2">
                          {Object.entries(analysis.result.porter_forces).map(
                            ([force, items]) => (
                              <div
                                key={force}
                                className="bg-white/60 rounded-lg p-2 border border-purple-100"
                              >
                                <p className="text-sm font-medium text-purple-800 capitalize">
                                  {force.replace("_", " ")}
                                </p>
                                <p className="text-xs text-purple-600">
                                  {items.length} factors
                                </p>
                              </div>
                            )
                          )}
                        </div>
                      </div>

                      {/* McKinsey 7S Card */}
                      <div className="bg-gradient-to-br from-green-50 to-emerald-100 rounded-xl p-6 border border-green-200">
                        <div className="flex items-center mb-4">
                          <div className="p-2 bg-green-500 rounded-lg">
                            <Users2 className="h-5 w-5 text-white" />
                          </div>
                          <h5 className="text-lg font-semibold text-gray-900 ml-3">
                            McKinsey 7S
                          </h5>
                        </div>
                        <div className="space-y-2">
                          {Object.entries(analysis.result.mckinsey_7s).map(
                            ([element, value]) => (
                              <div
                                key={element}
                                className="bg-white/60 rounded-lg p-2 border border-green-100"
                              >
                                <p className="text-sm font-medium text-green-800 capitalize">
                                  {element.replace("_", " ")}
                                </p>
                                <p className="text-xs text-green-600 truncate">
                                  {value}
                                </p>
                              </div>
                            )
                          )}
                        </div>
                      </div>
                    </div>

                    {/* Export Buttons */}
                    <div className="mt-6 pt-4 border-t border-gray-200">
                      <ExportButtons analysisResult={analysis.result} />
                    </div>

                    {/* Full Analysis View */}
                    <div className="mt-6">
                      <AnalysisDisplay
                        analysisResult={analysis.result}
                        companyName={analysis.companyName}
                      />
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>

          {mockUserAnalyses.length === 0 && (
            <div className="text-center py-12">
              <Building2 className="h-12 w-12 text-gray-400 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">
                No analyses yet
              </h3>
              <p className="text-gray-500">
                Start by analyzing your first company!
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
