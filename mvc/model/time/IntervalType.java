package mvc.model.time;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Seconds;

/**
 * Types of time intervals; values are in milliseconds
 */
public enum IntervalType {
  QUARTER, HOUR, DAY, WEEK, MONTH, YEAR;

  private final DateTimeZone defaultTimeZone = DateTimeZone.forID("America/New_York");

  /**
   * Returns the length in seconds of this interval in the given date
   * @param d the time where the interval starts
   * @return int in seconds of the length of this interval base on the given date
   */
  public int getSeconds(DateTime d) {
    DateTimeZone.setDefault(defaultTimeZone);

    switch (this) {
      case QUARTER:
        return 900;
      case HOUR:
        return 3600;
      case DAY:
        return Seconds.secondsBetween(d, d.plusDays(1)).getSeconds();
      case WEEK:
        return Seconds.secondsBetween(d, d.plusWeeks(1)).getSeconds();
      case MONTH:
        return Seconds.secondsBetween(d, d.plusMonths(1)).getSeconds();
      case YEAR:
        return Seconds.secondsBetween(d, d.plusYears(1)).getSeconds();
      default:
        throw new RuntimeException(this.name() + " is not a valid interval!");
    }
  }

}