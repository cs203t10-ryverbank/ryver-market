package cs203t10.ryver.market;

import java.util.Date;

import cs203t10.ryver.market.stock.Stock;

public class TestConstants {
    final public static Date FIRST_DATE = new Date(1604471501000L);
    final public static Date SECOND_DATE = new Date(1604471502000L);

    final public static Integer EXISTS_ID = 1;
    final public static Integer NOT_EXISTS_ID = 2;

    final public static Integer CUSTOMER_ID = 1;
    final public static Integer ACCOUNT_ID = 50;
    final public static String SYMBOL = "A1";
    final public static Stock STOCK = new Stock(SYMBOL);

    final public static Integer BUY_QUANTITY = 10000;
    final public static Integer BUY_QUANTITY_MARKET_TO_HIGH_PRICE_NOT_ENOUGH = 8300;
    final public static Integer SELL_QUANTITY = 10000;
    final public static Integer PARTIAL_QUANTITY = 5000;
    final public static Double MARKET_PRICE = 2.5;
    final public static Double HIGH_PRICE = 3.0;
    final public static Double LOW_PRICE = 2.0;
}
