package mvc.model.time;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * Contains all the utility methods used for calculating time related operations.
 */
public class TimeOp {

  // =====================
  // Floor/Ceiling Methods
  // =====================

  /**
   * Floors the given unix in terms of a week
   * @param unix
   * @return
   */
  public static long floorWeek(long unix) {
    DateTime d = new DateTime(unix * 1000L, DateTimeZone.forID("America/New_York"));
    return d.minusHours(d.getHourOfDay()).minusDays(d.getDayOfWeek() - 1).getMillis() / 1000L;
  }

  /**
   * Ceilings the given unix in terms of a week
   * @param unix
   * @return
   */
  public static long ceilWeek(long unix) {
    DateTime d = new DateTime(unix * 1000L, DateTimeZone.forID("America/New_York"));
    return d.plusDays(7 - d.getDayOfWeek() + 1).minusHours(d.getHourOfDay()).getMillis() / 1000L;
  }

  /**
   * Floors the given unix in terms of a date
   * @param unix
   * @return
   */
  public static long floorDate(long unix) {
    DateTime d = new DateTime(unix * 1000L, DateTimeZone.forID("America/New_York"));
    DateTime newD = new DateTime(d.getYear(), d.getMonthOfYear(), d.getDayOfMonth(),
            0, 0, 0, 000, DateTimeZone.forID("America/New_York"));
    return newD.getMillis() / 1000L;
  }

  /**
   * Floors the given unix in terms of a month
   * @param unix
   * @return
   */
  public static long floorMonth(long unix) {
    DateTime d = new DateTime(unix * 1000L, DateTimeZone.forID("America/New_York"));
    DateTime newD = new DateTime(d.getYear(), d.getMonthOfYear(), 1,
            0, 0, 0, 000, DateTimeZone.forID("America/New_York"));
    return newD.getMillis() / 1000L;
  }

  /**
   * Floors the given unix in terms of a year
   * @param unix
   * @return
   */
  public static long floorYear(long unix) {
    DateTime d = new DateTime(unix * 1000L, DateTimeZone.forID("America/New_York"));
    DateTime newD = new DateTime(d.getYear(), 1, 1,
            0, 0, 0, 000, DateTimeZone.forID("America/New_York"));
    return newD.getMillis() / 1000L;
  }
}
