import React from "react";
import { useAuth } from "../contexts/AuthContext";
import { useUserProfile } from "../hooks/useUserProfile";
import ProfileSidebar from "./Dashboard/ProfileSidebar";

interface LayoutProps {
  children: React.ReactNode;
  showSidebar?: boolean;
  sidebarPosition?: "left" | "right";
  containerClass?: string;
  mainContentClass?: string;
}

const Layout: React.FC<LayoutProps> = ({
  children,
  showSidebar = true,
  sidebarPosition = "left",
  containerClass = "min-h-screen bg-gray-50",
  mainContentClass = "",
}) => {
  const { logout } = useAuth();
  const { userProfile, isLoading, error } = useUserProfile();

  const handleLogout = async () => {
    try {
      await logout();
    } catch (error) {
      console.error("Logout failed:", error);
    }
  };

  if (!showSidebar) {
    return <>{children}</>;
  }

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading...</p>
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
    <div className={containerClass}>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
          {/* Profile Sidebar */}
          {sidebarPosition === "left" && (
            <div className="lg:col-span-1">
              <ProfileSidebar
                userProfile={userProfile}
                onLogout={handleLogout}
              />
            </div>
          )}

          {/* Main Content */}
          <div
            className={`${
              sidebarPosition === "left" ? "lg:col-span-3" : "lg:col-span-4"
            } ${mainContentClass}`}
          >
            {children}
          </div>

          {/* Profile Sidebar on Right */}
          {sidebarPosition === "right" && (
            <div className="lg:col-span-1">
              <ProfileSidebar
                userProfile={userProfile}
                onLogout={handleLogout}
              />
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Layout;
