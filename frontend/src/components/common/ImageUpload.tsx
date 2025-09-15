import React, { useState, useRef } from "react";
import { Camera, Upload, X, Loader2 } from "lucide-react";
import { ImageUploadService } from "../../services/imageUploadService";

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
    medium: "w-24 h-24",
    large: "w-32 h-32",
  };

  const handleFileSelect = async (file: File) => {
    setError(null);

    // Validate the image
    const validation = ImageUploadService.validateImage(file);
    if (!validation.isValid) {
      setError(validation.error!);
      return;
    }

    try {
      // Resize the image to reduce file size
      const resizedFile = await ImageUploadService.resizeImage(file);
      const preview = ImageUploadService.createPreviewUrl(resizedFile);

      // Clean up previous preview URL
      if (previewUrl) {
        ImageUploadService.revokePreviewUrl(previewUrl);
      }

      setPreviewUrl(preview);
      setImageLoadError(false);
      onImageChange(resizedFile, preview);
    } catch (err) {
      setError("Failed to process image. Please try another file.");
      console.error("Image processing error:", err);
    }
  };

  const handleFileInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      handleFileSelect(file);
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);

    const files = Array.from(e.dataTransfer.files);
    const imageFile = files.find((file) => file.type.startsWith("image/"));

    if (imageFile) {
      handleFileSelect(imageFile);
    } else {
      setError("Please drop an image file");
    }
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(true);
  };

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
  };

  const handleRemoveImage = () => {
    if (previewUrl) {
      ImageUploadService.revokePreviewUrl(previewUrl);
      setPreviewUrl(null);
    }
    setError(null);
    setImageLoadError(false);
    onImageChange(null, null);

    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  };

  const handleClick = () => {
    fileInputRef.current?.click();
  };

  const displayImage = previewUrl || currentImage;

  return (
    <div className={`space-y-3 ${className}`}>
      {showLabel && (
        <label className="block text-sm font-medium text-gray-700">
          {label}
        </label>
      )}

      <div className="flex items-center space-x-4">
        {/* Image Preview */}
        <div
          className={`
            ${
              sizeClasses[size]
            } rounded-full overflow-hidden border-4 border-white shadow-lg
            relative group cursor-pointer bg-gray-100
            ${isUploading ? "opacity-50" : ""}
          `}
          onClick={handleClick}
        >
          {displayImage && !imageLoadError ? (
            <>
              <img
                src={displayImage}
                alt="Profile"
                className="w-full h-full object-cover"
                onError={() => {
                  console.error("Image failed to load:", displayImage);
                  setImageLoadError(true);
                }}
                onLoad={() => {
                  console.log("Image loaded successfully:", displayImage);
                  setImageLoadError(false);
                }}
              />
              {!isUploading && (
                <div className="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-50 flex items-center justify-center transition-opacity">
                  <Camera
                    className="text-white opacity-0 group-hover:opacity-100 transition-opacity"
                    size={size === "small" ? 16 : size === "medium" ? 20 : 24}
                  />
                </div>
              )}
            </>
          ) : (
            <div className="w-full h-full bg-gray-200 flex items-center justify-center">
              <Camera
                className="text-gray-400"
                size={size === "small" ? 16 : size === "medium" ? 20 : 24}
              />
            </div>
          )}

          {isUploading && (
            <div className="absolute inset-0 flex items-center justify-center bg-black bg-opacity-50">
              <Loader2
                className="animate-spin text-white"
                size={size === "small" ? 16 : size === "medium" ? 20 : 24}
              />
            </div>
          )}
        </div>

        {/* Upload Area */}
        <div className="flex-1">
          <div
            className={`
              border-2 border-dashed rounded-lg p-4 text-center transition-colors cursor-pointer
              ${
                dragActive
                  ? "border-blue-500 bg-blue-50"
                  : "border-gray-300 hover:border-gray-400"
              }
              ${isUploading ? "opacity-50 cursor-not-allowed" : ""}
            `}
            onDrop={handleDrop}
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onClick={!isUploading ? handleClick : undefined}
          >
            <Upload className="mx-auto mb-2 text-gray-400" size={20} />
            <p className="text-sm text-gray-600">
              {isUploading
                ? "Uploading..."
                : "Click to upload or drag and drop"}
            </p>
            <p className="text-xs text-gray-500 mt-1">
              PNG, JPG, WebP up to 5MB
            </p>
          </div>

          {/* Action Buttons */}
          {(displayImage || error) && !isUploading && (
            <div className="flex space-x-2 mt-2">
              {displayImage && (
                <button
                  type="button"
                  onClick={handleRemoveImage}
                  className="flex items-center px-3 py-1 text-sm text-red-600 hover:text-red-700 hover:bg-red-50 rounded transition-colors"
                >
                  <X size={14} className="mr-1" />
                  Remove
                </button>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Error Messages */}
      {error && <p className="text-sm text-red-600 mt-2">{error}</p>}
      {imageLoadError && displayImage && (
        <p className="text-sm text-yellow-600 mt-2">
          Image failed to load. Please try uploading a new one.
        </p>
      )}

      {/* Hidden File Input */}
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
