# COMPARISON SYSTEM Documentation

## Overview  
Comparison System ব্যবহারকারীদের multiple কোম্পানির competitive analysis একসাথে side-by-side compare করার সুবিধা দেয়। এটি saved analyses তুলনা, নতুন analyses generate করে তুলনা, বা উভয়ের combination সমর্থন করে। সিস্টেম comprehensive metrics comparison, insights generation, investment recommendations, এবং visualizations চার্ট ও গ্রাফের মাধ্যমে প্রদান করে।

## External Dependencies

### Required Services  
- **Ollama Llama 3.2**: AI analysis এবং recommendations (local/Azure deployment)  
- **MongoDB**: Data persistence এবং retrieval  
- **SupabaseStorage**: Cloud storage for visualizations  
- **Tavily API**: Web search functionality  

### Optional Services  
- **Firebase Storage**: Alternative cloud storage  
- **LinkedIn API**: Enhanced company profile data  
- **Various Web Sources**: Company information scraping  

### Java Libraries  
- **JFreeChart**: Chart এবং graph generation  
- **Spring Boot**: Framework এবং dependency injection  
- **Spring Security**: Authentication এবং authorization  
- **Spring Data MongoDB**: Database access  
- **Apache HTTP Client**: Web scraping এবং API calls  

## Process Flow Sequences

### 1. Compare Existing Analyses Flow  
**Endpoint**: `POST /api/comparison/compare-existing`

**Function Call Sequence:**  
ComparisonController.compareExistingAnalyses()
├── getUserIdFromUsername() - ইউজার authentication
├── UserService.getAnalysisById() - প্রতিটি analysis retrieval (loop)
├── AnalysisConversionUtil.convertToComparisonFormat() - analysis format conversion (loop)
├── AnalysisConversionUtil.addExistingAnalysisMetadata() - metadata যোগ (loop)
├── ComparisonService.computeComparison() - তুলনা হিসাব করা
│   ├── prepareAnalysisText() - প্রতি analysis থেকে টেক্সট বের করা (loop)
│   ├── calculateScores() - sentiment এবং risk scores হিসাব (loop)
│   │   ├── AiUtil.invokeWithTemplate() - AI ভিত্তিক scoring
│   │   └── [Fallback] calculateSentimentScore() + calculateRiskRating()
│   ├── generateInsights() - comparative insights তৈরি
│   └── generateInvestmentRecommendations() - AI-ভিত্তিক বিনিয়োগ পরামর্শ তৈরি
├── ComparisonVisualizationService.generateRadarChart() - রাডার চার্ট তৈরি
├── ComparisonVisualizationService.generateBarGraph() - বার চার্ট তৈরি
├── ComparisonVisualizationService.generateScatterPlot() - স্ক্যাটার প্লট তৈরি
└── [Optional] ComparisonResultRepository.save() - ডাটাবেসে ফলাফল সংরক্ষণ

text

**Fallback Actions:**  
- Database Access Failure হলে partial data সহ এরর মেসেজ প্রদান  
- AI Scoring Failure হলে keyword-based fallback scoring ব্যবহার  
- Visualization Upload Failure হলে Base64 encoded images প্রদান  
- Analysis Not Found হলে specific error message রিটার্ন  

---

### 2. Compare New Companies Flow  
**Endpoint**: `POST /api/comparison/compare`

**Function Call Sequence:**  
ComparisonController.compare()
├── File handling (যদি multipart files থাকে)
├── প্রতিটি কোম্পানির জন্য লুপ:
│   ├── RagService.analyzeCompetitor() - competitive analysis generate
│   │   ├── TavilyUtil.search() - ওয়েব থেকে কোম্পানি তথ্য অনুসন্ধান
│   │   │   └── [Fallback] TavilyFallbackService.getLinkedInAnalysisFallback()
│   │   ├── ScrapingUtil.extractTextFromUrl() - content extraction (loop)
│   │   │   └── [Fallback] উপলব্ধ content দিয়ে কাজ চালিয়ে যাওয়া
│   │   ├── AiUtil.getSummaryTemplate() + invokeWithTemplate() - AI summarization
│   │   └── AiUtil.getDiffWithRagTemplate() + invokeWithTemplate() - strategy generation
│   ├── AnalysisService.generateSwot() - SWOT analysis
│   │   └── AiUtil.getSwotTemplate() + invokeWithTemplate()
│   ├── AnalysisService.generatePestel() - PESTEL analysis
│   │   └── AiUtil.getPestelTemplate() + invokeWithTemplate()
│   ├── AnalysisService.generatePorterForces() - Porter's 5 Forces analysis
│   │   └── AiUtil.getPorterTemplate() + invokeWithTemplate()
│   ├── AnalysisService.generateBcgMatrix() - BCG Matrix analysis
│   │   └── AiUtil.getBcgTemplate() + invokeWithTemplate()
│   ├── AnalysisService.generateMckinsey7s() - McKinsey 7S analysis
│   │   └── AiUtil.getMckinseyTemplate() + invokeWithTemplate()
│   ├── VisualizationService.generateSwotImage() - ফ্রেমওয়ার্ক ভিজ্যুয়ালাইজেশন
│   ├── VisualizationService.generatePestelImage()
│   ├── VisualizationService.generatePorterImage()
│   ├── VisualizationService.generateBcgImage()
│   ├── VisualizationService.generateMckinseyImage()
│   ├── LinkedInSlugUtil.getLinkedInCompanySlug() - LinkedIn profile পাওয়া
│   └── ModularScrapingService.getLinkedInAnalysis() - LinkedIn scraping
│       └── [Fallback] TavilyFallbackService.getLinkedInAnalysisFallback()
├── ComparisonService.computeComparison() - existing flow অনুযায়ী
├── ComparisonVisualizationService.generateRadarChart()
├── ComparisonVisualizationService.generateBarGraph()
├── ComparisonVisualizationService.generateScatterPlot()
└── [Optional] individual analyses এবং comparison result সেভ করা

text

**Fallback Actions:**  
- Tavily Search Failure হলে TavilyFallbackService থেকে পূর্বনির্ধারিত তথ্য ব্যবহার  
- Web Scraping Failure হলে partial data দিয়ে insights তৈরি  
- AI Template Failure হলে খালি সেকশন এবং error নির্দেশক সহ রিটার্ন  
- LinkedIn Scraping Failure হলে fallback অথবা LinkedIn analysis skip করা  
- Visualization Generation Failure হলে placeholder images অথবা error messages  
- File Upload Failure হলে file context ছাড়া কাজ চালানো  

---

### 3. Enhanced Comparison Flow  
**Endpoint**: `POST /api/comparison/compare-enhanced`

**Function Call Sequence:**  
ComparisonController.compareEnhanced()
├── ComparisonRequest অবজেক্ট parse করা
├── request parameters validate করা
├── existing analyses process করা (যদি analysisIds থাকে):
│   ├── UserService.getAnalysisById() - প্রতিটি ID এর জন্য লুপ
│   ├── AnalysisConversionUtil.convertToComparisonFormat()
│   └── AnalysisConversionUtil.addExistingAnalysisMetadata()
├── নতুন কোম্পানিগুলো Process করা (যদি companyNames থাকে):
│   └── নতুন কোম্পানি তুলনা ফ্লোর মতোই
├── ComparisonService.computeComparison()
├── Visualizations generate করা
└── [Optional] request flags অনুযায়ী results সেভ করা

text

**Fallback Actions:**  
- Mixed Request Validation Failure হলে available valid items দিয়ে partial results রিটার্ন  
- Partial Analysis Failure হলে সফল বিশ্লেষণগুলো নিয়ে response, ব্যর্থতা উল্লেখ সহ  
- Save Operation Failure হলেও comparison result রিটার্ন করা হয়  

---

## Service Descriptions

### ComparisonController.java  
**Purpose:** comparison operations এর জন্য REST API endpoints  
**Key Dependencies:** ComparisonService, RagService, AnalysisService, UserService, ComparisonVisualizationService  

---

## Error Handling & Fallbacks

### Service-Level Fallbacks  
১. AI Service Failures: keyword-based algorithms দিয়ে sentiment এবং risk scoring করা হয়  
২. Search Service Failures: TavilyFallbackService থেকে predefined company তথ্য নেয়া হয়  
৩. Storage Service Failures: cloud URL এর পরিবর্তে Base64 encoded images পাঠানো হয়  
৪. Scraping Failures: available data থেকে কাজ চালানো হয় এবং missing sections চিহ্নিত হয়  

### Data-Level Fallbacks  
১. Missing Analysis Data: খালি structure এবং error সূচক প্রদান  
২. Incomplete Metrics: available data থেকে হিসাব করা হয় ডিসক্লেইমারসহ  
৩. Visualization Failures: placeholder images এবং error messages প্রদর্শন  
৪. Database Failures: in-memory processing এবং save failure notifications প্রদান  

### Response-Level Fallbacks  
১. Partial Failures: সফল অপারেশন এবং failure notes সহ রিটার্ন  
২. Complete Failures: comprehensive error response এবং retry নির্দেশনা  
৩. Timeout Handling: graceful degradation এবং timeout indicators  
৪. Authentication Failures: স্পষ্ট error messages এবং সমাধান নির্দেশনা  