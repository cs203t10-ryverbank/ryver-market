package cs203t10.ryver.market.stock.scrape;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import cs203t10.ryver.market.stock.Stock;
import cs203t10.ryver.market.stock.StockRecord;


public class ScraperFake{
    
    private List<String> getRecordFromLine(String line) {
        List<String> values = new ArrayList<String>();
        try (Scanner rowScanner = new Scanner(line)) {
            rowScanner.useDelimiter(",");
            while (rowScanner.hasNext()) {
                values.add(rowScanner.next());
            }
        }
        return values;
    }
    public List<StockRecord> buildFakeRecords() {
        List<StockRecord> records = new ArrayList<>();
        Date date = new Date();
        try (Scanner scanner = new Scanner(new File("src/main/java/cs203t10/ryver/market/stock/scrape/FakeStocks.csv"));) {
            while (scanner.hasNextLine()) {
                List<String> temp = getRecordFromLine(scanner.nextLine());
                String symbol = temp.get(3);
                Double price = Double.parseDouble(temp.get(0));
                Integer volume = Integer.parseInt(temp.get(2));
                Stock stock = Stock.builder().symbol(symbol).build();
                StockRecord fake = StockRecord.builder()
                    .stock(stock)
                    .submittedDate(date)
                    .price(price)
                    .totalVolume(volume)
                    .build();
                records.add(fake);
                System.out.println(String.format("Added StockRecord(%s, %s, %s, %s)", symbol, price, volume, date));
            }
        } catch(Exception ex) {
            System.out.println("Failed to load fake stocks");
            System.out.println(ex.getMessage());
            System.out.println(ex.getStackTrace());
        }
        return records;
    }
}