import React, { useState, useRef } from "react";
import Header from "../components/Header.js";
import ServiceCard from "../components/ServiceCard.js";
import ServiceModal from "../components/ServiceModal.js";
import Layout from "../components/Layout";
import { services } from "../data/services";
import type { Service, AnalysisResult } from "../types";
import type { LayoutRef } from "../components/Layout";

const Home: React.FC = () => {
  const layoutRef = useRef<LayoutRef>(null);
  const [selectedService, setSelectedService] = useState<Service | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [analysisResult, setAnalysisResult] = useState<AnalysisResult | null>(
    null
  );

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

    // Refresh profile statistics when analysis is completed
    if (result && layoutRef.current) {
      layoutRef.current.refreshProfile();
    }
  };

  return (
    <Layout
      ref={layoutRef}
      containerClass="min-h-screen bg-gray-50"
      mainContentClass="flex flex-col"
    >
      {/* Show test page if toggled */}
      {/* {showTestPage ? (
        <div className="relative">
          <div className="absolute top-4 right-4 z-50 flex space-x-2">
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
      ) :  */}

      <div className="bg-white rounded-2xl shadow-lg overflow-hidden min-h-full">
        {/* Navigation Buttons */}
        {/* <div className="absolute top-4 right-4 z-50 flex space-x-2">
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
          </div> */}

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
        <footer className="w-full py-4 text-center text-gray-600 border-t border-gray-200 bg-gray-50">
          InsightFlow &copy; {new Date().getFullYear()}. All rights reserved.
        </footer>
      </div>
    </Layout>
  );
};

export default Home;
