import React, { useState, useEffect, useRef } from "react";
import { useSearchParams } from "react-router-dom";
import {
  User,
  Building2,
  CheckCircle,
  AlertTriangle,
  Loader2,
  Save,
  ChartNoAxesCombined,
  BarChart3,
  Trash2,
  Info,
  Heart,
  Target,
  Users,
  Zap,
  Shield,
  TrendingUp,
  Eye,
} from "lucide-react";
import { useAuth } from "../contexts/AuthContext";
import { ApiService } from "../services/api";
import Layout, { type LayoutRef } from "../components/Layout";
import ImageUpload from "../components/common/ImageUpload";
import Pagination from "../components/common/Pagination";
import ComparisonModal from "../components/Dashboard/ComparisonModal";
import type { UserAnalysis } from "../types";

const Settings: React.FC = () => {
  const layoutRef = useRef<LayoutRef>(null);
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
  const [selectedComparison, setSelectedComparison] = useState<any | null>(
    null
  );
  const [showComparisonModal, setShowComparisonModal] = useState(false);
  const [deletingComparison, setDeletingComparison] = useState<string | null>(
    null
  );
  const [showDeleteComparisonModal, setShowDeleteComparisonModal] = useState<
    string | null
  >(null);

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

  // Add smooth scrolling animation for About tab
  useEffect(() => {
    if (activeTab === "about") {
      const observer = new IntersectionObserver(
        (entries) => {
          entries.forEach((entry) => {
            if (entry.isIntersecting) {
              entry.target.classList.add("animate-fade-in-up");
            }
          });
        },
        { threshold: 0.1 }
      );

      const elements = document.querySelectorAll(".fade-in-on-scroll");
      elements.forEach((el) => observer.observe(el));

      return () => observer.disconnect();
    }
  }, [activeTab]);

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
      layoutRef.current?.refreshProfile();
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

  const handleViewComparison = async (comparisonId: string) => {
    try {
      const comparison = await ApiService.getSavedComparison(comparisonId);
      setSelectedComparison(comparison);
      setShowComparisonModal(true);
    } catch (error) {
      setMessage({
        type: "error",
        text: "Failed to load comparison. Please try again.",
      });
      setTimeout(() => setMessage(null), 3000);
    }
  };

  const handleDeleteComparison = async (comparisonId: string) => {
    try {
      setDeletingComparison(comparisonId);
      await ApiService.deleteSavedComparison(comparisonId);

      // Update total count
      setTotalComparisons((prev) => prev - 1);

      // Remove from local state
      setComparisonResults((prev) =>
        prev.filter((comparison) => comparison.id !== comparisonId)
      );

      // If this was the last item on the current page and not the first page, go to previous page
      if (comparisonResults.length === 1 && comparisonCurrentPage > 1) {
        setComparisonCurrentPage((prev) => prev - 1);
      }

      setShowDeleteComparisonModal(null);

      layoutRef.current?.refreshProfile();
      setMessage({ type: "success", text: "Comparison deleted successfully!" });
      setTimeout(() => setMessage(null), 3000);
    } catch (error) {
      setMessage({
        type: "error",
        text: "Failed to delete comparison. Please try again.",
      });
      setTimeout(() => setMessage(null), 3000);
    } finally {
      setDeletingComparison(null);
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

  const renderTabNavigation = () => {
    const tabClasses: Record<string, { active: string; inactive: string }> = {
      profile: {
        active: "border-orange-500 text-orange-600",
        inactive:
          "border-transparent text-gray-500 hover:text-orange-600 hover:border-orange-500",
      },
      analyses: {
        active: "border-green-500 text-green-600",
        inactive:
          "border-transparent text-gray-500 hover:text-green-600 hover:border-green-500",
      },
      comparisons: {
        active: "border-purple-500 text-purple-600",
        inactive:
          "border-transparent text-gray-500 hover:text-purple-600 hover:border-purple-500",
      },
      about: {
        active: "border-red-500 text-red-600",
        inactive:
          "border-transparent text-gray-500 hover:text-red-600 hover:border-red-500",
      },
    };

    return (
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
            { id: "about", label: "About", icon: Info },
          ].map((tab) => {
            const Icon = tab.icon;
            const isActive = activeTab === tab.id;

            return (
              <button
                key={tab.id}
                onClick={() => handleTabChange(tab.id)}
                className={`flex items-center px-3 py-2 border-b-2 font-medium text-sm cursor-pointer
                      transition-colors duration-300 ease-in-out
                      ${
                        isActive
                          ? tabClasses[tab.id].active
                          : `${
                              tabClasses[tab.id].inactive
                            } hover:text-opacity-90`
                      }`}
              >
                <Icon className="h-4 w-4 mr-2 transition-colors duration-300 ease-in-out" />
                {tab.label}
              </button>
            );
          })}
        </div>
      </div>
    );
  };

  if (!user) return null;

  return (
    <Layout
      ref={layoutRef}
      containerClass="min-h-screen bg-gradient-to-br from-gray-50 to-blue-50"
    >
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
        <div className="">
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
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6 animate-fade-in-up">
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
                        placeholder="Enter first name"
                        className="block w-full px-3 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 text-gray-900 focus:border-blue-500 transition duration-300 ease-in-out hover:scale-[1.02] hover:shadow-md"
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
                        placeholder="Enter last name"
                        className="block w-full px-3 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 text-gray-900 focus:border-blue-500 transition duration-300 ease-in-out hover:scale-[1.02] hover:shadow-md"
                      />
                    </div>
                  </div>

                  {/* Bio Field */}
                  <div className="animate-fade-in-up delay-100">
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
                      placeholder="Tell us about yourself... (max 500 characters)"
                      className="block w-full px-3 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 text-gray-900 focus:border-blue-500 resize-none transition duration-300 ease-in-out hover:scale-[1.01] hover:shadow-md"
                    />
                    <p className="mt-1 text-sm text-gray-500">
                      {bio.length}/500 characters
                    </p>
                  </div>

                  {/* Save Button */}
                  <div className="flex justify-end animate-fade-in-up delay-200">
                    <button
                      onClick={handleSaveProfile}
                      disabled={isSaving || isUploadingImage}
                      className="flex items-center px-6 py-2 bg-gradient-to-r from-blue-600 to-teal-600 cursor-pointer text-white rounded-lg hover:from-blue-700 hover:to-teal-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed shadow-lg transition-all duration-300 transform hover:-translate-y-0.5 hover:scale-[1.02]"
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
                      <Building2 className="h-5 w-5 mr-2 text-green-600" />
                      Analysis Management
                    </h2>
                    <p className="text-sm text-gray-600 mt-1">
                      Manage your company analysis reports
                    </p>
                  </div>
                  {totalAnalyses > 0 && (
                    <div className="text-sm text-green-800">
                      {totalAnalyses} total
                      {totalAnalyses !== 1 ? " analyses" : " analysis"}
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
                            className="ml-4 p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors disabled:opacity-50 cursor-pointer"
                            title={`Delete Analysis for ${analysis.companyName}`}
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
                  <ChartNoAxesCombined className="h-5 w-5 mr-2 text-purple-600" />
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
                                <BarChart3 className="h-5 w-5 text-purple-600" />
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
                              {/* View (Eye) button */}
                              <button
                                className="flex items-center justify-center px-3 py-1 text-purple-700 rounded-md hover:bg-purple-50 transition-colors text-sm cursor-pointer"
                                onClick={() =>
                                  handleViewComparison(comparison.id)
                                }
                              >
                                <Eye className="h-4 w-4" />
                              </button>

                              {/* Delete (Trash2) button */}
                              <button
                                className="flex items-center justify-center px-3 py-1 text-red-700 rounded-md hover:bg-red-50 transition-colors text-sm cursor-pointer disabled:opacity-50"
                                onClick={() =>
                                  setShowDeleteComparisonModal(comparison.id)
                                }
                                disabled={deletingComparison === comparison.id}
                              >
                                {deletingComparison === comparison.id ? (
                                  <div className="flex items-center">
                                    <Loader2 className="h-4 w-4 animate-spin mr-1" />
                                    Deleting...
                                  </div>
                                ) : (
                                  <Trash2 className="h-4 w-4" />
                                )}
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

          {/* {activeTab === "reports" && (
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
          )} */}

          {/* {activeTab === "insights" && (
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
          )} */}

          {activeTab === "about" && (
            <div className="animate-fade-in" key={activeTab}>
              <div className="px-6 py-4 border-b border-gray-200 fade-in-on-scroll">
                <h2 className="text-lg font-semibold text-gray-900 flex items-center">
                  <Info className="h-5 w-5 mr-2" />
                  About InsightFlow
                </h2>
                <p className="text-sm text-gray-600 mt-1">
                  Learn about our mission, features, and the technology behind
                  InsightFlow
                </p>
              </div>

              <div className="p-6 space-y-8">
                {/* Mission Section */}
                <div className="bg-gradient-to-br from-blue-50 to-indigo-50 rounded-lg p-6 fade-in-on-scroll">
                  <div className="flex items-center mb-4">
                    <Target className="h-6 w-6 text-blue-600 mr-3" />
                    <h3 className="text-xl font-semibold text-gray-900">
                      Our Mission
                    </h3>
                  </div>
                  <p className="text-gray-700 leading-relaxed">
                    InsightFlow empowers businesses and individuals to transform
                    raw data into actionable insights. Our AI-driven platform
                    democratizes advanced analytics, making sophisticated
                    business analysis accessible to everyone, regardless of
                    their technical expertise.
                  </p>
                </div>

                {/* Key Features */}
                <div className="fade-in-on-scroll">
                  <h3 className="text-xl font-semibold text-gray-900 mb-4 flex items-center">
                    <Zap className="h-6 w-6 text-yellow-500 mr-3" />
                    Key Features
                  </h3>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="bg-white border border-gray-200 rounded-lg p-4 hover:shadow-lg transition-all duration-300 transform hover:-translate-y-1">
                      <div className="flex items-center mb-2">
                        <BarChart3 className="h-5 w-5 text-purple-600 mr-2" />
                        <h4 className="font-medium text-gray-900">
                          AI-Powered Analysis
                        </h4>
                      </div>
                      <p className="text-sm text-gray-600">
                        Advanced machine learning algorithms analyze your data
                        to uncover hidden patterns and trends.
                      </p>
                    </div>
                    <div className="bg-white border border-gray-200 rounded-lg p-4 hover:shadow-lg transition-all duration-300 transform hover:-translate-y-1">
                      <div className="flex items-center mb-2">
                        <TrendingUp className="h-5 w-5 text-green-600 mr-2" />
                        <h4 className="font-medium text-gray-900">
                          Predictive Insights
                        </h4>
                      </div>
                      <p className="text-sm text-gray-600">
                        Forecast trends and identify opportunities before your
                        competition with our predictive models.
                      </p>
                    </div>
                    <div className="bg-white border border-gray-200 rounded-lg p-4 hover:shadow-lg transition-all duration-300 transform hover:-translate-y-1">
                      <div className="flex items-center mb-2">
                        <Users className="h-5 w-5 text-blue-600 mr-2" />
                        <h4 className="font-medium text-gray-900">
                          Collaborative Workspace
                        </h4>
                      </div>
                      <p className="text-sm text-gray-600">
                        Share insights with your team and collaborate on
                        analysis projects in real-time.
                      </p>
                    </div>
                    <div className="bg-white border border-gray-200 rounded-lg p-4 hover:shadow-lg transition-all duration-300 transform hover:-translate-y-1">
                      <div className="flex items-center mb-2">
                        <Shield className="h-5 w-5 text-red-600 mr-2" />
                        <h4 className="font-medium text-gray-900">
                          Enterprise Security
                        </h4>
                      </div>
                      <p className="text-sm text-gray-600">
                        Bank-level security with end-to-end encryption and
                        compliance with industry standards.
                      </p>
                    </div>
                  </div>
                </div>

                {/* Technology Stack */}
                <div className="fade-in-on-scroll">
                  <h3 className="text-xl font-semibold text-gray-900 mb-4">
                    Technology Stack
                  </h3>
                  <div className="bg-gray-50 rounded-lg p-6">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <div>
                        <h4 className="font-medium text-gray-900 mb-3">
                          Frontend
                        </h4>
                        <ul className="space-y-2 text-sm text-gray-600">
                          <li>• React with TypeScript</li>
                          <li>• Tailwind CSS for styling</li>
                          <li>• Vite for build optimization</li>
                          <li>• Responsive design principles</li>
                        </ul>
                      </div>
                      <div>
                        <h4 className="font-medium text-gray-900 mb-3">
                          Backend
                        </h4>
                        <ul className="space-y-2 text-sm text-gray-600">
                          <li>• Java Spring Boot framework</li>
                          <li>• RESTful API architecture</li>
                          <li>• MongoDB for data storage</li>
                          <li>• JWT authentication</li>
                        </ul>
                      </div>
                      <div>
                        <h4 className="font-medium text-gray-900 mb-3">
                          AI & Analytics
                        </h4>
                        <ul className="space-y-2 text-sm text-gray-600">
                          <li>• Ollama for local AI processing</li>
                          <li>• Tavily for web search integration</li>
                          <li>• Custom analysis algorithms</li>
                          <li>• Machine learning models</li>
                        </ul>
                      </div>
                      <div>
                        <h4 className="font-medium text-gray-900 mb-3">
                          Infrastructure
                        </h4>
                        <ul className="space-y-2 text-sm text-gray-600">
                          <li>• Docker containerization</li>
                          <li>• Cloud-ready deployment</li>
                          <li>• Scalable architecture</li>
                          <li>• Real-time data processing</li>
                        </ul>
                      </div>
                    </div>
                  </div>
                </div>

                {/* Company Values */}
                <div className="fade-in-on-scroll">
                  <h3 className="text-xl font-semibold text-gray-900 mb-4 flex items-center">
                    <Heart className="h-6 w-6 text-red-500 mr-3" />
                    Our Values
                  </h3>
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div className="text-center p-4 hover:bg-blue-50 rounded-lg transition-colors duration-300">
                      <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-3">
                        <Users className="h-6 w-6 text-blue-600" />
                      </div>
                      <h4 className="font-medium text-gray-900 mb-2">
                        User-Centric
                      </h4>
                      <p className="text-sm text-gray-600">
                        Every feature is designed with our users' needs and
                        success in mind.
                      </p>
                    </div>
                    <div className="text-center p-4 hover:bg-green-50 rounded-lg transition-colors duration-300">
                      <div className="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-3">
                        <TrendingUp className="h-6 w-6 text-green-600" />
                      </div>
                      <h4 className="font-medium text-gray-900 mb-2">
                        Innovation
                      </h4>
                      <p className="text-sm text-gray-600">
                        We continuously push the boundaries of what's possible
                        with AI and analytics.
                      </p>
                    </div>
                    <div className="text-center p-4 hover:bg-purple-50 rounded-lg transition-colors duration-300">
                      <div className="w-12 h-12 bg-purple-100 rounded-full flex items-center justify-center mx-auto mb-3">
                        <Shield className="h-6 w-6 text-purple-600" />
                      </div>
                      <h4 className="font-medium text-gray-900 mb-2">
                        Trust & Security
                      </h4>
                      <p className="text-sm text-gray-600">
                        Your data privacy and security are our highest
                        priorities.
                      </p>
                    </div>
                  </div>
                </div>

                {/* Version & Credits */}
                <div className="bg-gradient-to-r from-gray-50 to-blue-50 rounded-lg p-6 fade-in-on-scroll">
                  <div className="text-center">
                    <h3 className="text-xl font-semibold text-gray-900 mb-4">
                      InsightFlow v1.0
                    </h3>
                    <p className="text-gray-600 mb-4">
                      Built with passion for data-driven decision making
                    </p>
                    <div className="flex items-center justify-center space-x-4 text-sm text-gray-500">
                      <span>© 2025 InsightFlow</span>
                      <span>•</span>
                      <span>Powered by Ollama & Tavily</span>
                      <span>•</span>
                      <span>Made with ❤️</span>
                    </div>
                  </div>
                </div>

                {/* Contact & Support */}
                <div className="fade-in-on-scroll">
                  <h3 className="text-xl font-semibold text-gray-900 mb-4">
                    Get in Touch
                  </h3>
                  <div className="bg-white border border-gray-200 rounded-lg p-6 hover:shadow-lg transition-shadow duration-300">
                    <p className="text-gray-600 mb-4">
                      Have questions, feedback, or suggestions? We'd love to
                      hear from you!
                    </p>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div>
                        <h4 className="font-medium text-gray-900 mb-2">
                          Support
                        </h4>
                        <p className="text-sm text-gray-600">
                          For technical support and bug reports, please reach
                          out through our support channels.
                        </p>
                      </div>
                      <div>
                        <h4 className="font-medium text-gray-900 mb-2">
                          Feedback
                        </h4>
                        <p className="text-sm text-gray-600">
                          Your feedback helps us improve. Share your thoughts
                          and feature requests with our team.
                        </p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Delete Confirmation Modal */}
      {showDeleteModal && (
        <div
          className="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-center justify-center p-4 z-50"
          onClick={() => setShowDeleteModal(null)}
        >
          <div
            className="bg-white/10 backdrop-blur-md border border-white/20 rounded-xl shadow-2xl max-w-sm w-full sm:w-96 p-6 animate-in fade-in-10 zoom-in-95 duration-200"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="flex items-start mb-4">
              <div className="p-2 bg-red-500/10 rounded-full mr-3">
                <AlertTriangle className="h-5 w-5 text-red-400" />
              </div>
              <div>
                <h3 className="text-lg font-semibold text-white">
                  Delete Analysis
                </h3>
                <p className="text-sm text-gray-300">
                  This action cannot be undone.
                </p>
              </div>
            </div>

            <p className="text-gray-200 text-sm mb-6">
              Are you sure you want to delete this analysis? All associated data
              will be permanently removed.
            </p>

            <div className="flex space-x-3">
              <button
                onClick={() => setShowDeleteModal(null)}
                className="flex-1 px-4 py-2 bg-white/10 border border-white/20 text-gray-200 rounded-md hover:bg-white/20 transition-colors duration-200 text-sm"
              >
                Cancel
              </button>
              <button
                onClick={() =>
                  showDeleteModal && handleDeleteAnalysis(showDeleteModal)
                }
                disabled={isDeletingAnalysis === showDeleteModal}
                className="flex-1 px-4 py-2 bg-red-600/80 text-white rounded-md hover:bg-red-700/90 transition-colors duration-200 disabled:opacity-50 disabled:cursor-not-allowed text-sm"
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

      {showDeleteComparisonModal && (
        <div
          className="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-center justify-center p-4 z-50"
          onClick={() => setShowDeleteComparisonModal(null)}
        >
          <div
            className="bg-white/10 backdrop-blur-md border border-white/20 rounded-xl shadow-2xl max-w-sm w-full sm:w-96 p-6 animate-in fade-in-10 zoom-in-95 duration-200"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="flex items-start mb-4">
              <div className="p-2 bg-red-500/10 rounded-full mr-3">
                <AlertTriangle className="h-5 w-5 text-red-400" />
              </div>
              <div>
                <h3 className="text-lg font-semibold text-white">
                  Delete Comparison
                </h3>
                <p className="text-sm text-gray-300">
                  This action cannot be undone.
                </p>
              </div>
            </div>

            <p className="text-gray-200 text-sm mb-6">
              Are you sure you want to delete this comparison? All associated
              data will be permanently removed.
            </p>

            <div className="flex space-x-3">
              <button
                onClick={() => setShowDeleteComparisonModal(null)}
                className="flex-1 px-4 py-2 bg-white/10 border border-white/20 text-gray-200 rounded-md hover:bg-white/20 transition-colors duration-200 text-sm"
              >
                Cancel
              </button>
              <button
                onClick={() =>
                  showDeleteComparisonModal &&
                  handleDeleteComparison(showDeleteComparisonModal)
                }
                disabled={deletingComparison === showDeleteComparisonModal}
                className="flex-1 px-4 py-2 bg-red-600/80 text-white rounded-md hover:bg-red-700/90 transition-colors duration-200 disabled:opacity-50 disabled:cursor-not-allowed text-sm"
              >
                {deletingComparison === showDeleteComparisonModal ? (
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

      {/* Comparison Modal */}
      {showComparisonModal && selectedComparison && (
        <ComparisonModal
          selectedComparison={selectedComparison}
          onClose={() => {
            setShowComparisonModal(false);
            setSelectedComparison(null);
          }}
        />
      )}
    </Layout>
  );
};

// Add styles for fade-in animations similar to Landing page
const styles = `
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInUpCustom {
  0% {
    opacity: 0;
    transform: translateY(20px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fade-in-up {
  animation: fadeInUp 0.8s ease-out forwards;
}

.animate-fade-in {
  animation: fadeInUpCustom 0.9s ease-out both;
}

.fade-in-on-scroll {
  opacity: 0;
  transform: translateY(30px);
  transition: opacity 0.8s ease-out, transform 0.8s ease-out;
}
`;

// Inject styles
if (typeof document !== "undefined") {
  const existingStyle = document.getElementById("settings-animations");
  if (!existingStyle) {
    const styleElement = document.createElement("style");
    styleElement.id = "settings-animations";
    styleElement.textContent = styles;
    document.head.appendChild(styleElement);
  }
}

export default Settings;
