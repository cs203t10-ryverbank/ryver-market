package cs203t10.ryver.market.stock.scrape;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;

import cs203t10.ryver.market.stock.Stock;
import cs203t10.ryver.market.stock.StockRecord;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfAllElementsLocatedBy;

public class SgxScraper {

    public final static String SGX_URL = "https://www.sgx.com/indices/products/sti/";

    WebDriver driver;

    Date scrapeDate = new Date();

    public SgxScraper() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--window-size=800,600");
        driver = new ChromeDriver(options);
    }

    public List<StockRecord> getAllStockRecords() {
        Set<StockRecord> result = new HashSet<>(30);
        try {
            driver.get(SGX_URL);
            // Wait for page data to load.
            Thread.sleep(800);
            scrollToTable();
            hideConsentBanner();
            // Add stock data that is initially mounted to the DOM.
            result.addAll(getCurrentStockRecordsFromMountedRows());
            scrollTableDown();
            // Give some time for data to hydrate the DOM.
            Thread.sleep(800);
            // Add any stock data that has been newly mounted to the DOM.
            result.addAll(getCurrentStockRecordsFromMountedRows());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            driver.quit();
        }
        return result.stream()
                .filter(record -> record.getStock().getSymbol() != null)
                .collect(Collectors.toList());
    }

    private void scrollToTable() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("document.querySelector('sgx-table-list')"
                + ".scrollIntoView()", "");
    }

    private void hideConsentBanner() {
        // Hide consent banner.
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("document.querySelector('sgx-consent-banner')"
                + ".style.display = 'none'", "");
    }

    /**
     * Scrape stock data from data table rows which are currently mounted
     * to the DOM.
     */
    private Set<StockRecord> getCurrentStockRecordsFromMountedRows()
            throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        List<WebElement> tableRows = wait.until(
                presenceOfAllElementsLocatedBy(By.cssSelector("sgx-table-row")));
        return tableRows.stream()
                .map(row -> getStock(row))
                .filter(record -> !record.getStock().getSymbol().isBlank())
                .collect(Collectors.toSet());
    }

    /**
     * Simulate interaction with the interface.
     *
     * The scrollbar element must be visible.
     */
    private void scrollTableDown() {
        // Scroll the data table down to render the rest of data required.
        WebElement scrollBar = driver.findElement(
                By.cssSelector(".vertical-scrolling-bar"));
        Actions dragger = new Actions(driver);
        dragger
            .moveToElement(scrollBar)
            .clickAndHold()
            .moveByOffset(0, 200)
            .build().perform();
    }

    private StockRecord getStock(WebElement row) {
        Stock stock = Stock.builder().symbol(getSymbol(row)).build();
        return StockRecord.builder()
                .stock(stock)
                // Use date when scraper was initialized as the submitted
                // date for all stocks scraped in this instance.
                .submittedDate(scrapeDate)
                .price(getLastPrice(row))
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
