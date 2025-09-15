import React, { createContext, useContext, useState, useEffect } from "react";
import type { ReactNode } from "react";
import type { User } from "../types";
import { ApiService } from "../services/api.js";

interface AuthContextType {
  user: User | null;
  login: (email: string, password: string) => Promise<boolean>;
  signup: (
    firstName: string,
    lastName: string,
    email: string,
    password: string,
    profileImage?: File
  ) => Promise<boolean>;
  updateUserProfile: () => Promise<void>;
  logout: () => void;
  handleAuthError: () => void;
  isLoading: boolean;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({
  children,
}) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true); // Start with loading true
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  // Check if user is authenticated on mount
  useEffect(() => {
    const checkAuth = async () => {
      if (ApiService.isAuthenticated()) {
        setIsLoading(true);
        try {
          const userProfile = await ApiService.getUserProfile();
          setUser({
            id: userProfile.id,
            firstName: userProfile.firstName,
            lastName: userProfile.lastName,
            email: userProfile.email,
            avatar: userProfile.avatar,
            bio: userProfile.bio,
            createdAt: userProfile.createdAt,
            lastLogin: userProfile.lastLogin,
          });
          setIsAuthenticated(true);
        } catch (error) {
          console.error("Failed to get user profile:", error);
          ApiService.logout();
          setUser(null);
          setIsAuthenticated(false);
        } finally {
          setIsLoading(false);
        }
      } else {
        // No authentication token found
        setIsLoading(false);
      }
    };

    checkAuth();
  }, []);

  const login = async (email: string, password: string): Promise<boolean> => {
    setIsLoading(true);
    try {
      await ApiService.login({ email, password });
      setIsAuthenticated(true);

      // Try to get user profile - if this fails, still consider login successful
      try {
        const userProfile = await ApiService.getUserProfile();
        setUser({
          id: userProfile.id,
          firstName: userProfile.firstName,
          lastName: userProfile.lastName,
          email: userProfile.email,
          avatar: userProfile.avatar,
          createdAt: userProfile.createdAt,
          lastLogin: userProfile.lastLogin,
        });
      } catch (profileError) {
        console.warn("Failed to fetch profile after login:", profileError);
        // Set basic user info from login data
        setUser({
          id: "temp",
          firstName: "",
          lastName: "",
          email: email,
          avatar: `https://ui-avatars.com/api/?name=${email}&background=0D8ABC&color=fff`,
          createdAt: new Date().toISOString(),
          lastLogin: new Date().toISOString(),
        });
      }

      setIsLoading(false);
      return true;
    } catch (error) {
      console.error("Login failed:", error);
      setUser(null);
      setIsAuthenticated(false);
      setIsLoading(false);
      throw error; // Re-throw so component can show error message
    }
  };

  const signup = async (
    firstName: string,
    lastName: string,
    email: string,
    password: string,
    profileImage?: File
  ): Promise<boolean> => {
    setIsLoading(true);
    try {
      await ApiService.signup({
        firstName,
        lastName,
        email,
        password,
        profileImage,
      });
      setIsAuthenticated(true);

      // Try to get user profile - if this fails, still consider signup successful
      try {
        const userProfile = await ApiService.getUserProfile();
        setUser({
          id: userProfile.id,
          firstName: userProfile.firstName,
          lastName: userProfile.lastName,
          email: userProfile.email,
          avatar: userProfile.avatar,
          bio: userProfile.bio,
          createdAt: userProfile.createdAt,
          lastLogin: userProfile.lastLogin,
        });
      } catch (profileError) {
        console.warn("Failed to fetch profile after signup:", profileError);
        // Set basic user info from signup data
        setUser({
          id: "temp",
          firstName: firstName,
          lastName: lastName,
          email: email,
          avatar: `https://ui-avatars.com/api/?name=${firstName}+${lastName}&background=0D8ABC&color=fff`,
          createdAt: new Date().toISOString(),
          lastLogin: new Date().toISOString(),
        });
      }

      setIsLoading(false);
      return true;
    } catch (error) {
      console.error("Signup failed:", error);
      setUser(null);
      setIsAuthenticated(false);
      setIsLoading(false);
      throw error; // Re-throw so component can show error message
    }
  };
  const logout = () => {
    ApiService.logout();
    setUser(null);
    setIsAuthenticated(false);
  };

  const handleAuthError = () => {
    console.log("Handling authentication error - logging out user");
    logout();
  };

  const updateUserProfile = async () => {
    try {
      const userProfile = await ApiService.getUserProfile();
      setUser({
        id: userProfile.id,
        firstName: userProfile.firstName,
        lastName: userProfile.lastName,
        email: userProfile.email,
        avatar: userProfile.avatar,
        bio: userProfile.bio,
        createdAt: userProfile.createdAt,
        lastLogin: userProfile.lastLogin,
      });
    } catch (error) {
      console.error("Failed to update user profile:", error);
      throw error;
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        login,
        signup,
        updateUserProfile,
        logout,
        handleAuthError,
        isLoading,
        isAuthenticated,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};
