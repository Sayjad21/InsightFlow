# InsightFlow

## API Endpoints

From the **CompetitiveAnalysisControllerTest.class** and project structure, here are the likely endpoints you can test:

### Base URL: `http://localhost:8000`

### Competitive Analysis Endpoints

````http
POST /api/competitive-analysis/analyze
Content-Type: application/json

{
  "companyName": "Tesla",
  "industry": "Automotive"
}
````

````http
POST /api/competitive-analysis/scrape
Content-Type: application/json

{
  "url": "https://example.com",
  "analysisType": "SWOT"
}
````

### RAG Service Endpoints (based on RagServiceTest)

````http
POST /api/rag/query
Content-Type: application/json

{
  "query": "What is the competitive landscape for electric vehicles?",
  "context": "automotive industry"
}
````

### Visualization Endpoints (based on VisualizationServiceTest)

````http
POST /api/visualization/generate
Content-Type: application/json

{
  "analysisType": "SWOT",
  "data": {
    "strengths": ["Strong brand", "Innovation"],
    "weaknesses": ["High prices"],
    "opportunities": ["Market expansion"],
    "threats": ["Competition"]
  }
}
````

### File Upload Endpoints (based on FileUtilTest)

````http
POST /api/files/upload
Content-Type: multipart/form-data

[Upload file with form field name "file"]
````

### Scraping Endpoints (based on ScrapingServiceTest)

````http
POST /api/scraping/linkedin
Content-Type: application/json

{
  "profileUrl": "https://linkedin.com/company/example",
  "scrapeType": "company"
}
````

