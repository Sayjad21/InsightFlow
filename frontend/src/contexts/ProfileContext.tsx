import React, { createContext, useContext } from "react";
import { useUserProfile } from "../hooks/useUserProfile";
import type { UserProfileResponse } from "../services/api";

interface ProfileContextType {
  userProfile: UserProfileResponse | null;
  isLoading: boolean;
  error: string | null;
  refreshProfile: () => void;
}

const ProfileContext = createContext<ProfileContextType | undefined>(undefined);

export const ProfileProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const userProfileData = useUserProfile();

  return (
    <ProfileContext.Provider value={userProfileData}>
      {children}
    </ProfileContext.Provider>
  );
};

export const useProfileContext = (): ProfileContextType => {
  const context = useContext(ProfileContext);
  if (!context) {
    throw new Error("useProfileContext must be used within a ProfileProvider");
  }
  return context;
};
