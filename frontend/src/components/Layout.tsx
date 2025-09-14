import React, { useImperativeHandle, forwardRef } from "react";
import { Loader2, AlertTriangle } from "lucide-react";
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

export interface LayoutRef {
  refreshProfile: () => void;
}

const Layout = forwardRef<LayoutRef, LayoutProps>(
  (
    {
      children,
      showSidebar = true,
      sidebarPosition = "left",
      containerClass = "min-h-screen bg-gray-50",
      mainContentClass = "",
    },
    ref
  ) => {
    const { logout } = useAuth();
    const { userProfile, isLoading, error, refreshProfile } = useUserProfile();

    // Expose refreshProfile function to parent components
    useImperativeHandle(ref, () => ({
      refreshProfile,
    }));

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

    return (
      <div className={containerClass}>
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
            {/* Profile Sidebar */}
            {sidebarPosition === "left" && (
              <div className="lg:col-span-1">
                {isLoading ? (
                  <div className="bg-white rounded-2xl shadow-lg p-6 h-fit">
                    <div className="text-center">
                      <Loader2 className="h-8 w-8 animate-spin text-purple-600 mx-auto mb-4" />
                      <p className="text-gray-600">Loading profile...</p>
                    </div>
                  </div>
                ) : error ? (
                  <div className="bg-white rounded-2xl shadow-lg p-6 h-fit">
                    <div className="text-center">
                      <AlertTriangle className="h-8 w-8 text-red-600 mx-auto mb-4" />
                      <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
                        Profile Error: {error}
                      </div>
                    </div>
                  </div>
                ) : (
                  <ProfileSidebar
                    userProfile={userProfile}
                    onLogout={handleLogout}
                  />
                )}
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
                {isLoading ? (
                  <div className="bg-white rounded-2xl shadow-lg p-6 h-fit">
                    <div className="text-center">
                      <Loader2 className="h-8 w-8 animate-spin text-purple-600 mx-auto mb-4" />
                      <p className="text-gray-600">Loading profile...</p>
                    </div>
                  </div>
                ) : error ? (
                  <div className="bg-white rounded-2xl shadow-lg p-6 h-fit">
                    <div className="text-center">
                      <AlertTriangle className="h-8 w-8 text-red-600 mx-auto mb-4" />
                      <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
                        Profile Error: {error}
                      </div>
                    </div>
                  </div>
                ) : (
                  <ProfileSidebar
                    userProfile={userProfile}
                    onLogout={handleLogout}
                  />
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    );
  }
);

Layout.displayName = "Layout";

export default Layout;
