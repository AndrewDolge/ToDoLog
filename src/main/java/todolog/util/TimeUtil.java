package todolog.util;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility Class that contains static methods to normalize log times across the
 * system.
 * 
 * The system uses epoch seconds, a Long that represents the number of seconds
 * since the unix epoch.
 */
public class TimeUtil {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("Y-MM-dd hh:mm a");



    public static String convertEpochToString(long epoch) {
        return LocalDateTime.ofEpochSecond(epoch, 0, getDefaultOffset()).format(formatter);
    }// convertEpochToString

    public static ZoneOffset getDefaultOffset() {
        return OffsetDateTime.now().getOffset();
    }

    
    public static long getNow() {
        return ZonedDateTime.now().toEpochSecond();
    }// getNow

    private static ZonedDateTime getStartOfDay() {
        return ZonedDateTime.now().toLocalDate().atStartOfDay(ZoneId.systemDefault());
    }// getStartOfDay

    public static long getDayStart() {
        return getStartOfDay().toEpochSecond();
    }// getDayStart

    public static long getNextDayStart(){
        return getStartOfDay().plusDays(1).toEpochSecond();
    }
    
    /*
    public static long getNow() {
        return ZonedDateTime.now().toEpochSecond();
    }// getNow

    private static ZonedDateTime getStartOfDay() {
        return ZonedDateTime.now().toLocalDate().atStartOfDay(ZoneId.systemDefault());
    }// getStartOfDay

    public static long getDayStart() {
        return getStartOfDay().toLocalDate().atTime(ZonedDateTime.now().getHour(), ZonedDateTime.now().getMinute(),0).toEpochSecond(getDefaultOffset());
    }// getDayStart

    public static long getNextDayStart() {
        return getDayStart() + 60;
    }// getNextDayStart
    */
    

}