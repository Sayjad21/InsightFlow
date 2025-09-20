# Single Company Competitive Analysis System Documentation - InsightFlow

## Overview
The Single Company Competitive Analysis System is a comprehensive business intelligence pipeline that provides in-depth competitive analysis for individual companies using RAG (Retrieval Augmented Generation) technology. The system supports multiple analysis frameworks (SWOT, PESTEL, Porter's Five Forces, BCG Matrix, McKinsey 7S), LinkedIn intelligence gathering, and professional visualization capabilities.

---
## Technology Stack & Tools

### Core Technologies
- **MongoDB**: Primary database for user analysis storage and company data persistence
- **JWT Authentication**: Secure user authentication and request authorization
- **Ollama Llama 3.2:latest**: Latest local LLM for competitive insights and strategic recommendations
- **Firebase Storage**: Cloud storage for framework visualization charts

### Web Scraping & Data Collection
- **ModularScrapingService**: Primary data collection with architectural modularity
- **TavilyFallbackService**: Fallback data source for robust data collection
- **LinkedInSlugUtil**: LinkedIn company profile identification and mapping
- **Jsoup**: HTML parsing and web content extraction

### Visualization & Chart Generation
- **Custom VisualizationService**: Framework-specific chart generation
- **Chart Libraries**: Professional visualization for SWOT, PESTEL, Porter's, BCG, McKinsey analysis
- **Base64 Encoding**: Fallback image encoding for chart delivery
- **Multi-format Support**: PNG, SVG, and base64 chart formats

### File Management & Utilities
- **FileUtil**: File upload management and processing
- **TimeUtil**: Timestamp generation and time-based operations
- **ByteArrayResource**: In-memory file generation for downloads
- **MultipartFile**: File upload handling and validation

### AI & Natural Language Processing
- **Ollama Nomic Text Embeddings**: 768-dimensional vector generation for competitive intelligence
- **Vector Database**: Semantic search and similarity matching with 768-dimensional vectors
- **Prompt Engineering**: Strategic analysis prompt optimization for Llama 3.2:latest
- **Ollama Llama 3.2:latest**: Local LLM deployment for advanced natural language generation and business insights
- **Azure Deployment**: Cloud-hosted Ollama instance for production scalability

### Performance & Reliability
- **Exception Handling**: Comprehensive error management across all components
- **Fallback Mechanisms**: Robust system degradation and alternative execution paths
- **Logging**: SLF4J comprehensive logging for debugging and monitoring
- **Validation**: Input validation and data quality assurance

---


## System Integration Points

### External API Dependencies
1. **Ollama API**: Local/Azure-hosted Llama 3.2:latest for competitive intelligence, strategic insights, and business analysis
2. **Firebase Storage**: Chart image storage with automatic failover to base64
3. **LinkedIn Data Sources**: Company profile information and business intelligence
4. **TavilyAPI**: Alternative data source for comprehensive company information

### Internal Service Architecture
1. **Authentication Layer**: JWT-based user management and request authorization
2. **Analysis Engine**: Multi-framework business analysis orchestration
3. **Visualization Pipeline**: Chart generation and cloud storage management
4. **Data Collection Layer**: Modular scraping with fallback mechanisms

### Database Schema
- **user_analyses**: Competitive analysis results with user associations and timestamps
- **uploaded_files**: File storage for enhanced analysis context
- **authentication**: User credentials and JWT token management

### Analysis Framework Integration
- **SWOT Analysis**: Strengths, Weaknesses, Opportunities, Threats framework
- **PESTEL Analysis**: Political, Economic, Social, Technological, Environmental, Legal factors
- **Porter's Five Forces**: Competitive rivalry, new entrants, substitutes, buyer/supplier power
- **BCG Matrix**: Market share and growth rate portfolio analysis
- **McKinsey 7S**: Strategy, Structure, Systems, Style, Staff, Skills, Shared Values model


## System Architecture & Components

### Core Controller
1. **CompetitiveAnalysisController**- Primary endpoint for single company competitive analysis requests

### Core Services
1. **RagService**- Retrieval Augmented Generation engine with OpenAI integration
2. **AnalysisService**- Core business analysis logic and framework orchestration  
3. **ModularScrapingService** - Web scraping with modular architecture and TavilyFallbackService
4. **VisualizationService** - Analysis visualization and chart generation for frameworks
5. **LinkedInSlugUtil** - LinkedIn company profile identification and analysis

### Supporting Utilities
1. **FileUtil** - File upload and management for analysis input
2. **TimeUtil** - Time-based operations and formatting

### Data Models
1. **UserAnalysis** - Stored analysis results and user associations
2. **Analysis Framework Models**: SwotResponse, PestelResponse, PortersFiveForcesResponse, BcgMatrixResponse, McKinsey7sResponse
3. **CompanyAnalysisResponse** - Single company analysis response structure

### Repository
1. **UserAnalysisRepository** - Analysis results storage and retrieval

---

## File Dependencies & Relationships

### Controller Dependencies
```
CompetitiveAnalysisController
├── RagService (AI-powered competitive insights)
├── AnalysisService (business analysis frameworks)
├── ModularScrapingService (data collection with fallback)
├── VisualizationService (framework chart generation)
├── FileUtil (file upload management)
├── TimeUtil (timestamp and time operations)
├── LinkedInSlugUtil (LinkedIn company identification)
└── User authentication/authorization
```

### Service Layer Dependencies
```
RagService
├── Ollama API integration (Llama 3.2:latest analysis)
├── Nomic text embedding generation (768-dimensional vectors)
├── Context retrieval and vector similarity search
├── Strategy recommendations generation via Llama 3.2:latest
└── Response parsing and validation

AnalysisService
├── SWOT Analysis framework
├── PESTEL Analysis framework
├── Porter's Five Forces framework
├── BCG Matrix framework
├── McKinsey 7S framework
└── Data validation and processing

ModularScrapingService
├── TavilyFallbackService (primary data source)
├── LinkedIn data integration
├── Web scraping with retry mechanisms
├── Data cleaning and validation
└── Multi-source aggregation

VisualizationService
├── SWOT visualization generation
├── PESTEL chart creation
├── Porter's Forces diagram
├── BCG Matrix quadrant visualization
├── McKinsey 7S model chart
└── Firebase/cloud storage integration

LinkedInSlugUtil
├── Company name to LinkedIn slug mapping
├── LinkedIn profile URL generation
├── Company identification algorithms
└── LinkedIn data validation
```

---

## Execution Flow Analysis

### Single Company Competitive Analysis Request Flow
**Endpoint**: `POST /api/analyze`

#### Execution Sequence:
1. **CompetitiveAnalysisController.analyze()**
   - Validates analysis request and user authentication
   - Handles optional file upload using FileUtil
   - Extracts company name from request parameters
   - Initiates parallel analysis across multiple frameworks

2. **RagService.analyzeCompetitor()**
   - Processes company data and optional uploaded file
   - Creates 768-dimensional embeddings using Ollama Nomic text model
   - TavilyUtil দিয়ে target কোম্পানির নাম ব্যবহার করে web থেকে overview, business model,strategy analysis সংক্রান্ত তথ্য সার্চ করে তার URL সংগ্রহ করে।
   - ScrapingUtil দিয়ে ঐ URL থেকে টেক্সট এক্সট্র্যাক্ট করে, তারপর সংশ্লিষ্ট কোম্পানির তথ্য ফিল্টার করে প্রাসঙ্গিক তথ্য রাখে।
   - Performs vector similarity search for market insights
   - Generates AI-powered strategy recommendations using Ollama Llama 3.2:latest
   - Returns structured competitive analysis with sources

3. **AnalysisService Framework Processing (Parallel Execution)**
   - **generateSwot()**: SWOT analysis (Strengths, Weaknesses, Opportunities, Threats)
   - **generatePestel()**: PESTEL analysis (Political, Economic, Social, Technological, Environmental, Legal)
   - **generatePorterForces()**: Porter's Five Forces analysis
   - **generateBcgMatrix()**: BCG Matrix positioning analysis
   - **generateMckinsey7s()**: McKinsey 7S model analysis

4. **VisualizationService Chart Generation (Parallel Execution)**
   - **generateSwotImage()**: SWOT quadrant visualization
   - **generatePestelImage()**: PESTEL factor analysis chart
   - **generatePorterImage()**: Porter's Forces diagram
   - **generateBcgImage()**: BCG Matrix quadrant chart
   - **generateMckinseyImage()**: McKinsey 7S model visualization

5. **LinkedInSlugUtil & ModularScrapingService**
   - **getLinkedInCompanySlug()**: Maps company name to LinkedIn profile
   - **getLinkedInAnalysis()**: Scrapes LinkedIn intelligence data
   - Uses TavilyFallbackService for robust data collection

6. **Response Assembly & Alternative Sources**
   - Validates and enhances data sources
   - Generates alternative sources if primary sources insufficient
   - Assembles comprehensive analysis response
   - Links analysis to authenticated user account

### Company Analysis File Generation Flow
**Endpoint**: `POST /api/generate-company-file`

#### Execution Sequence:
1. **CompetitiveAnalysisController.generateCompanyFile()**
   - Validates user authentication and company name
   - Handles optional file upload for enhanced analysis
   - Calls generateCompanyAnalysisText() for comprehensive text generation

2. **generateCompanyAnalysisText() Internal Processing**
   - **Header Generation**: Creates formatted analysis header with timestamp
   - **RAG Analysis**: Calls RagService for competitive intelligence and differentiation strategy
   - **LinkedIn Analysis**: Uses LinkedInSlugUtil and ModularScrapingService for LinkedIn intelligence
   - **Framework Analysis Execution**: Runs all 5 analysis frameworks in sequence
   - **Text Formatting**: Removes HTML tags and formats for plain text output

3. **File Response Generation**
   - Creates downloadable text file with comprehensive analysis
   - Sets appropriate HTTP headers for file download
   - Returns ByteArrayResource with UTF-8 encoding

#### Response:
- Downloadable text file: `{CompanyName}_analysis.txt`
- Contains complete competitive analysis in structured text format
- Suitable for comparative analysis uploads and strategic planning

---

## Detailed Function Documentation

### CompetitiveAnalysisController Functions

#### `analyze(@RequestPart("company_name") String companyName, @RequestPart("file") MultipartFile file, Authentication authentication)`
- **Purpose**: Main endpoint for single company competitive analysis requests
- **Input Parameters**:
  - `companyName`: Target company name for analysis
  - `file`: Optional file upload for enhanced analysis context (MultipartFile)
  - `authentication`: User authentication context for request tracking
- **Processing Steps**:
  - Validates user authentication and extracts username
  - Handles optional file upload using FileUtil.saveUploadedFile()
  - Calls RagService.analyzeCompetitor() for Ollama Llama 3.2:latest powered competitive intelligence
  - Executes parallel analysis across 5 frameworks via AnalysisService
  - Generates visualizations for each framework via VisualizationService
  - Retrieves LinkedIn intelligence using LinkedInSlugUtil and ModularScrapingService
  - Validates and enhances sources using generateAlternativeSources()
  - Assembles comprehensive response with all analysis components
- **Output**: Complete competitive analysis response with frameworks, visualizations, and Ollama-powered AI insights
- **Error Handling**: Comprehensive exception handling for file upload, API failures, and analysis errors
- **Fallback Actions**: Uses alternative sources if primary sources insufficient, continues analysis if individual components fail

#### `generateCompanyFile(@RequestParam("company_name") String companyName, @RequestPart("file") MultipartFile file, Authentication authentication)`
- **Purpose**: Generates downloadable comprehensive company analysis text file
- **Input Parameters**:
  - `companyName`: Target company name for analysis
  - `file`: Optional file upload for enhanced analysis
  - `authentication`: User authentication for request tracking
- **Processing Steps**:
  - Validates authentication and handles file upload to uploaded_files directory
  - Calls generateCompanyAnalysisText() for comprehensive text generation
  - Creates UTF-8 encoded ByteArrayResource for file download
  - Sets appropriate HTTP headers for file download response
  - Generates sanitized filename based on company name
- **Output**: Downloadable text file with complete competitive analysis
- **Error Handling**: Handles file I/O errors and analysis generation failures
- **Fallback Actions**: Continues analysis generation even if individual components fail

#### `generateCompanyAnalysisText(String companyName, String filePath)` (Private Method)
- **Purpose**: Internal method for comprehensive company analysis text generation
- **Input Parameters**:
  - `companyName`: Company name for analysis
  - `filePath`: Optional uploaded file path for enhanced context
- **Processing Steps**:
  - **Header Generation**: Creates formatted analysis header with timestamp and file info
  - **RAG Analysis**: Calls RagService for competitive intelligence and strategy recommendations
  - **LinkedIn Analysis**: Uses LinkedInSlugUtil and ModularScrapingService for LinkedIn data
  - **Framework Analysis**: Executes all 5 frameworks (SWOT, PESTEL, Porter's, BCG, McKinsey 7S)
  - **Text Formatting**: Removes HTML tags and formats for plain text output
  - **Error Handling**: Individual try-catch blocks for each analysis component
- **Output**: Structured text analysis suitable for download and comparative use
- **Tools Used**: All analysis services, HTML cleaning, text formatting
- **Fallback Actions**: Continues generation with error messages if components fail

#### `testLinkedInAnalysis(@RequestBody Map<String, String> request)`
- **Purpose**: Test endpoint for debugging LinkedIn analysis functionality
- **Input Parameters**:
  - `request`: Map containing companyName for testing
- **Processing Steps**:
  - Validates company name parameter
  - Calls LinkedInSlugUtil.getLinkedInCompanySlug() for company mapping
  - Executes ModularScrapingService.getLinkedInAnalysis() for data extraction
  - Returns structured response with LinkedIn analysis results
- **Output**: LinkedIn analysis test results with debug information
- **Error Handling**: Comprehensive error reporting for debugging purposes
- **Tools Used**: LinkedInSlugUtil, ModularScrapingService

### RagService Functions

#### `generateInsights(String companyData, String analysisType)`
- **Purpose**: Generates AI-powered insights using Retrieval Augmented Generation with Ollama
- **Input Parameters**:
  - `companyData`: Scraped and processed company information
  - `analysisType`: Analysis framework (SWOT, PESTEL, Porter's, etc.)
- **Processing Steps**:
  - Creates 768-dimensional embeddings from company data using Ollama Nomic text model
  - Performs vector similarity search against knowledge base with cosine similarity
  - Constructs context-aware prompts with retrieved information for Llama 3.2:latest
  - Calls Ollama Llama 3.2:latest API (local/Azure) for insight generation
  - Parses and validates Llama 3.2:latest response
  - Formats insights according to analysis framework
- **Output**: Structured insights with framework-specific recommendations
- **Tools Used**: Ollama Llama 3.2:latest, Ollama Nomic embeddings, Vector database, 768-dimensional similarity search
- **Error Handling**: Graceful degradation to basic analysis if Ollama services fail
- **Fallback Actions**: Uses template-based insights, returns generic recommendations, logs errors for investigation

#### `performEmbeddingSearch(String query, int topK)`
- **Purpose**: Performs semantic search using vector embeddings
- **Input Parameters**:
  - `query`: Search query text
  - `topK`: Number of top results to return
- **Processing Steps**:
  - Generates 768-dimensional embeddings for search query using Ollama Nomic text model
  - Performs cosine similarity search against vector database with 768-dimensional vectors
  - Ranks and filters results by relevance threshold
  - Returns most relevant context snippets with similarity scores
- **Output**: List of relevant context snippets with similarity scores
- **Tools Used**: Ollama Nomic text model, Vector database, 768-dimensional similarity calculations
- **Fallback Actions**: Returns keyword-based search results if Ollama embedding fails

### RagService Functions

#### `analyzeCompetitor(String filePath, String companyName)`
- **Purpose**: Generates AI-powered competitive intelligence using Retrieval Augmented Generation with Ollama
- **Input Parameters**:
  - `filePath`: Optional uploaded file path for enhanced analysis context
  - `companyName`: Target company name for competitive analysis
- **Processing Steps**:
  - Processes uploaded file content if provided for enhanced context
  - Creates 768-dimensional embeddings from company data using Ollama Nomic text model
  - Performs vector similarity search against competitive intelligence knowledge base
  - Constructs context-aware prompts with retrieved competitive information for Llama 3.2:latest
  - Calls Ollama Llama 3.2:latest API (local/Azure deployment) for strategic insights and differentiation recommendations
  - Parses and validates Llama 3.2:latest response for competitive analysis
  - Extracts and validates information sources and links
  - Formats competitive intelligence according to business strategy frameworks
- **Output**: Map containing summaries, strategy recommendations, and source links
- **Tools Used**: Ollama Llama 3.2:latest, Ollama Nomic embeddings (768-dim), Vector database, Strategic prompt engineering
- **Error Handling**: Graceful degradation to basic analysis if Ollama services fail
- **Fallback Actions**: Uses template-based competitive insights, returns generic recommendations, logs errors for investigation

### AnalysisService Functions

#### `generateSwot(String companyName)`
- **Purpose**: Generates comprehensive SWOT analysis for target company
- **Input Parameters**:
  - `companyName`: Target company name for SWOT analysis
- **Processing Steps**:
  - Collects company data through ModularScrapingService
  - Analyzes internal factors (Strengths and Weaknesses)
  - Analyzes external factors (Opportunities and Threats)
  - Validates and structures SWOT components
- **Output**: Map containing strengths, weaknesses, opportunities, and threats lists
- **Tools Used**: Strategic analysis frameworks, Data collection services
- **Fallback Actions**: Uses industry-standard SWOT templates if data collection fails

#### `generatePestel(String companyName)`
- **Purpose**: Generates comprehensive PESTEL analysis for target company
- **Input Parameters**:
  - `companyName`: Target company name for PESTEL analysis
- **Processing Steps**:
  - Analyzes Political factors affecting the company
  - Evaluates Economic environmental factors
  - Assesses Social and cultural factors
  - Reviews Technological factors and innovation landscape
  - Examines Environmental and sustainability factors
  - Investigates Legal and regulatory factors
- **Output**: Map containing political, economic, social, technological, environmental, and legal factor lists
- **Tools Used**: Macro-environmental analysis frameworks, Industry research
- **Fallback Actions**: Uses generic industry PESTEL factors if specific data unavailable

#### `generatePorterForces(String companyName)`
- **Purpose**: Generates Porter's Five Forces analysis for competitive positioning
- **Input Parameters**:
  - `companyName`: Target company name for Porter's analysis
- **Processing Steps**:
  - Analyzes competitive rivalry in the industry
  - Evaluates threat of new entrants to the market
  - Assesses threat of substitute products/services
  - Reviews bargaining power of suppliers
  - Examines bargaining power of buyers/customers
- **Output**: Map containing rivalry, new_entrants, substitutes, supplier_power, and buyer_power lists
- **Tools Used**: Competitive analysis frameworks, Market research
- **Fallback Actions**: Uses standard competitive factors if detailed analysis unavailable

#### `generateBcgMatrix(String companyName)`
- **Purpose**: Generates BCG Matrix analysis for product/service portfolio positioning
- **Input Parameters**:
  - `companyName`: Target company name for BCG Matrix analysis
- **Processing Steps**:
  - Identifies company's main products/services
  - Calculates market share for each product/service
  - Determines growth rate for each market segment
  - Positions products in BCG Matrix quadrants (Stars, Cash Cows, Question Marks, Dogs)
- **Output**: Map of products/services with market_share and growth_rate metrics
- **Tools Used**: Portfolio analysis frameworks, Market data analysis
- **Fallback Actions**: Uses estimated market positions if precise data unavailable

#### `generateMckinsey7s(String companyName)`
- **Purpose**: Generates McKinsey 7S model analysis for organizational effectiveness
- **Input Parameters**:
  - `companyName`: Target company name for McKinsey 7S analysis
- **Processing Steps**:
  - Analyzes Strategy (competitive approach and direction)
  - Evaluates Structure (organizational hierarchy and design)
  - Reviews Systems (processes and procedures)
  - Assesses Style (leadership and management approach)
  - Examines Staff (human resources and capabilities)
  - Identifies Skills (core competencies and capabilities)
  - Defines Shared Values (culture and core beliefs)
- **Output**: Map containing strategy, structure, systems, style, staff, skills, and shared_values descriptions
- **Tools Used**: Organizational analysis frameworks, Corporate research
- **Fallback Actions**: Uses general organizational patterns if specific data unavailable

---

## Fallback Actions & Error Handling

### RagService Fallbacks
1. **Ollama Llama 3.2:latest API Failure**:
   - Uses template-based competitive insights
   - Returns generic strategic recommendations
   - Logs error for investigation and retry with Azure fallback
   - Provides basic competitive analysis without AI enhancement
   - Switches between local Ollama and Azure deployment if available

2. **Ollama Nomic Embedding Generation Failure**:
   - Falls back to keyword-based competitive search
   - Uses cached 768-dimensional embeddings if available
   - Provides basic text similarity matching for competitor data
   - Maintains competitive intelligence functionality without vector search
   - Attempts Azure Ollama instance if local instance fails

3. **Vector Database Unavailable**:
   - Uses local competitive intelligence knowledge base
   - Returns predefined industry competitive insights
   - Maintains basic competitive analysis functionality without RAG
   - Provides generic strategic recommendations
   - Falls back to direct Ollama analysis without context retrieval

### VisualizationService Fallbacks
1. **Firebase Storage Failure**:
   - Automatic fallback to base64 encoding for framework charts
   - Local file system storage option for chart images
   - Maintains visualization functionality without cloud storage
   - Returns base64-encoded charts for frontend rendering

2. **Chart Generation Failure**:
   - Returns raw framework data for frontend rendering
   - Provides simplified chart alternatives (text-based visualizations)
   - Maintains data integrity for SWOT, PESTEL, Porter's, BCG, McKinsey analysis
   - Uses fallback chart templates

### Data Collection Fallbacks
1. **ModularScrapingService Failure**:
   - TavilyFallbackService integration for alternative data sources
   - Cached company data usage from previous analyses
   - LinkedIn data collection as alternative source
   - Generic industry data when specific company data unavailable

2. **LinkedIn Analysis Failure**:
   - Falls back to generic company profile analysis
   - Uses alternative business intelligence sources
   - Provides basic company information from cached data
   - Maintains analysis flow with reduced LinkedIn insights

3. **File Upload Issues**:
   - Continues analysis without uploaded file context
   - Uses company name only for analysis
   - Provides standard competitive analysis without file enhancement
   - Maintains full functionality with basic inputs

### Analysis Framework Fallbacks
1. **Individual Framework Failure**:
   - Continues with remaining frameworks if one fails
   - Uses template-based framework analysis for failed components
   - Provides partial analysis results with available data
   - Logs framework-specific errors for debugging

2. **Data Quality Issues**:
   - Data validation and cleaning for competitive intelligence
   - Missing data interpolation using industry standards
   - Quality score weighting for analysis reliability
   - Fallback to generic industry analysis patterns

### Source Enhancement Fallbacks
1. **Insufficient Source Links**:
   - generateAlternativeSources() method activation
   - Industry-standard source generation
   - Fallback to reputable business intelligence sources
   - Maintains minimum source requirements for credibility

---
