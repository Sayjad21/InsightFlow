import type { Service } from "../types";

export const services: Service[] = [
  {
    id: 1,
    title: "Automated Competitive Monitoring",
    description:
      "Gathering of public information on competitors (websites, news, social media, simulated internal databases)",
    icon: "layers",
    details: {
      overview:
        "Our automated competitive monitoring service continuously tracks and analyzes competitor activities across multiple channels. We gather public information from websites, news articles, social media, and other sources to provide real-time insights into your competitive landscape.",
      benefits: [
        "Real-time competitive intelligence",
        "Comprehensive multi-channel monitoring",
        "Automated data collection and analysis",
        "Early warning system for competitive threats",
      ],
      features: [
        "Website and content monitoring",
        "Social media activity tracking",
        "News and press release analysis",
        "Product launch and pricing monitoring",
        "LinkedIn company page analysis",
        "Automated reporting and alerts",
      ],
    },
  },
  {
    id: 2,
    title: "Differentiation Strategy",
    description:
      "Analysis of collected information to provide strategic recommendations (pricing, product, positioning, innovation)",
    icon: "users",
    details: {
      overview:
        "Our differentiation strategy service analyzes your competitive landscape to identify unique positioning opportunities. We provide data-driven strategic recommendations for pricing, product development, market positioning, and innovation to help you stand out in your industry.",
      benefits: [
        "Clear competitive differentiation strategy",
        "Data-driven pricing and positioning recommendations",
        "Market opportunity identification",
        "Innovation roadmap for competitive advantage",
      ],
      features: [
        "Competitive pricing analysis and recommendations",
        "Product positioning and value proposition development",
        "Market gap analysis and opportunity identification",
        "Strategic innovation recommendations",
        "Brand differentiation strategies",
        "Go-to-market strategy optimization",
      ],
    },
  },
  {
    id: 3,
    title: "Strategic Document Generation",
    description:
      "Automated creation of structured documents (such as SWOT, Porter’s Five Forces, BCG Matrix, etc.) ready for reporting use",
    icon: "database",
    details: {
      overview:
        "Our strategic document generation service creates comprehensive business analysis reports using established strategic frameworks. We automatically generate professional documents including SWOT analysis, Porter's Five Forces, BCG Matrix, PESTEL analysis, and McKinsey 7S models, complete with visualizations and actionable insights.",
      benefits: [
        "Professional strategic analysis documents",
        "Multiple strategic frameworks in one report",
        "Visual charts and diagrams included",
        "Ready-to-use business presentations",
      ],
      features: [
        "SWOT analysis with visual matrix",
        "Porter's Five Forces assessment",
        "BCG Matrix positioning analysis",
        "PESTEL environmental analysis",
        "McKinsey 7S organizational framework",
        "Exportable charts and visualizations",
      ],
    },
  },
];
