package mvc.model;

import mvc.controller.CityOfPeaks_Controller;
import mvc.model.datasource.*;
import processing.core.PApplet;
import processing.data.Table;
import processing.data.TableRow;
import mvc.model.time.IntervalType;
import mvc.model.time.TimeRangeUtil;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contains model information of the entire program.
 */
public class CityOfPeaks_Model implements IEnergyVizModel {

  // Native WH
  private Dimension nativeWH;
  private CityOfPeaks_Controller controller;

  // Data
  private Building[] listOfBuildings;
  private List<NumberDS<Float>> listOfSensorData;
  private NumberDS<Float> sumEnergyData;

  // ONE TO MANY
  private Map<Integer, List<Sensor>> buildingToSensorsMap = new HashMap<>();

  // ONE TO ONE
  private List<BuildingToEnergy> listOfEnergyReadingsByBuilding = new ArrayList<>();

  public List<NumberDS<Float>> getListOfSensorData() {
    return listOfSensorData;
  }

  public Map<Integer, List<Sensor>> getBuildingToSensorMap() {
    return buildingToSensorsMap;
  }

  public List<BuildingToEnergy> getListOfEnergyReadingsByBuilding() {
    return listOfEnergyReadingsByBuilding;
  }

  // TIME COMPONENTS
//  TimeRangeUtil dailyTimeline = new TimeRangeUtil(TimeRangeUtil.getUnixInEastern(2013, 10, 10, 11), IntervalType.DAY);
  TimeRangeUtil dailyTimeline = new TimeRangeUtil(TimeRangeUtil.getUnixInEastern(2013, 10, 1, 0), IntervalType.DAY);

  protected StringBuilder loadingText = new StringBuilder();

  /**
   * Loads (in another thread):
   * 1. ANALYSIS COMPONENTS
   * 2. GRAPHICAL COMPONENTS
   */
  public void loadEverything(Dimension nativeWH, PApplet loader) {

    this.nativeWH = nativeWH;

    Table sensorProperties = loader.loadTable("sensorProperties.csv", "header");
    Table energyData = loader.loadTable("measureDB_parallel_div4_with_sums.csv", "header");

    // Initializes all collected sensor data
    listOfSensorData = new ArrayList<>();
    for (int siteNum = 0; siteNum < sensorProperties.getRowCount(); ++siteNum) {
      listOfSensorData.add(
              new NumberDSFloat(
                      energyData,
                      dailyTimeline.getStartUnix(), dailyTimeline.getEndUnix(),
                      IntervalType.QUARTER, dailyTimeline.getIntervalType(),
                      Integer.toString(siteNum), "timestamp", loadingText));
      loadingText.setLength(0);
      loadingText
              .append("Indexing ")
              .append(sensorProperties.getRow(siteNum).getString("Name"))
              .append("...");
    }

    sumEnergyData = new NumberDSFloat(
            energyData,
            dailyTimeline.getStartUnix(), dailyTimeline.getEndUnix(),
            IntervalType.QUARTER, dailyTimeline.getIntervalType(),
            "all", "timestamp", loadingText);



    // Connects building to related sensors
    initSensorMapping(sensorProperties);

    loadingText.setLength(0);
    loadingText.append("Done mapping sensors!");

    // Connects building to one dataline
    initDataMapping();

    loadingText.setLength(0);
    loadingText.append("Done data mapping!");

    //
    Table categoryColors = loader.loadTable("buildingTypeRename.csv", "header");
    listOfBuildings = initBuildings(loader.loadTable("buildingProperties.csv", "header"), categoryColors, loader);

    loadingText.setLength(0);
    loadingText.append("Done mapping initializing buildings!");

    // The very first analysis of all peak values.
    calcStatisticsByBuilding(true); // true stands for: initialization

    loadingText.setLength(0);
    loadingText.append("Done calculating statistics!");
  }

  // init: Sensor mapping (bID, List<sID>) pairs
  private void initSensorMapping(Table sensorProperties) {
    Objects.requireNonNull(sensorProperties);
    for (TableRow row : sensorProperties.rows()) {
      Sensor sensor = new Sensor(
              row.getInt("sID"),
              row.getString("Name"),
              row.getString("Abbreviation"),
              row.getInt("bID"));
      if (buildingToSensorsMap.get(sensor.getBID()) == null) {
        List<Sensor> sensorList = new ArrayList<>();
        sensorList.add(sensor);
        buildingToSensorsMap.put(sensor.getBID(), sensorList);
      } else {
        buildingToSensorsMap.get(sensor.getBID()).add(sensor);
      }
    }
  }

  // init: Data mapping (bID, data) pairs
  private void initDataMapping() {
//    listOfEnergyReadingsByBuilding = buildingToSensorsMap.entrySet()
//            .stream()
//            .map((pair) -> new BuildingToEnergy(pair.getKey(), pair.getValue()
//                                                              .stream()
//                                                              .map((sensor) -> (listOfSensorData.get(sensor.getSID())))
//                                                              .reduce(null, (a, b) -> (combineData(a, b)))))
//            .collect(Collectors.toList());
    listOfEnergyReadingsByBuilding = buildingToSensorsMap.entrySet()
            .stream()
            .map((pair) -> new BuildingToEnergy(pair.getKey(), pair.getValue()
                    .stream()
                    .map((sensor) -> (listOfSensorData.get(sensor.getSID())))
                    .findFirst()))
            .collect(Collectors.toList());
  }

  public NumberDS<Float> combineData(NumberDS<Float> a, NumberDS<Float> b) {


    if (a == null) {
      if (b == null) {
        return null;
      } else {
        return b;
      }
    } else {
      if (b == null) {
        return a;
      } else {
        // DO NOTHING
      }
    }

    System.out.println("Combining table: " + a.getValueColName() + " with " + b.getValueColName());

    Table combinedTable = new Table();
    String newValueName = a.getValueColName() + " + " + b.getValueColName();
    combinedTable.addColumn(a.getUnixColumnName(), Table.LONG);
    combinedTable.addColumn(newValueName, Table.FLOAT);

    Table a_dataTable = a.getDataTable();
    Table b_dataTable = b.getDataTable();

    for (int i = 0; i < a_dataTable.getRowCount(); ++i) {
      TableRow aRow = a_dataTable.getRow(i);
      TableRow bRow = b_dataTable.getRow(i);
      TableRow newRow = combinedTable.addRow();
      newRow.setLong(a.getUnixColumnName(), aRow.getLong(a.getUnixColumnName()));
      newRow.setFloat(newValueName, aRow.getFloat(a.getValueColName()) + bRow.getFloat(b.getValueColName()));
      combinedTable.addRow();
    }

    return new NumberDSFloat(combinedTable, a.getParentUnixStart(), a.getParentUnixEnd(), a.getSegmentLength(),
            a.getParentLength(), newValueName, a.getUnixColumnName(), loadingText);
  }

  public class BuildingToEnergy {
    private int bID;
    private Optional<NumberDS<Float>> dataSource;

    public BuildingToEnergy(int bID, Optional<NumberDS<Float>> dataSource) {
      this.bID = Objects.requireNonNull(bID);
      this.dataSource = dataSource;
    }

    public int getbID() {
      return bID;
    }

    public Optional<NumberDS<Float>> getDataSource() {
      return dataSource;
    }
  }

  public NumberDS<Float> getSumEnergyData() {
    return sumEnergyData;
  }

  @Override
  public Building[] getListOfBuildings() {
    return listOfBuildings;
  }

  // init: Building List, BuildingOutlineMap
  private Building[] initBuildings(Table buildingData, Table categoryColors, PApplet parentView) {
    Building[] listOfBuildings = new Building[buildingData.getRowCount()];

    TableRow originRow = buildingData.getRow(12);
    Point2D.Double originLatLong =
            StringParseUtil.parseCentroidString(originRow.getString("Centroid"));

    for (int bIndex = 0; bIndex < listOfBuildings.length; ++bIndex) {
      TableRow row = buildingData.getRow(bIndex);
      listOfBuildings[bIndex] = new Building(
              row.getInt("bID"), // ID
              row.getString("shortName"), // Short name
              row.getString("Primary Use"), // USE
              row.getInt("Floors"), // Floors
              row.getString("Name"), // Name
              categoryColors,  // Color mapping

              // Parse -> centroid
              GeoUtil.convertGeoToScreen(
                      StringParseUtil.parseCentroidString(row.getString("Centroid")),
                      originLatLong,
                      nativeWH.width,
                      nativeWH.height),

              // Parse a String (set of sets) -> a list of String/Points (list of points)s
              StringParseUtil.getListFromSet(row.getString("Outline")),
              originLatLong,
              nativeWH,
              buildingToSensorsMap.get(bIndex).toArray(new Sensor[buildingToSensorsMap.get(bIndex).size()]));

      listOfBuildings[bIndex].initColorAndOutline(parentView);
    }
    return listOfBuildings;
  }

  boolean isTimeProgressing = true;
  /**
   * Plays / Pauses the time and animation.
   */
  public void togglePlayPause() {
    isTimeProgressing = !isTimeProgressing;
  }

  public boolean isTimeProgressing() {
    return isTimeProgressing;
  }

  @Override
  public TimeRangeUtil getTimeline() {
    return dailyTimeline;
  }

  /**
   * Returns the current energy value of the given sensor ID
   * @param sensorID the given sensor ID
   * @return the current energy value of the given sensor ID
   */
  public Optional<Float> getCurEnergyLevel(int sensorID) {
    return listOfSensorData.get(sensorID).requestValue_BoundedIndex(dailyTimeline.getCurIdx());
  }

  private float dayPercentage = 0f;
  public float getDayPercentage() {
    return dayPercentage;
  }

  public void decrementTimeAcceleration() {
    tAcc -= 0.00002f;

    if (tAcc < tAccLowerLimit) {
      tAcc = tAccLowerLimit;
    }
  }

  public void incrementTimeAcceleration() {
    tAcc += 0.00002f;
    if (tAcc > tAccUpperLimit) {
      tAcc = tAccUpperLimit;
    }
  }

  private float tAcc = 0f;
  private float tAccUpperLimit = 0.0001f;
  private float tAccLowerLimit = -0.0001f;

  private float tVel = 0.0005f;
  private float tVelUpperLimit = 0.002f;
  private float tVelLowerLimit = 0.0005f;

  // increment day percentage
  public void incrementDayPercentage() {

    // Accelerate
    tVel += tAcc;

    // Limit speed
    if (tVel > tVelUpperLimit) {
      tVel = tVelUpperLimit;
    } else if (tVel < tVelLowerLimit) {
      tVel = tVelLowerLimit;
    }

    float increment = tVel;

    if (dayPercentage + increment <= 1) {
      dayPercentage += increment;
    } else if (dayPercentage + increment >= 1) {
      dayPercentage = 1;
    }

    if (dailyTimeline.getJumpDateTime().getMillis() <= dailyTimeline.toDateTimeFor(TimeRangeUtil.CUR).getMillis()) {
      jumpToNext();
      dayPercentage = 0;
    } else {
      // derive index from percentage
      int index = (int) Math.floor((dailyTimeline.getEndIdx() + 1) * dayPercentage);
      if (index >= dailyTimeline.getEndIdx() + 1) {
        index = dailyTimeline.getEndIdx();
      }
      dailyTimeline.scrubTo(index);
    }
  }

  public void setTimePercentage(float newDayPercentage) {
    if (newDayPercentage < 0f || newDayPercentage > 1f) {
      throw new IllegalArgumentException("Setting time percentage to a value outside of 0 to 1: " + newDayPercentage);
    }



    dayPercentage = newDayPercentage;

    if (newDayPercentage == 1) {
      dayPercentage -= 0.0000001f;
    }

    // derive index from percentage
    int index = (int) Math.floor((dailyTimeline.getEndIdx() + 1) * dayPercentage);
    dailyTimeline.scrubTo(index);
  }

  public int getNumberOfBuildings() {
    return listOfBuildings.length;
  }


  /**
   * Represents all statistics of a building, which all of its sensors considered.
   * Role: caches the results of combining data from many sensors in a building
   */
  public class BuildingStat {

    private int bID;
    private Optional<Moment<Float>> peakValue = Optional.empty();
    private Optional<Moment<Float>> baseValue = Optional.empty();
    private Optional<Float> range;

    public BuildingStat(int bID, Optional<Moment<Float>> peak, Optional<Moment<Float>> base) {
      this.bID = bID;
      refresh(peak, base);
    }

    /**
     * Precondition: pairs of peak/base elements correspond to one sensor, and therefore must have equal length
     * @param peak
     * @param base
     */
//    public void refresh(List<Optional<Moment<Float>>> peaks, List<Optional<Moment<Float>>> bases) {
//
//      if (peaks.size() != bases.size()) {
//        throw new RuntimeException(
//                "Given peaks (" + peaks.size() + ") and bases (" + bases.size() + ") have different sizes! " +
//                        "Not possible in one to one relationship.");
//      }
//
//      // No peaks or bases.
//      if (peaks.size() == 0) {
//        peakValue = Optional.empty();
//        baseValue = Optional.empty();
//        range = Optional.empty();
//        return;
//      }
//
//      // Not optional: parses optional peaks into 100% valid data
//      Map<Long, List<Moment<Float>>> peaksGroupByTime = new HashMap<>();
//      Map<Long, List<Moment<Float>>> basesGroupByTime = new HashMap<>();
//
//      // For each peak/base: group by time into two maps
//      for (int i = 0; i < peaks.size(); ++i) {
//
//        if (bID == 38) {
//          System.out.println("WVB peak #" + i + " = " + peaks.get(i).get().getValueAtMoment());
//          System.out.println("WVB base #" + i + " = " + bases.get(i).get().getValueAtMoment());
//        }
//
//        Optional<Moment<Float>> p = peaks.get(i);
//        Optional<Moment<Float>> b = bases.get(i);
//
//        // If there is no peak, there is no base; If there is no base; there is no peak. Skip
//        if (!p.isPresent() || !b.isPresent()) {
//          continue;
//        }
//
//        List<Moment<Float>> peaksQueriedByTime = peaksGroupByTime.get(p.get().getTimeAtMoment());
//        if (peaksQueriedByTime == null) {
//          peaksQueriedByTime = new ArrayList<>();
//          peaksQueriedByTime.add(p.get());
//          peaksGroupByTime.put(p.get().getTimeAtMoment(), peaksQueriedByTime);
//        } else {
//          peaksQueriedByTime.add(p.get());
//        }
//
//        List<Moment<Float>> basesQueriedByTime = basesGroupByTime.get(b.get().getTimeAtMoment());
//        if (basesQueriedByTime == null) {
//          basesQueriedByTime = new ArrayList<>();
//          basesQueriedByTime.add(b.get());
//          basesGroupByTime.put(b.get().getTimeAtMoment(), basesQueriedByTime);
//        } else {
//          basesQueriedByTime.add(b.get());
//        }
//      }
//
//      // ==================================================================
//      // Post Condition: Values either exist or not. No optionals or nulls.
//
//      // If all the given Optional<>s are empty, then it means the map has no data, so return now
//      if (peaksGroupByTime.isEmpty() || basesGroupByTime.isEmpty()) {
//        peakValue = Optional.empty();
//        baseValue = Optional.empty();
//        range = Optional.empty();
//        return;
//      }
//
//      // Map is NOT empty...
//      // Find the maximum (combined peak values) after grouping all sensors by time
//
//      // map a list of lists -> list of max values -> max value
//      peakValue = peaksGroupByTime.values()
//              .stream()
//              .map((peaksAtTime) -> peaksAtTime
//                      .stream()
//                      .reduce(new Moment<Float>(0f), (a, b) -> (combineMoments_Float(a, b))))
//              .max((o1, o2) -> {
//                if (o1.getValueAtMoment() == o2.getValueAtMoment())
//                  return 0;
//                else if (o1.getValueAtMoment() < o2.getValueAtMoment())
//                  return -1;
//                else
//                  return 1;
//              });
//
//      // map a list of lists -> list of max values -> max value
//      baseValue = basesGroupByTime.values()
//              .stream()
//              .map((basesAtTime) -> basesAtTime
//                      .stream()
//                      .reduce(new Moment<Float>(0f), (a, b) -> (combineMoments_Float(a, b))))
//              .min((o1, o2) -> {
//                if (o1.getValueAtMoment() == o2.getValueAtMoment())
//                  return 0;
//                else if (o1.getValueAtMoment() < o2.getValueAtMoment())
//                  return -1;
//                else
//                  return 1;
//              });
//
//      if (!peakValue.isPresent() || !baseValue.isPresent()) {
//        range = Optional.empty();
//      } else {
//        range = Optional.of(peakValue.get().getValueAtMoment() - baseValue.get().getValueAtMoment());
//      }
//    }

    public void refresh(Optional<Moment<Float>> peak, Optional<Moment<Float>> base) {
      peakValue = peak;
      baseValue = base;

      if (!peakValue.isPresent() || !baseValue.isPresent()) {
        range = Optional.empty();
      } else {
        range = Optional.of(peakValue.get().getValueAtMoment() - baseValue.get().getValueAtMoment());
      }
    }

    // Combine by value, returns a new moment with a's time, and a's sensorID
//    private Moment<Float> combineMoments_Float(Moment<Float> a, Moment<Float> b) {
//      return new Moment<>(b.getTimeAtMoment(), a.getValueAtMoment() + b.getValueAtMoment(), b.getSensorID());
//    }

    /**
     * Returns cached peak value
     */
    public Optional<Moment<Float>> getPeak() {
      return peakValue;
    }

    /**
     * Returns cached base value
     */
    public Optional<Moment<Float>> getBase() {
      return baseValue;
    }

    /**
     * Return range (peak - base) from cached peak and base
     */
    public Optional<Float> getRange() {
      return range;
    }
  }

  /**
   * Mutate: individual sensor data source to become indexed with the current timeline
   */
  private void updateSensorDataSources() {
    // Update all uncombined sensor data
    for (NumberDS<Float> d : listOfSensorData) {
      d.updateStats(dailyTimeline.getStartUnix(), dailyTimeline.getEndUnix(),
              IntervalType.QUARTER, dailyTimeline.getIntervalType());
    }

    // Update the campus sum data
    sumEnergyData.updateStats(dailyTimeline.getStartUnix(), dailyTimeline.getEndUnix(),
            IntervalType.QUARTER, dailyTimeline.getIntervalType());
//    System.out.println(sumEnergyData.getIndexedArray().size());
    for (Optional<Float> i : sumEnergyData.getIndexedArray()) {
//      System.out.println(i);
    }

    // Update the combined data sources buildings with *MORE THAN 1* sensor
    for (Building b : listOfBuildings) {
      if (1 < b.getSensors().length) {
        // TODO check for presence
        listOfEnergyReadingsByBuilding.get(b.getBID()).getDataSource().get().updateStats(
                dailyTimeline.getStartUnix(), dailyTimeline.getEndUnix(),
                IntervalType.QUARTER, dailyTimeline.getIntervalType());
      }
    }
  }

  List<BuildingStat> buildingStatistics;
  /**
   * @return the list of all current PeakValues
   */
  public List<BuildingStat> getBuildingStatistics() {
    return buildingStatistics;
  }

  /**
   * Mutate: buildingStatistics to re-calculate to all local Moment objects of sensors
   * @param isEmpty is this the first time initializing the building stats (list is empty?)
   */
  private void calcStatisticsByBuilding(boolean isEmpty) {

    // Initialization:
    if (isEmpty) {
      buildingStatistics = new ArrayList<>();
    }

    // Use the new peak and base values to refresh each building's statistics
    for (Building b : listOfBuildings) {
      // TODO check for presence
      NumberDS<Float> dataPerBuilding = listOfEnergyReadingsByBuilding.get(b.getBID()).getDataSource().get();

      Optional<Moment<Float>> building_PeakValue = dataPerBuilding.getLocalPeakValue();
      Optional<Moment<Float>> building_BaseValue = dataPerBuilding.getLocalBaseValue();

//      if (b.getName().equals("Barletta")) {
//        System.out.println("building_PeakValue = " + building_PeakValue);
//        for (Optional<Float> f : dataPerBuilding.getIndexedArray()) {
//          System.out.println(f);
//        }
//      }

      if (isEmpty) {
        // Add:
        buildingStatistics.add(new BuildingStat(b.getBID(), building_PeakValue, building_BaseValue));
      } else {
        // Mutate:
        buildingStatistics.get(b.getBID()).refresh(building_PeakValue, building_BaseValue);
      }
    }
  }

  public void jumpToNext() {
    getTimeline().jumpToNextSection();          // ALWAYS FIRST: update timeline to next section
    updateSensorDataSources();                  // update sensor data sources
    calcStatisticsByBuilding(false);            // calcuate statistics grouped by buildings
  }

  public void jumpToPrev() {
    getTimeline().jumpToPrevSection();
    updateSensorDataSources();
    calcStatisticsByBuilding(false);
  }

  public void refreshDataAndStats() {
    updateSensorDataSources();
    calcStatisticsByBuilding(false);
  }

}
