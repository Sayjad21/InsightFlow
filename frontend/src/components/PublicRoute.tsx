import React from "react";
import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";

interface PublicRouteProps {
  children: React.ReactNode;
}

const PublicRoute: React.FC<PublicRouteProps> = ({ children }) => {
  const { user } = useAuth();
  const location = useLocation();

  if (user) {
    // If user is authenticated, redirect to dashboard but preserve any query parameters
    // from the intended destination or default to dashboard
    const from = (location.state as any)?.from?.pathname || "/dashboard";
    const search = (location.state as any)?.from?.search || location.search;
    return <Navigate to={`${from}${search}`} replace />;
  }

  return <>{children}</>;
};

export default PublicRoute;
