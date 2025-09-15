import React, { useState, useEffect } from "react";
import { useSearchParams } from "react-router-dom";
import {
  User,
  Building2,
  CheckCircle,
  AlertTriangle,
  Loader2,
  Save,
  ChartNoAxesCombined,
  FileText,
  BarChart3,
  Trash2,
} from "lucide-react";
import { useAuth } from "../contexts/AuthContext";
import { ApiService } from "../services/api";
import Layout from "../components/Layout";
import ImageUpload from "../components/common/ImageUpload";
import Pagination from "../components/common/Pagination";
import type { UserAnalysis } from "../types";

const Settings: React.FC = () => {
  const { user, updateUserProfile } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();
  const activeTab = searchParams.get("tab") || "profile";

  // Profile form state
  const [firstName, setFirstName] = useState(user?.firstName || "");
  const [lastName, setLastName] = useState(user?.lastName || "");
  const [bio, setBio] = useState("");
  const [profileImage, setProfileImage] = useState<File | null>(null);
  const [isUploadingImage, setIsUploadingImage] = useState(false);
  const [isSaving, setSaving] = useState(false);

  // Data state
  const [userAnalyses, setUserAnalyses] = useState<UserAnalysis[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalAnalyses, setTotalAnalyses] = useState(0);
  const [isLoadingAnalyses, setIsLoadingAnalyses] = useState(false);

  // Comparison state
  const [comparisonResults, setComparisonResults] = useState<any[]>([]);
  const [comparisonCurrentPage, setComparisonCurrentPage] = useState(1);
  const [comparisonTotalPages, setComparisonTotalPages] = useState(1);
  const [totalComparisons, setTotalComparisons] = useState(0);
  const [isLoadingComparisons, setIsLoadingComparisons] = useState(false);

  const [message, setMessage] = useState<{
    type: "success" | "error";
    text: string;
  } | null>(null);
  const [showDeleteModal, setShowDeleteModal] = useState<string | null>(null);
  const [isDeletingAnalysis, setIsDeletingAnalysis] = useState<string | null>(
    null
  );

  // Load user profile data including bio
  useEffect(() => {
    const loadUserProfile = async () => {
      try {
        const profile = await ApiService.getUserProfile();
        console.log("Loaded profile avatar:", profile.avatar); // Debug log
        setFirstName(profile.firstName || "");
        setLastName(profile.lastName || "");
        setBio(profile.bio || "");
      } catch (error) {
        console.error("Failed to load profile:", error);
      }
    };

    if (user) {
      console.log("Current user avatar:", user.avatar); // Debug log
      loadUserProfile();
    }
  }, [user]);

  // Fetch user analyses with pagination
  useEffect(() => {
    const fetchAnalyses = async () => {
      if (activeTab !== "analyses") return;

      try {
        setIsLoadingAnalyses(true);
        const data = await ApiService.getUserAnalyses(currentPage - 1, 10); // Convert to 0-based index
        setUserAnalyses(data.analyses);
        setTotalPages(data.totalPages);
        setTotalAnalyses(data.total);
      } catch (error) {
        console.error("Failed to fetch analyses:", error);
      } finally {
        setIsLoadingAnalyses(false);
      }
    };

    if (user) {
      fetchAnalyses();
    }
  }, [user, activeTab, currentPage]);

  // Fetch user comparisons with pagination
  useEffect(() => {
    const fetchComparisons = async () => {
      if (activeTab !== "comparisons") return;

      try {
        setIsLoadingComparisons(true);
        const response = await ApiService.getSavedComparisons(
          comparisonCurrentPage - 1,
          10
        );

        console.log("Settings comparison response:", response);

        // Handle paginated response
        if (response && typeof response === "object" && "content" in response) {
          // Handle PageableResponse structure
          setComparisonResults(response.content || []);
          setTotalComparisons(response.totalElements || 0);
          setComparisonTotalPages(response.totalPages || 1);
        } else if (Array.isArray(response)) {
          // Handle legacy array response (fallback)
          setComparisonResults(response);
          setTotalComparisons(response.length);
          setComparisonTotalPages(Math.ceil(response.length / 10));
        } else {
          setComparisonResults([]);
          setTotalComparisons(0);
          setComparisonTotalPages(1);
        }
      } catch (error) {
        console.error("Failed to fetch comparisons:", error);
        setComparisonResults([]);
        setTotalComparisons(0);
        setComparisonTotalPages(1);
      } finally {
        setIsLoadingComparisons(false);
      }
    };

    if (user) {
      fetchComparisons();
    }
  }, [user, activeTab, comparisonCurrentPage]);

  const handleImageChange = (file: File | null, _: string | null) => {
    setProfileImage(file);
    setIsUploadingImage(false);
  };

  const handleSaveProfile = async () => {
    if (!firstName.trim() || !lastName.trim()) {
      setMessage({
        type: "error",
        text: "Please fill in all required fields.",
      });
      return;
    }

    try {
      setSaving(true);
      setIsUploadingImage(true);

      await ApiService.updateUserProfile({
        firstName: firstName.trim(),
        lastName: lastName.trim(),
        bio: bio.trim() || undefined,
        profileImage: profileImage || undefined,
      });

      // Update the user context with fresh data
      await updateUserProfile();

      setMessage({ type: "success", text: "Profile updated successfully!" });
      setProfileImage(null);
      setTimeout(() => setMessage(null), 3000);
    } catch (error) {
      setMessage({
        type: "error",
        text:
          error instanceof Error
            ? error.message
            : "Failed to update profile. Please try again.",
      });
      setTimeout(() => setMessage(null), 3000);
    } finally {
      setSaving(false);
      setIsUploadingImage(false);
    }
  };

  const handleDeleteAnalysis = async (analysisId: string) => {
    try {
      setIsDeletingAnalysis(analysisId);
      await ApiService.deleteUserAnalysis(analysisId);

      // Update total count
      setTotalAnalyses((prev) => prev - 1);

      // Remove from local state
      setUserAnalyses((prev) =>
        prev.filter((analysis) => analysis.id !== analysisId)
      );

      // If this was the last item on the current page and not the first page, go to previous page
      if (userAnalyses.length === 1 && currentPage > 1) {
        setCurrentPage((prev) => prev - 1);
      }

      setShowDeleteModal(null);
      setMessage({ type: "success", text: "Analysis deleted successfully!" });
      setTimeout(() => setMessage(null), 3000);
    } catch (error) {
      setMessage({
        type: "error",
        text:
          error instanceof Error
            ? error.message
            : "Failed to delete analysis. Please try again.",
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

  const handleTabChange = (tab: string) => {
    setSearchParams({ tab });
  };

  const renderTabNavigation = () => (
    <div className="border-b border-gray-200 mb-6">
      <div className="flex space-x-8">
        {[
          { id: "profile", label: "Profile", icon: User },
          { id: "analyses", label: "Analyses", icon: Building2 },
          {
            id: "comparisons",
            label: "Comparisons",
            icon: ChartNoAxesCombined,
          },
          { id: "reports", label: "Reports", icon: FileText },
          { id: "insights", label: "Insights", icon: BarChart3 },
        ].map((tab) => {
          const Icon = tab.icon;
          const isActive = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              onClick={() => handleTabChange(tab.id)}
              className={`flex items-center px-3 py-2 border-b-2 font-medium text-sm transition-colors ${
                isActive
                  ? "border-blue-500 text-blue-600"
                  : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
              }`}
            >
              <Icon className="h-4 w-4 mr-2" />
              {tab.label}
            </button>
          );
        })}
      </div>
    </div>
  );

  if (!user) return null;

  return (
    <Layout containerClass="min-h-screen bg-gradient-to-br from-gray-50 to-blue-50">
      <div className="max-w-6xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Settings</h1>
          <p className="text-gray-600">Manage your account and preferences</p>
        </div>

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

        {/* Tab Navigation */}
        {renderTabNavigation()}

        {/* Tab Content */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-200">
          {activeTab === "profile" && (
            <>
              <div className="px-6 py-4 border-b border-gray-200">
                <h2 className="text-lg font-semibold text-gray-900 flex items-center">
                  <User className="h-5 w-5 mr-2" />
                  Profile Settings
                </h2>
                <p className="text-sm text-gray-600 mt-1">
                  Update your personal information and profile picture
                </p>
              </div>

              <div className="p-6">
                <div className="space-y-6">
                  {/* Profile Image Upload */}
                  <ImageUpload
                    key={user?.id} // Force re-render when user changes
                    currentImage={user?.avatar}
                    onImageChange={handleImageChange}
                    isUploading={isUploadingImage}
                    size="large"
                    label="Profile Picture"
                  />

                  {/* Name Fields */}
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div>
                      <label
                        htmlFor="firstName"
                        className="block text-sm font-medium text-gray-700 mb-2"
                      >
                        First Name *
                      </label>
                      <input
                        id="firstName"
                        type="text"
                        value={firstName}
                        onChange={(e) => setFirstName(e.target.value)}
                        className="block w-full px-3 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 text-gray-900 focus:border-blue-500"
                        placeholder="Enter first name"
                      />
                    </div>

                    <div>
                      <label
                        htmlFor="lastName"
                        className="block text-sm font-medium text-gray-700 mb-2"
                      >
                        Last Name *
                      </label>
                      <input
                        id="lastName"
                        type="text"
                        value={lastName}
                        onChange={(e) => setLastName(e.target.value)}
                        className="block w-full px-3 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 text-gray-900 focus:border-blue-500"
                        placeholder="Enter last name"
                      />
                    </div>
                  </div>

                  {/* Bio Field */}
                  <div>
                    <label
                      htmlFor="bio"
                      className="block text-sm font-medium text-gray-700 mb-2"
                    >
                      Bio (Optional)
                    </label>
                    <textarea
                      id="bio"
                      rows={4}
                      value={bio}
                      onChange={(e) => setBio(e.target.value)}
                      maxLength={500}
                      className="block w-full px-3 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 text-green-900 text-2xl focus:border-blue-500 resize-none"
                      placeholder="Tell us about yourself... (max 500 characters)"
                    />
                    <p className="mt-1 text-sm text-gray-500">
                      {bio.length}/500 characters
                    </p>
                  </div>

                  {/* Save Button */}
                  <div className="flex justify-end">
                    <button
                      onClick={handleSaveProfile}
                      disabled={isSaving || isUploadingImage}
                      className="flex items-center px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                    >
                      {isSaving || isUploadingImage ? (
                        <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                      ) : (
                        <Save className="h-4 w-4 mr-2" />
                      )}
                      {isSaving || isUploadingImage
                        ? "Saving..."
                        : "Save Changes"}
                    </button>
                  </div>
                </div>
              </div>
            </>
          )}

          {activeTab === "analyses" && (
            <>
              <div className="px-6 py-4 border-b border-gray-200">
                <div className="flex justify-between items-center">
                  <div>
                    <h2 className="text-lg font-semibold text-gray-900 flex items-center">
                      <Building2 className="h-5 w-5 mr-2" />
                      Analysis Management
                    </h2>
                    <p className="text-sm text-gray-600 mt-1">
                      Manage your company analysis reports
                    </p>
                  </div>
                  {totalAnalyses > 0 && (
                    <div className="text-sm text-gray-600">
                      {totalAnalyses} total analysis
                      {totalAnalyses !== 1 ? "es" : ""}
                    </div>
                  )}
                </div>
              </div>

              <div className="p-6">
                {isLoadingAnalyses ? (
                  <div className="text-center py-8">
                    <Loader2 className="h-8 w-8 animate-spin text-blue-600 mx-auto mb-4" />
                    <p className="text-gray-600">Loading analyses...</p>
                  </div>
                ) : userAnalyses.length > 0 ? (
                  <>
                    <div className="space-y-4">
                      {userAnalyses.map((analysis) => (
                        <div
                          key={analysis.id}
                          className="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:shadow-sm transition-shadow"
                        >
                          <div className="flex-1">
                            <h3 className="font-medium text-gray-900">
                              {analysis.companyName}
                            </h3>
                            <p className="text-sm text-gray-500">
                              Created: {formatDate(analysis.analysisDate)}
                            </p>
                            <span
                              className={`inline-block mt-1 px-2 py-1 text-xs rounded-full ${
                                analysis.status === "COMPLETED"
                                  ? "bg-green-100 text-green-800"
                                  : analysis.status === "PENDING"
                                  ? "bg-yellow-100 text-yellow-800"
                                  : "bg-red-100 text-red-800"
                              }`}
                            >
                              {analysis.status}
                            </span>
                          </div>

                          <button
                            onClick={() => setShowDeleteModal(analysis.id)}
                            disabled={isDeletingAnalysis === analysis.id}
                            className="ml-4 p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors disabled:opacity-50"
                          >
                            {isDeletingAnalysis === analysis.id ? (
                              <Loader2 className="h-4 w-4 animate-spin" />
                            ) : (
                              <Trash2 className="h-4 w-4" />
                            )}
                          </button>
                        </div>
                      ))}
                    </div>

                    {/* Pagination */}
                    {totalPages > 1 && (
                      <div className="mt-6 flex justify-center">
                        <Pagination
                          currentPage={currentPage}
                          totalPages={totalPages}
                          onPageChange={setCurrentPage}
                          isLoading={isLoadingAnalyses}
                        />
                      </div>
                    )}
                  </>
                ) : (
                  <div className="text-center py-12">
                    <Building2 className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                    <h3 className="text-lg font-medium text-gray-900 mb-2">
                      No analyses yet
                    </h3>
                    <p className="text-gray-600">
                      Start analyzing companies to see them here.
                    </p>
                  </div>
                )}
              </div>
            </>
          )}

          {activeTab === "comparisons" && (
            <>
              <div className="px-6 py-4 border-b border-gray-200">
                <h2 className="text-lg font-semibold text-gray-900 flex items-center">
                  <ChartNoAxesCombined className="h-5 w-5 mr-2" />
                  Comparison Management
                </h2>
                <p className="text-sm text-gray-600 mt-1">
                  Manage your company comparison reports ({totalComparisons}{" "}
                  total)
                </p>
              </div>

              <div className="p-6">
                {isLoadingComparisons ? (
                  <div className="flex items-center justify-center py-12">
                    <Loader2 className="h-8 w-8 animate-spin text-blue-600 mr-2" />
                    <span className="text-gray-600">
                      Loading comparisons...
                    </span>
                  </div>
                ) : comparisonResults.length > 0 ? (
                  <>
                    <div className="space-y-4">
                      {comparisonResults.map((comparison: any) => (
                        <div
                          key={comparison.id}
                          className="bg-white border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow"
                        >
                          <div className="flex items-start justify-between">
                            <div className="flex-1">
                              <div className="flex items-center space-x-2 mb-2">
                                <BarChart3 className="h-5 w-5 text-blue-600" />
                                <h3 className="text-lg font-medium text-gray-900">
                                  {comparison.companyNames?.join(" vs ") ||
                                    `Comparison (${
                                      comparison.numberOfCompanies || 0
                                    } companies)`}
                                </h3>
                              </div>
                              <p className="text-sm text-gray-500 mb-2">
                                Created:{" "}
                                {new Date(
                                  comparison.comparisonDate
                                ).toLocaleDateString()}
                              </p>
                              <p className="text-sm text-gray-600">
                                Type: {comparison.comparisonType || "Standard"}
                              </p>
                            </div>
                            <div className="flex space-x-2">
                              <button
                                className="px-3 py-1 bg-blue-100 text-blue-700 rounded-md hover:bg-blue-200 transition-colors text-sm"
                                onClick={() => {
                                  // Handle view comparison
                                  console.log(
                                    "View comparison:",
                                    comparison.id
                                  );
                                }}
                              >
                                View
                              </button>
                              <button
                                className="px-3 py-1 bg-red-100 text-red-700 rounded-md hover:bg-red-200 transition-colors text-sm"
                                onClick={() => {
                                  // Handle delete comparison
                                  console.log(
                                    "Delete comparison:",
                                    comparison.id
                                  );
                                }}
                              >
                                Delete
                              </button>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>

                    {/* Pagination */}
                    {comparisonTotalPages > 1 && (
                      <div className="mt-6 flex justify-center">
                        <Pagination
                          currentPage={comparisonCurrentPage}
                          totalPages={comparisonTotalPages}
                          onPageChange={setComparisonCurrentPage}
                          isLoading={isLoadingComparisons}
                        />
                      </div>
                    )}
                  </>
                ) : (
                  <div className="text-center py-12">
                    <ChartNoAxesCombined className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                    <h3 className="text-lg font-medium text-gray-900 mb-2">
                      No comparisons yet
                    </h3>
                    <p className="text-gray-600">
                      Create company comparisons to see them here.
                    </p>
                  </div>
                )}
              </div>
            </>
          )}

          {activeTab === "reports" && (
            <>
              <div className="px-6 py-4 border-b border-gray-200">
                <h2 className="text-lg font-semibold text-gray-900 flex items-center">
                  <FileText className="h-5 w-5 mr-2" />
                  Report Management
                </h2>
                <p className="text-sm text-gray-600 mt-1">
                  View and manage generated reports
                </p>
              </div>

              <div className="p-6">
                <div className="text-center py-12">
                  <FileText className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                  <h3 className="text-lg font-medium text-gray-900 mb-2">
                    Coming Soon
                  </h3>
                  <p className="text-gray-600">
                    Report management features will be available soon.
                  </p>
                </div>
              </div>
            </>
          )}

          {activeTab === "insights" && (
            <>
              <div className="px-6 py-4 border-b border-gray-200">
                <h2 className="text-lg font-semibold text-gray-900 flex items-center">
                  <BarChart3 className="h-5 w-5 mr-2" />
                  Business Insights
                </h2>
                <p className="text-sm text-gray-600 mt-1">
                  AI-generated insights from your analyses
                </p>
              </div>

              <div className="p-6">
                <div className="text-center py-12">
                  <BarChart3 className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                  <h3 className="text-lg font-medium text-gray-900 mb-2">
                    Coming Soon
                  </h3>
                  <p className="text-gray-600">
                    AI insights features will be available soon.
                  </p>
                </div>
              </div>
            </>
          )}
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
