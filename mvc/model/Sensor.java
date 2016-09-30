package mvc.model;

/**
 * Container class that saves:
 * - sensor ID
 * - sensor name
 * - sensor abbreviation
 * - sensor associated building ID
 */
public class Sensor {
  private final int SENSOR_ID;
  private final String NAME;
  private final String ABBREV;
  private final int ASSOCIATED_BUILDING_ID;

  public Sensor(int sID, String sensorName, String sensorAbbrev, int bID) {
    SENSOR_ID = sID;
    NAME = sensorName;
    ABBREV = sensorAbbrev;
    ASSOCIATED_BUILDING_ID = bID;
  }

  /**
   * @return the sensor ID
   */
  public int getSID() {
    return SENSOR_ID;
  }

  /**
   * @return the sensor name
   */
  public String getName() {
    return NAME;
  }

  /**
   * @return the sensor abbrev
   */
  public String getAbbrev() {
    return ABBREV;
  }

  /**
   * @return the associated building ID
   */
  public int getBID() {
    return ASSOCIATED_BUILDING_ID;
  }
}
