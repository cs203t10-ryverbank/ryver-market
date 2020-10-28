package cs203t10.ryver.market.util;

public class DoubleUtils {

    private DoubleUtils() { }

    public static Double getRoundedToNearestCent(Double value) {
        int centsPerDollar = 100;
        Double roundedValue = value * centsPerDollar;
        roundedValue = (double) Math.round(value);
        roundedValue /= centsPerDollar;
        return roundedValue;
    }

}

