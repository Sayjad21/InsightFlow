// src/components/Header.tsx
import React from "react";
import { cn } from "../lib/utils";
import { Button } from "./ui/button";
import CompanyAnalysis from "./CompanyAnalysis";

interface HeaderProps {
  onAnalysisComplete: (result: any) => void;
}

const Header: React.FC<HeaderProps> = ({ onAnalysisComplete }) => {
  return (
    <div className={cn("text-center mb-16 py-12 px-4 sm:px-6 lg:px-8")}>
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
        InsightFlow autonomously extracts, analyzes, and transforms market
        data into bold, actionable strategies, empowering your business to
        outpace the competition.
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
