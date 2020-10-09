package cs203t10.ryver.market.stock.service;

import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
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
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, 10);
        try {
            driver.get(SGX_URL);
            List<WebElement> tableRows = wait.until(presenceOfAllElementsLocatedBy(By.cssSelector("sgx-table-row")));
            return tableRows.stream()
                    .map(StockSgxScrapingService::getStock)
                    .collect(Collectors.toList());
        } finally {
            driver.quit();
        }
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
        return Double.parseDouble(value);
    }

    private static Integer getTotalVolume(WebElement row) {
        String value = getCellWithId(row, "vl").getAttribute("innerHTML");
        value = value.replaceAll(",", "");
        return (int) (Double.parseDouble(value) * 1000);
    }

}
