package cs203t10.ryver.market.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    private DateUtils() { }

    /**
     * Market is open from 9am to 5pm every weekday.
     */
    public static boolean isMarketOpen(Date date) {
        return isBetween9to5(date) && isWeekDay(date);
    }

    public static boolean isBetween9to5(Date date) {
        // Trade is made between 9am and 5pm.
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        String strCurrentTime = formatter.format(date);
        LocalTime target = LocalTime.parse(strCurrentTime);

        return !(target.isBefore(LocalTime.parse("09:00:00")) || target.isAfter(LocalTime.parse("17:00:00")));
    }

    public static boolean isWeekDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            return false;
        }
        return true;
    }

    public static boolean isFuture(Date date, Date today) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddhhmmss");
        return fmt.format(date).compareTo(fmt.format(today)) > 0;
    }

    public static boolean isFuture(Date date) {
        return isFuture(date, getCurrentDate());
    }

    public static Date getCurrentDate() {
        ZoneId defaultZoneId = ZoneId.systemDefault();
        LocalDateTime localDate = LocalDateTime.now();
        Date todayDate = Date.from(localDate.atZone(defaultZoneId).toInstant());
        return todayDate;
    }

}

