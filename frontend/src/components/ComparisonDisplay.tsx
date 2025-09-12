import React, { useState } from "react";
import {
  ChevronDown,
  ChevronUp,
  BarChart3,
  TrendingUp,
  Users,
  Target,
  Shield,
  AlertTriangle,
  Zap,
  Building2,
} from "lucide-react";

interface ComparisonDisplayProps {
  comparisonResult: any;
  companyNames: string[];
}

const ComparisonDisplay: React.FC<ComparisonDisplayProps> = ({
  comparisonResult,
  companyNames,
}) => {
  const [expandedSections, setExpandedSections] = useState<Set<string>>(
    new Set(["overview"])
  );

  const toggleSection = (section: string) => {
    const newExpandedSections = new Set(expandedSections);
    if (expandedSections.has(section)) {
      newExpandedSections.delete(section);
    } else {
      newExpandedSections.add(section);
    }
    setExpandedSections(newExpandedSections);
  };

  const renderSection = (
    key: string,
    title: string,
    content: any,
    icon: React.ReactNode
  ) => {
    if (!content) return null;

    const isExpanded = expandedSections.has(key);

    return (
      <div className="border border-gray-200 rounded-lg overflow-hidden">
        <button
          onClick={() => toggleSection(key)}
          className="w-full px-6 py-4 bg-gray-50 hover:bg-gray-100 flex items-center justify-between text-left transition-colors"
        >
          <div className="flex items-center">
            {icon}
            <h3 className="text-lg font-semibold text-gray-900 ml-3">
              {title}
            </h3>
          </div>
          {isExpanded ? (
            <ChevronUp className="h-5 w-5 text-gray-500" />
          ) : (
            <ChevronDown className="h-5 w-5 text-gray-500" />
          )}
        </button>
        {isExpanded && (
          <div className="px-6 py-4 bg-white">
            {typeof content === "string" ? (
              <div className="prose max-w-none text-gray-700 leading-relaxed">
                {content.split("\n").map((line, index) => (
                  <p key={index} className="mb-2 last:mb-0">
                    {line}
                  </p>
                ))}
              </div>
            ) : Array.isArray(content) ? (
              <ul className="space-y-2">
                {content.map((item, index) => (
                  <li key={index} className="flex items-start">
                    <span className="inline-block w-2 h-2 bg-purple-500 rounded-full mt-2 mr-3 flex-shrink-0"></span>
                    <span className="text-gray-700">{item}</span>
                  </li>
                ))}
              </ul>
            ) : (
              <div className="text-gray-700">
                <pre className="whitespace-pre-wrap font-sans">
                  {JSON.stringify(content, null, 2)}
                </pre>
              </div>
            )}
          </div>
        )}
      </div>
    );
  };

  return (
    <div className="space-y-6">
      {/* Companies Overview */}
      <div className="bg-gradient-to-r from-purple-50 to-blue-50 rounded-xl p-6 border border-purple-200">
        <div className="flex items-center mb-4">
          <Building2 className="h-6 w-6 text-purple-600 mr-3" />
          <h2 className="text-xl font-bold text-gray-900">
            Comparison Overview
          </h2>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {companyNames.map((company, index) => (
            <div key={index} className="bg-white rounded-lg p-4 shadow-sm">
              <div className="flex items-center">
                <div className="p-2 bg-blue-100 rounded-lg">
                  <Building2 className="h-4 w-4 text-blue-600" />
                </div>
                <div className="ml-3">
                  <h4 className="font-semibold text-gray-900">{company}</h4>
                  <p className="text-sm text-gray-500">Company {index + 1}</p>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Comparison Results */}
      <div className="space-y-4">
        {/* Overall Analysis */}
        {renderSection(
          "overview",
          "Overall Analysis",
          comparisonResult.overallAnalysis,
          <TrendingUp className="h-5 w-5 text-blue-600" />
        )}

        {/* Key Differences */}
        {renderSection(
          "differences",
          "Key Differences",
          comparisonResult.keyDifferences,
          <BarChart3 className="h-5 w-5 text-purple-600" />
        )}

        {/* Strengths Comparison */}
        {renderSection(
          "strengths",
          "Strengths Analysis",
          comparisonResult.strengthsComparison,
          <Zap className="h-5 w-5 text-green-600" />
        )}

        {/* Strategic Recommendations */}
        {renderSection(
          "recommendations",
          "Strategic Recommendations",
          comparisonResult.strategicRecommendations,
          <Target className="h-5 w-5 text-orange-600" />
        )}

        {/* Risk Assessment */}
        {renderSection(
          "risks",
          "Risk Assessment",
          comparisonResult.riskAssessment,
          <Shield className="h-5 w-5 text-red-600" />
        )}

        {/* Market Position */}
        {renderSection(
          "market",
          "Market Position Analysis",
          comparisonResult.marketPositionAnalysis,
          <Users className="h-5 w-5 text-indigo-600" />
        )}

        {/* Competitive Advantages */}
        {renderSection(
          "advantages",
          "Competitive Advantages",
          comparisonResult.competitiveAdvantages,
          <AlertTriangle className="h-5 w-5 text-yellow-600" />
        )}
      </div>

      {/* Additional Data */}
      {comparisonResult.additionalData && (
        <div className="bg-gray-50 rounded-lg p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">
            Additional Analysis Data
          </h3>
          <pre className="text-sm text-gray-600 whitespace-pre-wrap font-mono overflow-x-auto">
            {JSON.stringify(comparisonResult.additionalData, null, 2)}
          </pre>
        </div>
      )}
    </div>
  );
};

export default ComparisonDisplay;
