import React from "react";
import {
  X,
  Shield,
  AlertTriangle,
  TrendingUp,
  Target,
  Users2,
  BarChart3,
  Link as LinkIcon,
  Lightbulb,
  LinkedinIcon,
} from "lucide-react";
import type { AnalysisResult } from "../types";
import { LucideLinkedin } from "lucide-react";

interface AnalysisModalProps {
  analysisResult: AnalysisResult;
  companyName?: string;
  onClose: () => void;
}

const AnalysisModal: React.FC<AnalysisModalProps> = ({
  analysisResult,
  companyName,
  onClose,
}) => {
  if (!analysisResult) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-black/70 backdrop-blur-sm"
        onClick={onClose}
      ></div>

      {/* Modal */}
      <div
        className="bg-teal-800/30 backdrop-blur-sm rounded-2xl w-full max-w-6xl max-h-[90vh] overflow-auto relative z-10 shadow-xl shadow-blue-900/20"
        style={{
          animation: "modalFadeIn 0.3s ease-out forwards",
        }}
      >
        {/* Close button */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-gray-400 hover:text-white p-2 rounded-full hover:bg-white/10 transition-colors z-10"
        >
          <X className="h-5 w-5" />
        </button>

        {/* Content */}
        <div className="p-8">
          <h2 className="text-2xl font-bold text-white mb-6">
            {companyName || analysisResult.company_name} - Full Analysis Report
          </h2>

          <div className="space-y-8">
            {/* Company Summary */}
            <div className="bg-white/5 rounded-xl p-6">
              <h3 className="text-xl font-semibold text-green-400 mb-4">
                {companyName || analysisResult.company_name} - Key Insights
              </h3>
              {analysisResult.summaries &&
                analysisResult.summaries.length > 0 && (
                  <div className="mb-6">
                    <h5 className="font-medium text-gray-300 mb-3">
                      Detailed Analysis Summaries (
                      {analysisResult.summaries.length})
                    </h5>
                    <div className="space-y-4">
                      {analysisResult.summaries.map(
                        (summary: string, idx: number) => (
                          <div
                            key={idx}
                            className="bg-gray-500/10 border border-gray-500/30 rounded-lg p-4"
                          >
                            <div className="flex items-start">
                              <span className="flex-shrink-0 w-6 h-6 bg-green-600 text-gray-900 rounded-full flex items-center justify-center text-xs font-medium mr-3 mt-0.5">
                                {idx + 1}
                              </span>
                              <div className="flex-1">
                                <div
                                  className="text-sm text-gray-200 leading-relaxed"
                                  dangerouslySetInnerHTML={{ __html: summary }}
                                />
                              </div>
                            </div>
                          </div>
                        )
                      )}
                    </div>
                  </div>
                )}
            </div>

            {/* Strategy Recommendations */}
            {analysisResult.strategy_recommendations && (
              <div className="mb-6">
                <h5 className="font-medium text-green-400 mb-3">
                  Strategic Recommendations
                </h5>
                <div className="bg-blue-500/10 border border-blue-500/30 border-l-4 border-l-blue-400 rounded-r-lg p-4">
                  <div
                    className="text-sm text-gray-200 leading-relaxed"
                    dangerouslySetInnerHTML={{
                      __html: analysisResult.strategy_recommendations,
                    }}
                  />
                </div>
              </div>
            )}

            {/* SWOT Analysis */}
            <div className="bg-white/5 rounded-xl p-6">
              <div className="flex items-center mb-6">
                <div className="p-2 bg-purple-500/20 rounded-lg border border-purple-500/30">
                  <BarChart3 className="h-6 w-6 text-purple-300" />
                </div>
                <h4 className="text-xl font-semibold text-green-400 ml-3">
                  SWOT Analysis
                </h4>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
              {/* Strengths */}
              <div className="bg-blue-500/10 border border-blue-500/30 rounded-lg p-5">
                  <div className="flex items-center mb-3">
                    <Shield className="h-5 w-5 text-blue-400 mr-2" />
                    <h5 className="font-semibold text-blue-300">Strengths</h5>
                  </div>
                  <ul className="space-y-2">
                    {analysisResult.swot_lists.strengths.map(
                      (strength: string, index: number) => (
                        <li
                          key={index}
                          className="text-sm text-blue-300 flex items-start"
                        >
                          <span className="w-2 h-2 bg-blue-400 rounded-full mt-2 mr-2 flex-shrink-0"></span>
                          {strength}
                        </li>
                      )
                    )}
                  </ul>
                </div>

              {/* Weaknesses */}
              <div className="bg-orange-500/10 border border-orange-500/30 rounded-lg p-5">
                  <div className="flex items-center mb-3">
                    <AlertTriangle className="h-5 w-5 text-orange-400 mr-2" />
                    <h5 className="font-semibold text-orange-300">Weaknesses</h5>
                  </div>
                  <ul className="space-y-2">
                    {analysisResult.swot_lists.weaknesses.map(
                      (weakness: string, index: number) => (
                        <li
                          key={index}
                          className="text-sm text-orange-300 flex items-start"
                        >
                          <span className="w-2 h-2 bg-orange-400 rounded-full mt-2 mr-2 flex-shrink-0"></span>
                          {weakness}
                        </li>
                      )
                    )}
                  </ul>
                </div>

              {/* Opportunities */}
              <div className="bg-green-500/10 border border-green-500/30 rounded-lg p-5">
                  <div className="flex items-center mb-3">
                    <TrendingUp className="h-5 w-5 text-green-400 mr-2" />
                    <h5 className="font-semibold text-green-300">
                      Opportunities
                    </h5>
                  </div>
                  <ul className="space-y-2">
                    {analysisResult.swot_lists.opportunities.map(
                      (opportunity: string, index: number) => (
                        <li
                          key={index}
                          className="text-sm text-green-300 flex items-start"
                        >
                          <span className="w-2 h-2 bg-green-400 rounded-full mt-2 mr-2 flex-shrink-0"></span>
                          {opportunity}
                        </li>
                      )
                    )}
                  </ul>
                </div>

              {/* Threats */}
              <div className="bg-red-500/10 border border-red-500/30 rounded-lg p-5">
                  <div className="flex items-center mb-3">
                    <AlertTriangle className="h-5 w-5 text-red-400 mr-2" />
                    <h5 className="font-semibold text-red-300">Threats</h5>
                  </div>
                  <ul className="space-y-2">
                    {analysisResult.swot_lists.threats.map(
                      (threat: string, index: number) => (
                        <li
                          key={index}
                          className="text-sm text-red-300 flex items-start"
                        >
                          <span className="w-2 h-2 bg-red-400 rounded-full mt-2 mr-2 flex-shrink-0"></span>
                          {threat}
                        </li>
                      )
                    )}
                  </ul>
                </div>
              </div>

              {/* SWOT Visualization */}
              {analysisResult.swot_image && (
                <div className="bg-white/5 rounded-lg p-6 mt-6 border border-gray-500/30">
                  {/* <h5 className="text-lg font-semibold text-blue-400 mb-4">
                    SWOT Matrix Visualization
                  </h5> */}
                  <div className="flex justify-center">
                    <img
                      src={
                        analysisResult.swot_image.startsWith("data:")
                          ? analysisResult.swot_image
                          : analysisResult.swot_image.startsWith("http")
                          ? analysisResult.swot_image
                          : `data:image/png;base64,${analysisResult.swot_image}`
                      }
                      alt="SWOT Analysis Diagram"
                      className="max-w-full h-auto rounded-lg shadow-sm border border-gray-500/30"
                    />
                  </div>
                </div>
              )}
            </div>

            {/* PESTEL Analysis */}
            <div className="bg-white/5 rounded-xl p-6">
              <div className="flex items-center mb-6">
                <div className="p-2 bg-emerald-500/20 rounded-lg border border-emerald-500/30">
                  <BarChart3 className="h-6 w-6 text-emerald-300" />
                </div>
                <h4 className="text-xl font-semibold text-green-400 ml-3">
                  PESTEL Analysis
                </h4>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-6">
                {Object.entries(analysisResult.pestel_lists).map(
                  ([factor, items]: [string, string[]]) => (
                    <div
                      key={factor}
                      className="bg-emerald-500/10 border border-emerald-500/30 rounded-lg p-4"
                    >
                      <h5 className="font-semibold text-emerald-300 mb-2 capitalize">
                        {factor}
                      </h5>
                      <ul className="space-y-1">
                        {items.map((item: string, index: number) => (
                          <li
                            key={index}
                            className="text-sm text-emerald-200 flex items-start"
                          >
                            <span className="w-1.5 h-1.5 bg-emerald-400 rounded-full mt-2 mr-2 flex-shrink-0"></span>
                            {item}
                          </li>
                        ))}
                      </ul>
                    </div>
                  )
                )}
              </div>

              {/* PESTEL Visualization */}
              {analysisResult.pestel_image && (
                <div className="bg-white/5 rounded-lg p-6 mt-6 border border-gray-500/30">
                  {/* <h5 className="text-lg font-semibold text-blue-400 mb-4">
                    PESTEL Matrix Visualization
                  </h5> */}
                  <div className="flex justify-center">
                    <img
                      src={
                        analysisResult.pestel_image.startsWith("data:")
                          ? analysisResult.pestel_image
                          : analysisResult.pestel_image.startsWith("http")
                          ? analysisResult.pestel_image
                          : `data:image/png;base64,${analysisResult.pestel_image}`
                      }
                      alt="PESTEL Analysis Diagram"
                      className="max-w-full h-auto rounded-lg shadow-sm border border-gray-500/30"
                    />
                  </div>
                </div>
              )}
            </div>

            {/* Porter's Five Forces */}
            <div className="bg-white/5 rounded-xl p-6">
              <div className="flex items-center mb-6">
                <div className="p-2 bg-indigo-500/20 rounded-lg border border-indigo-500/30">
                  <Target className="h-6 w-6 text-indigo-300" />
                </div>
                <h4 className="text-xl font-semibold text-green-400 ml-3">
                  Porter's Five Forces
                </h4>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-6">
                {Object.entries(analysisResult.porter_forces).map(
                  ([force, items]: [string, string[]]) => (
                    <div
                      key={force}
                      className="bg-indigo-500/10 border border-indigo-500/30 rounded-lg p-4"
                    >
                      <h5 className="font-semibold text-indigo-300 mb-2 capitalize">
                        {force.replace(/_/g, " ")}
                      </h5>
                      <ul className="space-y-1">
                        {items.map((item: string, index: number) => (
                          <li
                            key={index}
                            className="text-sm text-indigo-200 flex items-start"
                          >
                            <span className="w-1.5 h-1.5 bg-indigo-400 rounded-full mt-2 mr-2 flex-shrink-0"></span>
                            {item}
                          </li>
                        ))}
                      </ul>
                    </div>
                  )
                )}
              </div>

              {/* Porter's Forces Visualization */}
              {analysisResult.porter_image && (
                <div className="bg-white/5 rounded-lg p-6 mt-6 border border-gray-500/30">
                  <h5 className="text-lg font-semibold text-green-400 mb-4">
                    Porter's Five Forces Diagram
                  </h5>
                  <div className="flex justify-center">
                    <img
                      src={
                        analysisResult.porter_image.startsWith("data:")
                          ? analysisResult.porter_image
                          : analysisResult.porter_image.startsWith("http")
                          ? analysisResult.porter_image
                          : `data:image/png;base64,${analysisResult.porter_image}`
                      }
                      alt="Porter's Five Forces Diagram"
                      className="max-w-full h-auto rounded-lg shadow-sm border border-gray-500/30"
                    />
                  </div>
                </div>
              )}
            </div>

            {/* McKinsey 7S Framework */}
            <div className="bg-white/5 rounded-xl p-6">
              <div className="flex items-center mb-6">
                <div className="p-2 bg-green-500/20 rounded-lg border border-green-500/30">
                  <Users2 className="h-6 w-6 text-green-300" />
                </div>
                <h4 className="text-xl font-semibold text-green-400 ml-3">
                  McKinsey 7S Framework
                </h4>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
                {Object.entries(analysisResult.mckinsey_7s).map(
                  ([element, value]: [string, string]) => (
                    <div
                      key={element}
                      className="bg-green-500/10 border border-green-500/30 rounded-lg p-4"
                    >
                      <h5 className="font-semibold text-green-300 mb-2 capitalize">
                        {element.replace(/_/g, " ")}
                      </h5>
                      <p className="text-sm text-green-200">{value}</p>
                    </div>
                  )
                )}
              </div>

              {/* McKinsey 7S Visualization */}
              {analysisResult.mckinsey_image && (
                <div className="bg-white/5 rounded-lg p-6 mt-6 border border-gray-500/30">
                  <h5 className="text-lg font-semibold text-green-400 mb-4">
                    McKinsey 7S Framework Diagram
                  </h5>
                  <div className="flex justify-center">
                    <img
                      src={
                        analysisResult.mckinsey_image.startsWith("data:")
                          ? analysisResult.mckinsey_image
                          : analysisResult.mckinsey_image.startsWith("http")
                          ? analysisResult.mckinsey_image
                          : `data:image/png;base64,${analysisResult.mckinsey_image}`
                      }
                      alt="McKinsey 7S Framework Diagram"
                      className="max-w-full h-auto rounded-lg shadow-sm border border-gray-500/30"
                    />
                  </div>
                </div>
              )}
            </div>

            {/* BCG Matrix */}
            {Object.keys(analysisResult.bcg_matrix).length > 0 && (
              <div className="bg-white/5 rounded-xl p-6">
                <div className="flex items-center mb-6">
                  <div className="p-2 bg-orange-500/20 rounded-lg border border-orange-500/30">
                    <BarChart3 className="h-6 w-6 text-orange-300" />
                  </div>
                  <h4 className="text-xl font-semibold text-green-400 ml-3">
                    BCG Matrix
                  </h4>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
                  {Object.entries(analysisResult.bcg_matrix).map(
                    ([product, data]: [
                      string,
                      { market_share: number; growth_rate: number }
                    ]) => (
                      <div
                        key={product}
                        className="bg-orange-500/10 border border-orange-500/30 rounded-lg p-4"
                      >
                        <h5 className="font-semibold text-orange-300 mb-2">
                          {product}
                        </h5>
                        <div className="space-y-1 text-sm text-orange-200">
                          <p>Market Share: {data.market_share}%</p>
                          <p>Growth Rate: {data.growth_rate}%</p>
                        </div>
                      </div>
                    )
                  )}
                </div>

                {/* BCG Matrix Visualization */}
                {analysisResult.bcg_image && (
                  <div className="bg-white/5 rounded-lg p-6 mt-6 border border-gray-500/30">
                    <h5 className="text-lg font-semibold text-green-400 mb-4">
                      BCG Matrix Diagram
                    </h5>
                    <div className="flex justify-center">
                      <img
                        src={
                          analysisResult.bcg_image.startsWith("data:")
                            ? analysisResult.bcg_image
                            : analysisResult.bcg_image.startsWith("http")
                            ? analysisResult.bcg_image
                            : `data:image/png;base64,${analysisResult.bcg_image}`
                        }
                        alt="BCG Matrix Diagram"
                        className="max-w-full h-auto rounded-lg shadow-sm border border-gray-500/30"
                      />
                    </div>
                  </div>
                )}
              </div>
            )}

            {/* LinkedIn Analysis */}
            {analysisResult.linkedin_analysis && (
              <div className="mb-6">
                <h4 className="font-medium text-green-400 mb-3 flex items-center space-x-2">
                  <LinkedinIcon className="h-5 w-5 text-blue-300" />
                  <span>LinkedIn-Based Analysis</span>
                </h4>
                <div className="bg-blue-500/10 border border-blue-500/30 rounded-lg p-4">
                  <div
                    className="text-sm text-blue-200 leading-relaxed"
                    dangerouslySetInnerHTML={{
                      __html: analysisResult.linkedin_analysis,
                    }}
                  />
                </div>
              </div>
            )}

            {/* Sources */}
            {analysisResult.sources && analysisResult.sources.length > 0 && (
              <div className="bg-white/5 rounded-xl p-6">
                <h4 className="text-lg font-semibold text-green-400 mb-3">
                  <LinkIcon className="h-5 w-5 inline-block mr-2 text-blue-300" />
                  Sources
                </h4>
                <div className="space-y-2">
                  {analysisResult.sources.map(
                    (source: string, index: number) => (
                      <div key={index} className="flex items-start space-x-2">
                        <span className="text-blue-300 text-sm font-medium mt-1">
                          {index + 1}.
                        </span>
                        <a
                          href={source}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="text-blue-300 hover:text-blue-200 text-sm underline break-all"
                        >
                          {source}
                        </a>
                      </div>
                    )
                  )}
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default AnalysisModal;
