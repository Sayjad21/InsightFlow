import { useState, useEffect } from "react";
import { ApiService, type UserProfileResponse } from "../services/api";

export const useUserProfile = () => {
  const [userProfile, setUserProfile] = useState<UserProfileResponse | null>(
    null
  );
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchUserProfile = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const profileData = await ApiService.getUserProfile();
      setUserProfile(profileData);
    } catch (err) {
      console.error("Failed to fetch user profile:", err);
      setError(err instanceof Error ? err.message : "Failed to load profile");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchUserProfile();
  }, []);

  const refreshProfile = () => {
    fetchUserProfile();
  };

  return {
    userProfile,
    isLoading,
    error,
    refreshProfile,
  };
};
