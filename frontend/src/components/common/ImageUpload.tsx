import React, { useState, useRef } from "react";
import { Camera, Upload, X, Loader2 } from "lucide-react";
import { ImageUploadService } from "../../services/imageUploadService";
import { useAuth } from "../../contexts/AuthContext";

interface ImageUploadProps {
  currentImage?: string;
  onImageChange: (file: File | null, previewUrl: string | null) => void;
  isUploading?: boolean;
  className?: string;
  size?: "small" | "medium" | "large";
  showLabel?: boolean;
  label?: string;
}

const ImageUpload: React.FC<ImageUploadProps> = ({
  currentImage,
  onImageChange,
  isUploading = false,
  className = "",
  size = "medium",
  showLabel = true,
  label = "Profile Picture",
}) => {
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [dragActive, setDragActive] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [imageLoadError, setImageLoadError] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const sizeClasses = {
    small: "w-16 h-16",
    medium: "w-28 h-28",
    large: "w-36 h-36",
  };

  const handleFileSelect = async (file: File) => {
    setError(null);
    setImageLoadError(false);

    const validation = ImageUploadService.validateImage(file);
    if (!validation.isValid) {
      setError(validation.error!);
      return;
    }

    try {
      const resizedFile = await ImageUploadService.resizeImage(file);
      const preview = await ImageUploadService.fileToBase64(resizedFile);

      setPreviewUrl(preview);
      onImageChange(resizedFile, preview);
    } catch {
      setError("Failed to process image. Please try another file.");
      setImageLoadError(true);
    }
  };

  const handleFileInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) handleFileSelect(file);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);

    const files = Array.from(e.dataTransfer.files);
    const imageFile = files.find((file) => file.type.startsWith("image/"));
    if (imageFile) handleFileSelect(imageFile);
    else setError("Please drop an image file");
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    setDragActive(true);
  };

  const handleDragLeave = () => setDragActive(false);

  const handleRemoveImage = () => {
    setPreviewUrl(null);
    setError(null);
    setImageLoadError(false);
    onImageChange(null, null);
    if (fileInputRef.current) fileInputRef.current.value = "";
  };

  const handleClick = () => fileInputRef.current?.click();

  const displayImage =
    previewUrl && !imageLoadError ? previewUrl : currentImage || "";

  return (
    <div className={`space-y-3 ${className}`}>
      {showLabel && (
        <label className="block text-sm font-medium text-gray-700">
          {label}
        </label>
      )}

      <div className="flex items-center space-x-4">
        {/* Profile Image */}
        <div
          className={`
            ${
              sizeClasses[size]
            } rounded-full overflow-hidden border-4 border-white shadow-xl
            relative group cursor-pointer bg-gradient-to-tr from-gray-200 to-gray-300 transition-transform transform hover:scale-105
            ${isUploading ? "opacity-60 cursor-not-allowed" : ""}
          `}
          onClick={handleClick}
        >
          {displayImage && !imageLoadError ? (
            <>
              <img
                src={displayImage}
                alt="Profile"
                className="w-full h-full object-cover relative z-10"
                onError={() => setImageLoadError(true)}
                onLoad={() => setImageLoadError(false)}
              />
              {!isUploading && (
                <div className="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-30 flex items-center justify-center transition-opacity">
                  <Camera
                    className="text-white opacity-0 group-hover:opacity-100 transition-opacity"
                    size={size === "small" ? 16 : size === "medium" ? 20 : 24}
                  />
                </div>
              )}
            </>
          ) : (
            <div className="w-full h-full flex items-center justify-center bg-gray-200 rounded-full">
              <Camera
                className="text-gray-400 animate-pulse"
                size={size === "small" ? 16 : size === "medium" ? 20 : 24}
              />
            </div>
          )}

          {isUploading && (
            <div className="absolute inset-0 flex items-center justify-center bg-black bg-opacity-50 rounded-full">
              <Loader2
                className="animate-spin text-white"
                size={size === "small" ? 16 : size === "medium" ? 20 : 24}
              />
            </div>
          )}
        </div>

        {/* Drag & Drop Area */}
        <div className="flex-1">
          <div
            className={`
              border-2 border-dashed rounded-xl p-5 text-center transition-all duration-300 cursor-pointer
              ${
                dragActive
                  ? "border-blue-400 bg-blue-50 shadow-inner"
                  : "border-gray-300 hover:border-blue-400 hover:shadow-md"
              }
              ${isUploading ? "opacity-50 cursor-not-allowed" : ""}
            `}
            onDrop={handleDrop}
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onClick={!isUploading ? handleClick : undefined}
          >
            <Upload className="mx-auto mb-2 text-gray-400" size={20} />
            <p className="text-sm text-gray-600 font-medium">
              {isUploading ? "Uploading..." : "Click or drag & drop to upload"}
            </p>
            <p className="text-xs text-gray-500 mt-1">
              PNG, JPG, WebP up to 5MB
            </p>
          </div>

          {(displayImage || error) && !isUploading && (
            <div className="flex space-x-2 mt-2">
              {previewUrl && (
                <button
                  type="button"
                  onClick={handleRemoveImage}
                  className="flex items-center px-3 py-1 text-sm text-red-600 hover:text-red-700 hover:bg-red-50 rounded-lg shadow-sm transition-all cursor-pointer"
                >
                  <X size={14} className="mr-1" />
                  Remove
                </button>
              )}
            </div>
          )}
        </div>
      </div>

      {error && <p className="text-sm text-red-600 mt-2">{error}</p>}
      {imageLoadError && displayImage && (
        <p className="text-sm text-yellow-600 mt-2">
          Image failed to load. Please try uploading a new one.
        </p>
      )}

      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        onChange={handleFileInputChange}
        className="hidden"
        disabled={isUploading}
      />
    </div>
  );
};

export default ImageUpload;
