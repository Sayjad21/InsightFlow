import type { UserAnalysis, AnalysisResult } from "../types";

export const mockAnalysisResults: AnalysisResult[] = [
  {
    company_name: "Apple Inc.",
    summaries: [
      "Apple Inc. is a technology giant known for innovative consumer electronics, software, and services.",
      "The company has shown consistent growth in revenue and market capitalization over the past decade.",
      "Strong brand loyalty and ecosystem integration remain key competitive advantages.",
    ],
    sources: [
      "https://www.apple.com/investor/",
      "https://www.sec.gov/edgar/browse/?CIK=320193",
      "https://finance.yahoo.com/quote/AAPL/",
    ],
    strategy_recommendations:
      "Focus on expanding services revenue, continue innovation in AR/VR, and strengthen supply chain resilience.",
    swot_lists: {
      strengths: [
        "Strong brand recognition and customer loyalty",
        "Innovative product design and user experience",
        "Robust ecosystem integration",
        "Strong financial position",
      ],
      weaknesses: [
        "High product prices limiting market reach",
        "Dependence on iPhone revenue",
        "Limited customization options",
        "Closed ecosystem approach",
      ],
      opportunities: [
        "Expansion in emerging markets",
        "Growth in services and subscriptions",
        "AR/VR technology advancement",
        "Health and wellness market",
      ],
      threats: [
        "Intense competition in smartphone market",
        "Regulatory scrutiny and antitrust concerns",
        "Economic downturns affecting luxury spending",
        "Supply chain disruptions",
      ],
    },
    swot_image:
      "https://via.placeholder.com/600x400/1e40af/ffffff?text=Apple+SWOT+Analysis",
    pestel_image:
      "https://via.placeholder.com/600x400/dc2626/ffffff?text=Apple+PESTEL+Analysis",
    porter_forces: {
      rivalry: [
        "High competition from Samsung, Google, and other tech companies",
        "Constant innovation pressure in consumer electronics",
      ],
      new_entrants: [
        "High barriers to entry due to brand loyalty and ecosystem",
        "Significant R&D investment requirements",
      ],
      substitutes: [
        "Android smartphones and tablets",
        "Windows PCs and Chromebooks",
      ],
      buyer_power: [
        "Individual consumers have limited bargaining power",
        "Enterprise customers may have more negotiating leverage",
      ],
      supplier_power: [
        "Key component suppliers have moderate power",
        "Apple's scale provides negotiating leverage",
      ],
    },
    porter_image:
      "https://via.placeholder.com/600x400/059669/ffffff?text=Apple+Porter's+Five+Forces",
    bcg_matrix: {
      iPhone: { market_share: 0.85, growth_rate: 0.05 },
      Services: { market_share: 0.45, growth_rate: 0.2 },
      Mac: { market_share: 0.25, growth_rate: 0.08 },
      iPad: { market_share: 0.65, growth_rate: 0.03 },
    },
    bcg_image:
      "https://via.placeholder.com/600x400/7c2d12/ffffff?text=Apple+BCG+Matrix",
    mckinsey_7s: {
      strategy:
        "Innovation-driven premium positioning with ecosystem integration",
      structure: "Functional organization with product-based divisions",
      systems: "Integrated supply chain and retail operations",
      style: "Secretive, perfectionist culture with attention to detail",
      staff: "Highly skilled workforce with emphasis on design and engineering",
      skills:
        "Core competencies in design, user experience, and technology integration",
      shared_values:
        "Think different, innovation, simplicity, and user-centric design",
    },
    mckinsey_image:
      "https://via.placeholder.com/600x400/7c3aed/ffffff?text=Apple+McKinsey+7S",
    linkedin_analysis:
      "Apple's LinkedIn presence shows strong employer branding with 18M+ followers. Regular updates about company culture, diversity initiatives, and job opportunities maintain high engagement rates.",
  },
  {
    company_name: "Microsoft Corporation",
    summaries: [
      "Microsoft Corporation is a leading technology company providing software, services, devices, and solutions worldwide.",
      "The company has successfully transitioned to a cloud-first, mobile-first strategy under CEO Satya Nadella.",
      "Strong performance in Azure cloud services and Office 365 subscriptions drives consistent revenue growth.",
    ],
    sources: [
      "https://www.microsoft.com/en-us/investor",
      "https://www.sec.gov/edgar/browse/?CIK=789019",
      "https://finance.yahoo.com/quote/MSFT/",
    ],
    strategy_recommendations:
      "Continue cloud expansion, invest in AI integration across products, and expand into emerging technologies like quantum computing.",
    swot_lists: {
      strengths: [
        "Dominant position in enterprise software",
        "Strong cloud services growth",
        "Diversified product portfolio",
        "Strong financial performance",
      ],
      weaknesses: [
        "Legacy system dependencies",
        "Limited consumer hardware success",
        "Complex pricing structures",
        "Competition in mobile market",
      ],
      opportunities: [
        "AI and machine learning integration",
        "Growing cloud adoption globally",
        "Gaming market expansion",
        "Quantum computing development",
      ],
      threats: [
        "Intense competition from Google and Amazon",
        "Cybersecurity threats and data breaches",
        "Regulatory compliance requirements",
        "Open source software alternatives",
      ],
    },
    swot_image:
      "https://via.placeholder.com/600x400/1e40af/ffffff?text=Microsoft+SWOT+Analysis",
    pestel_image:
      "https://via.placeholder.com/600x400/dc2626/ffffff?text=Microsoft+PESTEL+Analysis",
    porter_forces: {
      rivalry: [
        "Intense competition with Google, Amazon, and Apple",
        "Constant innovation in cloud and productivity software",
      ],
      new_entrants: [
        "High barriers due to established customer base",
        "Significant investment required for cloud infrastructure",
      ],
      substitutes: [
        "Open source alternatives like Linux and LibreOffice",
        "Google Workspace and other productivity suites",
      ],
      buyer_power: [
        "Enterprise customers have significant negotiating power",
        "Individual consumers have limited influence",
      ],
      supplier_power: [
        "Hardware suppliers have moderate power",
        "Software partnerships are strategically important",
      ],
    },
    porter_image:
      "https://via.placeholder.com/600x400/059669/ffffff?text=Microsoft+Porter's+Five+Forces",
    bcg_matrix: {
      Azure: { market_share: 0.35, growth_rate: 0.45 },
      "Office 365": { market_share: 0.55, growth_rate: 0.15 },
      Windows: { market_share: 0.75, growth_rate: 0.02 },
      Gaming: { market_share: 0.25, growth_rate: 0.12 },
    },
    bcg_image:
      "https://via.placeholder.com/600x400/7c2d12/ffffff?text=Microsoft+BCG+Matrix",
    mckinsey_7s: {
      strategy: "Cloud-first, AI-driven transformation with enterprise focus",
      structure: "Product divisions with strong cloud integration",
      systems: "Global cloud infrastructure and enterprise solutions",
      style: "Collaborative, inclusive culture under Nadella's leadership",
      staff: "Diverse workforce with strong engineering capabilities",
      skills: "Cloud computing, AI/ML, enterprise software development",
      shared_values:
        "Empower every person and organization on the planet to achieve more",
    },
    mckinsey_image:
      "https://via.placeholder.com/600x400/7c3aed/ffffff?text=Microsoft+McKinsey+7S",
    linkedin_analysis:
      "Microsoft maintains strong LinkedIn presence with 15M+ followers. Focus on thought leadership, diversity content, and workplace culture showcases the company's transformation.",
  },
  {
    company_name: "Amazon.com Inc.",
    summaries: [
      "Amazon.com Inc. is a multinational technology and e-commerce company based in Seattle, Washington.",
      "The company has diversified from online retail to cloud computing, digital streaming, and artificial intelligence.",
      "AWS (Amazon Web Services) has become a major profit driver, leading the cloud computing market.",
    ],
    sources: [
      "https://www.amazon.com/ir",
      "https://www.sec.gov/edgar/browse/?CIK=1018724",
      "https://finance.yahoo.com/quote/AMZN/",
    ],
    strategy_recommendations:
      "Expand international presence, invest in logistics automation, and continue AWS innovation while improving retail profitability.",
    swot_lists: {
      strengths: [
        "Market leadership in e-commerce and cloud computing",
        "Vast logistics and distribution network",
        "Strong customer loyalty and Prime membership",
        "Continuous innovation and diversification",
      ],
      weaknesses: [
        "Low profit margins in retail business",
        "Dependence on third-party sellers",
        "High employee turnover rates",
        "Regulatory scrutiny over market dominance",
      ],
      opportunities: [
        "Expansion in emerging markets",
        "Growth in advertising business",
        "Healthcare and pharmacy services",
        "Autonomous delivery and drone technology",
      ],
      threats: [
        "Intense competition in e-commerce and cloud",
        "Regulatory actions and antitrust concerns",
        "Economic downturns affecting consumer spending",
        "Cybersecurity risks and data breaches",
      ],
    },
    swot_image:
      "https://via.placeholder.com/600x400/1e40af/ffffff?text=Amazon+SWOT+Analysis",
    pestel_image:
      "https://via.placeholder.com/600x400/dc2626/ffffff?text=Amazon+PESTEL+Analysis",
    porter_forces: {
      rivalry: [
        "Intense competition from Walmart, Google, Microsoft",
        "Price competition in e-commerce market",
      ],
      new_entrants: [
        "Moderate barriers in e-commerce, high in cloud",
        "Network effects create competitive advantages",
      ],
      substitutes: [
        "Traditional retail stores",
        "Other cloud service providers",
      ],
      buyer_power: [
        "Individual consumers have limited power",
        "Prime membership increases switching costs",
      ],
      supplier_power: [
        "Third-party sellers depend on Amazon's platform",
        "Amazon has significant negotiating leverage",
      ],
    },
    porter_image:
      "https://via.placeholder.com/600x400/059669/ffffff?text=Amazon+Porter's+Five+Forces",
    bcg_matrix: {
      AWS: { market_share: 0.42, growth_rate: 0.35 },
      "E-commerce": { market_share: 0.38, growth_rate: 0.12 },
      Advertising: { market_share: 0.15, growth_rate: 0.25 },
      "Prime Video": { market_share: 0.08, growth_rate: 0.18 },
    },
    bcg_image:
      "https://via.placeholder.com/600x400/7c2d12/ffffff?text=Amazon+BCG+Matrix",
    mckinsey_7s: {
      strategy: "Customer obsession with long-term value creation",
      structure: "Decentralized two-pizza teams with business unit autonomy",
      systems: "Advanced logistics, cloud infrastructure, and data analytics",
      style: "Data-driven decision making with high performance standards",
      staff: "Diverse, high-performing workforce with leadership principles",
      skills: "E-commerce, cloud computing, logistics, and customer service",
      shared_values:
        "Customer obsession, ownership, invent and simplify, think big",
    },
    mckinsey_image:
      "https://via.placeholder.com/600x400/7c3aed/ffffff?text=Amazon+McKinsey+7S",
    linkedin_analysis:
      "Amazon's LinkedIn strategy focuses on employer branding with 28M+ followers. Content highlights innovation, workplace diversity, and career opportunities across different business units.",
  },
];

export const mockUserAnalyses: UserAnalysis[] = [
  {
    id: "1",
    companyName: "Apple Inc.",
    analysisDate: "2024-12-15T10:30:00Z",
    status: "completed",
    result: mockAnalysisResults[0],
  },
  {
    id: "2",
    companyName: "Microsoft Corporation",
    analysisDate: "2024-12-14T15:45:00Z",
    status: "completed",
    result: mockAnalysisResults[1],
  },
  {
    id: "3",
    companyName: "Amazon.com Inc.",
    analysisDate: "2024-12-13T09:20:00Z",
    status: "completed",
    result: mockAnalysisResults[2],
  },
  {
    id: "4",
    companyName: "Google (Alphabet Inc.)",
    analysisDate: "2024-12-12T14:15:00Z",
    status: "pending",
  },
  {
    id: "5",
    companyName: "Tesla Inc.",
    analysisDate: "2024-12-11T11:30:00Z",
    status: "failed",
  },
];
