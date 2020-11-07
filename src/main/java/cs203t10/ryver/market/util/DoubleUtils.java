package cs203t10.ryver.market.util;

public class DoubleUtils {

    private DoubleUtils() { }

    public static Double getRoundedToNearestCent(Double value) {
        int centsPerDollar = 100;
        Double roundedValue = value * centsPerDollar;
        roundedValue = (double) Math.round(roundedValue);
        roundedValue /= centsPerDollar;
        return roundedValue;
    }

    public static Integer getFlooredToNearestHundred(double value) {
        Integer stocksPerHundred = 100;
        return ((int) value / 100) * stocksPerHundred;
    }

}

