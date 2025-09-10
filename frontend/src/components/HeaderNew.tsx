// src/components/Header.tsx
import React from "react";
import { Link } from "react-router-dom";
import { LayoutDashboard } from "lucide-react";
import { cn } from "../lib/utils.js";
import { Button } from "./ui/button.js";
import CompanyAnalysis from "./CompanyAnalysis.js";
import { useAuth } from "../contexts/AuthContext.js";

interface HeaderProps {
  onAnalysisComplete: (result: any) => void;
}

const Header: React.FC<HeaderProps> = ({ onAnalysisComplete }) => {
  const { user } = useAuth();

  return (
    <div className={cn("text-center mb-16 py-12 px-4 sm:px-6 lg:px-8")}>
      {/* User Profile Bar */}
      {user && (
        <div className="flex justify-end mb-8">
          <div className="flex items-center space-x-4 bg-white/10 backdrop-blur-sm rounded-full px-4 py-2 border border-white/20">
            <div className="flex items-center space-x-2">
              <img
                src={user.avatar}
                alt={`${user.firstName} ${user.lastName}`}
                className="w-8 h-8 rounded-full border-2 border-white/20"
              />
              <span className="text-gray-700 font-medium">
                Welcome, {user.firstName}!
              </span>
            </div>
            <Link
              to="/dashboard"
              className="flex items-center px-3 py-1.5 bg-gradient-to-r from-blue-500 to-purple-600 text-white text-sm rounded-full hover:from-blue-600 hover:to-purple-700 transition-all duration-200"
            >
              <LayoutDashboard className="h-4 w-4 mr-1" />
              Dashboard
            </Link>
          </div>
        </div>
      )}

      <h1
        className={cn(
          "text-4xl sm:text-5xl lg:text-6xl font-extrabold",
          "bg-gradient-to-r from-blue-600 to-indigo-600 bg-clip-text text-transparent",
          "animate-fade-in-down mb-6"
        )}
      >
        InsightFlow - AI-Powered Competitive Intelligence
      </h1>
      <p
        className={cn(
          "text-lg sm:text-xl lg:text-2xl text-gray-700 max-w-3xl mx-auto mb-8",
          "leading-relaxed tracking-wide",
          "animate-fade-in-up"
        )}
      >
        Unleash the future of competitive intelligence with AI-driven precision.
        InsightFlow autonomously extracts, analyzes, and transforms market data
        into bold, actionable strategies, empowering your business to outpace
        the competition.
      </p>

      {/* Call-to-Action Button */}
      <Button
        variant="default"
        size="lg"
        className={cn(
          "mt-4 bg-gradient-to-r from-blue-500 to-indigo-500 hover:from-blue-600 hover:to-indigo-600",
          "text-white font-semibold py-3 px-6 rounded-full",
          "transition-all duration-300 transform hover:scale-105"
        )}
        onClick={() =>
          document
            .getElementById("company-analysis")
            ?.scrollIntoView({ behavior: "smooth" })
        }
      >
        Start Your Analysis
      </Button>

      {/* Company Analysis Component */}
      <div id="company-analysis" className="max-w-2xl mx-auto mt-12">
        <CompanyAnalysis
          isVisible={true}
          onAnalysisComplete={onAnalysisComplete}
        />
      </div>
    </div>
  );
};

export default Header;
