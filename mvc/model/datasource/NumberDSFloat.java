package mvc.model.datasource;

import java.util.Optional;

import processing.data.Table;
import processing.data.TableRow;
import mvc.model.time.IntervalType;

public class NumberDSFloat extends NumberDS<Float> {

  @Override
  public Optional<Float> getRange() {
    if (getMaxVal().isPresent() && getMinVal().isPresent()) {
      return Optional.of(getMaxVal().get() - getMinVal().get());
    }
    return Optional.empty();
  }

  @Override
  public Optional<Float> diffFromMin(Float target) {
    if (getMinVal().isPresent()) {
      return Optional.of(target - getMinVal().get());
    }
    return Optional.empty();
  }

  public NumberDSFloat(Table sourcedata, long mainUnixStart,
                       long mainUnixEnd, IntervalType segmentLength,
                       IntervalType parentLength, String desiredColumn,
                       String unixColumnName, StringBuilder loadingText) {
    super(sourcedata, mainUnixStart, mainUnixEnd, segmentLength, parentLength,
            desiredColumn, unixColumnName);

    loadingText.setLength(0);
    loadingText.append("Indexing " + desiredColumn + "...");
  }

  @Override
  public Optional<Float> getValueFromRow(TableRow row) {
    Float value = row.getFloat(valueColName);
    if (value == null || value.isNaN()) {
      return Optional.empty();
    }
    return Optional.of(value);
  }

  @Override
  public void printStatus(long startTime, long updatedStart, long updatedEnd, IntervalType parentLength) {
    println("Indexing took: " + (System.nanoTime() - startTime) + " ns!");
    println("=== DataSource (" + valueColName + ") =====================");
    println("Main timerange: " + parentUnixStart + " - " + parentUnixEnd);
    println("Source timerange (total)  : " + entireMinUnix + " - " + entireMaxUnix);
    if (isHugging)
      println("Source timerange (current): "
              + longAtRC(startRowIndex, unixColumnName) + " ["+ requestIndexWithUnix(updatedStart) +"] - "
              + longAtRC(endRowIndex, unixColumnName) + " ["+ requestIndexWithUnix(updatedEnd) + "]");
    println("Segment Interval: " + segmentLength.name());
    println("Entire Interval: " + parentLength);
    println("Min = " + this.minValue);
    println("Max = " + this.maxValue);
    println("");
  }

  @Override
  public Optional<Float> requestValue_TableIndex(int index) {
    Float result = dataTable.getRow(index).getFloat(valueColName);
    if (result == null || Float.isNaN(result)) {
      return Optional.empty();
    }
    return Optional.of(result);
  }

  // BAD! DON'T USE
  @Override
  public int requestIndexWithUnix(long requestedTime) throws RuntimeException {
    if (requestedTime < entireMinUnix || requestedTime > entireMaxUnix) {
      throw new RuntimeException("Requested time: " + requestedTime + " is OOB!");
    }
    // Search for such a time, comparingUnixValuesBasedOnCurrentInterval (this is assuming increasing time!)
    int tick = 0;
    for (TableRow row : dataTable.rows()) {
      long result = row.getLong(unixColumnName) - requestedTime;
      if (result < 0) {
        // Keep going
      } else if (result == 0) {
        // Found
        return tick;
      } else {
        // Not found
        throw new RuntimeException("Requested time: " + requestedTime + " was not found (Early exit)!");
      }
      ++tick;
    }
    throw new RuntimeException("Requested time: " + requestedTime + " was not found (Finished entire loop)!");
  }
}
