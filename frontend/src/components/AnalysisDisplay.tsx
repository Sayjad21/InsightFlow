import React from "react";
import {
  BarChart3,
  Target,
  Users2,
  Lightbulb,
  ExternalLink,
  LucideLinkedin,
  Link,
} from "lucide-react";
import type { AnalysisResult } from "../types";

interface AnalysisDisplayProps {
  analysisResult: AnalysisResult;
  companyName?: string;
}

const AnalysisDisplay: React.FC<AnalysisDisplayProps> = ({
  analysisResult,
  companyName,
}) => {
  if (!analysisResult) return null;

  return (
    <div className="space-y-8">
      {/* Company Summary */}
      <div className="bg-yellow-50 rounded-xl p-6 shadow-sm border border-gray-200">
        <h3 className="text-xl font-bold text-gray-900 mb-4">
          {companyName || analysisResult.company_name} - Key Insights
        </h3>
        <div className="space-y-3">
          {analysisResult.summaries.map((summary, index) => (
            <div
              key={index}
              className="text-gray-700 leading-relaxed prose prose-sm max-w-none"
              dangerouslySetInnerHTML={{ __html: summary }}
            />
          ))}
        </div>
      </div>

      {/* Strategy Recommendations */}
      <div className="bg-gradient-to-br from-blue-50 to-indigo-100 rounded-xl p-6 border border-blue-200">
        <div className="flex items-center mb-4">
          <div className="p-2 bg-blue-500 rounded-lg">
            <Lightbulb className="h-5 w-5 text-white" />
          </div>
          <h4 className="text-lg font-semibold text-gray-900 ml-3">
            Strategic Recommendations
          </h4>
        </div>
        <div
          className="text-gray-700 leading-relaxed prose prose-sm max-w-none"
          dangerouslySetInnerHTML={{
            __html: analysisResult.strategy_recommendations,
          }}
        />
      </div>

      {/* SWOT Analysis */}
      <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-200">
        <div className="flex items-center mb-6">
          <div className="p-2 bg-purple-500 rounded-lg">
            <BarChart3 className="h-6 w-6 text-white" />
          </div>
          <h4 className="text-xl font-semibold text-gray-900 ml-3">
            SWOT Analysis
          </h4>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
          {/* Strengths */}
          {/* <div className="bg-green-50 rounded-lg p-5 border border-green-200">
            <div className="flex items-center mb-3">
              <Shield className="h-5 w-5 text-green-600 mr-2" />
              <h5 className="font-semibold text-green-800">Strengths</h5>
            </div>
            <ul className="space-y-2">
              {analysisResult.swot_lists.strengths.map((strength, index) => (
                <li
                  key={index}
                  className="text-sm text-green-700 flex items-start"
                >
                  <span className="w-2 h-2 bg-green-500 rounded-full mt-2 mr-2 flex-shrink-0"></span>
                  {strength}
                </li>
              ))}
            </ul>
          </div> */}

          {/* Weaknesses */}
          {/* <div className="bg-red-50 rounded-lg p-5 border border-red-200">
            <div className="flex items-center mb-3">
              <AlertTriangle className="h-5 w-5 text-red-600 mr-2" />
              <h5 className="font-semibold text-red-800">Weaknesses</h5>
            </div>
            <ul className="space-y-2">
              {analysisResult.swot_lists.weaknesses.map((weakness, index) => (
                <li
                  key={index}
                  className="text-sm text-red-700 flex items-start"
                >
                  <span className="w-2 h-2 bg-red-500 rounded-full mt-2 mr-2 flex-shrink-0"></span>
                  {weakness}
                </li>
              ))}
            </ul>
          </div> */}

          {/* Opportunities */}
          {/* <div className="bg-blue-50 rounded-lg p-5 border border-blue-200">
            <div className="flex items-center mb-3">
              <TrendingUp className="h-5 w-5 text-blue-600 mr-2" />
              <h5 className="font-semibold text-blue-800">Opportunities</h5>
            </div>
            <ul className="space-y-2">
              {analysisResult.swot_lists.opportunities.map(
                (opportunity, index) => (
                  <li
                    key={index}
                    className="text-sm text-blue-700 flex items-start"
                  >
                    <span className="w-2 h-2 bg-blue-500 rounded-full mt-2 mr-2 flex-shrink-0"></span>
                    {opportunity}
                  </li>
                )
              )}
            </ul>
          </div> */}

          {/* Threats */}
          {/* <div className="bg-orange-50 rounded-lg p-5 border border-orange-200">
            <div className="flex items-center mb-3">
              <AlertTriangle className="h-5 w-5 text-orange-600 mr-2" />
              <h5 className="font-semibold text-orange-800">Threats</h5>
            </div>
            <ul className="space-y-2">
              {analysisResult.swot_lists.threats.map((threat, index) => (
                <li
                  key={index}
                  className="text-sm text-orange-700 flex items-start"
                >
                  <span className="w-2 h-2 bg-orange-500 rounded-full mt-2 mr-2 flex-shrink-0"></span>
                  {threat}
                </li>
              ))}
            </ul>
          </div> */}
        </div>

        {/* SWOT Visualization */}
        {analysisResult.swot_image && (
          <div className="bg-white rounded-lg p-6 shadow-sm border border-gray-200 mt-6">
            <h5 className="text-lg font-semibold text-gray-900 mb-4">
              SWOT Matrix Visualization
            </h5>
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
                className="max-w-full h-auto rounded-lg shadow-sm"
              />
            </div>
          </div>
        )}
      </div>

      {/* PESTEL Analysis */}
      <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-200">
        <div className="flex items-center mb-6">
          <div className="p-2 bg-emerald-500 rounded-lg">
            <BarChart3 className="h-6 w-6 text-white" />
          </div>
          <h4 className="text-xl font-semibold text-gray-900 ml-3">
            PESTEL Analysis
          </h4>
        </div>

        {/* <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-6">
          {Object.entries(analysisResult.pestel_lists).map(
            ([factor, items]) => (
              <div
                key={factor}
                className="bg-emerald-50 rounded-lg p-4 border border-emerald-200"
              >
                <h5 className="font-semibold text-emerald-800 mb-2 capitalize">
                  {factor}
                </h5>
                <ul className="space-y-1">
                  {items.map((item, index) => (
                    <li
                      key={index}
                      className="text-sm text-emerald-700 flex items-start"
                    >
                      <span className="w-1.5 h-1.5 bg-emerald-500 rounded-full mt-2 mr-2 flex-shrink-0"></span>
                      {item}
                    </li>
                  ))}
                </ul>
              </div>
            )
          )}
        </div> */}

        {/* PESTEL Visualization */}
        {analysisResult.pestel_image && (
          <div className="bg-white rounded-lg p-6 shadow-sm border border-gray-200 mt-6">
            <h5 className="text-lg font-semibold text-gray-900 mb-4">
              PESTEL Matrix Visualization
            </h5>
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
                className="max-w-full h-auto rounded-lg shadow-sm"
              />
            </div>
          </div>
        )}
      </div>

      {/* Porter's Five Forces */}
      <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-200">
        <div className="flex items-center mb-6">
          <div className="p-2 bg-indigo-500 rounded-lg">
            <Target className="h-6 w-6 text-white" />
          </div>
          <h4 className="text-xl font-semibold text-gray-900 ml-3">
            Porter's Five Forces
          </h4>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-6">
          {Object.entries(analysisResult.porter_forces).map(
            ([force, items]) => (
              <div
                key={force}
                className="bg-indigo-50 rounded-lg p-4 border border-indigo-200"
              >
                <h5 className="font-semibold text-indigo-800 mb-2 capitalize">
                  {force.replace("_", " ")}
                </h5>
                <ul className="space-y-1">
                  {items.map((item, index) => (
                    <li
                      key={index}
                      className="text-sm text-indigo-700 flex items-start"
                    >
                      <span className="w-1.5 h-1.5 bg-indigo-500 rounded-full mt-2 mr-2 flex-shrink-0"></span>
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
          <div className="bg-white rounded-lg p-6 shadow-sm border border-gray-200 mt-6">
            <h5 className="text-lg font-semibold text-gray-900 mb-4">
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
                className="max-w-full h-auto rounded-lg shadow-sm"
              />
            </div>
          </div>
        )}
      </div>

      {/* McKinsey 7S Framework */}
      <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-200">
        <div className="flex items-center mb-6">
          <div className="p-2 bg-green-500 rounded-lg">
            <Users2 className="h-6 w-6 text-white" />
          </div>
          <h4 className="text-xl font-semibold text-gray-900 ml-3">
            McKinsey 7S Framework
          </h4>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
          {Object.entries(analysisResult.mckinsey_7s).map(
            ([element, value]) => (
              <div
                key={element}
                className="bg-green-50 rounded-lg p-4 border border-green-200"
              >
                <h5 className="font-semibold text-green-800 mb-2 capitalize">
                  {element.replace("_", " ")}
                </h5>
                <p className="text-sm text-green-700">{value}</p>
              </div>
            )
          )}
        </div>

        {/* McKinsey 7S Visualization */}
        {analysisResult.mckinsey_image && (
          <div className="bg-white rounded-lg p-6 shadow-sm border border-gray-200 mt-6">
            <h5 className="text-lg font-semibold text-gray-900 mb-4">
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
                className="max-w-full h-auto rounded-lg shadow-sm"
              />
            </div>
          </div>
        )}
      </div>

      {/* BCG Matrix */}
      {Object.keys(analysisResult.bcg_matrix).length > 0 && (
        <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-200">
          <div className="flex items-center mb-6">
            <div className="p-2 bg-orange-500 rounded-lg">
              <BarChart3 className="h-6 w-6 text-white" />
            </div>
            <h4 className="text-xl font-semibold text-gray-900 ml-3">
              BCG Matrix
            </h4>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
            {Object.entries(analysisResult.bcg_matrix).map(
              ([product, data]) => (
                <div
                  key={product}
                  className="bg-orange-50 rounded-lg p-4 border border-orange-200"
                >
                  <h5 className="font-semibold text-orange-800 mb-2">
                    {product}
                  </h5>
                  <div className="space-y-1">
                    <p className="text-sm text-orange-700">
                      Market Share: {(data.market_share * 100).toFixed(1)}%
                    </p>
                    <p className="text-sm text-orange-700">
                      Growth Rate: {data.growth_rate.toFixed(1)}%
                    </p>
                  </div>
                </div>
              )
            )}
          </div>

          {/* BCG Matrix Visualization */}
          {analysisResult.bcg_image && (
            <div className="bg-white rounded-lg p-6 shadow-sm border border-gray-200 mt-6">
              <h5 className="text-lg font-semibold text-gray-900 mb-4">
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
                  className="max-w-full h-auto rounded-lg shadow-sm"
                />
              </div>
            </div>
          )}
        </div>
      )}

      {/* LinkedIn Analysis */}
      {analysisResult.linkedin_analysis && (
        <div className="bg-blue-50 rounded-xl p-6 border border-blue-200">
          <h4 className="text-lg font-semibold text-blue-900 mb-3 flex items-center gap-2">
            <LucideLinkedin className="h-5 w-5 text-blue-700" />
            Analysis
          </h4>

          <div
            className="text-blue-800 prose prose-sm max-w-none"
            dangerouslySetInnerHTML={{
              __html: analysisResult.linkedin_analysis,
            }}
          />
        </div>
      )}

      {/* Sources */}
      {analysisResult.sources && analysisResult.sources.length > 0 && (
        <div className="bg-pink-50 rounded-xl p-6 border border-gray-200">
          <h4 className="text-lg font-semibold text-gray-900 mb-3">
            <Link className="h-5 w-5 inline-block mr-2 text-gray-700" />
            Sources
          </h4>
          <div className="space-y-2">
            {analysisResult.sources.map((source, index) => (
              <div
                key={index}
                className="flex items-center text-sm text-gray-600"
              >
                <ExternalLink className="h-4 w-4 mr-2" />
                <a
                  href={source}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="hover:text-blue-600 transition-colors duration-200"
                >
                  {new URL(source).origin}
                </a>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default AnalysisDisplay;
