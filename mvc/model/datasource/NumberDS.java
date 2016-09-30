package mvc.model.datasource;

import mvc.model.time.IntervalType;
import mvc.model.time.TimeRangeUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import processing.data.Table;
import processing.data.TableRow;

/**
 * Represents a numerically valued data source
 * Only numerical data can have: min, max, range and so on.
 * @param <N> a numerical, comparable type
 */
public abstract class NumberDS<N extends Number & Comparable<? super N>> extends DataSource<N> {

  /**
   * Numerical data only:
   * @return
   */
  public abstract Optional<N> getRange();

  /**
   * Numerical data only
   * @param target
   * @return
   */
  public abstract Optional<N> diffFromMin(N target);

  public NumberDS(Table sourcedata, long mainUnixStart,
                  long mainUnixEnd, IntervalType segmentLength,
                  IntervalType parentLength, String desiredColumn,
                  String unixColumnName) {
    super(sourcedata, mainUnixStart, mainUnixEnd, segmentLength, parentLength,
            desiredColumn, unixColumnName);

    // Find Max and min values
    Optional<N> curMax = Optional.empty();
    Optional<N> curMin = Optional.empty();
    for (TableRow row : dataTable.rows()) {
      Optional<N> i = getValueFromRow(row);

      if (!i.isPresent()) {
        // DO NOTHING, no value at row
      } else {
        // If curMax is not present OR (isPresent AND bigger) -> replace with maybeMax
        if (!curMax.isPresent() || (curMax.isPresent() && i.get().compareTo(curMax.get()) > 0)) {
          curMax = i;
        }

        if (!curMin.isPresent() || (curMin.isPresent() && i.get().compareTo(curMin.get()) < 0)) {
          curMin = i;
        }
      }
    }
    maxValue = curMax;
    minValue = curMin;
    // End

    updateStats(mainUnixStart, mainUnixEnd, segmentLength, parentLength);
  }

  @Override
  public void updateLocalMax() {
    Optional<N> max = Optional.empty();
    Optional<Integer> index = Optional.empty();

    for(int i = 0; i < getIndexedArray().size(); ++i) {
      Optional<N> curVal = getIndexedArray().get(i);
      if (curVal.isPresent() && max.isPresent()) {
        if (curVal.get().compareTo(max.get()) > 0){
          max = Optional.of(curVal.get());
          index = Optional.of(i);
        }
      } else if (curVal.isPresent()) {
        max = Optional.of(curVal.get());
        index = Optional.of(i);
      }
    }
    this.idxAtLocalMax = index;
    this.localMax = max;
  }

  @Override
  public void updateLocalMin() {
    Optional<N> min = Optional.empty();
    Optional<Integer> index = Optional.empty();

    for (int i = 0; i < getIndexedArray().size(); ++i) {
      Optional<N> curVal = getIndexedArray().get(i);
      if (curVal.isPresent() && min.isPresent()) {
        if (curVal.get().compareTo(min.get()) < 0){
          min = Optional.of(curVal.get());
          index = Optional.of(i);
        }
      } else if (curVal.isPresent()) {
        min = Optional.of(curVal.get());
        index = Optional.of(i);
      }
    }

    this.idxAtLocalMin = index;
    this.localMin = min;
  }

  @Override
  // Updates:
  // 	 If the source is hugging the new timerange, make efficient adjustments, and index
  //   else: index normally
  public void updateStats(long updatedStart, long updatedEnd, IntervalType newSegmentLength, IntervalType parentLength) {

//    println("Updating " + this.valueColName + "...");

    long startTime = System.nanoTime();
    setSegmentLength(newSegmentLength);

    this.parentUnixStart = updatedStart;
    this.parentUnixEnd = updatedEnd;

    // Case 1: The min && max of the data source is "hugging" the new range.

    if (isStartUnixValid() && isEndUnixValid()) {
      isHugging = true;
//      println(valueColName +  " is hugging");
      // 1. Update start and end indexes
      Integer startIndex = requestIndexWithUnix(updatedStart);
      Integer endIndex = requestIndexWithUnix(updatedEnd);

      // 2. Index
      int size = endIndex - startIndex;
//      println(""+size);
      indexedValues = new ArrayList<>(size);
      for (int i = startIndex; i < endIndex; ++i) {
        indexedValues.add(requestValue_TableIndex(i));
      }
//      System.out.println(indexedValues);

      this.startRowIndex = startIndex;
      this.endRowIndex = endIndex;
    }
    // Case 2: Not hugging
    else {
      isHugging = false;
//      println(valueColName +  " is NOT hugging");
      DateTime startDate = new DateTime(updatedStart * 1000L, DateTimeZone.forID("America/New_York"));
      int numPositions = TimeRangeUtil.computeHowMany15Mins(startDate, parentLength);

//      println("Source timerange (total)  : " + this.entireMinUnix + " - " + this.entireMaxUnix);
//      println(""+numPositions);


      indexedValues = new ArrayList<>(numPositions);
      for (int i = 0; i < numPositions; ++i) {
        indexedValues.add(Optional.empty());
      }

      long unixInterval = segmentLength.getSeconds(startDate);

      Optional<Integer> foundIndex = Optional.empty();
      for (int i = 0; i < numPositions; ++i) {
        if (foundIndex.isPresent()) {
          foundIndex = requestIndexAtUnix(updatedStart + i * unixInterval, foundIndex.get());
        } else {
          foundIndex = requestIndexAtUnix(updatedStart + i * unixInterval, 0);
        }
        if (foundIndex.isPresent()) {
          indexedValues.set(i, getValueFromRow(dataTable.getRow(foundIndex.get())));
        }
      }
    }

    updateLocalMin();
    updateLocalMax();
  }

  public abstract void printStatus(long startTime, long updatedStart, long updatedEnd, IntervalType parentLength);

  @Override
  public Optional<Integer> requestIndexAtUnix(long requestedTime, int startIndex) {
    if (requestedTime < entireMinUnix || requestedTime > entireMaxUnix) {
      return Optional.empty();
    }

    // Search for such a time, comparingUnixValuesBasedOnCurrentInterval (this is assuming increasing time!)
    for (int i = startIndex; i < dataTable.getRowCount(); ++i) {
      TableRow row = dataTable.getRow(i);
      int result = compareUnixValuesBasedOnCurrentInterval(row.getLong(unixColumnName), requestedTime);
      if (result < 0) {
        // Keep going, but add to i to jump ahead (assuming in regular intervals, and assuming increasing time)
        float fifteenMinGaps = (requestedTime - row.getLong(unixColumnName)) / 60.0f / 15.0f;
        i += fifteenMinGaps - 1;
      } else if (result == 0) {
        // Found
        return Optional.of(i);
      } else {
        // Not found
        return Optional.empty();
      }
    }
    return Optional.empty();
  }

  /**
   * Visits the relevant implementation of the DS for type N, then returns the value with
   * {@code valColName} and given row index.
   * @param index any positive number
   * @return
   */
  @Override
  public abstract Optional<N> requestValue_TableIndex(int index);

  @Override
  public Optional<N> requestValueAtUnix(long requestedTime) {
    Optional<Integer> maybeIndex = requestIndexAtUnix(requestedTime, 0);
    if (maybeIndex.isPresent()) {
      return requestValue_TableIndex(maybeIndex.get());
    }
    return Optional.empty();
  }

  @Override
  public List<Optional<N>> getIndexedArray() {
    return indexedValues;
  }

  /**
   * Returns the Moment object representing the local max
   * @return the Optional<Moment<N>> of the local max within the indexed list.
   */
  public Optional<Moment<N>> getLocalPeakValue() {
    if (getIndexOfLocalMax().isPresent() && getLocalMax().isPresent()) {
      return Optional.of(new Moment<>(
              longAtRC(startRowIndex + getIndexOfLocalMax().get(), unixColumnName).get(),
              getLocalMax().get(),
              valueColName));
    }
    return Optional.empty();
  }

  /**
   * Returns the Moment object representing the local min
   * @return the Optional<Moment<N>> of the local min within the indexed list.
   */
  public Optional<Moment<N>> getLocalBaseValue() {
    if (getIndexOfLocalMin().isPresent() && getLocalMin().isPresent()) {
      return Optional.of(new Moment<>(
              longAtRC(startRowIndex + getIndexOfLocalMin().get(), unixColumnName).get(),
              getLocalMin().get(),
              valueColName));
    }
    return Optional.empty();
  }
}
