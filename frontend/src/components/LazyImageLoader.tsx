import React, { useState, useRef, useEffect } from "react";
import { ApiService } from "../services/api";
import { Loader2, AlertCircle, Eye, EyeOff } from "lucide-react";

interface LazyImageLoaderProps {
  /** Type of visualization: 'swot', 'pestel', 'porter', 'bcg', 'mckinsey' */
  visualizationType: "swot" | "pestel" | "porter" | "bcg" | "mckinsey";
  /** Company name for single analysis visualization */
  companyName?: string;
  /** Comparison data for comparison visualizations */
  comparisonData?: any;
  /** Type of comparison chart: 'radar', 'bar', 'scatter' */
  comparisonChartType?: "radar" | "bar" | "scatter";
  /** Alt text for the image */
  altText: string;
  /** Additional CSS classes */
  className?: string;
  /** Whether to load immediately or wait for user interaction */
  loadOnDemand?: boolean;
  /** Whether to show the image initially collapsed */
  collapsible?: boolean;
  /** Title for the visualization section */
  title?: string;
}

const LazyImageLoader: React.FC<LazyImageLoaderProps> = ({
  visualizationType,
  companyName,
  comparisonData,
  comparisonChartType,
  altText,
  className = "",
  loadOnDemand = true,
  collapsible = false,
  title,
}) => {
  const [imageUrl, setImageUrl] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isVisible, setIsVisible] = useState(!collapsible);
  const [hasIntersected, setHasIntersected] = useState(false);
  const imageRef = useRef<HTMLDivElement>(null);

  // Intersection Observer for lazy loading
  useEffect(() => {
    if (!loadOnDemand || hasIntersected) return;

    const observer = new IntersectionObserver(
      (entries) => {
        const [entry] = entries;
        if (entry.isIntersecting) {
          setHasIntersected(true);
          loadImage();
        }
      },
      {
        threshold: 0.1,
        rootMargin: "50px",
      }
    );

    if (imageRef.current) {
      observer.observe(imageRef.current);
    }

    return () => {
      if (imageRef.current) {
        observer.unobserve(imageRef.current);
      }
    };
  }, [loadOnDemand, hasIntersected]);

  const loadImage = async () => {
    if (loading || imageUrl) return;

    setLoading(true);
    setError(null);

    try {
      let response: Response;

      if (comparisonData && comparisonChartType) {
        // Load comparison visualization
        const endpoint = `/api/visualizations/comparison/${comparisonChartType}`;
        response = await fetch(`http://localhost:8000${endpoint}`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            ...(ApiService.isAuthenticated() && {
              Authorization: `Bearer ${localStorage.getItem("authToken")}`,
            }),
          },
          body: JSON.stringify(comparisonData),
        });
      } else if (companyName) {
        // Load single analysis visualization
        const endpoint = `/api/visualizations/${visualizationType}/${encodeURIComponent(
          companyName
        )}`;
        response = await fetch(`http://localhost:8000${endpoint}`, {
          headers: {
            ...(ApiService.isAuthenticated() && {
              Authorization: `Bearer ${localStorage.getItem("authToken")}`,
            }),
          },
        });
      } else {
        throw new Error(
          "Either companyName or comparisonData must be provided"
        );
      }

      if (!response.ok) {
        throw new Error(`Failed to load visualization: ${response.status}`);
      }

      const blob = await response.blob();
      const url = URL.createObjectURL(blob);
      setImageUrl(url);
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Failed to load visualization"
      );
      console.error("Error loading visualization:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleLoadClick = () => {
    if (!imageUrl && !loading) {
      loadImage();
    }
  };

  const toggleVisibility = () => {
    setIsVisible(!isVisible);
    if (!isVisible && !imageUrl && !loading) {
      // Load image when expanding for the first time
      loadImage();
    }
  };

  // Cleanup blob URL on unmount
  useEffect(() => {
    return () => {
      if (imageUrl) {
        URL.revokeObjectURL(imageUrl);
      }
    };
  }, [imageUrl]);

  const renderContent = () => {
    if (loading) {
      return (
        <div className="flex items-center justify-center p-8 bg-gray-50 rounded-lg">
          <div className="flex items-center space-x-3">
            <Loader2 className="h-5 w-5 animate-spin text-purple-600" />
            <span className="text-gray-600">Generating visualization...</span>
          </div>
        </div>
      );
    }

    if (error) {
      return (
        <div className="flex items-center justify-center p-8 bg-red-50 rounded-lg border border-red-200">
          <div className="flex items-center space-x-3">
            <AlertCircle className="h-5 w-5 text-red-500" />
            <div>
              <p className="text-red-700 font-medium">
                Failed to load visualization
              </p>
              <p className="text-red-600 text-sm">{error}</p>
              <button
                onClick={() => {
                  setError(null);
                  loadImage();
                }}
                className="mt-2 px-3 py-1 bg-red-100 hover:bg-red-200 text-red-700 rounded-md text-sm transition-colors"
              >
                Retry
              </button>
            </div>
          </div>
        </div>
      );
    }

    if (!imageUrl) {
      return (
        <div className="flex items-center justify-center p-8 bg-gray-50 rounded-lg border border-gray-200">
          <button
            onClick={handleLoadClick}
            className="flex items-center space-x-3 px-4 py-2 bg-purple-600 hover:bg-purple-700 text-white rounded-md transition-colors"
          >
            <Eye className="h-4 w-4" />
            <span>Load Visualization</span>
          </button>
        </div>
      );
    }

    return (
      <div className="bg-gray-50 rounded-lg p-4 border border-gray-200">
        <img
          src={imageUrl}
          alt={altText}
          className={`w-full max-w-4xl mx-auto rounded-lg shadow-sm ${className}`}
          onError={() => setError("Failed to display image")}
        />
      </div>
    );
  };

  const containerClasses = collapsible
    ? "border border-gray-200 rounded-lg overflow-hidden"
    : "";

  return (
    <div ref={imageRef} className={containerClasses}>
      {collapsible && title && (
        <button
          onClick={toggleVisibility}
          className="w-full px-6 py-4 bg-gray-50 hover:bg-gray-100 flex items-center justify-between text-left transition-colors"
        >
          <h4 className="text-lg font-semibold text-gray-900">{title}</h4>
          {isVisible ? (
            <EyeOff className="h-5 w-5 text-gray-500" />
          ) : (
            <Eye className="h-5 w-5 text-gray-500" />
          )}
        </button>
      )}

      {isVisible && (
        <div className={collapsible ? "px-6 py-4 bg-white" : ""}>
          {renderContent()}
        </div>
      )}
    </div>
  );
};

export default LazyImageLoader;
