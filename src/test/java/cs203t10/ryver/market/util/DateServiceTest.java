package cs203t10.ryver.market.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DateServiceTest {

    DateService dateService;

    @BeforeEach
    public void setupDateService() {
        dateService = new DateService();
    }

    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");

    @Test
    public void isMarketOpen_rightTimeAndDate() throws ParseException {
        Date rightTimeAndDate = format.parse("2020/10/20 10:10:10");
        assertTrue(dateService.isMarketOpen(rightTimeAndDate));
    }

    @Test
    public void isMarketOpen_wrongTimeRightDate() throws ParseException {
        Date tooEarly = format.parse("2020/10/20 08:10:10");
        Date tooLate = format.parse("2020/10/20 19:10:10");
        assertFalse(dateService.isMarketOpen(tooEarly));
        assertFalse(dateService.isMarketOpen(tooLate));
    }

    @Test
    public void isMarketOpen_rightOn9and5() throws ParseException {
        Date justEarly = format.parse("2020/10/20 09:00:00");
        Date justLate = format.parse("2020/10/20 17:00:00");
        assertTrue(dateService.isMarketOpen(justEarly));
        assertTrue(dateService.isMarketOpen(justLate));
    }

    @Test
    public void isMarketOpen_rightTimeWrongDate() throws ParseException {
        Date saturday = format.parse("2020/10/17 10:10:10");
        Date sunday = format.parse("2020/10/18 10:10:10");
        assertFalse(dateService.isMarketOpen(saturday));
        assertFalse(dateService.isMarketOpen(sunday));
    }

    @Test
    public void isFuture_byDate() throws ParseException {
        Date saturday = format.parse("2020/10/17 10:10:10");
        Date sunday = format.parse("2020/10/18 10:10:10");
        assertFalse(dateService.isFuture(saturday, saturday));
        assertFalse(dateService.isFuture(saturday, sunday));
        assertTrue(dateService.isFuture(sunday, saturday));
    }

    @Test
    public void isFuture_byTime() throws ParseException {
        Date first = format.parse("2020/10/17 10:10:10");
        Date second = format.parse("2020/10/17 10:16:10");
        assertFalse(dateService.isFuture(first, first));
        assertFalse(dateService.isFuture(first, second));
        assertTrue(dateService.isFuture(second, first));
    }

}
