package mvc.model.datasource;

import java.util.List;
import java.util.Optional;

import processing.data.Table;
import processing.data.TableRow;
import mvc.model.time.IntervalType;
import mvc.model.time.TimeRangeUtil;

/**
 * Represents a indexed data source that holds all data
 * @param <T> the data source type
 */
public abstract class DataSource<T> {

  protected Table dataTable;

  protected long parentUnixStart, parentUnixEnd;
  protected long entireMinUnix, entireMaxUnix;

  protected boolean isIncreasing, isHugging;

  protected Optional<T> maxValue, minValue;
  protected Optional<Integer> idxAtLocalMax, idxAtLocalMin;
  protected Optional<T> localMax, localMin;

  protected Integer startRowIndex = null;
  protected Integer endRowIndex = null;

  protected IntervalType segmentLength;
  protected IntervalType parentLength;

  protected String valueColName;
  protected String unixColumnName;

  protected List<Optional<T>> indexedValues; // use this for unhugged ranges only!

  /**
   * This is for wholesome data with entire ranges covered ONLY, with no missing data!!
   * Will break if bad data is given.
   * @param sourcedata
   * @param mainUnixStart
   * @param mainUnixEnd
   */
  public DataSource(Table sourcedata,
                    long mainUnixStart, long mainUnixEnd,
                    IntervalType segmentLength, IntervalType parentLength,
                    String valueColName, String idxColName) {

    this.segmentLength = segmentLength;
    this.parentLength = parentLength;
    this.valueColName = valueColName;
    this.unixColumnName = idxColName;

    this.dataTable = sourcedata;

    this.parentUnixStart = mainUnixStart;
    this.parentUnixEnd = mainUnixEnd;

    this.entireMinUnix = findUnixStart();
    this.entireMaxUnix = findUnixEnd();

    this.isIncreasing = this.isChronoOrder();
  }

  public String getValueColName() {
    return valueColName;
  }

  public String getUnixColumnName() {
    return unixColumnName;
  }

  public Table getDataTable() {
    return dataTable;
  }

  public long getParentUnixStart() {
    return parentUnixStart;
  }

  public long getParentUnixEnd() {
    return parentUnixEnd;
  }

  public IntervalType getSegmentLength() {
    return segmentLength;
  }

  public IntervalType getParentLength() {
    return parentLength;
  }


  // Finds the lowest unix time of the source (which is sorted in order; lowest of first or last row assumed to be the start)
  private long findUnixStart() {
    TableRow firstRow = dataTable.getRow(0);
    TableRow lastRow = dataTable.getRow(dataTable.getRowCount() - 1);

    if (firstRow.getLong(unixColumnName) <= lastRow.getLong(unixColumnName)) {
      return firstRow.getLong(unixColumnName);
    } else {
      return lastRow.getLong(unixColumnName);
    }
  }

  // Finds the highest unix time of the source (which is sorted in order; highest of first or last row assumed to be the end);
  private long findUnixEnd() {
    TableRow firstRow = dataTable.getRow(0);
    TableRow lastRow = dataTable.getRow(dataTable.getRowCount() - 1);

    if (firstRow.getLong(unixColumnName) <= lastRow.getLong(unixColumnName)) {
      return lastRow.getLong(unixColumnName);
    } else {
      return firstRow.getLong(unixColumnName);
    }
  }

  // Determines whether the data is in time order; from 0 to last row increasing (assumes data is sorted)
  boolean isChronoOrder() {
    TableRow firstRow = dataTable.getRow(0);
    TableRow lastRow = dataTable.getRow(dataTable.getRowCount() - 1);

    return firstRow.getLong(unixColumnName) <= lastRow.getLong(unixColumnName);
  }

  // Determines whether the start unix of this data <= the start unix of the parent
  boolean isStartUnixValid() {
    return this.compareUnixValuesBasedOnCurrentInterval(this.entireMinUnix, this.parentUnixStart) <= 0;
  }

  // Determines whether the end unix of this data >= the end unix of the parent
  boolean isEndUnixValid() {
    return this.compareUnixValuesBasedOnCurrentInterval(this.entireMaxUnix, this.parentUnixEnd) >= 0;
  }

  public Optional<T> getLocalMax() {
    return localMax;
  }
  public Optional<T> getLocalMin() {
    return localMin;
  }
  public Optional<T> getMaxVal() {
    return maxValue;
  }
  public Optional<T> getMinVal() {
    return minValue;
  }
  public Optional<Integer> getIndexOfLocalMax() {
    return idxAtLocalMax;
  }
  public Optional<Integer> getIndexOfLocalMin() {
    return idxAtLocalMax;
  }

  public abstract void updateLocalMax();

  public abstract void updateLocalMin();

  // if the given time is OOB of this source, then throw RuntimeExcepton (this is assuming increasing time!)
  public abstract Optional<Integer> requestIndexAtUnix(long requestedTime, int startIndex);

  public abstract int requestIndexWithUnix(long requestedTime) throws RuntimeException;

  /**
   * Returns the value at the requested UNIX time
   * @param requestedTime
   * @return
   */
  public abstract Optional<T> requestValueAtUnix(long requestedTime);

  /**
   * Requests a value based on the column name @ the given index.
   * @param index any positive number
   */
  public abstract Optional<T> requestValue_TableIndex(int index);

  // Updates:
  // 	 If the source is hugging the new timerange, make efficient adjustments, and index
  //   else: index normally
  public abstract void updateStats(long updatedStart, long updatedEnd, IntervalType newSegmentLength, IntervalType parentLength);

  public abstract List<Optional<T>> getIndexedArray();

  /**
   * Return an Optional<Long> using the given row and column name
   * TODO invalid rowNumber/columnName/type check
   * @param rowNumber the row index of the value to retrieve
   * @param columnName the column name of the value to retrieve
   */
  protected Optional<Long> longAtRC(int rowNumber, String columnName) {
    Long value = dataTable.getRow(rowNumber).getLong(columnName);
    if (value == null) {
      return Optional.empty();
    }
    return Optional.of(value);
  }

  /**
   * Return an Optional<Integer> using the given row and column name
   * TODO invalid rowNumber/columnName/type check
   * @param rowNumber the row index of the value to retrieve
   * @param columnName the column name of the value to retrieve
   */
  protected Optional<Integer> intAtRC(int rowNumber, String columnName) {
    Integer value = dataTable.getRow(rowNumber).getInt(columnName);
    if (value == null) {
      return Optional.empty();
    }
    return Optional.of(value);
  }

  /**
   * Return an Optional<String> using the given row and column name
   * TODO invalid rowNumber/columnName/type check
   * @param rowNumber the row index of the value to retrieve
   * @param columnName the column name of the value to retrieve
   */
  protected Optional<String> stringAtRC(int rowNumber, String columnName) {
    String value = dataTable.getRow(rowNumber).getString(columnName);
    if (value == null) {
      return Optional.empty();
    }
    return Optional.of(value);
  }

  /**
   * Return an Optional<Float> using the given row and column name
   * TODO invalid rowNumber/columnName/type check
   * @param rowNumber the row index of the value to retrieve
   * @param columnName the column name of the value to retrieve
   */
  protected Optional<Float> floatAtRC(int rowNumber, String columnName) {
    Float value = dataTable.getRow(rowNumber).getFloat(columnName);
    if (value == null) {
      return Optional.empty();
    }
    return Optional.of(value);
  }

  /**
   * Visits the relevant DataSource<T>, calls the type-appropriate method to get T
   * with the {@code valColName}, then outputs Optional<T>
   * @param row
   * @return
   */
  protected abstract Optional<T> getValueFromRow(TableRow row);

  public Optional<T> requestValue_BoundedIndex(int index) {
    return getIndexedArray().get(index);
  }

  protected int compareUnixValuesBasedOnCurrentInterval(long unix1, long unix2) {
    switch (this.segmentLength) {
      case QUARTER:
        return TimeRangeUtil.compareQuarterly(unix1, unix2);
      case HOUR:
        return TimeRangeUtil.compareHourly(unix1, unix2);
      case DAY:
        return TimeRangeUtil.compareDaily(unix1, unix2);
      case WEEK:
        return TimeRangeUtil.compareWeekly(unix1, unix2);
      case MONTH:
        return TimeRangeUtil.compareMonthly(unix1, unix2);
      case YEAR:
        return TimeRangeUtil.compareYearly(unix1, unix2);
      default:
        throw new RuntimeException("Bad interval enum given!!");
    }
  }

  protected void setSegmentLength(IntervalType newSegmentLength) {
    this.segmentLength = newSegmentLength;
  }

  protected void println(String s) {
    System.out.println(s);
  }

  protected void print(String s) {
    System.out.print(s);
  }
}
