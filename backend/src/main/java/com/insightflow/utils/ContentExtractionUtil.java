package com.insightflow.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility class for content extraction, parsing, and filtering from LinkedIn
 * pages.
 * Handles post extraction, text cleaning, and content validation.
 */
@Component
public class ContentExtractionUtil {

    private static final Logger logger = LoggerFactory.getLogger(ContentExtractionUtil.class);

    /**
     * Data class to hold extracted LinkedIn content
     */
    public static class LinkedInContent {
        public final String companyTitle;
        public final String description;
        public final List<String> posts;
        public final String fullText;

        public LinkedInContent(String companyTitle, String description, List<String> posts, String fullText) {
            this.companyTitle = companyTitle;
            this.description = description;
            this.posts = posts;
            this.fullText = fullText;
        }
    }

    /**
     * Extracts comprehensive content from a LinkedIn company page
     * 
     * @param driver      WebDriver instance with LinkedIn page loaded
     * @param companyName The company name for filtering and validation
     * @param companyId   LinkedIn company ID for file naming
     * @return LinkedInContent containing all extracted information
     */
    public LinkedInContent extractLinkedInContent(WebDriver driver, String companyName, String companyId) {
        logger.info("Starting comprehensive content extraction for company: {}", companyName);

        // Extract company title
        String companyTitle = extractCompanyTitle(driver);
        logger.info("Extracted company title: {}", companyTitle);

        // Extract company description
        String description = extractCompanyDescription(driver);
        logger.info("Extracted description, length: {}", description.length());

        // Extract posts with filtering
        List<String> posts = extractCompanyPosts(driver, companyName, companyTitle);
        logger.info("Extracted {} valid posts", posts.size());

        // Build comprehensive fullText
        String fullText = buildFullText(companyName, companyTitle, description, posts, driver);

        // Save debug information
        saveDebugContent(companyId, fullText);

        return new LinkedInContent(companyTitle, description, posts, fullText);
    }

    /**
     * Extracts company title from LinkedIn page with multiple fallback strategies
     */
    private String extractCompanyTitle(WebDriver driver) {
        try {
            // Strategy 1: Primary selector
            List<WebElement> titleElements = driver.findElements(By.cssSelector("h1.org-top-card-summary__title"));
            if (!titleElements.isEmpty()) {
                String title = titleElements.get(0).getText().trim();
                logger.debug("Found company title with primary selector: {}", title);
                return title;
            }

            // Strategy 2: Generic h1 selector
            titleElements = driver.findElements(By.cssSelector("h1"));
            if (!titleElements.isEmpty()) {
                String title = titleElements.get(0).getText().trim();
                logger.debug("Found company title with h1 selector: {}", title);
                return title;
            }

            // Strategy 3: XPath fallback
            titleElements = driver
                    .findElements(By.xpath("//h1[contains(@class, 'title') or contains(@class, 'summary')]"));
            if (!titleElements.isEmpty()) {
                String title = titleElements.get(0).getText().trim();
                logger.debug("Found company title with xpath selector: {}", title);
                return title;
            }

            logger.warn("Could not find company title with any selector");
            return "";

        } catch (Exception e) {
            logger.error("Error extracting company title: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Extracts company description from LinkedIn page
     */
    private String extractCompanyDescription(WebDriver driver) {
        try {
            // Strategy 1: Primary description selector
            List<WebElement> descElements = driver.findElements(
                    By.cssSelector("p.break-words.white-space-pre-wrap.t-black--light.text-body-medium"));

            if (!descElements.isEmpty()) {
                String desc = descElements.get(0).getText().trim();
                logger.debug("Found description with primary selector, length: {}", desc.length());
                return desc;
            }

            // Strategy 2: Alternative selectors
            String[] alternativeSelectors = {
                    "div.org-top-card-summary-info-list__info-item p",
                    "div.org-about-us-organization-description p",
                    "section.artdeco-card p",
                    "div[data-test-id='about-us-description'] p"
            };

            for (String selector : alternativeSelectors) {
                descElements = driver.findElements(By.cssSelector(selector));
                if (!descElements.isEmpty()) {
                    String desc = descElements.get(0).getText().trim();
                    if (!desc.isEmpty()) {
                        logger.debug("Found description with selector '{}', length: {}", selector, desc.length());
                        return desc;
                    }
                }
            }

            logger.warn("Could not find company description with any selector");
            return "";

        } catch (Exception e) {
            logger.error("Error extracting company description: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Extracts and filters company posts from LinkedIn page
     */
    private List<String> extractCompanyPosts(WebDriver driver, String companyName, String companyTitle) {
        String targetName = companyTitle.isEmpty() ? companyName : companyTitle;
        Pattern irrelevantPattern = createIrrelevantPostPattern(companyName);

        try {
            logger.info("Starting post extraction for company: {}", targetName);

            // First, navigate to the Posts tab to get more post content
            logger.debug("Step 1: Navigating to Posts tab");
            navigateToPostsTab(driver);

            // Click "See more" buttons to expand truncated posts
            logger.debug("Step 2: Clicking 'See more' buttons");
            clickSeeMoreButtons(driver);

            // Perform infinite scrolling to load more posts
            logger.debug("Step 3: Performing infinite scrolling");
            performInfiniteScrolling(driver);

            logger.debug("Step 4: Extracting posts using selectors");

            // Strategy 1: Primary post selector (after loading more content)
            List<String> posts = driver.findElements(By.cssSelector("div.feed-shared-update-v2 span[dir='ltr']"))
                    .stream()
                    .map(element -> cleanPostText(element.getText().trim()))
                    .filter(text -> isValidPost(text, targetName, irrelevantPattern))
                    .collect(Collectors.toList());

            if (!posts.isEmpty()) {
                logger.info("✅ Extracted {} posts with primary selector (after content loading)", posts.size());
                return posts.stream().distinct().collect(Collectors.toList());
            }

            // Strategy 2: Jsoup fallback
            logger.debug("Primary selector failed, trying Jsoup fallback");
            String html = driver.getPageSource();
            Document soup = Jsoup.parse(html);

            posts = soup.select("div.feed-shared-update-v2 span[dir='ltr']")
                    .stream()
                    .map(element -> cleanPostText(element.text().trim()))
                    .filter(text -> isValidPost(text, targetName, irrelevantPattern))
                    .collect(Collectors.toList());

            if (!posts.isEmpty()) {
                logger.info("✅ Extracted {} posts with Jsoup primary fallback", posts.size());
                return posts.stream().distinct().collect(Collectors.toList());
            }

            // Strategy 3: Alternative Jsoup selectors
            posts = soup.select("div.update-components-text span, div.feed-shared-text span")
                    .stream()
                    .map(element -> cleanPostText(element.text().trim()))
                    .filter(text -> isValidPost(text, targetName, irrelevantPattern))
                    .collect(Collectors.toList());

            logger.info("✅ Used Jsoup alternative selectors, found {} posts", posts.size());
            return posts.stream().distinct().collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("❌ Error extracting company posts: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Creates a dynamic pattern for filtering irrelevant posts
     */
    private Pattern createIrrelevantPostPattern(String companyName) {
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
     * Cleans post text by removing noise and normalizing content
     */
    private String cleanPostText(String text) {
        if (text == null) {
            return "";
        }

        return text.replaceAll("hashtag\\s*#\\w+", "") // Remove hashtags
                .replaceAll("http[s]?://\\S+", "") // Remove URLs
                .replaceAll("[^\\p{ASCII}]", "") // Remove non-ASCII (emojis, etc.)
                .replaceAll("\\s+", " ") // Normalize spaces
                .replaceAll("\\.\\.\\.", "...") // Clean ellipsis
                .trim();
    }

    /**
     * Validates if a post is worth including in the analysis
     */
    private boolean isValidPost(String text, String companyName, Pattern irrelevantPattern) {
        if (text == null || text.isBlank()) {
            return false;
        }

        // Basic quality checks
        if (text.length() <= 30 || // Too short
                text.split("\\s+").length <= 5 || // Too few words
                text.matches("^[A-Z\\s]+$") || // All caps
                !text.contains(" ") || // Single word
                text.equalsIgnoreCase(companyName)) { // Just company name
            return false;
        }

        // Filter out irrelevant patterns
        return !irrelevantPattern.matcher(text).matches();
    }

    /**
     * Builds comprehensive full text from all extracted components
     */
    private String buildFullText(String companyName, String companyTitle, String description,
            List<String> posts, WebDriver driver) {
        StringBuilder fullText = new StringBuilder();

        // Add company information
        String displayName = companyTitle.isEmpty() ? companyName : companyTitle;
        if (!displayName.isEmpty()) {
            fullText.append("Company: ").append(displayName).append("\n");
        }

        if (!description.isEmpty()) {
            fullText.append("Description: ").append(description).append("\n");
        }

        // Add posts
        for (String post : posts) {
            fullText.append("Post: ").append(post).append("\n");
        }

        // Fallback for low content - extract more general content
        if (fullText.length() < 100) {
            String fallbackContent = extractFallbackContent(driver);
            if (!fallbackContent.isEmpty()) {
                fullText.append("MainContent: ").append(fallbackContent).append("\n");
                logger.info("Used fallback content extraction, length: {}", fallbackContent.length());
            }
        }

        String finalText = fullText.toString().trim();

        // Handle content length - truncate if too long, supplement if too short
        if (finalText.length() > 30000) {
            finalText = finalText.substring(0, 30000) + "... [Truncated for analysis]";
            logger.warn("Truncated LinkedIn content to {} chars to avoid LLM timeouts", finalText.length());
        } else if (finalText.length() < 50) {
            finalText = "Company: " + companyName + "\nDescription: Limited information available from LinkedIn.";
            logger.warn("Very limited content extracted, using minimal fallback");
        }

        logger.info("Final content - Length: {}, Posts: {}", finalText.length(), posts.size());
        return finalText;
    }

    /**
     * Extracts fallback content when primary extraction yields insufficient data
     */
    private String extractFallbackContent(WebDriver driver) {
        try {
            String html = driver.getPageSource();
            Document soup = Jsoup.parse(html);

            // Try to get main content area
            Element mainElement = soup.selectFirst("main");
            if (mainElement != null) {
                String mainContent = mainElement.text();
                if (mainContent.length() > 100) {
                    return mainContent.length() > 2000 ? mainContent.substring(0, 2000) : mainContent;
                }
            }

            // Fallback to body text
            String bodyText = soup.text();
            return bodyText.length() > 2000 ? bodyText.substring(0, 2000) : bodyText;

        } catch (Exception e) {
            logger.error("Error extracting fallback content: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Saves debug content to file for troubleshooting
     */
    private void saveDebugContent(String companyId, String fullText) {
        try {
            String filename = "linkedin_" + companyId + "_fulltext.txt";
            Files.writeString(Paths.get(filename), fullText);
            logger.debug("Saved debug content to: {}", filename);
        } catch (Exception e) {
            logger.warn("Failed to save debug content: {}", e.getMessage());
        }
    }

    /**
     * Navigates to Posts tab to access more post content (from original
     * ScrapingService)
     */
    private void navigateToPostsTab(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // Try clicking Posts tab via linkText
            try {
                wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Posts")));
                driver.findElement(By.linkText("Posts")).click();
                Thread.sleep(3000);
                logger.debug("Clicked 'Posts' tab via linkText");
                return;
            } catch (Exception e) {
                logger.debug("Failed to click 'Posts' tab via linkText: {}", e.getMessage());
            }

            // Fallback to CSS selector
            try {
                wait.until(ExpectedConditions
                        .elementToBeClickable(By.cssSelector("a.org-page-navigation__item-anchor[href*='posts']")));
                driver.findElement(By.cssSelector("a.org-page-navigation__item-anchor[href*='posts']")).click();
                Thread.sleep(3000);
                logger.debug("Clicked 'Posts' tab via CSS selector");
                return;
            } catch (Exception e2) {
                logger.debug("Failed to click 'Posts' tab via CSS: {}", e2.getMessage());
            }

            // Final fallback - navigate to posts subpage directly
            String currentUrl = driver.getCurrentUrl();
            if (!currentUrl.endsWith("/posts/")) {
                String postsUrl = currentUrl.replaceAll("/$", "") + "/posts/";
                driver.get(postsUrl);
                Thread.sleep(3000);
                logger.debug("Fallback: Navigated to posts subpage");
            }

        } catch (Exception e) {
            logger.warn("Error navigating to Posts tab: {}", e.getMessage());
        }
    }

    /**
     * Clicks "See more" buttons to expand truncated posts (from original
     * ScrapingService)
     */
    private void clickSeeMoreButtons(WebDriver driver) {
        try {
            // Primary selector for "See more" buttons
            List<WebElement> seeMoreButtons = driver.findElements(
                    By.cssSelector("button[aria-label*='see more'], button span.feed-shared-see-more-text"));

            for (WebElement button : seeMoreButtons) {
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
                    Thread.sleep(1000);
                    logger.debug("Clicked 'See more' button");
                } catch (Exception e) {
                    logger.debug("Failed to click see more button: {}", e.getMessage());
                }
            }

            // Fallback selector for "See more" buttons
            if (seeMoreButtons.isEmpty()) {
                seeMoreButtons = driver.findElements(
                        By.xpath("//button[contains(text(), 'more') or contains(text(), 'See more')]"));
                for (WebElement button : seeMoreButtons) {
                    try {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
                        Thread.sleep(1000);
                        logger.debug("Clicked 'See more' button via xpath");
                    } catch (Exception e) {
                        logger.debug("Failed to click see more button via xpath: {}", e.getMessage());
                    }
                }
            }

            logger.debug("Processed {} 'See more' buttons", seeMoreButtons.size());

        } catch (Exception e) {
            logger.warn("Error clicking 'See more' buttons: {}", e.getMessage());
        }
    }

    /**
     * Performs infinite scrolling to load more posts (from original
     * ScrapingService)
     */
    private void performInfiniteScrolling(WebDriver driver) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            long lastHeight = (Long) js.executeScript("return document.body.scrollHeight");
            int attempts = 0;
            int maxAttempts = 10; // Reduced from original to be more reasonable

            logger.debug("Starting infinite scrolling to load more posts");

            while (attempts < maxAttempts) {
                // Scroll to bottom
                js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
                Thread.sleep(3000); // Wait for content to load

                // Check if new content loaded
                long newHeight = (Long) js.executeScript("return document.body.scrollHeight");
                logger.debug("Scroll attempt {}: lastHeight={}, newHeight={}", attempts + 1, lastHeight, newHeight);

                if (newHeight == lastHeight) {
                    logger.debug("No new content loaded, stopping scroll");
                    break;
                }

                lastHeight = newHeight;
                attempts++;
            }

            logger.debug("Infinite scrolling completed after {} attempts", attempts);

        } catch (Exception e) {
            logger.warn("Error during infinite scrolling: {}", e.getMessage());
        }
    }

    /**
     * Simple content preparation - ONLY formatting, no analysis
     */
    public String prepareContentForLLM(String companyName, String profileName, String description, List<String> posts) {
        StringBuilder content = new StringBuilder();

        content.append("=== COMPANY PROFILE ===\n");
        content.append("Company: ").append(companyName).append("\n");

        if (!profileName.isEmpty() && !profileName.equals(companyName)) {
            content.append("LinkedIn Profile Name: ").append(profileName).append("\n");
        }

        if (!description.isEmpty()) {
            content.append("Description: ").append(description).append("\n");
        }

        content.append("\n=== RECENT POSTS ===\n");
        if (!posts.isEmpty()) {
            for (int i = 0; i < Math.min(posts.size(), 10); i++) {
                content.append(i + 1).append(". ").append(posts.get(i)).append("\n");
            }
        } else {
            content.append("No recent posts available.\n");
        }

        return content.toString();
    }
}