import React, { useState } from "react";
import { Link } from "react-router-dom";
import { LayoutDashboard } from "lucide-react";
import Header from "../components/Header.js";
import ServiceCard from "../components/ServiceCard.js";
import ServiceModal from "../components/ServiceModal.js";
import TestPage from "../components/TestPage.js";
import { services } from "../data/services";
import type { Service, AnalysisResult } from "../types";

const Home: React.FC = () => {
  const [selectedService, setSelectedService] = useState<Service | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [analysisResult, setAnalysisResult] = useState<AnalysisResult | null>(
    null
  );
  const [showTestPage, setShowTestPage] = useState(false);

  const handleCardClick = (service: Service) => {
    setSelectedService(service);
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setTimeout(() => setSelectedService(null), 300); // Clear after animation
  };

  const handleAnalysisComplete = (result: AnalysisResult | null) => {
    setAnalysisResult(result);
  };

  // Show test page if toggled
  if (showTestPage) {
    return (
      <div>
        <div className="fixed top-4 right-4 z-50 flex space-x-2">
          <Link
            to="/dashboard"
            className="flex items-center px-4 py-2 bg-gradient-to-r from-blue-500 to-purple-600 text-white rounded-md hover:from-blue-600 hover:to-purple-700 transition-all duration-200 shadow-lg"
          >
            <LayoutDashboard className="h-4 w-4 mr-2" />
            Dashboard
          </Link>
          <button
            onClick={() => setShowTestPage(false)}
            className="px-4 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700 transition-colors duration-200 shadow-lg"
          >
            Back to Analysis
          </button>
        </div>
        <TestPage />
      </div>
    );
  }

  return (
    <div
      className="min-h-screen text-black flex flex-col"
      style={{
        background:
          "linear-gradient(135deg, #eef2ff 0%, #faf5ff 50%, #fdf2f8 100%)",
      }}
    >
      {/* Navigation Buttons */}
      <div className="fixed top-4 right-4 z-50 flex space-x-2">
        <Link
          to="/dashboard"
          className="flex items-center px-4 py-2 bg-gradient-to-r from-blue-500 to-purple-600 text-white rounded-md hover:from-blue-600 hover:to-purple-700 transition-all duration-200 shadow-lg"
        >
          <LayoutDashboard className="h-4 w-4 mr-2" />
          Dashboard
        </Link>
        <button
          onClick={() => setShowTestPage(true)}
          className="px-4 py-2 bg-green-500 text-white rounded-md hover:bg-green-600 transition-colors duration-200 shadow-lg"
        >
          Test API
        </button>
      </div>

      <div className="container mx-auto px-4 py-16 flex-grow">
        <Header onAnalysisComplete={handleAnalysisComplete} />

        {/* Service Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-5xl mx-auto">
          {services.map((service) => (
            <ServiceCard
              key={service.id}
              service={service}
              onClick={() => handleCardClick(service)}
            />
          ))}
        </div>

        {/* Modal */}
        <ServiceModal
          service={selectedService}
          isOpen={isModalOpen}
          onClose={handleCloseModal}
          analysisResult={analysisResult}
        />
      </div>

      {/* Footer */}
      <footer className="w-full py-4 text-center text-white/70 border-t border-white/10">
        Powered by Ollama, tavily
      </footer>
    </div>
  );
};

export default Home;
