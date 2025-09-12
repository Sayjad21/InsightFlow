package com.insightflow.services;

import com.insightflow.utils.AiUtil;
import com.insightflow.utils.TavilyUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ScrapingService {

    private static final Logger logger = LoggerFactory.getLogger(ScrapingService.class);

    @Value("${linkedin.email}")
    private String linkedinEmail;

    @Value("${linkedin.password}")
    private String linkedinPassword;

    @Autowired
    private AiUtil aiUtil;

    @Autowired
    private TavilyUtil tavilyUtil;

    private final Random random = new Random();
    private final String[] userAgents = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:119.0) Gecko/20100101 Firefox/119.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:119.0) Gecko/20100101 Firefox/119.0",
            "Mozilla/5.0 (X11; Linux x86_64; rv:119.0) Gecko/20100101 Firefox/119.0",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:118.0) Gecko/20100101 Firefox/118.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.14; rv:118.0) Gecko/20100101 Firefox/118.0"
    };

    // Track usage to avoid rate limiting
    private long lastRequestTime = 0;
    private int requestCount = 0;
    private static final long MIN_REQUEST_INTERVAL = 300000; // 5 minutes between requests
    private static final int MAX_REQUESTS_PER_HOUR = 10;

    /**
     * Helper class to store LinkedIn company candidate information
     */
    private static class CompanyCandidate {
        final String slug;
        final String url;
        final String title;
        final String content;
        int followerCount = -1;
        double relevanceScore = 0.0;
        String selectionMethod = "unknown";

        CompanyCandidate(String slug, String url, String title, String content) {
            this.slug = slug;
            this.url = url;
            this.title = title != null ? title : "";
            this.content = content != null ? content : "";
        }
    }

    /**
     * Cleanup any orphaned Chrome processes that might interfere with new sessions
     */
    private void cleanupChromeProcesses() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                // Windows cleanup
                ProcessBuilder pb = new ProcessBuilder("taskkill", "/f", "/im", "chrome.exe");
                pb.start().waitFor();
                pb = new ProcessBuilder("taskkill", "/f", "/im", "chromedriver.exe");
                pb.start().waitFor();
            } else {
                // Linux/Mac cleanup
                ProcessBuilder pb = new ProcessBuilder("pkill", "-f", "chrome");
                pb.start().waitFor();
                pb = new ProcessBuilder("pkill", "-f", "chromedriver");
                pb.start().waitFor();
            }
            logger.info("Cleaned up any orphaned Chrome processes");
        } catch (Exception e) {
            logger.warn("Could not clean up Chrome processes: {}", e.getMessage());
        }
    }

    public String getLinkedInAnalysis(String companyName) {
        // Rate limiting to avoid CAPTCHA
        synchronized (this) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastRequestTime < MIN_REQUEST_INTERVAL) {
                long waitTime = MIN_REQUEST_INTERVAL - (currentTime - lastRequestTime);
                logger.warn("Rate limiting: waiting {} ms before next request", waitTime);
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Request interrupted due to rate limiting", e);
                }
            }

            // Reset hourly counter
            if (currentTime - lastRequestTime > 3600000) { // 1 hour
                requestCount = 0;
            }

            if (requestCount >= MAX_REQUESTS_PER_HOUR) {
                throw new RuntimeException("Hourly request limit exceeded. Please wait before making more requests.");
            }

            lastRequestTime = currentTime;
            requestCount++;
        }

        WebDriver driver = null;
        String tempUserDataDir = null;
        try {
            // Clean up any orphaned processes first
            cleanupChromeProcesses();

            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();

            // Create unique temporary user data directory for each session
            tempUserDataDir = System.getProperty("java.io.tmpdir") + "chrome_user_data_" +
                    System.currentTimeMillis() + "_" + random.nextInt(10000);

            // Enhanced anti-detection measures
            options.addArguments("--headless=new"); // Use "--headless=new" for Chrome 109+
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--window-size=" + (1920 + random.nextInt(100)) + "," + (1080 + random.nextInt(100)));
            options.addArguments("--disable-notifications");
            options.addArguments("--user-data-dir=" + tempUserDataDir);
            options.addArguments("--disable-background-timer-throttling");
            options.addArguments("--disable-backgrounding-occluded-windows");
            options.addArguments("--disable-renderer-backgrounding");
            options.addArguments("--disable-features=TranslateUI");
            options.addArguments("--disable-ipc-flooding-protection");
            options.addArguments("user-agent=" + userAgents[random.nextInt(userAgents.length)]);

            driver = new ChromeDriver(options);

            // Remove Firefox-specific JS for navigator.webdriver (Chrome handles this
            // differently)
            ((JavascriptExecutor) driver)
                    .executeScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");

            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

            // Random delay before starting
            Thread.sleep(2000 + random.nextInt(3000));

            // Login with human-like behavior
            logger.info("Navigating to LinkedIn login page");
            driver.get("https://www.linkedin.com/login");

            // Random delay to simulate reading
            Thread.sleep(1000 + random.nextInt(2000));

            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
            logger.info("Entering login credentials");

            // Type credentials with human-like delays
            typeHumanLike(driver.findElement(By.id("username")), linkedinEmail);
            Thread.sleep(500 + random.nextInt(1000));
            typeHumanLike(driver.findElement(By.id("password")), linkedinPassword);
            Thread.sleep(1000 + random.nextInt(1500));

            driver.findElement(By.xpath("//button[@type='submit']")).click();
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("linkedin.com/feed"),
                    ExpectedConditions.urlContains("linkedin.com/checkpoint")));
            Thread.sleep(3000 + random.nextInt(2000));

            // Check for CAPTCHA
            String currentUrl = driver.getCurrentUrl();
            if (currentUrl.contains("checkpoint") || currentUrl.contains("captcha")) {
                logger.error("CAPTCHA detected at URL: {}", currentUrl);
                throw new RuntimeException("CAPTCHA encountered during LinkedIn login");
            }

            // Navigate to company page
            String companyId = getLinkedInCompanyId(companyName);
            String companyUrl = "https://www.linkedin.com/company/" + companyId;
            logger.info("Navigating to company page: {}", companyUrl);
            driver.get(companyUrl);
            Thread.sleep(5000);

            // Log for debugging
            logger.info("Current URL after navigation: {}", driver.getCurrentUrl());
            logger.info("Page title: {}", driver.getTitle());

            // Extract company title with fallbacks
            String companyTitle = "";
            try {
                if (!driver.findElements(By.cssSelector("h1.org-top-card-summary__title")).isEmpty()) {
                    companyTitle = driver.findElement(By.cssSelector("h1.org-top-card-summary__title")).getText();
                    logger.info("Found company title with selector 1: {}", companyTitle);
                } else if (!driver.findElements(By.cssSelector("h1")).isEmpty()) {
                    companyTitle = driver.findElement(By.cssSelector("h1")).getText();
                    logger.info("Found company title with h1 selector: {}", companyTitle);
                } else if (!driver
                        .findElements(By.xpath("//h1[contains(@class, 'title') or contains(@class, 'summary')]"))
                        .isEmpty()) {
                    companyTitle = driver
                            .findElement(By.xpath("//h1[contains(@class, 'title') or contains(@class, 'summary')]"))
                            .getText();
                    logger.info("Found company title with xpath selector: {}", companyTitle);
                } else {
                    logger.warn("Could not find company title with any selector");
                    String pageSource = driver.getPageSource();
                    Files.writeString(Paths.get("linkedin_debug_" + companyId + ".html"), pageSource);
                    logger.info("Saved page source to linkedin_debug_{}.html for debugging", companyId);
                }
            } catch (Exception e) {
                logger.error("Error finding company title: {}", e.getMessage());
            }

            // Click "About" tab with fallback
            try {
                wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About")));
                driver.findElement(By.linkText("About")).click();
                Thread.sleep(2000);
                logger.info("Clicked 'About' tab");
            } catch (Exception e) {
                logger.warn("Failed to click 'About' tab: {}", e.getMessage());
                driver.get(companyUrl + "/about/");
                Thread.sleep(2000);
                logger.info("Fallback: Navigated to about subpage");
            }

            // Extract description with fallback
            String description = "";
            try {
                if (!driver
                        .findElements(
                                By.cssSelector("p.break-words.white-space-pre-wrap.t-black--light.text-body-medium"))
                        .isEmpty()) {
                    description = driver
                            .findElement(By
                                    .cssSelector("p.break-words.white-space-pre-wrap.t-black--light.text-body-medium"))
                            .getText();
                } else if (!driver.findElements(By.xpath("//h2[contains(text(), 'About us')]/following-sibling::p[1]"))
                        .isEmpty()) {
                    description = driver
                            .findElement(By.xpath("//h2[contains(text(), 'About us')]/following-sibling::p[1]"))
                            .getText();
                }
                logger.info("Extracted description, length: {}", description.length());
            } catch (Exception e) {
                logger.warn("Failed to extract description: {}", e.getMessage());
            }

            // Click "Posts" tab with multiple fallbacks
            try {
                wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Posts")));
                driver.findElement(By.linkText("Posts")).click();
                Thread.sleep(3000);
                logger.info("Clicked 'Posts' tab via linkText");
            } catch (Exception e) {
                logger.warn("Failed to click 'Posts' tab via linkText: {}", e.getMessage());
                try {
                    wait.until(ExpectedConditions
                            .elementToBeClickable(By.cssSelector("a.org-page-navigation__item-anchor[href*='posts']")));
                    driver.findElement(By.cssSelector("a.org-page-navigation__item-anchor[href*='posts']")).click();
                    Thread.sleep(3000);
                    logger.info("Clicked 'Posts' tab via fallback CSS selector");
                } catch (Exception e2) {
                    logger.warn("Failed to click 'Posts' tab via CSS: {}", e2.getMessage());
                    driver.get(companyUrl + "/posts/");
                    Thread.sleep(3000);
                    logger.info("Fallback: Navigated to posts subpage");
                }
            }

            // Click "See more" buttons
            try {
                List<org.openqa.selenium.WebElement> seeMoreButtons = driver.findElements(
                        By.cssSelector("button[aria-label*='see more'], button span.feed-shared-see-more-text"));
                for (org.openqa.selenium.WebElement button : seeMoreButtons) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
                    Thread.sleep(1000);
                    logger.info("Clicked 'See more' button");
                }
                // Fallback for "See more" buttons
                if (seeMoreButtons.isEmpty()) {
                    seeMoreButtons = driver.findElements(
                            By.xpath("//button[contains(text(), 'more') or contains(text(), 'See more')]"));
                    for (org.openqa.selenium.WebElement button : seeMoreButtons) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
                        Thread.sleep(1000);
                        logger.info("Clicked 'See more' button via xpath");
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to click 'See more' buttons: {}", e.getMessage());
            }

            // Scroll to load posts
            JavascriptExecutor js = (JavascriptExecutor) driver;
            long lastHeight = (Long) js.executeScript("return document.body.scrollHeight");
            int attempts = 0;
            int maxAttempts = 10;
            while (attempts < maxAttempts) {
                js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
                Thread.sleep(3000);
                long newHeight = (Long) js.executeScript("return document.body.scrollHeight");
                logger.info("Scroll attempt {}: lastHeight={}, newHeight={}", attempts + 1, lastHeight, newHeight);
                if (newHeight == lastHeight)
                    break;
                lastHeight = newHeight;
                attempts++;
            }

            String html = driver.getPageSource();
            logger.debug("Page source length: {}", html.length());
            Files.writeString(Paths.get("linkedin_" + companyId + ".html"), html);

            // Extract posts with multiple selectors
            String name = companyTitle.isEmpty() ? companyName : companyTitle;
            // Dynamic irrelevant pattern based on company name
            Pattern irrelevantPostPattern = createIrrelevantPostPattern(companyName);
            List<String> posts = driver.findElements(By.cssSelector("div.feed-shared-update-v2 span[dir='ltr']"))
                    .stream()
                    .map(element -> cleanPostText(element.getText().trim()))
                    .filter(text -> isValidPost(text, name, irrelevantPostPattern))
                    .collect(Collectors.toList());

            // Fallback with Jsoup (apply same filter)
            if (posts.isEmpty()) {
                Document soup = Jsoup.parse(html);
                posts = soup.select("div.feed-shared-update-v2 span[dir='ltr']")
                        .stream()
                        .map(element -> cleanPostText(element.text().trim()))
                        .filter(text -> isValidPost(text, name, irrelevantPostPattern))
                        .collect(Collectors.toList());

                if (posts.isEmpty()) {
                    posts = soup.select("div.update-components-text span, div.feed-shared-text span")
                            .stream()
                            .map(element -> cleanPostText(element.text().trim()))
                            .filter(text -> isValidPost(text, name, irrelevantPostPattern))
                            .collect(Collectors.toList());
                    logger.info("Used Jsoup fallback for posts, found {} posts", posts.size());
                }
            }

            // Deduplicate posts
            posts = posts.stream().distinct().collect(Collectors.toList());
            logger.info("After deduplication, posts count: {}", posts.size());

            // Construct fullText with labeled sections
            StringBuilder fullText = new StringBuilder();
            if (!name.isEmpty())
                fullText.append("Company: ").append(name).append("\n");
            if (!description.isEmpty())
                fullText.append("Description: ").append(description).append("\n");
            for (String post : posts) {
                fullText.append("Post: ").append(post).append("\n");
            }

            // Fallback for low content
            if (fullText.length() < 100) {
                Document soup = Jsoup.parse(html);
                String mainContent = soup.selectFirst("main") != null ? soup.selectFirst("main").text() : soup.text();
                fullText.append("MainContent: ").append(mainContent).append("\n");
                logger.info("Used fallback content extraction, length: {}", mainContent.length());
            }

            String finalText = fullText.toString().trim();
            // Truncate to ~7,500 tokens (30,000 chars)
            if (finalText.length() > 30000) {
                finalText = finalText.substring(0, 30000) + "... [Truncated for analysis]";
                logger.warn("Truncated LinkedIn content to {} chars to avoid LLM timeouts", finalText.length());
            }
            if (finalText.length() < 50) {
                finalText = "Company: " + companyName + "\nDescription: Limited information available from LinkedIn.";
            }

            logger.info("Parsed data - Name: {}, Description length: {}, Posts count: {}, Full text length: {}",
                    name, description.length(), posts.size(), finalText.length());
            logger.info("Full text snippet: {}", finalText.length() > 200 ? finalText.substring(0, 200) : finalText);
            Files.writeString(Paths.get("linkedin_" + companyId + "_fulltext.txt"), finalText);

            // Prepare optimized content for LLM analysis
            String optimizedContent = prepareContentForLLM(companyName, name, description, posts);

            // LLM analysis with enhanced prompt
            String template = aiUtil.getLinkedInAnalysisTemplate();
            Map<String, Object> variables = Map.of(
                    "content", optimizedContent,
                    "company_name", companyName,
                    "post_count", posts.size());
            String content;
            try {
                content = aiUtil.invokeWithTemplate(template, variables);
                // Basic validation only - let LLM handle structure
                if (content == null || content.trim().isEmpty() || content.length() < 200) {
                    logger.warn("LLM output insufficient, using simplified fallback");
                    content = createSimpleFallback(companyName, description, posts.size());
                }
            } catch (Exception e) {
                logger.error("LLM invocation failed: {}", e.getMessage(), e);
                content = createSimpleFallback(companyName, description, posts.size());
            }

            // Format for HTML
            content = content.replace("\n", "<br>")
                    .replaceAll("<br>{2,}", "<br>") // Single <br> between sections
                    .replaceAll("####\\s*([^<]+)", "<strong>$1</strong><br>")
                    .replaceAll("-\\s*", "- ") // Clean bullet spacing
                    .replaceAll("\\*\\*([^\\*]+)\\*\\*", "<strong>$1</strong>")
                    .replaceAll("\\s{2,}", " ") // Normalize spaces
                    .replaceAll("([a-zA-Z])<br>([a-zA-Z])", "$1 $2") // Fix split words
                    .replaceAll("<br>\\s*-", "<br>-"); // Ensure clean bullet starts
            logger.info("Successfully generated LinkedIn analysis for {}", companyName);
            return "<strong>LinkedIn Analysis of " + companyName + "</strong><br><br>" + content;

        } catch (Exception e) {
            logger.error("Failed to perform LinkedIn analysis for {}: {}", companyName, e.getMessage(), e);
            throw new RuntimeException("Failed to perform LinkedIn analysis for " + companyName + ": " + e.getMessage(),
                    e);
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                    logger.info("WebDriver closed");
                } catch (Exception e) {
                    logger.error("Error closing WebDriver: {}", e.getMessage());
                }
            }

            // Clean up temporary user data directory
            if (tempUserDataDir != null) {
                try {
                    Files.walk(Paths.get(tempUserDataDir))
                            .sorted(java.util.Comparator.reverseOrder())
                            .map(java.nio.file.Path::toFile)
                            .forEach(java.io.File::delete);
                    logger.info("Cleaned up temporary user data directory: {}", tempUserDataDir);
                } catch (Exception e) {
                    logger.warn("Failed to clean up temporary user data directory {}: {}", tempUserDataDir,
                            e.getMessage());
                }
            }
        }
    }

    /**
     * Creates a dynamic irrelevant post pattern based on common patterns
     */
    private Pattern createIrrelevantPostPattern(String companyName) {
        // Build pattern for common irrelevant content
        StringBuilder patternBuilder = new StringBuilder("(?i)^(");

        // Common promotional/engagement patterns
        patternBuilder.append("drop your emoji|register now|sign up here|brb, adding|")
                .append("watch the full conversation|choose from|life just got|less scrolling|")
                .append("join us|check out|upcoming|event|webinar|demo|register|subscribe|")
                .append("click here|learn more|find out|discover|explore|")
                .append("congratulations|thank you|thanks|welcome|excited to|thrilled to|")
                .append("don't miss|limited time|act now|book now|save the date|")
                .append("follow us|like this|share this|comment below|")

                // Date patterns and generic announcements
                .append("\\d{4}|january|february|march|april|may|june|july|august|september|october|november|december|")
                .append("monday|tuesday|wednesday|thursday|friday|saturday|sunday|")
                .append("today|tomorrow|yesterday|this week|next week|last week|")

                // Short/incomplete content
                .append("\\w{1,3}|coming soon|stay tuned|more to come|updates|news|announcement")

                .append(")$");

        return Pattern.compile(patternBuilder.toString());
    }

    /**
     * Cleans post text by removing noise
     */
    private String cleanPostText(String text) {
        return text.replaceAll("hashtag\\s*#\\w+", "") // Remove hashtags
                .replaceAll("http[s]?://\\S+", "") // Remove URLs
                .replaceAll("[^\\p{ASCII}]", "") // Remove non-ASCII (emojis, etc.)
                .replaceAll("\\s+", " ") // Normalize spaces
                .replaceAll("\\.\\.\\.", "...") // Clean ellipsis
                .trim();
    }

    /**
     * Validates if a post is worth including
     */
    private boolean isValidPost(String text, String companyName, Pattern irrelevantPattern) {
        return !text.isBlank() &&
                !text.equalsIgnoreCase(companyName) &&
                !irrelevantPattern.matcher(text).matches() &&
                text.length() > 30 && // Reduced minimum length
                text.split("\\s+").length > 5 && // Require >5 words
                !text.matches("^[A-Z\\s]+$") && // Not all caps
                text.contains(" "); // Must contain spaces (not just one word)
    }

    private String getLinkedInCompanyId(String companyName) {
        String normalizedName = companyName.toLowerCase().trim();

        // Existing hardcoded fallback for common companies
        switch (normalizedName) {
            case "tesla":
                return "tesla-motors";
            case "meta":
            case "facebook":
                return "meta";
            case "alphabet":
                return "google";
            case "x":
            case "twitter":
                return "twitter";
            case "openai":
                return "openai";
            case "microsoft":
                return "microsoft";
            case "apple":
                return "apple";
            case "amazon":
                return "amazon";
            case "netflix":
                return "netflix";
            default:
                // Dynamic search using TavilyUtil with follower count validation
                try {
                    logger.info("Searching for LinkedIn company page for: {}", companyName);
                    List<Map<String, Object>> searchResults = tavilyUtil
                            .search("site:linkedin.com/company " + companyName, 3);

                    if (searchResults.isEmpty()) {
                        logger.warn("No LinkedIn search results found for {}; using normalized name", companyName);
                        return normalizedName.replaceAll("[^a-z0-9-]", "");
                    }

                    // Extract all potential LinkedIn company slugs
                    List<CompanyCandidate> candidates = new ArrayList<>();
                    for (Map<String, Object> result : searchResults) {
                        String url = (String) result.get("url");
                        String title = (String) result.get("title");
                        String content = (String) result.get("content");

                        if (url != null && url.contains("linkedin.com/company/")) {
                            // Extract slug after /company/
                            String slug = url.substring(url.indexOf("/company/") + 9);
                            // Remove trailing parts like query parameters or sub-paths
                            slug = slug.replaceAll("[/?#].*", "");

                            if (!slug.isEmpty()) {
                                candidates.add(new CompanyCandidate(slug, url, title, content));
                                logger.info("Found LinkedIn candidate for {}: {} ({})", companyName, slug, title);
                            }
                        }
                    }

                    if (candidates.isEmpty()) {
                        logger.warn("No valid LinkedIn company URLs found for {}; using normalized name", companyName);
                        return normalizedName.replaceAll("[^a-z0-9-]", "");
                    }

                    // If only one candidate, return it
                    if (candidates.size() == 1) {
                        logger.info("Single LinkedIn candidate found for {}: {}", companyName, candidates.get(0).slug);
                        return candidates.get(0).slug;
                    }

                    // Multiple candidates - try Chrome validation first, then smart fallback
                    logger.info("Multiple LinkedIn candidates found for {}, validating...", companyName);
                    CompanyCandidate bestCandidate = selectBestCandidateWithFallback(companyName, candidates);

                    logger.info("Selected best LinkedIn candidate for {}: {} (method: {})",
                            companyName, bestCandidate.slug, bestCandidate.selectionMethod);
                    return bestCandidate.slug;

                } catch (Exception e) {
                    logger.error("Failed to dynamically find LinkedIn ID for {}: {}", companyName, e.getMessage(), e);
                    // Fallback to normalized name
                    return normalizedName.replaceAll("[^a-z0-9-]", "");
                }
        }
    }

    /**
     * Prepares optimized content for LLM analysis with strategic context
     */
    private String prepareContentForLLM(String companyName, String profileName, String description,
            List<String> posts) {
        StringBuilder content = new StringBuilder();

        // Company basic info with competitive context
        content.append("=== COMPANY PROFILE ===\n");
        content.append("Company: ").append(companyName).append("\n");
        if (!profileName.equals(companyName)) {
            content.append("LinkedIn Profile: ").append(profileName).append("\n");
        }

        // Add industry context for competitive analysis
        String industryContext = getIndustryContext(companyName, description, posts);
        if (!industryContext.isEmpty()) {
            content.append("Industry Context: ").append(industryContext).append("\n");
        }
        content.append("\n");

        // Company description with key metrics extraction
        if (!description.isEmpty()) {
            content.append("=== COMPANY DESCRIPTION ===\n");
            String enhancedDescription = extractKeyMetrics(description);
            content.append(enhancedDescription).append("\n\n");
        }

        // Strategic post analysis (deduplicated and prioritized)
        if (!posts.isEmpty()) {
            content.append("=== STRATEGIC ACTIVITIES ANALYSIS ===\n");
            content.append("Total posts analyzed: ").append(posts.size()).append("\n\n");

            // Prioritize and deduplicate posts strategically
            List<String> strategicPosts = prioritizeStrategicPosts(posts);

            // Group by strategic themes, not just categories
            Map<String, List<String>> strategicThemes = analyzeStrategicThemes(strategicPosts);

            for (Map.Entry<String, List<String>> theme : strategicThemes.entrySet()) {
                content.append("STRATEGIC THEME - ").append(theme.getKey().toUpperCase()).append(":\n");
                for (String post : theme.getValue()) {
                    content.append("• ").append(extractStrategicInsight(post)).append("\n");
                }
                content.append("\n");
            }
        } else {
            content.append("=== ACTIVITIES ===\n");
            content.append("No recent LinkedIn posts available for analysis\n\n");
        }

        return content.toString();
    }

    /**
     * Provides industry context for competitive analysis
     */
    /**
     * Provides general industry context based on company description and posts
     */
    private String getIndustryContext(String companyName, String description, List<String> posts) {
        // Analyze content to determine industry context
        String combined = (companyName + " " + description + " " + String.join(" ", posts)).toLowerCase();

        List<String> industries = new ArrayList<>();
        Map<String, String> competitors = new HashMap<>();

        // Technology indicators
        if (combined.contains("artificial intelligence") || combined.contains(" ai ") ||
                combined.contains("machine learning") || combined.contains("gpt") || combined.contains("llm")) {
            industries.add("AI/ML");
            competitors.put("AI/ML", "Google, OpenAI, Anthropic, Microsoft");
        }

        if (combined.contains("cloud") || combined.contains("azure") || combined.contains("aws")) {
            industries.add("Cloud Computing");
            competitors.put("Cloud Computing", "AWS, Microsoft Azure, Google Cloud");
        }

        if (combined.contains("social") || combined.contains("platform") || combined.contains("network")) {
            industries.add("Social Media/Platforms");
            competitors.put("Social Media/Platforms", "Meta, Twitter/X, TikTok, LinkedIn");
        }

        if (combined.contains("search") || combined.contains("advertising") || combined.contains("marketing")) {
            industries.add("Digital Advertising");
            competitors.put("Digital Advertising", "Google, Meta, Amazon, Microsoft");
        }

        if (combined.contains("electric") || combined.contains("automotive") || combined.contains("vehicle")) {
            industries.add("Electric Vehicles");
            competitors.put("Electric Vehicles", "Tesla, BYD, Toyota, Volkswagen");
        }

        if (combined.contains("entertainment") || combined.contains("streaming") || combined.contains("media")) {
            industries.add("Digital Media");
            competitors.put("Digital Media", "Netflix, Disney, Amazon Prime, Apple");
        }

        // Build context string
        if (industries.isEmpty()) {
            return "Technology sector with various digital services";
        }

        String primaryIndustry = industries.get(0);
        String competitorList = competitors.get(primaryIndustry);
        return primaryIndustry + (competitorList != null ? ", competing with " + competitorList : "");
    }

    /**
     * Extracts key metrics and scale indicators from description
     */
    private String extractKeyMetrics(String description) {
        StringBuilder enhanced = new StringBuilder(description);

        // Look for and highlight key metrics
        String desc = description.toLowerCase();
        if (desc.contains("countries")) {
            enhanced.append("\n[SCALE INDICATOR: Global presence mentioned]");
        }
        if (desc.contains("employees") || desc.contains("people")) {
            enhanced.append("\n[SCALE INDICATOR: Workforce size mentioned]");
        }
        if (desc.contains("billion") || desc.contains("million")) {
            enhanced.append("\n[SCALE INDICATOR: Large numbers mentioned - potential revenue/users]");
        }

        return enhanced.toString();
    }

    /**
     * Prioritizes posts based on strategic importance
     */
    private List<String> prioritizeStrategicPosts(List<String> posts) {
        return posts.stream()
                .filter(post -> isStrategicallySignificant(post))
                .sorted((a, b) -> getStrategicScore(b) - getStrategicScore(a))
                .limit(10) // Limit to top 10 most strategic posts
                .collect(Collectors.toList());
    }

    /**
     * Checks if a post is strategically significant
     */
    private boolean isStrategicallySignificant(String post) {
        String postLower = post.toLowerCase();
        return postLower.contains("launch") || postLower.contains("partnership") ||
                postLower.contains("expansion") || postLower.contains("acquisition") ||
                postLower.contains("investment") || postLower.contains("milestone") ||
                postLower.contains("market") || postLower.contains("growth") ||
                postLower.contains("competition") || postLower.contains("strategy") ||
                postLower.contains("billion") || postLower.contains("million") ||
                postLower.contains("global") || postLower.contains("international");
    }

    /**
     * Scores posts by strategic importance
     */
    private int getStrategicScore(String post) {
        int score = 0;
        String postLower = post.toLowerCase();

        // High-value strategic indicators
        if (postLower.contains("acquisition") || postLower.contains("merger"))
            score += 10;
        if (postLower.contains("partnership") || postLower.contains("alliance"))
            score += 8;
        if (postLower.contains("launch") || postLower.contains("release"))
            score += 7;
        if (postLower.contains("expansion") || postLower.contains("market"))
            score += 6;
        if (postLower.contains("investment") || postLower.contains("funding"))
            score += 6;
        if (postLower.contains("billion") || postLower.contains("million"))
            score += 5;
        if (postLower.contains("global") || postLower.contains("international"))
            score += 4;
        if (postLower.contains("competition") || postLower.contains("vs") || postLower.contains("versus"))
            score += 4;

        return score;
    }

    /**
     * Analyzes posts for strategic themes
     */
    private Map<String, List<String>> analyzeStrategicThemes(List<String> posts) {
        Map<String, List<String>> themes = new HashMap<>();

        for (String post : posts) {
            String postLower = post.toLowerCase();

            if (postLower.contains("acquisition") || postLower.contains("merger") || postLower.contains("investment")) {
                themes.computeIfAbsent("Market Consolidation", k -> new ArrayList<>()).add(post);
            } else if (postLower.contains("partnership") || postLower.contains("alliance")
                    || postLower.contains("collaboration")) {
                themes.computeIfAbsent("Strategic Partnerships", k -> new ArrayList<>()).add(post);
            } else if (postLower.contains("launch") || postLower.contains("release")
                    || postLower.contains("introducing")) {
                themes.computeIfAbsent("Product Innovation", k -> new ArrayList<>()).add(post);
            } else if (postLower.contains("expansion") || postLower.contains("global")
                    || postLower.contains("international") || postLower.contains("market")) {
                themes.computeIfAbsent("Market Expansion", k -> new ArrayList<>()).add(post);
            } else if (postLower.contains("competition") || postLower.contains("vs") || postLower.contains("versus")
                    || postLower.contains("challenge")) {
                themes.computeIfAbsent("Competitive Positioning", k -> new ArrayList<>()).add(post);
            } else {
                themes.computeIfAbsent("Operational Updates", k -> new ArrayList<>()).add(post);
            }
        }

        // Limit posts per theme to avoid redundancy
        themes.replaceAll((theme, postList) -> postList.stream().limit(3).collect(Collectors.toList()));

        return themes;
    }

    /**
     * Extracts strategic insight from a post
     */
    private String extractStrategicInsight(String post) {
        // Clean and focus on strategic elements
        String cleaned = post.replaceAll("\\s+", " ").trim();

        // If short enough, return as-is
        if (cleaned.length() <= 150) {
            return cleaned;
        }

        // Extract strategic core - first sentence plus any strategic keywords
        String[] sentences = cleaned.split("\\. ");
        StringBuilder insight = new StringBuilder();

        // Always include first sentence
        if (sentences.length > 0) {
            insight.append(sentences[0]);
            if (!sentences[0].endsWith(".")) {
                insight.append(".");
            }
        }

        // Add sentences with strategic value
        for (int i = 1; i < sentences.length && insight.length() < 140; i++) {
            String sentence = sentences[i].toLowerCase();
            if (sentence.contains("market") || sentence.contains("compete") ||
                    sentence.contains("customer") || sentence.contains("revenue") ||
                    sentence.contains("growth") || sentence.contains("scale") ||
                    sentence.contains("million") || sentence.contains("billion") ||
                    sentence.contains("partnership") || sentence.contains("expansion")) {
                insight.append(" ").append(sentences[i]);
                if (!sentences[i].endsWith(".")) {
                    insight.append(".");
                }
                break; // Only add one strategic sentence to avoid redundancy
            }
        }

        return insight.toString();
    }

    /**
     * Creates a strategic fallback when LLM fails
     */
    private String createSimpleFallback(String companyName, String description, int postCount) {
        StringBuilder content = new StringBuilder();

        content.append("#### Strategic Analysis: ").append(companyName).append("\n\n");

        // Strategic positioning based on available data
        content.append("#### I. Strategic Positioning\n\n");
        if (!description.isEmpty() && description.length() > 20) {
            String industryContext = getIndustryContext(companyName, description, new ArrayList<>());
            content.append("**Market Position:** ");
            String shortDesc = description.length() > 200 ? description.substring(0, 200) + "..." : description;
            content.append(shortDesc);

            if (!industryContext.isEmpty()) {
                content.append("\n\n**Competitive Landscape:** ").append(industryContext);
            }
            content.append("\n\n");
        } else {
            content.append("**Market Position:** Limited company description available for strategic analysis.\n\n");
        }

        // Activity analysis
        content.append("#### II. Activity Analysis\n\n");
        if (postCount > 0) {
            content.append("**Communication Strategy:** Active LinkedIn presence with ")
                    .append(postCount).append(" recent posts indicating ongoing strategic communications.\n\n");
            content.append("**Market Engagement:** Regular stakeholder communication suggests ")
                    .append("focus on brand positioning and market presence.\n\n");
        } else {
            content.append("**Communication Strategy:** Limited recent LinkedIn activity detected, ")
                    .append("suggesting either strategic communication focus elsewhere or private company approach.\n\n");
        }

        // Strategic implications
        content.append("#### III. Strategic Intelligence\n\n");
        content.append("**Analysis Limitation:** Detailed strategic analysis requires additional data sources. ");
        content.append("Current LinkedIn activity provides limited visibility into strategic direction.\n\n");
        content.append("**Recommendation:** Supplement with financial reports, press releases, and ")
                .append("competitive intelligence for comprehensive strategic assessment.\n");

        return content.toString();
    }

    /**
     * Types text with human-like delays to avoid detection
     */
    private void typeHumanLike(WebElement element, String text) {
        element.clear();
        for (char c : text.toCharArray()) {
            element.sendKeys(String.valueOf(c));
            try {
                Thread.sleep(50 + random.nextInt(100)); // Random delay between keystrokes
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Selects the best LinkedIn company candidate with smart fallback strategies
     */
    private CompanyCandidate selectBestCandidateWithFallback(String companyName, List<CompanyCandidate> candidates) {
        // First, calculate relevance scores for all candidates
        for (CompanyCandidate candidate : candidates) {
            candidate.relevanceScore = calculateRelevanceScore(companyName, candidate);
            logger.info("Candidate {} - Title: '{}', Relevance: {}",
                    candidate.slug, candidate.title, candidate.relevanceScore);
        }

        // Strategy 1: Try Chrome validation (best accuracy)
        CompanyCandidate chromeValidated = tryChromeValidation(companyName, candidates);
        if (chromeValidated != null) {
            chromeValidated.selectionMethod = "chrome-validation";
            return chromeValidated;
        }

        // Strategy 2: Try Jsoup validation (lighter, no Chrome dependency)
        logger.info("Chrome validation failed for {}, trying Jsoup validation...", companyName);
        CompanyCandidate jsoupValidated = tryJsoupValidation(companyName, candidates);
        if (jsoupValidated != null) {
            jsoupValidated.selectionMethod = "jsoup-validation";
            return jsoupValidated;
        }

        // Strategy 3: Smart heuristic selection (final fallback)
        logger.warn("Both Chrome and Jsoup validation failed for {}, using heuristic analysis", companyName);
        CompanyCandidate heuristicBest = selectByHeuristics(companyName, candidates);
        heuristicBest.selectionMethod = "heuristic-analysis";
        return heuristicBest;
    }

    /**
     * Attempts Chrome-based validation (original method with better error handling)
     */
    private CompanyCandidate tryChromeValidation(String companyName, List<CompanyCandidate> candidates) {
        WebDriver driver = null;
        String tempUserDataDir = null;

        try {
            // Setup lightweight Chrome instance for validation
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();

            tempUserDataDir = System.getProperty("java.io.tmpdir") + "chrome_validate_" +
                    System.currentTimeMillis() + "_" + random.nextInt(10000);

            options.addArguments("--headless=new");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--user-data-dir=" + tempUserDataDir);
            options.addArguments("--disable-notifications");
            options.addArguments("--timeout=10000");
            options.addArguments("user-agent=" + userAgents[random.nextInt(userAgents.length)]);

            driver = new ChromeDriver(options);
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

            // Check each candidate
            for (CompanyCandidate candidate : candidates) {
                try {
                    logger.info("Validating candidate: {} for company: {}", candidate.slug, companyName);

                    // Navigate to the LinkedIn company page (public view)
                    String publicUrl = "https://www.linkedin.com/company/" + candidate.slug;
                    driver.get(publicUrl);
                    Thread.sleep(2000); // Wait for page load

                    // Calculate relevance score based on title/content match
                    candidate.relevanceScore = calculateRelevanceScore(companyName, candidate);

                    // Try to extract follower count from public page
                    candidate.followerCount = extractFollowerCount(driver);

                    logger.info("Candidate {} - Followers: {}, Relevance: {}",
                            candidate.slug, candidate.followerCount, candidate.relevanceScore);

                } catch (Exception e) {
                    logger.warn("Failed to validate candidate {}: {}", candidate.slug, e.getMessage());
                    candidate.followerCount = -1; // Mark as failed
                }
            }

            // Select best candidate based on follower count and relevance
            return candidates.stream()
                    .filter(c -> c.followerCount > 0) // Only valid candidates
                    .max((c1, c2) -> {
                        // Primary sort: follower count (higher is better)
                        int followerComparison = Integer.compare(c1.followerCount, c2.followerCount);
                        if (followerComparison != 0) {
                            return followerComparison;
                        }
                        // Secondary sort: relevance score (higher is better)
                        return Double.compare(c1.relevanceScore, c2.relevanceScore);
                    })
                    .orElse(candidates.get(0)); // Fallback to first if none have follower counts

        } catch (Exception e) {
            logger.error("Error during Chrome candidate validation: {}", e.getMessage());
            return null; // Return null to indicate Chrome validation failed
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception e) {
                    logger.warn("Error closing validation driver: {}", e.getMessage());
                }
            }

            // Clean up temp directory
            if (tempUserDataDir != null) {
                try {
                    Files.walk(Paths.get(tempUserDataDir))
                            .sorted(java.util.Comparator.reverseOrder())
                            .map(java.nio.file.Path::toFile)
                            .forEach(java.io.File::delete);
                } catch (Exception e) {
                    logger.warn("Failed to clean up temp directory: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Attempts to validate company candidates using Jsoup (HTTP + HTML parsing)
     * This is lighter weight than Chrome validation but still gets actual follower
     * counts
     */
    private CompanyCandidate tryJsoupValidation(String companyName, List<CompanyCandidate> candidates) {
        try {
            logger.info("Starting Jsoup validation for {} with {} candidates", companyName, candidates.size());

            // Try to extract follower counts using Jsoup
            for (CompanyCandidate candidate : candidates) {
                try {
                    String linkedinUrl = "https://www.linkedin.com/company/" + candidate.slug;
                    logger.debug("Attempting Jsoup follower extraction for: {}", linkedinUrl);

                    // Use Jsoup to fetch and parse the LinkedIn page
                    Document doc = Jsoup.connect(linkedinUrl)
                            .userAgent(
                                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                            .timeout(10000)
                            .followRedirects(true)
                            .get();

                    // Try multiple strategies to extract follower count
                    int followerCount = extractFollowerCountFromHtml(doc);

                    if (followerCount > 0) {
                        candidate.followerCount = followerCount;
                        logger.info("Jsoup extracted {} followers for candidate: {} ({})",
                                followerCount, candidate.title, candidate.slug);
                    } else {
                        logger.warn("No followers found via Jsoup for candidate: {} ({})",
                                candidate.title, candidate.slug);
                        candidate.followerCount = -1;
                    }

                } catch (Exception e) {
                    logger.warn("Failed to validate candidate {} via Jsoup: {}", candidate.slug, e.getMessage());
                    candidate.followerCount = -1; // Mark as failed
                }
            }

            // Select best candidate based on follower count and relevance
            return candidates.stream()
                    .filter(c -> c.followerCount > 0) // Only valid candidates
                    .max((c1, c2) -> {
                        // Primary sort: follower count (higher is better)
                        int followerComparison = Integer.compare(c1.followerCount, c2.followerCount);
                        if (followerComparison != 0) {
                            return followerComparison;
                        }
                        // Secondary sort: relevance score (higher is better)
                        return Double.compare(c1.relevanceScore, c2.relevanceScore);
                    })
                    .orElse(null); // Return null if no candidates have valid follower counts

        } catch (Exception e) {
            logger.warn("Jsoup validation failed for {}: {}", companyName, e.getMessage());
            return null;
        }
    }

    /**
     * Extracts follower count from LinkedIn company page HTML using multiple
     * strategies
     */
    private int extractFollowerCountFromHtml(Document doc) {
        try {
            // Strategy 1: Look for follower count in meta tags
            Elements metaTags = doc.select("meta[name*=follower], meta[property*=follower], meta[content*=follower]");
            for (Element meta : metaTags) {
                String content = meta.attr("content");
                int count = parseFollowerCount(content);
                if (count > 0) {
                    logger.debug("Found follower count in meta tag: {}", count);
                    return count;
                }
            }

            // Strategy 2: Look for follower count in structured data (JSON-LD)
            Elements jsonLdElements = doc.select("script[type=application/ld+json]");
            for (Element jsonLd : jsonLdElements) {
                String jsonContent = jsonLd.html();
                if (jsonContent.contains("follower") || jsonContent.contains("Follower")) {
                    int count = parseFollowerCount(jsonContent);
                    if (count > 0) {
                        logger.debug("Found follower count in JSON-LD: {}", count);
                        return count;
                    }
                }
            }

            // Strategy 3: Look for follower count in page text/elements
            // Common patterns: "X followers", "X Followers", etc.
            String pageText = doc.text();
            int count = parseFollowerCount(pageText);
            if (count > 0) {
                logger.debug("Found follower count in page text: {}", count);
                return count;
            }

            // Strategy 4: Look in specific LinkedIn elements (if any are accessible)
            Elements followerElements = doc.select("*:containsOwn(followers), *:containsOwn(Followers)");
            for (Element element : followerElements) {
                String text = element.text();
                int elementCount = parseFollowerCount(text);
                if (elementCount > 0) {
                    logger.debug("Found follower count in element: {}", elementCount);
                    return elementCount;
                }
            }

            logger.debug("No follower count found in HTML content");
            return -1;

        } catch (Exception e) {
            logger.warn("Error extracting follower count from HTML: {}", e.getMessage());
            return -1;
        }
    }

    /**
     * Selects best candidate using heuristics when Chrome validation fails
     */
    private CompanyCandidate selectByHeuristics(String companyName, List<CompanyCandidate> candidates) {
        String lowerCompanyName = companyName.toLowerCase();

        // Strategy 1: Look for exact or near-exact matches
        for (CompanyCandidate candidate : candidates) {
            String lowerSlug = candidate.slug.toLowerCase();
            String lowerTitle = candidate.title.toLowerCase();

            // For "miro" case: "mirohq" should win over "miro" and "miro-distribution"
            if (lowerSlug.equals(lowerCompanyName + "hq")) {
                logger.info("Found HQ variant for {}: {}", companyName, candidate.slug);
                return candidate;
            }

            // Exact slug match with higher relevance score
            if (lowerSlug.equals(lowerCompanyName) && candidate.relevanceScore >= 50.0) {
                logger.info("Found exact slug match with high relevance for {}: {}", companyName, candidate.slug);
                return candidate;
            }

            // Check if title contains company name as exact word
            if (lowerTitle.contains(lowerCompanyName) && candidate.relevanceScore >= 70.0) {
                logger.info("Found title match with high relevance for {}: {} ({})", companyName, candidate.title,
                        candidate.slug);
                return candidate;
            }
        }

        // Strategy 2: Prioritize by relevance score and avoid distribution/reseller
        // companies
        CompanyCandidate best = candidates.stream()
                .filter(c -> !isLikelyDistributor(c)) // Filter out distributors
                .max((c1, c2) -> Double.compare(c1.relevanceScore, c2.relevanceScore))
                .orElse(candidates.stream()
                        .max((c1, c2) -> Double.compare(c1.relevanceScore, c2.relevanceScore))
                        .orElse(candidates.get(0)));

        logger.info("Selected candidate {} by heuristics (relevance: {}, is_distributor: {})",
                best.slug, best.relevanceScore, isLikelyDistributor(best));

        return best;
    }

    /**
     * Checks if a candidate is likely a distributor/reseller rather than the main
     * company
     */
    private boolean isLikelyDistributor(CompanyCandidate candidate) {
        String lowerSlug = candidate.slug.toLowerCase();
        String lowerTitle = candidate.title.toLowerCase();
        String lowerContent = candidate.content.toLowerCase();

        // Distributors often have these keywords
        return lowerSlug.contains("distribution") ||
                lowerSlug.contains("distributor") ||
                lowerSlug.contains("dealer") ||
                lowerSlug.contains("reseller") ||
                lowerTitle.contains("distribution") ||
                lowerTitle.contains("distributor") ||
                lowerTitle.contains("dealer") ||
                lowerTitle.contains("reseller") ||
                lowerContent.contains("authorized dealer") ||
                lowerContent.contains("official distributor");
    }

    /**
     * Calculates relevance score between company name and LinkedIn candidate
     */
    private double calculateRelevanceScore(String companyName, CompanyCandidate candidate) {
        double score = 0.0;
        String lowerCompanyName = companyName.toLowerCase();
        String lowerTitle = candidate.title.toLowerCase();
        String lowerContent = candidate.content.toLowerCase();

        // Exact match in title gets highest score
        if (lowerTitle.equals(lowerCompanyName)) {
            score += 100.0;
        } else if (lowerTitle.contains(lowerCompanyName)) {
            score += 50.0;
        }

        // Partial matches in title
        String[] companyWords = lowerCompanyName.split("\\s+");
        for (String word : companyWords) {
            if (word.length() > 2 && lowerTitle.contains(word)) {
                score += 10.0;
            }
        }

        // Content matches (lower weight)
        if (lowerContent.contains(lowerCompanyName)) {
            score += 20.0;
        }

        // Known corporate suffixes
        if (lowerTitle.contains(" inc") || lowerTitle.contains(" corp") ||
                lowerTitle.contains(" ltd") || lowerTitle.contains(" llc")) {
            score += 5.0;
        }

        return score;
    }

    /**
     * Extracts follower count from LinkedIn public company page
     */
    private int extractFollowerCount(WebDriver driver) {
        try {
            // Multiple selectors for follower count
            String[] selectors = {
                    "div.org-top-card-summary-info-list__info-item:contains('followers')",
                    "div[class*='follower']",
                    "span[class*='follower']",
                    "div.org-top-card-summary-info-list div:contains('follower')",
                    "div:contains('followers')"
            };

            for (String selector : selectors) {
                try {
                    List<WebElement> elements = driver.findElements(By.cssSelector(selector.split(":contains")[0]));
                    for (WebElement element : elements) {
                        String text = element.getText().toLowerCase();
                        if (text.contains("follower")) {
                            int count = parseFollowerCount(text);
                            if (count > 0) {
                                return count;
                            }
                        }
                    }
                } catch (Exception e) {
                    // Continue to next selector
                }
            }

            // Fallback: search entire page for follower patterns
            String pageText = driver.getPageSource().toLowerCase();
            return parseFollowerCountFromPage(pageText);

        } catch (Exception e) {
            logger.debug("Failed to extract follower count: {}", e.getMessage());
            return -1;
        }
    }

    /**
     * Parses follower count from text
     */
    private int parseFollowerCount(String text) {
        try {
            // Look for patterns like "1,234 followers", "1.2K followers", "1.2M followers"
            Pattern pattern = Pattern.compile("([\\d,\\.]+)\\s*([kmb]?)\\s*followers?", Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher matcher = pattern.matcher(text);

            if (matcher.find()) {
                String numberStr = matcher.group(1).replace(",", "");
                String suffix = matcher.group(2).toLowerCase();

                double number = Double.parseDouble(numberStr);

                switch (suffix) {
                    case "k":
                        return (int) (number * 1000);
                    case "m":
                        return (int) (number * 1000000);
                    case "b":
                        return (int) (number * 1000000000);
                    default:
                        return (int) number;
                }
            }
        } catch (Exception e) {
            logger.debug("Failed to parse follower count from: {}", text);
        }
        return -1;
    }

    /**
     * Parses follower count from entire page content
     */
    private int parseFollowerCountFromPage(String pageContent) {
        Pattern pattern = Pattern.compile("([\\d,\\.]+)\\s*([kmb]?)\\s*followers?", Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(pageContent);

        int maxCount = -1;
        while (matcher.find()) {
            int count = parseFollowerCount(matcher.group());
            if (count > maxCount) {
                maxCount = count;
            }
        }

        return maxCount;
    }

}