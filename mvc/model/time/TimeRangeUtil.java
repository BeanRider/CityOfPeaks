package mvc.model.time;
import java.text.SimpleDateFormat;

import org.joda.time.*;

/**
 * Represents a time spanType, including its bounds, interval, and current value
 */
public class TimeRangeUtil {

  public static final int START = -1;
  public static final int CUR = 0;
  public static final int END = 1;

  private DateTime start;
  // INVARIANT: START <= CURRENT <= (END - subInterval)
  private DateTime current;
  private DateTime end;

  private IntervalType spanType;

  // For caching
  private int segmentCount = 0;

  /**
   * Constructs by: Setting a starting time, length, and interval
   * @param startUnix   - initial begin time (seconds)
   * @param spanType    - entire segment length
   */
  public TimeRangeUtil(long startUnix, IntervalType spanType) {
    DateTimeZone.setDefault(DateTimeZone.forID("America/New_York"));

    updateTime(startUnix, spanType);

    System.out.println(start.getMillis());
    System.out.println(end.getMillis());
  }

  /**
   * @param year
   * @return how many days in given year
   */
  public static int daysInYear(int year) {
    DateTime dateTime = new DateTime(year, 1, 14, 0, 0, 0, 000, DateTimeZone.forID("America/New_York"));
    return dateTime.dayOfYear().getMaximumValue();
  }

  /**
   * @param year
   * @param month
   * @return how many days in given year and month
   */
  public static int daysInMonth(int year, int month) {
    DateTime dateTime = new DateTime(year, month, 14, 0, 0, 0, 000, DateTimeZone.forID("America/New_York"));
    return dateTime.dayOfMonth().getMaximumValue();
  }

  /**
   * Convert the given eastern YMD into unix time
   * @param year given eastern year
   * @param month given eastern month
   * @param day given eastern day
   * @return converted unix timestamp (seconds)
   */
  public static long getUnixInEastern(int year, int month, int day, int hour) {
    DateTime dateTime = new DateTime(year, month, day, hour, 0, 0, DateTimeZone.forID("America/New_York"));
    return dateTime.getMillis() / 1000L;
  }

  public static DateTime computeDatePlusInterval(DateTime start, IntervalType length) {
    switch (length) {
      case YEAR:
        return start.plusYears(1);
      case MONTH:
        return start.plusMonths(1);
      case WEEK:
        return start.plusWeeks(1);
      case DAY:
        return start.plusDays(1);
      case HOUR:
        return start.plusHours(1);
      default:
        throw new RuntimeException("Unsupported IntervalType: " + length.name());
    }
  }

  /**
   * @return how many days are in this interval
   */
  public int computeHowMany15Mins() {
    return Hours.hoursBetween(start, end).getHours() * 4;
  }

  /**
   * Pre-condition: given start must be rounded in terms of 15 minutes
   * @param start rounded in 15 minutes
   * @param length length of time after start
   * @return number of 15 minutes between start and start + length
   */
  public static int computeHowMany15Mins(DateTime start, IntervalType length) {
    return Hours.hoursBetween(start, computeDatePlusInterval(start, length)).getHours() * 4;
  }

  /**
   * MUTATION: Updates the timeline in terms of starting unix and timeline length
   * @param newUnixStartTime in seconds
   * @param newLength
   */
  public void updateTime(long newUnixStartTime, IntervalType newLength) {
    spanType = newLength;

    System.out.println("newUnixStartTime:" + newUnixStartTime);
    System.out.println("in start:" + newUnixStartTime * 1000L);
    start = new DateTime(newUnixStartTime * 1000L);
    current = new DateTime(start);
    end = computeDatePlusInterval(start, spanType);

    // Cache
    segmentCount = computeHowMany15Mins();
  }

  /**
   * MUTATION: Updates the timeline in terms of starting unix, keeping the previous timeline length
   * @param newUnixStartTime in seconds
   */
  public void updateTime(long newUnixStartTime) {
    start = new DateTime(newUnixStartTime * 1000L);
    current = new DateTime(start);
    end = computeDatePlusInterval(start, spanType);

    // Cache
    segmentCount = computeHowMany15Mins();
  }

  /**
   * MUTATION: Updates the current index of the timeline
   * @param index
   */
  public void scrubTo(int index) {
    DateTime newCurrentTime = start.plusMinutes(index * 15);
//    System.out.println("Attempting to scrub to index = " + index + ";");
//    System.out.println("New Current Time = " + newCurrentTime.getMillis());
//    System.out.println("Start Time = " + start.getMillis());
    if (checkDateRange(newCurrentTime)) {
      throw new RuntimeException(index + " isn't something you can scrub to!");
    } else {
      current = newCurrentTime;
    }
  }

  /**
   * MUTATION: Move forward in time by one increment (depending on the IntervalType subSegmentLength);
   * if incremented on the ending index of this current timeline, then jump to next section.
   */
  void increment() {
    DateTime incrementedDateTime = current.plusMinutes(15);
    if (checkDateRange(incrementedDateTime)) {
      jumpToNextSection();
    } else {
      current = current.plusMinutes(15);
    }
  }

  /**
   * Move backward in time by one increment (depending on the IntervalType subSegmentLength);
   * if incremented on the starting index of this current timeline, jump to prev section.
   */
  void decrement() {
    DateTime decrementedDateTime = current.minusMinutes(15);
    if (checkDateRange(decrementedDateTime)) {
      jumpToPrevSection();
    } else {
      current = current.minusMinutes(15);
    }
  }

  /**
   * @return the start unix value of this timeline
   */
  public long getStartUnix() {
    return start.getMillis() / 1000L;
  }

  /**
   * @return the current unix value of this timeline
   */
  public long getCurUnix() {
    return current.getMillis() / 1000L;
  }

  /**
   * @return the end unix value of this timeline
   */
  public long getEndUnix() {
    return end.getMillis() / 1000L;
  }

  /**
   * @return the current index this timeline
   */
  public int getCurIdx() {
    // TODO possibly called intensively, might need caching current index
    return (int) ((current.getMillis() - start.getMillis()) / (1000 * 60 * 15));
  }

  /**
   * @return the last index this timeline, which is the last portion of time this timeline can scrub to.
   */
  public int getEndIdx() {
    return segmentCount - 1;
  }

  /**
   * @return The DateTime in which will take this interval to the next interval
   */
  public DateTime getJumpDateTime() {
    return end.minusMinutes(15);
  }


  /**
   * @param unix
   * @return the day of week of the given unix
   */
  public static int getDayOfWeek(long unix) {
    return new DateTime(unix * 1000L).getDayOfWeek();
  }

  /**
   * @param unix
   * @return the character of the day of week of the given unix
   */
  public static char getDayOfWeekLetter(long unix) {
    return getDayAsOneLetterStringFromNum(getDayOfWeek(unix));
  }

  /**
   * @param i
   * @return given the number representation of a day of week, return the appropriate DoW character
   */
  public static char getDayAsOneLetterStringFromNum(int i) {
    DateTime date = new DateTime(DateTimeZone.forID("America/New_York"));
    date = date.withDayOfWeek(i);
    // System.out.println(date.dayOfWeek().getAsText());
    return date.dayOfWeek().getAsText().charAt(0);
  }

  /**
   * Returns the HH:mm digital time of the given option
   * @param option - one of: START, CUR, END
   * @return the HH:mm digital time string of the given option
   */
  String getDigitalTime(int option) {
    return String.format("%02d", toDateTimeFor(option).getHourOfDay()) + ":" +
            String.format("%02d", toDateTimeFor(option).getMinuteOfHour());
  }

  /**
   * Compares two Unix timestamps if they are on the same hour.
   *
   * @param 	unix1 - first Unix timestamp
   * @param 	unix2 - second Unix timestamp
   * @return
   */
  public static int compareHourly(long unix1, long unix2) {
    DateTime d1 = new DateTime(unix1 * 1000L);
    DateTime d2 = new DateTime(unix2 * 1000L);

    int year1 = d1.getYear();
    int year2 = d2.getYear();
//    	System.out.println(year1 + " vs. " + year2);

    int mo1 = d1.getMonthOfYear();
    int mo2 = d2.getMonthOfYear();
//    	System.out.println(mo1 + " vs. " + mo2);

    int day1 = d1.getDayOfMonth();
    int day2 = d2.getDayOfMonth();
//    	System.out.println(day1 + " vs. " + day2);

    int hour1 = d1.getHourOfDay();
    int hour2 = d2.getHourOfDay();

    if (year1 < year2) {
      return -1;
    } else if (year1 > year2) {
      return 1;
    } else {
      if (mo1 < mo2) {
        return -1;
      } else if (mo1 > mo2) {
        return 1;
      } else {
        if (day1 < day2) {
          return -1;
        } else if (day1 > day2) {
          return 1;
        } else {
          if (hour1 < hour2) {
            return -1;
          } else if (hour1 > hour2) {
            return 1;
          } else {
            return 0;
          }
        }
      }
    }
  }

  /**
   * Compares two Unix timestamps if they are on the same day.
   *
   * @param 	unix1 - first Unix timestamp
   * @param 	unix2 - second Unix timestamp
   * @return
   */
  public static int compareDaily(long unix1, long unix2) {
    DateTime d1 = new DateTime(unix1 * 1000L);
    DateTime d2 = new DateTime(unix2 * 1000L);

    int year1 = d1.getYear();
    int year2 = d2.getYear();
//    	System.out.println(year1 + " vs. " + year2);

    int mo1 = d1.getMonthOfYear();
    int mo2 = d2.getMonthOfYear();
//    	System.out.println(mo1 + " vs. " + mo2);

    int day1 = d1.getDayOfMonth();
    int day2 = d2.getDayOfMonth();
//    	System.out.println(day1 + " vs. " + day2);

    if (year1 < year2) {
      return -1;
    } else if (year1 > year2) {
      return 1;
    } else {
      if (mo1 < mo2) {
        return -1;
      } else if (mo1 > mo2) {
        return 1;
      } else {
        if (day1 < day2) {
          return -1;
        } else if (day1 > day2) {
          return 1;
        } else {
          return 0;
        }
      }
    }

  }

  /**
   * Compares two Unix timestamps if they are on the same month.
   *
   * @param 	unix1 - first Unix timestamp
   * @param 	unix2 - second Unix timestamp
   * @return
   */
  public static int compareMonthly(long unix1, long unix2) {
    DateTime d1 = new DateTime(unix1 * 1000L);
    DateTime d2 = new DateTime(unix2 * 1000L);

    int year1 = d1.getYear();
    int year2 = d2.getYear();
//    	System.out.println(year1 + " vs. " + year2);

    int mo1 = d1.getMonthOfYear();
    int mo2 = d2.getMonthOfYear();
//    	System.out.println(mo1 + " vs. " + mo2);

    if (year1 < year2) {
      return -1;
    } else if (year1 > year2) {
      return 1;
    } else {
      if (mo1 < mo2) {
        return -1;
      } else if (mo1 > mo2) {
        return 1;
      } else {
        return 0;
      }
    }
  }

  /**
   * Compares two Unix timestamps if they are on the same year.
   *
   * @param 	unix1 - first Unix timestamp
   * @param 	unix2 - second Unix timestamp
   * @return
   */
  public static int compareYearly(long unix1, long unix2) {
    DateTime d1 = new DateTime(unix1 * 1000L);
    DateTime d2 = new DateTime(unix2 * 1000L);

    int year1 = d1.getYear();
    int year2 = d2.getYear();
//    	System.out.println(year1 + " vs. " + year2);

    if (year1 < year2) {
      return -1;
    } else if (year1 > year2) {
      return 1;
    } else {
      return 0;
    }

  }
  public final static SimpleDateFormat slashesFormater = new SimpleDateFormat("MM/dd/yy"); // Used by

  /**
   * Compares two Unix timestamps if they are on the same week.
   *
   * @param 	unix1 - first Unix timestamp
   * @param 	unix2 - second Unix timestamp
   * @return
   */
  public static int compareWeekly(long unix1, long unix2) {
    DateTime d1 = new DateTime(unix1 * 1000L);
    DateTime d2 = new DateTime(unix2 * 1000L);

    int year1 = d1.getYear();
    int year2 = d2.getYear();
//    	System.out.println(year1 + " vs. " + year2);

    int mo1 = d1.getMonthOfYear();
    int mo2 = d2.getMonthOfYear();
//    	System.out.println(mo1 + " vs. " + mo2);

    int w1 = d1.getWeekOfWeekyear(); // TODO test
    int w2 = d2.getWeekOfWeekyear(); // TODO test

    if (year1 < year2) {
      return -1;
    } else if (year1 > year2) {
      return 1;
    } else {
      if (mo1 < mo2) {
        return -1;
      } else if (mo1 > mo2) {
        return 1;
      } else {
        if (w1 < w2) {
          return -1;
        } else if (w1 > w2) {
          return 1;
        } else {
          return 0;
        }
      }
    }
  }

  /**
   * Compares two Unix timestamps if they are on the same 15 minut.
   *
   * @param 	unix1 - first Unix timestamp
   * @param 	unix2 - second Unix timestamp
   * @return
   */
  public static int compareQuarterly(long unix1, long unix2) {
    DateTime d1 = new DateTime(unix1 * 1000L);
    DateTime d2 = new DateTime(unix2 * 1000L);

    int year1 = d1.getYear();
    int year2 = d2.getYear();
//    	System.out.println(year1 + " vs. " + year2);

    int mo1 = d1.getMonthOfYear();
    int mo2 = d2.getMonthOfYear();
//    	System.out.println(mo1 + " vs. " + mo2);

    int day1 = d1.getDayOfMonth();
    int day2 = d2.getDayOfMonth();
//    	System.out.println(day1 + " vs. " + day2);

    int hour1 = d1.getHourOfDay();
    int hour2 = d2.getHourOfDay();

    int minute1 = d1.getMinuteOfHour();
    int minute2 = d2.getMinuteOfHour();

    if (year1 < year2) {
      return -1;
    } else if (year1 > year2) {
      return 1;
    } else {
      if (mo1 < mo2) {
        return -1;
      } else if (mo1 > mo2) {
        return 1;
      } else {
        if (day1 < day2) {
          return -1;
        } else if (day1 > day2) {
          return 1;
        } else {
          if (hour1 < hour2) {
            return -1;
          } else if (hour1 > hour2) {
            return 1;
          } else {
            if (Math.abs(minute2 - minute1) <= 15) {
              return 0;
            } else if (minute1 > minute2) {
              return 1;
            } else {
              return -1;
            }
          }
        }
      }
    }
  }

  /**
   * If the given DateTime in millis if under start or over or equal to end millis, return true.
   * @param d
   * @return
   */
  private boolean checkDateRange(DateTime d) {
    return d.getMillis() < start.getMillis() || end.getMillis() <= d.getMillis();
  }

  /**
   * Returns DateTime for the given option
   * @param option CUR, START, END
   * @return DateTime depending on option
   */
  public DateTime toDateTimeFor(int option) {
    switch(option) {
      case START:
        return new DateTime(start);
      case CUR:
        return new DateTime(current);
      case END:
        return new DateTime(end);
    }
    throw new RuntimeException("That is not an option for date!");
  }

  /**
   * Returns the Date object that the given index is referring to in this timeline
   * @param index
   * @return Date the given index is referring to in this timeline
   * @throws RuntimeException, if the given index is not within the timeline right now.
   */
  public DateTime getDateForIndex(int index) {
    DateTime testDate = start.plusMinutes(index * 15);
    if (checkDateRange(testDate)) {
      throw new RuntimeException(index + " is not within the current timerange!");
    }
    // System.out.println("ms = " + (unixStart + index * 15 * 60) * 1000L);
    return testDate;
  }

  /**
   * Jumps to the next section of time
   */
  public void jumpToNextSection() {
    updateTime(getEndUnix());
  }

  /**
   * Jumps to the beginning of the previous section (defined by spanType) of time
   */
  public void jumpToPrevSection() {
    DateTime d = toDateTimeFor(CUR);

    long unixNextStart;

    switch (spanType) {
      case YEAR:
        d = d
                .withMillisOfSecond(0)
                .withSecondOfMinute(0)
                .withMinuteOfHour(0)
                .withHourOfDay(0)
                .withDayOfMonth(1)
                .withMonthOfYear(1);
        unixNextStart = d.minusYears(1).getMillis() / 1000;
        break;
      case MONTH:
        d = d
                .withMillisOfSecond(0)
                .withSecondOfMinute(0)
                .withMinuteOfHour(0)
                .withHourOfDay(0)
                .withDayOfMonth(1);
        unixNextStart = d.minusMonths(1).getMillis() / 1000;
        break;
      case WEEK:
        d = d
                .withMillisOfSecond(0)
                .withSecondOfMinute(0)
                .withMinuteOfHour(0)
                .withHourOfDay(0);
        unixNextStart = d.minusWeeks(1).getMillis() / 1000;
        break;
      case DAY:
        d = d
                .withMillisOfSecond(0)
                .withSecondOfMinute(0)
                .withMinuteOfHour(0)
                .withHourOfDay(0);
        unixNextStart = d.minusDays(1).getMillis() / 1000;
        break;
      case HOUR:
        d = d
                .withMillisOfSecond(0)
                .withSecondOfMinute(0)
                .withMinuteOfHour(0);
        unixNextStart = d.minusHours(1).getMillis() / 1000;
        break;
      default:
        throw new RuntimeException(spanType.name() + " cannot be recognized!");
    }
    updateTime(unixNextStart);
  }

  /**
   * @return the timeline's interval type
   */
  public IntervalType getIntervalType() {
    return spanType;
  }
}
