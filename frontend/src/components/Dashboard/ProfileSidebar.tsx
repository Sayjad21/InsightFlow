import React from "react";
import { Link, useLocation } from "react-router-dom";
import {
  LogOut,
  Calendar,
  Building2,
  CheckCircle,
  Clock,
  Settings,
  ChartNoAxesCombined,
  Home as HomeIcon,
} from "lucide-react";
import type { UserProfileResponse } from "../../services/api";
import { useAuth } from "../../contexts/AuthContext";

interface ProfileSidebarProps {
  userProfile: UserProfileResponse | null;
  onLogout: () => void;
}

const ProfileSidebar: React.FC<ProfileSidebarProps> = ({
  userProfile,
  onLogout,
}) => {
  const location = useLocation();
  const { user } = useAuth();

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  };

  const isActivePage = (path: string) => {
    return location.pathname === path;
  };

  return (
    <div className="bg-white rounded-2xl shadow-lg p-6 h-fit">
      {/* Profile Header */}
      <div className="text-center mb-6">
        <div className="w-20 h-20 bg-gradient-to-br from-purple-500 to-blue-600 rounded-full flex items-center justify-center mx-auto mb-4">
          {/* <User className="h-10 w-10 text-white" /> */}
          {user && (
            <img
              src={user.avatar}
              alt={`${user.firstName} ${user.lastName}`}
              className="w-20 h-20 rounded-full border-3     border-white/20"
            />
          )}
        </div>
        <h2 className="text-xl font-semibold text-gray-900">
          {userProfile?.firstName} {userProfile?.lastName}
        </h2>
        <p className="text-gray-600">{userProfile?.email}</p>
        <span className="inline-block mt-2 px-3 py-1 bg-green-100 text-green-800 rounded-full text-sm capitalize">
          {userProfile?.role}
        </span>
      </div>

      {/* Stats */}
      <div className="space-y-4 mb-6">
        <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
          <div className="flex items-center">
            <Building2 className="h-5 w-5 text-blue-600 mr-3" />
            <span className="text-gray-700">Total Analyses</span>
          </div>
          <span className="font-semibold text-blue-600">
            {userProfile?.totalAnalyses || 0}
          </span>
        </div>

        <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
          <div className="flex items-center">
            <CheckCircle className="h-5 w-5 text-green-600 mr-3" />
            <span className="text-gray-700">Successful</span>
          </div>
          <span className="font-semibold text-green-600">
            {userProfile?.successfulAnalyses || 0}
          </span>
        </div>

        <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
          <div className="flex items-center">
            <ChartNoAxesCombined className="h-5 w-5 text-indigo-600 mr-3" />
            <span className="text-gray-700">Total Comparisons</span>
          </div>
          <span className="font-semibold text-indigo-600">
            {userProfile?.totalComparisons || 0}
          </span>
        </div>

        <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
          <div className="flex items-center">
            <Calendar className="h-5 w-5 text-purple-600 mr-3" />
            <span className="text-gray-700">Member Since</span>
          </div>
          <span className="font-semibold text-purple-600 text-sm">
            {userProfile?.createdAt ? formatDate(userProfile.createdAt) : "N/A"}
          </span>
        </div>

        {userProfile?.lastLogin && (
          <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
            <div className="flex items-center">
              <Clock className="h-5 w-5 text-orange-600 mr-3" />
              <span className="text-gray-700">Last Login</span>
            </div>
            <span className="font-semibold text-orange-600 text-sm">
              {formatDate(userProfile.lastLogin)}
            </span>
          </div>
        )}
      </div>

      {/* Navigation */}
      <div className="space-y-2 mb-6">
        <Link
          to="/home"
          className={`flex items-center w-full p-3 text-left rounded-lg transition-colors ${
            isActivePage("/home")
              ? "bg-purple-100 text-purple-700 hover:bg-purple-200"
              : "text-gray-700 hover:bg-gray-100"
          }`}
        >
          <ChartNoAxesCombined className="h-5 w-5 mr-3" />
          Analysis
        </Link>
        <Link
          to="/dashboard"
          className={`flex items-center w-full p-3 text-left rounded-lg transition-colors ${
            isActivePage("/dashboard")
              ? "bg-purple-100 text-purple-700 hover:bg-purple-200"
              : "text-gray-700 hover:bg-gray-100"
          }`}
        >
          <HomeIcon className="h-5 w-5 mr-3" />
          Dashboard
        </Link>
        <Link
          to="/settings"
          className={`flex items-center w-full p-3 text-left rounded-lg transition-colors ${
            isActivePage("/settings")
              ? "bg-purple-100 text-purple-700 hover:bg-purple-200"
              : "text-gray-700 hover:bg-gray-100"
          }`}
        >
          <Settings className="h-5 w-5 mr-3" />
          Settings
        </Link>
      </div>

      {/* Logout */}
      <button
        onClick={onLogout}
        className="flex items-center w-full p-3 text-left text-red-600 rounded-lg hover:bg-red-50 transition-colors"
      >
        <LogOut className="h-5 w-5 mr-3" />
        Logout
      </button>
    </div>
  );
};

export default ProfileSidebar;
