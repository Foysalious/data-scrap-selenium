package scraper;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.NoSuchElementException;
import com.google.gson.Gson;

public class WebScraper {
    public static void main(String[] args) throws IOException {
        // Set up the ChromeDriver path
        WebDriverManager.chromedriver().setup();
        // Initialize the WebDriver
        WebDriver driver = new ChromeDriver();

        try {
            // Open the website
            driver.get("https://www.discoveryplus.com/ca/");

            // Scroll down twice to load more content
            JavascriptExecutor js = (JavascriptExecutor) driver;
            for (int i = 0; i < 1; i++) { // Scroll once
                js.executeScript("window.scrollBy(0, document.body.scrollHeight);");
                Thread.sleep(2000); // Wait for new content to load, adjust as needed
            }

            // Locate and interact with elements containing package information
            List<WebElement> packages = driver.findElements(By.cssSelector(".plan-v2 .l-offer")); // Select all package elements

            // Create a list to hold package data
            List<Package> packageList = new ArrayList<>();

            // Loop through each package and extract the data
            for (WebElement pkg : packages) {
                String title = pkg.findElement(By.className("l-title")).getText();
                String description = pkg.findElement(By.className("l-text")).getText();
                String price = pkg.findElement(By.className("l-price")).getText();
                String link = pkg.findElement(By.tagName("a")).getAttribute("href");

                // Create a Package object and add it to the list
                packageList.add(new Package(title, description, price, link));
            }

            // Extract FAQ section
            List<WebElement> faqs = driver.findElements(By.cssSelector(".faq-item-wrapper"));
            List<FAQ> faqList = new ArrayList<>();

            // Loop through the FAQ items
            for (WebElement faqItem : faqs) {
                // Click the FAQ header to reveal the answer
                WebElement faqHeader = faqItem.findElement(By.cssSelector(".faq-header-wrapper"));
                faqHeader.click();  // Simulate click to expand the answer

                // Extract question and answers
                String question = faqItem.findElement(By.cssSelector(".faq-item-heading")).getText();
                List<WebElement> answersElements = faqItem.findElements(By.cssSelector("[data-faq-type='answer']"));
                List<String> answers = new ArrayList<>();

                for (WebElement answer : answersElements) {
                    answers.add(answer.getText());
                }

                // Create an FAQ object and add it to the list
                faqList.add(new FAQ(question, answers));
            }

            // Create a final object to hold both the package and FAQ data
            ScrapedData scrapedData = new ScrapedData(packageList, faqList);

            // Convert the list to JSON format
            Gson gson = new Gson();
            String jsonOutput = gson.toJson(scrapedData);

            // Write the JSON output to a file
            try (FileWriter jsonWriter = new FileWriter("scraped_data.json")) {
                jsonWriter.write(jsonOutput);
            }
        } catch (NoSuchElementException e) {
            System.err.println("Element not found: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupted status
            System.err.println("Thread was interrupted: " + e.getMessage());
        } finally {
            // Close the browser
            driver.quit();
        }
    }

    // Package class to hold the package data
    static class Package {
        private String title;
        private String description;
        private String price;
        private String link;

        public Package(String title, String description, String price, String link) {
            this.title = title;
            this.description = description;
            this.price = price;
            this.link = link;
        }
    }

    // FAQ class to hold the FAQ data
    static class FAQ {
        private String question;
        private List<String> answers;

        public FAQ(String question, List<String> answers) {
            this.question = question;
            this.answers = answers;
        }
    }

    // Class to hold the overall scraped data
    static class ScrapedData {
        private List<Package> packages;
        private List<FAQ> faqs;

        public ScrapedData(List<Package> packages, List<FAQ> faqs) {
            this.packages = packages;
            this.faqs = faqs;
        }
    }
}