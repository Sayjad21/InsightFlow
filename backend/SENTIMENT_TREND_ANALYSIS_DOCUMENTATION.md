## Overview
Sentiment and Trend Analysis System রিয়েল-টাইম sentiment monitor করে, trend analyze করে, এবং multiple companies এর জন্য predictive insights generate করে। এটি news এবং social media থেকে sentiment তথ্য সংগ্রহ করে, AI analysis এর মাধ্যমে process করে, time-series ফরম্যাটে store করে, এবং trend visualization ও comparative analytics তৈরি করে।

### Java Libraries
- **JFreeChart**: চার্ট এবং গ্রাফ তৈরি করার জন্য
- **Apache Commons Math3**: স্ট্যাটিস্টিক্যাল calculation এবং regression analysis
- **Spring Data MongoDB**: time-series data access এর জন্য database integration
- **Spring Security**: authentication এবং authorization এর জন্য
- **RestTemplate**: external API call করার জন্য HTTP client

## External Dependencies

### Required Services
- **MongoDB**: Time-series data storage এবং retrieval এর জন্য
- **NewsAPI.org**: রিয়েল-টাইম news content access এর জন্য
- **Google Custom Search**: social media থেকে content discovery এর জন্য
- **SupabaseStorage**: visualizations এর cloud storage এর জন্য

### Optional Services
- **Spring Cache**: performance optimization এর জন্য caching ব্যবহার
- **Monitoring Services**: system health এবং performance monitor করার জন্য

## Process Flow Sequences

### 1. Automated Sentiment Collection Flow (Daily Scheduler)

**Trigger**: প্রতিদিন সকাল ৯টায় `@Scheduled(cron = "0 0 9 * * ?")` scheduler এর মাধ্যমে চালু হয়।

**Function Call Sequence**:

SentimentScheduler.collectDailySentiment()
├── MonitoredCompanyService.getActiveCompanyNames() - monitor করা কোম্পানির নামগুলো নেয়
├── প্রতিটি কোম্পানির জন্য loop:
│   ├── SentimentFetcherService.fetchAndScoreSentiment() - sentiment data সংগ্রহ ও scoring
│   │   ├── প্রত্যেক source ("news", "social") এর জন্য loop:
│   │   │   ├── fetchTextAndUrl() - external API থেকে content নেয়
│   │   │   │   ├── [News] NewsAPI.org API call - নতুন article fetch করে
│   │   │   │   └── [Social] Google Custom Search API call - social sentiment fetch করে
│   │   │   ├── AiUtil.invokeWithTemplate() - AI sentiment analysis চালায়
│   │   │   │   └── getCombinedAnalysisTemplate() + Ollama Llama 3.2
│   │   │   ├── parseAnalysisResult() - AI এর response parse করে (JSON/regex fallback)
│   │   │   └── createDataPoint() - sentiment data structure তৈরি করে
│   │   └── scored data points এর একটি List ফেরত দেয়
│   ├── প্রতিটি data point এর জন্য loop:
│   │   ├── SentimentRepository.findByCompanyNameAndTimestampBetween() - duplicate check করে
│   │   ├── SentimentData entity তৈরি করে
│   │   └── SentimentRepository.save() - MongoDB time-series collection এ save করে
│   └── process সম্পন্ন হলে log করে
└── skip list clear করে collection cycle শেষ হয়

text

**Response Sequence**: background process হওয়ায় direct HTTP response নেই।

**Fallback Actions**:
- External API fail হলে অন্য source থেকে data সংগ্রহ চালু থাকে
- AI analysis fail হলে regex দিয়ে number extraction চেষ্টা করে
- Database fail হলে error log করে পরের company তে যায়
- Network issue এ retry mechanism exponential backoff দিয়ে চলে
- Invalid data এ skip করে detailed log করে

### 2. Sentiment Trend Analysis Flow

**Endpoint**: `GET /api/sentiment/{companyName}/trend`

**Function Call Sequence**:

SentimentController.getSentimentTrend()
├── request থেকে parameters parse করে (companyName, days, sources)
├── SentimentTrendService.analyzeTrends() - মূল trend analysis
│   ├── SentimentRepository.findByCompanyNameAndTimestampBetween() - historical data আনে
│   ├── source filter এবং data quality validate করে
│   ├── time_series array valid data থেকে তৈরি করে
│   ├── sourceগুলো group করে source-specific analysis করে
│   ├── SentimentTrendCalculator.computeTrends() - mathematical analysis:
│   │   ├── DescriptiveStatistics.getMean() - average sentiment হিসাব করে
│   │   ├── DescriptiveStatistics.getStandardDeviation() - volatility নির্ধারণ করে
│   │   └── SimpleRegression.getSlope() - trend direction বের করে
│   ├── source-specific trend calculate করে loop করে
│   ├── SentimentTrendCalculator.detectSignificantEvents() - event detection:
│   │   ├── sentiment spike (positive/negative) শনাক্ত করে
│   │   ├── slope analysis দিয়ে trend reversal detect করে
│   │   └── average score থেকে deviation হিসাব করে
│   └── TimeUtil.nowAsString() - analysis timestamp যোগ করে
└── সম্পূর্ণ trend analysis response দেয়

text

**Fallback Actions**:
- Data কম হলে data quality warning সহ analysis দেয়
- mathematical fail হলে simplified calculation করে
- cache miss হলে cache ছাড়া recalculation হয়
- source filter fail হলে সব source ব্যবহার হয়

### 3. Sentiment Trend Visualization Flow

**Endpoint**: `GET /api/sentiment/{companyName}/trend/chart`

**Function Call Sequence**:

SentimentController.getSentimentTrendChart()
├── SentimentTrendService.analyzeTrends() - trend data আনে
├── SentimentTrendVisualizationService.generateTrendGraph() - visualization তৈরী করে:
│   ├── time_series data extract করে
│   ├── TimeSeriesCollection.addSeries() - JFreeChart data prepare করে
│   ├── data point গুলোর জন্য loop:
│   │   ├── parseDateTime() - timestamp parse করে (multiple formats supported)
│   │   ├── Hour.addOrUpdate() - 3-hour interval এ point add করে
│   │   └── score validate করে
│   ├── createStyledChart() - JFreeChart customization:
│   │   ├── ChartFactory.createTimeSeriesChart() - base chart তৈরি করে
│   │   ├── XYPlot styling (color, grid, background)
│   │   ├── XYLineAndShapeRenderer configure করে
│   │   └── DateAxis formatting করে
│   ├── ChartUtils.writeChartAsPNG() - PNG তে convert করে
│   ├── SupabaseStorageService.uploadImageFromStream() - cloud upload করে
│   │   └── fallback হিসেবে base64 encoding ব্যবহার হয়
│   └── chart URL বা base64 data return করে
└── trend data আর chart URL combined response দেয়

text

**Fallback Actions**:
- chart generate fail হলে data ছাড়া response দেয়
- cloud upload fail হলে base64 image দেয়
- time parsing fail হলে approximate timestamp দিয়ে warning সহ দেয়
- JFreeChart error এ সহজ chart বা data-only response দেয়

### 4. Multi-Company Comparison Flow

**Endpoint**: `GET /api/sentiment/comparison/chart`

**Function Call Sequence**:

SentimentController.getComparisonChart()
├── companies parameter parse করে (comma separated)
├── company list validate করে (2-5 companies)
├── প্রতিটি company এর জন্য loop:
│   ├── SentimentTrendService.analyzeTrends() - individual trend data নেয়
│   ├── data quality metrics collect করে
│   └── data কম থাকা company গুলো track করে
├── SentimentTrendVisualizationService.generateComparisonChart() - multi-company chart:
│   ├── multiple series সহ TimeSeriesCollection তৈরি করে
│   ├── প্রতিটি company এর জন্য loop:
│   │   ├── company-specific styling সহ TimeSeries তৈরি করে
│   │   ├── data point process এবং normalize করে
│   │   └── distinct color assign করে
│   ├── multi-line chart তৈরি করে legend সহ
│   ├── cross-company trend correlation analysis করে
│   └── comparative statistics calculate করে
├── insights aggregate করে generate করে
└── সম্পূর্ণ comparison response দেয়

text

**Fallback Actions**:
- multi-company data issue হলে থাকা company নিয়ে কাজ করে, missing note দেয়
- chart complexity বেশি হলে simplified chart বা individual chart generate করে
- correlation calculation fail হলে basic trend summary দেয়
- data কম থাকলে data quality disclaimerসহ response দেয়

## Error Handling & Fallbacks

### Data Collection Fallbacks
1. External API fail হলে available source থেকে কাজ চালায়
2. AI analysis fail হলে regex number extraction চেষ্টা করে
3. Network issue এ exponential backoff সহ retry করে
4. invalid data skip করে detailed logging করে

### Analysis Fallbacks
1. data কম হলে data quality warning সহ analysis দেয়
2. mathematical fail হলে সহজ statistical methods ব্যবহার করে
3. cache fail হলে ভুলে recalculation করে
4. time parsing fail হলে approximate timestamp সহ warning দেয়

### Visualization Fallbacks
1. cloud storage fail হলে base64-encoded images দেয়
2. chart generate fail হলে data-only response দেয়
3. time series fail হলে simplified chart তৈরী করে
4. multi-company fail হলে individual chart তৈরি করে

### Response-Level Fallbacks
1. partial data collection হলে successful operation সহ failure note দেয়
2. visualization fail হলে raw data error indicators সহ দেয়
3. timeout handle এর জন্য graceful degradation দেয়
4. authentication issues এ clear error message এবং resolution guideline দেয়