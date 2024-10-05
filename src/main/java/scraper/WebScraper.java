package scraper;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import java.io.FileWriter;
import java.io.IOException;
import org.openqa.selenium.NoSuchElementException;

public class WebScraper {
    public static void main(String[] args) throws IOException {
        // Set up the ChromeDriver path
        WebDriverManager.chromedriver().setup();
        // Initialize the WebDriver
        WebDriver driver = new ChromeDriver();

        try {
            // Open the website
            driver.get("https://www.discoveryplus.com/ca/");

            // Locate and interact with elements
            WebElement element = driver.findElement(By.id("elementID")); // Replace "elementID" with actual ID
            String data = element.getText();

            // Save data to a CSV file
            try (FileWriter csvWriter = new FileWriter("scraped_data.csv")) {
                csvWriter.append(data);
            }
        } catch (NoSuchElementException e) {
            System.err.println("Element not found: " + e.getMessage());
        } finally {
            // Close the browser
            driver.quit();
        }
    }
}