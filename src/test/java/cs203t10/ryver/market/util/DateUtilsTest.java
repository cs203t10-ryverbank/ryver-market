package cs203t10.ryver.market.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.Test;

public class DateUtilsTest {

    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");

    @Test
    public void isMarketOpen_rightTimeAndDate() throws ParseException {
        Date rightTimeAndDate = format.parse("2020/10/20 10:10:10");
        assertTrue(DateUtils.isMarketOpen(rightTimeAndDate));
    }

    @Test
    public void isMarketOpen_wrongTimeRightDate() throws ParseException {
        Date tooEarly = format.parse("2020/10/20 08:10:10");
        Date tooLate = format.parse("2020/10/20 19:10:10");
        assertFalse(DateUtils.isMarketOpen(tooEarly));
        assertFalse(DateUtils.isMarketOpen(tooLate));
    }

    @Test
    public void isMarketOpen_rightOn9and5() throws ParseException {
        Date justEarly = format.parse("2020/10/20 09:00:00");
        Date justLate = format.parse("2020/10/20 17:00:00");
        assertTrue(DateUtils.isMarketOpen(justEarly));
        assertTrue(DateUtils.isMarketOpen(justLate));
    }

    @Test
    public void isMarketOpen_rightTimeWrongDate() throws ParseException {
        Date saturday = format.parse("2020/10/17 10:10:10");
        Date sunday = format.parse("2020/10/18 10:10:10");
        assertFalse(DateUtils.isMarketOpen(saturday));
        assertFalse(DateUtils.isMarketOpen(sunday));
    }

    @Test
    public void isFuture_byDate() throws ParseException {
        Date saturday = format.parse("2020/10/17 10:10:10");
        Date sunday = format.parse("2020/10/18 10:10:10");
        assertFalse(DateUtils.isFuture(saturday, saturday));
        assertFalse(DateUtils.isFuture(saturday, sunday));
        assertTrue(DateUtils.isFuture(sunday, saturday));
    }

    @Test
    public void isFuture_byTime() throws ParseException {
        Date first = format.parse("2020/10/17 10:10:10");
        Date second = format.parse("2020/10/17 10:16:10");
        assertFalse(DateUtils.isFuture(first, first));
        assertFalse(DateUtils.isFuture(first, second));
        assertTrue(DateUtils.isFuture(second, first));
    }

}
