package cs203t10.ryver.market.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cs203t10.ryver.market.trade.ScheduledTradeService;

@Service
public class DateService {

    @Autowired
    ScheduledTradeService scheduledService;

    /**
     * If open = true or false, the market will follow the artificial
     * value. To return the market to regular behaviour, set open = null.
     *
     * Upon setting open, the market will open or close itself.
     */
    private Boolean open = null;

    public Boolean isArtificial() {
        return open != null;
    }

    public void setOpen(Boolean open) {
        this.open = open;
        if (open != null) {
            if (open) {
                scheduledService.openMarket();
            } else {
                scheduledService.closeMarket();
            }
            return;
        }
        Date now = getCurrentDate();
        if (isBetween9to5(now) && isWeekDay(now)) {
            scheduledService.openMarket();
        } else {
            scheduledService.closeMarket();
        }
    }

    public boolean isMarketOpen() {
        return isMarketOpen(getCurrentDate());
    }

    /**
     * Market is open from 9am to 5pm every weekday.
     */
    public boolean isMarketOpen(Date date) {
        if (open != null) {
            return open;
        }
        return isBetween9to5(date) && isWeekDay(date);
    }

    public boolean isBetween9to5(Date date) {
        // Trade is made between 9am and 5pm.
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        String strCurrentTime = formatter.format(date);
        LocalTime target = LocalTime.parse(strCurrentTime);

        return !(target.isBefore(LocalTime.parse("09:00:00")) || target.isAfter(LocalTime.parse("17:00:00")));
    }

    public boolean isWeekDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            return false;
        }
        return true;
    }

    public boolean isFuture(Date date, Date today) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddhhmmss");
        return fmt.format(date).compareTo(fmt.format(today)) > 0;
    }

    public boolean isFuture(Date date) {
        return isFuture(date, getCurrentDate());
    }

    public Date getCurrentDate() {
        ZoneId defaultZoneId = ZoneId.systemDefault();
        LocalDateTime localDate = LocalDateTime.now();
        Date todayDate = Date.from(localDate.atZone(defaultZoneId).toInstant());
        return todayDate;
    }

}

