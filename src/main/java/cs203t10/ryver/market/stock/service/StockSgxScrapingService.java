package cs203t10.ryver.market.stock.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import cs203t10.ryver.market.stock.Stock;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfAllElementsLocatedBy;

/**
 * Scrape SGX for updated Straits Time Index data.
 */
@Service
public class StockSgxScrapingService implements StockService {

    public final static String SGX_URL = "https://www.sgx.com/indices/products/sti/";

    public StockSgxScrapingService() {
        System.setProperty("webdriver.chrome.driver", "lib/chromedriver");
    }

    @Override
    public List<Stock> getAllStocks() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        WebDriver driver = new ChromeDriver(options);
        Set<Stock> result = new HashSet<>(30);
        try {
            driver.get(SGX_URL);
            // Add stock data that is initially mounted to the DOM.
            result.addAll(getCurrentStocksFromMountedRows(driver));

            scrollTableDown(driver);

            // Add any stock data that has been newly mounted to the DOM.
            result.addAll(getCurrentStocksFromMountedRows(driver));
            return List.copyOf(result);
        } catch (Exception e) {
            return List.copyOf(result);
        } finally {
            driver.quit();
        }
    }

    /**
     * Scrape stock data from data table rows which are currently mounted
     * to the DOM.
     */
    private Set<Stock> getCurrentStocksFromMountedRows(WebDriver driver) throws InterruptedException {
        // Give some time for data to hydrate the DOM.
        Thread.sleep(800);
        WebDriverWait wait = new WebDriverWait(driver, 10);
        List<WebElement> tableRows = wait.until(
                presenceOfAllElementsLocatedBy(By.cssSelector("sgx-table-row")));
        return tableRows.stream()
                .map(StockSgxScrapingService::getStock)
                .collect(Collectors.toSet());
    }

    public void scrollTableDown(WebDriver driver) {
        // Scroll the data table down to render the rest of data required.
        WebElement scrollBar = driver.findElement(
                By.cssSelector(".vertical-scrolling-bar"));
        Actions dragger = new Actions(driver);
        dragger
            .moveToElement(scrollBar)
            .clickAndHold()
            .moveByOffset(0, 400)
            .build().perform();
    }

    private static Stock getStock(WebElement row) {
        return Stock.builder()
                .symbol(getSymbol(row))
                .lastPrice(getLastPrice(row))
                .totalVolume(getTotalVolume(row))
                .build();
    }

    private static WebElement getCellWithId(WebElement row, String id) {
        String selector = String.format("[data-column-id='%s']", id);
        return row.findElement(By.cssSelector(selector));
    }

    private static String getSymbol(WebElement row) {
        return getCellWithId(row, "nc").getAttribute("innerHTML");
    }

    private static Double getLastPrice(WebElement row) {
        String value = getCellWithId(row, "lt").getAttribute("innerHTML");
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private static Integer getTotalVolume(WebElement row) {
        String value = getCellWithId(row, "vl").getAttribute("innerHTML");
        value = value.replaceAll(",", "");
        try {
            return (int) (Double.parseDouble(value) * 1000);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

}
