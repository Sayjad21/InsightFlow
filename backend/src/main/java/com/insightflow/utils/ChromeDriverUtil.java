package com.insightflow.utils;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.util.Random;

/**
 * Utility class for Chrome WebDriver management and anti-detection measures.
 * Handles WebDriver lifecycle, configuration, and human-like behavior simulation.
 */
@Component
public class ChromeDriverUtil {

    private static final Logger logger = LoggerFactory.getLogger(ChromeDriverUtil.class);

    private final Random random = new Random();
    
    private final String[] userAgents = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36"
    };

    /**
     * Cleanup any orphaned Chrome processes that might interfere with new sessions
     */
    public void cleanupChromeProcesses() {
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

    /**
     * Creates and configures a Chrome WebDriver instance with anti-detection measures
     * @return Configured WebDriver instance ready for use
     */
    public WebDriver createWebDriver() {
        logger.info("Creating Chrome WebDriver with anti-detection measures");

        // Clean up any orphaned processes first
        cleanupChromeProcesses();

        // Check if running in container environment (Render/Docker)
        String chromeBinary = System.getenv("CHROME_BIN");
        String chromeDriver = System.getenv("CHROME_DRIVER");
        boolean isContainerEnvironment = chromeBinary != null && !chromeBinary.isEmpty();

        // Setup ChromeDriver
        if (isContainerEnvironment && chromeDriver != null && !chromeDriver.isEmpty()) {
            logger.info("Container environment detected, using system ChromeDriver: {}", chromeDriver);
            System.setProperty("webdriver.chrome.driver", chromeDriver);
        } else {
            logger.info("Using WebDriverManager to setup ChromeDriver");
            WebDriverManager.chromedriver().setup();
        }

        ChromeOptions options = createChromeOptions(isContainerEnvironment, chromeBinary);

        try {
            WebDriver driver = new ChromeDriver(options);
            logger.info("✅ Chrome WebDriver instance created successfully");
            
            // Configure timeouts and add anti-detection measures
            configureWebDriver(driver);
            
            return driver;
        } catch (Exception e) {
            logger.error("❌ Failed to create Chrome WebDriver instance: {}", e.getMessage());
            
            // Detailed error logging for debugging
            logEnvironmentDetails(chromeBinary, chromeDriver, isContainerEnvironment);
            
            // Try fallback configuration in container environment
            if (isContainerEnvironment) {
                return createFallbackWebDriver(chromeBinary);
            }
            
            throw new RuntimeException("Failed to create Chrome WebDriver", e);
        }
    }

    /**
     * Creates Chrome options with anti-detection configuration
     */
    private ChromeOptions createChromeOptions(boolean isContainerEnvironment, String chromeBinary) {
        ChromeOptions options = new ChromeOptions();

        if (isContainerEnvironment) {
            logger.info("Container environment detected, using Chromium binary: {}", chromeBinary);
            options.setBinary(chromeBinary);
        }

        // Create unique temporary user data directory for each session
        String tempUserDataDir = System.getProperty("java.io.tmpdir") + "chrome_user_data_" +
                System.currentTimeMillis() + "_" + random.nextInt(10000);
        logger.info("Created temporary Chrome user data directory: {}", tempUserDataDir);

        // Enhanced anti-detection measures
        String selectedUserAgent = userAgents[random.nextInt(userAgents.length)];
        String windowSize = (1920 + random.nextInt(100)) + "," + (1080 + random.nextInt(100));

        logger.info("Configuring Chrome options:");
        logger.info("  - User Agent: {}", selectedUserAgent);
        logger.info("  - Window Size: {}", windowSize);
        logger.info("  - Container Environment: {}", isContainerEnvironment);

        if (isContainerEnvironment) {
            // Container-optimized Chrome arguments with better anti-detection
            options.addArguments("--headless");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--disable-extensions");
            options.addArguments("--disable-plugins");
            options.addArguments("--disable-background-timer-throttling");
            options.addArguments("--disable-backgrounding-occluded-windows");
            options.addArguments("--disable-renderer-backgrounding");
            options.addArguments("--no-first-run");
            options.addArguments("--disable-default-apps");
            options.addArguments("--window-size=" + windowSize);
            options.addArguments("--user-data-dir=" + tempUserDataDir);
            options.addArguments("--user-agent=" + selectedUserAgent);
            // Additional anti-detection arguments
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--exclude-switches=enable-automation");
            options.addArguments("--disable-web-security");
            options.addArguments("--allow-running-insecure-content");
            options.addArguments("--disable-features=VizDisplayCompositor");
            logger.info("Using container-optimized Chrome arguments with anti-detection");
        } else {
            // Local development Chrome arguments
            options.addArguments("--headless=new");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--window-size=" + windowSize);
            options.addArguments("--disable-notifications");
            options.addArguments("--user-data-dir=" + tempUserDataDir);
            options.addArguments("--disable-background-timer-throttling");
            options.addArguments("--disable-backgrounding-occluded-windows");
            options.addArguments("--disable-renderer-backgrounding");
            options.addArguments("--disable-features=TranslateUI");
            options.addArguments("--disable-ipc-flooding-protection");
            options.addArguments("user-agent=" + selectedUserAgent);
            logger.info("Using local development Chrome arguments");
        }

        return options;
    }

    /**
     * Configures WebDriver with timeouts and anti-detection JavaScript
     */
    private void configureWebDriver(WebDriver driver) {
        logger.info("Configuring WebDriver timeouts and anti-detection measures");
        
        // Set timeouts
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));

        // Enhanced JavaScript to hide automation markers
        logger.info("Executing enhanced JavaScript to hide automation markers");
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;

        // Hide webdriver property
        jsExecutor.executeScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");

        // Remove automation indicators
        jsExecutor.executeScript(
                "window.chrome = { runtime: {} }; Object.defineProperty(navigator, 'plugins', { get: () => [1, 2, 3, 4, 5] }); Object.defineProperty(navigator, 'languages', { get: () => ['en-US', 'en'] });");

        // Add realistic properties
        jsExecutor.executeScript(
                "Object.defineProperty(navigator, 'permissions', { get: () => ({ query: () => Promise.resolve({ state: 'granted' }) }) });");

        // Mimic human mouse behavior
        jsExecutor.executeScript(
                "['mousedown', 'mouseup', 'mousemove'].forEach(event => { document.addEventListener(event, () => {}, true); });");
    }

    /**
     * Attempts to create a fallback WebDriver with minimal configuration
     */
    private WebDriver createFallbackWebDriver(String chromeBinary) {
        logger.warn("Attempting fallback with minimal Chrome arguments");
        try {
            ChromeOptions fallbackOptions = new ChromeOptions();
            fallbackOptions.setBinary(chromeBinary);

            // Only the most essential arguments
            fallbackOptions.addArguments("--headless");
            fallbackOptions.addArguments("--no-sandbox");
            fallbackOptions.addArguments("--disable-dev-shm-usage");

            WebDriver driver = new ChromeDriver(fallbackOptions);
            logger.info("✅ Minimal fallback Chrome WebDriver instance created successfully");
            
            // Basic configuration
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));
            
            return driver;
        } catch (Exception fallbackException) {
            logger.error("❌ Minimal fallback Chrome WebDriver creation also failed: {}", fallbackException.getMessage());
            throw new RuntimeException("Failed to create Chrome WebDriver even with fallback configuration", fallbackException);
        }
    }

    /**
     * Logs detailed environment information for debugging WebDriver issues
     */
    private void logEnvironmentDetails(String chromeBinary, String chromeDriver, boolean isContainerEnvironment) {
        logger.error("Chrome Binary Path: {}", chromeBinary != null ? chromeBinary : "system default");
        logger.error("ChromeDriver Path: {}", chromeDriver != null ? chromeDriver : "WebDriverManager managed");
        logger.error("Is Container Environment: {}", isContainerEnvironment);
        logger.error("Java Version: {}", System.getProperty("java.version"));
        logger.error("OS Name: {}", System.getProperty("os.name"));
        logger.error("OS Arch: {}", System.getProperty("os.arch"));

        // Check if Chrome binary exists
        if (chromeBinary != null) {
            try {
                java.nio.file.Path chromePath = java.nio.file.Paths.get(chromeBinary);
                boolean exists = java.nio.file.Files.exists(chromePath);
                boolean executable = java.nio.file.Files.isExecutable(chromePath);
                logger.error("Chrome binary exists: {}, executable: {}", exists, executable);
            } catch (Exception pathEx) {
                logger.error("Error checking Chrome binary path: {}", pathEx.getMessage());
            }
        }

        // Check if ChromeDriver exists
        if (chromeDriver != null) {
            try {
                java.nio.file.Path driverPath = java.nio.file.Paths.get(chromeDriver);
                boolean exists = java.nio.file.Files.exists(driverPath);
                boolean executable = java.nio.file.Files.isExecutable(driverPath);
                logger.error("ChromeDriver exists: {}, executable: {}", exists, executable);
            } catch (Exception pathEx) {
                logger.error("Error checking ChromeDriver path: {}", pathEx.getMessage());
            }
        }

        // Test Chrome binary directly in container environment
        if (isContainerEnvironment && chromeBinary != null) {
            testChromeBinary(chromeBinary);
        }
    }

    /**
     * Tests Chrome binary directly to verify it works
     */
    private void testChromeBinary(String chromeBinary) {
        try {
            logger.warn("Testing Chrome binary directly: {}", chromeBinary);
            ProcessBuilder pb = new ProcessBuilder(chromeBinary, "--version");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            logger.warn("Chrome binary test - Exit code: {}, Output: {}", exitCode, output.toString());

            if (exitCode != 0) {
                logger.error("Chrome binary failed to run - this is likely the root cause");
            }
        } catch (Exception testEx) {
            logger.error("Failed to test Chrome binary directly: {}", testEx.getMessage());
        }
    }

    /**
     * Simulates human-like typing with random delays between keystrokes
     * @param element WebElement to type into
     * @param text Text to type
     */
    public void typeHumanLike(WebElement element, String text) {
        try {
            element.clear();
            for (char c : text.toCharArray()) {
                element.sendKeys(String.valueOf(c));
                // Random delay between keystrokes (50-200ms)
                int delay = 50 + random.nextInt(150);
                Thread.sleep(delay);
            }
            logger.debug("Typed text with human-like delays: {} characters", text.length());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Human-like typing interrupted: {}", e.getMessage());
            // Fallback to normal typing
            element.clear();
            element.sendKeys(text);
        } catch (Exception e) {
            logger.error("Error during human-like typing: {}", e.getMessage());
            throw new RuntimeException("Failed to type text", e);
        }
    }

    /**
     * Performs a human-like delay with random variance
     * @param baseDelayMs Base delay in milliseconds
     * @param varianceMs Additional random variance in milliseconds
     */
    public void humanLikeDelay(int baseDelayMs, int varianceMs) {
        try {
            int delay = baseDelayMs + (varianceMs > 0 ? random.nextInt(varianceMs) : 0);
            logger.debug("Human-like delay: {} ms", delay);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Human-like delay interrupted: {}", e.getMessage());
        }
    }

    /**
     * Gets a random user agent from the predefined list
     * @return Random user agent string
     */
    public String getRandomUserAgent() {
        return userAgents[random.nextInt(userAgents.length)];
    }

    /**
     * Safely closes WebDriver and cleans up resources
     * @param driver WebDriver instance to close
     */
    public void closeWebDriver(WebDriver driver) {
        if (driver != null) {
            try {
                driver.quit();
                logger.info("WebDriver closed successfully");
            } catch (Exception e) {
                logger.warn("Error closing WebDriver: {}", e.getMessage());
            }
        }
    }
}