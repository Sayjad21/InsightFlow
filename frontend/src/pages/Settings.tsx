import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import {
  ArrowLeft,
  User,
  Trash2,
  Save,
  AlertTriangle,
  CheckCircle,
  Loader2,
  Building2,
  Calendar,
} from "lucide-react";
import { useAuth } from "../contexts/AuthContext";
import { ApiService } from "../services/api";
import Layout from "../components/Layout";
import type { UserAnalysis } from "../types";

const Settings: React.FC = () => {
  const { user } = useAuth();
  const [firstName, setFirstName] = useState(user?.firstName || "");
  const [lastName, setLastName] = useState(user?.lastName || "");
  const [userAnalyses, setUserAnalyses] = useState<UserAnalysis[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isSaving, setSaving] = useState(false);
  const [message, setMessage] = useState<{
    type: "success" | "error";
    text: string;
  } | null>(null);
  const [showDeleteModal, setShowDeleteModal] = useState<string | null>(null);
  const [isDeletingAnalysis, setIsDeletingAnalysis] = useState<string | null>(
    null
  );

  // Fetch user analyses
  useEffect(() => {
    const fetchAnalyses = async () => {
      try {
        setIsLoading(true);
        const data = await ApiService.getUserAnalyses();
        setUserAnalyses(data.analyses);
      } catch (error) {
        console.error("Failed to fetch analyses:", error);
      } finally {
        setIsLoading(false);
      }
    };

    if (user) {
      fetchAnalyses();
    }
  }, [user]);

  const handleSaveProfile = async () => {
    if (!firstName.trim() || !lastName.trim()) {
      setMessage({ type: "error", text: "Please fill in all fields." });
      return;
    }

    try {
      setSaving(true);
      // TODO: Implement API call to update user profile
      // await ApiService.updateUserProfile({ firstName, lastName });

      // For now, just show success message
      setMessage({ type: "success", text: "Profile updated successfully!" });
      setTimeout(() => setMessage(null), 3000);
    } catch (error) {
      setMessage({
        type: "error",
        text: "Failed to update profile. Please try again.",
      });
      setTimeout(() => setMessage(null), 3000);
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteAnalysis = async (analysisId: string) => {
    try {
      setIsDeletingAnalysis(analysisId);
      // TODO: Implement API call to delete analysis
      // await ApiService.deleteUserAnalysis(analysisId);

      // For now, just remove from local state
      setUserAnalyses((prev) =>
        prev.filter((analysis) => analysis.id !== analysisId)
      );
      setShowDeleteModal(null);
      setMessage({ type: "success", text: "Analysis deleted successfully!" });
      setTimeout(() => setMessage(null), 3000);
    } catch (error) {
      setMessage({
        type: "error",
        text: "Failed to delete analysis. Please try again.",
      });
      setTimeout(() => setMessage(null), 3000);
    } finally {
      setIsDeletingAnalysis(null);
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

  if (!user) return null;

  return (
    <Layout containerClass="min-h-screen bg-gradient-to-br from-gray-50 to-blue-50">
      {/* Header */}
      {/* <header className="bg-white shadow-sm border-b border-gray-200 -mt-8 -mx-4 sm:-mx-6 lg:-mx-8 px-4 sm:px-6 lg:px-8 mb-8">
        <div className="max-w-7xl mx-auto">
          <div className="flex items-center justify-between h-16">
            <div className="flex items-center">
              <Link
                to="/dashboard"
                className="flex items-center px-3 py-2 text-sm font-medium text-gray-700 hover:text-gray-900 transition-colors duration-200 mr-4"
              >
                <ArrowLeft className="h-4 w-4 mr-2" />
                Back to Dashboard
              </Link>
              <Building2 className="h-8 w-8 text-blue-600 mr-3" />
              <h1 className="text-xl font-semibold text-gray-900">Settings</h1>
            </div>
          </div>
        </div>
      </header> */}

      <div className="max-w-4xl mx-auto">
        {/* Message */}
        {message && (
          <div
            className={`mb-6 p-4 rounded-lg flex items-center ${
              message.type === "success"
                ? "bg-green-50 border border-green-200 text-green-800"
                : "bg-red-50 border border-red-200 text-red-800"
            }`}
          >
            {message.type === "success" ? (
              <CheckCircle className="h-5 w-5 mr-2" />
            ) : (
              <AlertTriangle className="h-5 w-5 mr-2" />
            )}
            {message.text}
          </div>
        )}

        {/* Profile Settings */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-200 mb-8">
          <div className="px-6 py-4 border-b border-gray-200">
            <h2 className="text-lg font-semibold text-gray-900 flex items-center">
              <User className="h-5 w-5 mr-2" />
              Profile Settings
            </h2>
            <p className="text-sm text-gray-600 mt-1">
              Update your personal information
            </p>
          </div>

          <div className="p-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label
                  htmlFor="firstName"
                  className="block text-sm font-medium text-gray-700 mb-2"
                >
                  First Name
                </label>
                <input
                  type="text"
                  id="firstName"
                  value={firstName}
                  onChange={(e) => setFirstName(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                  placeholder="Enter your first name"
                />
              </div>

              <div>
                <label
                  htmlFor="lastName"
                  className="block text-sm font-medium text-gray-700 mb-2"
                >
                  Last Name
                </label>
                <input
                  type="text"
                  id="lastName"
                  value={lastName}
                  onChange={(e) => setLastName(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                  placeholder="Enter your last name"
                />
              </div>
            </div>

            <div className="mt-6">
              <button
                onClick={handleSaveProfile}
                disabled={isSaving}
                className="flex items-center px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isSaving ? (
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                ) : (
                  <Save className="h-4 w-4 mr-2" />
                )}
                {isSaving ? "Saving..." : "Save Changes"}
              </button>
            </div>
          </div>
        </div>

        {/* Analysis Management */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-200">
          <div className="px-6 py-4 border-b border-gray-200">
            <h2 className="text-lg font-semibold text-gray-900 flex items-center">
              <Building2 className="h-5 w-5 mr-2" />
              Analysis Management
            </h2>
            <p className="text-sm text-gray-600 mt-1">
              Manage your company analysis reports
            </p>
          </div>

          <div className="p-6">
            {isLoading ? (
              <div className="text-center py-8">
                <Loader2 className="h-8 w-8 animate-spin text-blue-600 mx-auto mb-4" />
                <p className="text-gray-600">Loading analyses...</p>
              </div>
            ) : userAnalyses.length > 0 ? (
              <div className="space-y-4">
                {userAnalyses.map((analysis) => (
                  <div
                    key={analysis.id}
                    className="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
                  >
                    <div className="flex items-center space-x-4">
                      <div className="p-2 bg-blue-100 rounded-lg">
                        <Building2 className="h-5 w-5 text-blue-600" />
                      </div>
                      <div>
                        <h3 className="font-medium text-gray-900">
                          {analysis.companyName}
                        </h3>
                        <p className="text-sm text-gray-500 flex items-center mt-1">
                          <Calendar className="h-4 w-4 mr-1" />
                          {formatDate(analysis.analysisDate)}
                        </p>
                      </div>
                    </div>

                    <div className="flex items-center space-x-2">
                      <span
                        className={`px-2 py-1 rounded-full text-xs font-medium ${
                          analysis.status === "COMPLETED"
                            ? "bg-green-100 text-green-800"
                            : analysis.status === "PENDING"
                            ? "bg-yellow-100 text-yellow-800"
                            : "bg-red-100 text-red-800"
                        }`}
                      >
                        {analysis.status}
                      </span>

                      <button
                        onClick={() => setShowDeleteModal(analysis.id)}
                        disabled={isDeletingAnalysis === analysis.id}
                        className="p-2 text-gray-400 hover:text-red-600 transition-colors disabled:opacity-50"
                        title="Delete Analysis"
                      >
                        {isDeletingAnalysis === analysis.id ? (
                          <Loader2 className="h-4 w-4 animate-spin" />
                        ) : (
                          <Trash2 className="h-4 w-4" />
                        )}
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-8">
                <Building2 className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                <h3 className="text-lg font-medium text-gray-900 mb-2">
                  No analyses found
                </h3>
                <p className="text-gray-500">
                  You haven't created any company analyses yet.
                </p>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Delete Confirmation Modal */}
      {showDeleteModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-2xl max-w-md w-full p-6">
            <div className="flex items-center mb-4">
              <div className="p-3 bg-red-100 rounded-full mr-4">
                <AlertTriangle className="h-6 w-6 text-red-600" />
              </div>
              <div>
                <h3 className="text-lg font-semibold text-gray-900">
                  Delete Analysis
                </h3>
                <p className="text-sm text-gray-600">
                  This action cannot be undone.
                </p>
              </div>
            </div>

            <p className="text-gray-700 mb-6">
              Are you sure you want to delete this analysis? All associated data
              will be permanently removed.
            </p>

            <div className="flex space-x-3">
              <button
                onClick={() => setShowDeleteModal(null)}
                className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={() =>
                  showDeleteModal && handleDeleteAnalysis(showDeleteModal)
                }
                disabled={isDeletingAnalysis === showDeleteModal}
                className="flex-1 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isDeletingAnalysis === showDeleteModal ? (
                  <>
                    <Loader2 className="h-4 w-4 mr-2 animate-spin inline" />
                    Deleting...
                  </>
                ) : (
                  "Delete"
                )}
              </button>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
};

export default Settings;
