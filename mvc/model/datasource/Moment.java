package mvc.model.datasource;

/**
 * Container for:
 * value @ moment
 * time @ moment
 * Sensor ID
 */
public class Moment<T extends Number> {

  private long momentTime;
  private T momentValue;
  private String sensorID;

  public Moment(long momentTime, T momentValue, String sensorID) {
    this.momentTime = momentTime;
    this.momentValue = momentValue;
    this.sensorID = sensorID;
  }

  public Moment(T v) {
    // TODO a bit dangerous to initialize with dummy value.
    this.momentValue = v;
  }

  /**
   * @return the peaktime in unix
   */
  public long getTimeAtMoment() {
    return momentTime;
  }

  /**
   * @return the peak energy value
   */
  public T getValueAtMoment() {
    return momentValue;
  }

  /**
   * @return the relevant sensor id
   */
  public String getSensorID() {
    return sensorID;
  }

}