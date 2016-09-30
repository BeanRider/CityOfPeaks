package mvc.model;

import mvc.model.datasource.Moment;
import mvc.model.datasource.NumberDS;
import mvc.model.time.TimeRangeUtil;

import java.util.List;
import java.util.Optional;

/**
 * Contains data model of entire program and getters for the view to access model data
 * Role: Provide parsed and analyzed data to the view, decouples the business model from the view.
 * Only allows view to access/read data using getter methods; cannot mutate data.
 */
public class CityOfPeaks_DataModel implements IEnergyVizModel {

  CityOfPeaks_Model businessModel = null;

  public CityOfPeaks_DataModel(IEnergyVizModel businessModel) {
    this.businessModel = (CityOfPeaks_Model) businessModel;
  }

  // =========
  // TIME DATA
  // =========

  @Override
  public TimeRangeUtil getTimeline() {
    return businessModel.getTimeline();
  }

  public float getDayPercentage() {
    return businessModel.getDayPercentage();
  }

  // =============
  // BUILDING DATA
  // =============

  @Override
  public Building[] getListOfBuildings() {
    return businessModel.getListOfBuildings();
  }

  public int getNumberOfBuildings() {
    return businessModel.getNumberOfBuildings();
  }

  // =================
  // SENSOR DATA (RAW)
  // =================

  public List<NumberDS<Float>> getListOfSensors() {
    return businessModel.getListOfSensorData();
  }

  @Override
  public NumberDS<Float> getSumEnergyData() {
    return businessModel.getSumEnergyData();
  }

  // =======================
  // SENSOR DATA (PROCESSED)
  // =======================

  /**
   * Returns a cached statistics. No calculations required.
   * @return
   */
  public List<CityOfPeaks_Model.BuildingStat> getListOfBuildingStatistics() {
    return businessModel.getBuildingStatistics();
  }

  /**
   * Requires processing
   * @param buildingID
   * @return the sum energy level of all sensors in the given buildingID, at the current time in the model.
   */
  public Optional<Float> getCurrentBuildingEnergyLevel(int buildingID) {
//System.out.println("\n\n");

    List<NumberDS<Float>> dataList = businessModel.getListOfSensorData();
    long curUnixTime = businessModel.getTimeline().getCurUnix();

    Optional<Float> energyLevel = Optional.empty();
    for (Sensor t : businessModel.getBuildingToSensorMap().get(buildingID)) {
      NumberDS<Float> buildingSensorData = dataList.get(t.getSID());
      Optional<Float> value = buildingSensorData.requestValueAtUnix(curUnixTime);
      if (value.isPresent()) {
        if (energyLevel.isPresent()) {
          energyLevel = Optional.of(value.get() + energyLevel.get());
        } else {
          energyLevel = value;
        }
      }
      // WVB
      if (buildingID == 38) {
//        System.out.println(energyLevel.get());
      }
    }
    return energyLevel;
  }

  /**
   * @param bID
   * @return the max sum energy level of all sensors in the given buildingID, at the current <b>timerange type</b>.
   */
  public Optional<Float> getBuildingPeakValue(int bID) {
    Optional<Moment<Float>> peak = getListOfBuildingStatistics().get(bID).getPeak();
    if (peak.isPresent()) {
      return Optional.of(peak.get().getValueAtMoment());
    } else {
      return Optional.empty();
    }
  }

  /**
   * @param bID
   * @return the peak-time of all sensors combined in the given buildingID, at the current <b>timerange type</b>.
   */
  public Optional<Long> getBuildingPeakTime(int bID) {
    Optional<Moment<Float>> peak = getListOfBuildingStatistics().get(bID).getPeak();
    if (peak.isPresent()) {
      return Optional.of(peak.get().getTimeAtMoment());
    } else {
      return Optional.empty();
    }
  }

  /**
   * @param bID
   * @return the min sum energy level of all sensors in the given buildingID, at the current <b>timerange type</b>.
   */
  public Optional<Float> getBuildingBaseValue(int bID) {
    Optional<Moment<Float>> base = getListOfBuildingStatistics().get(bID).getBase();
    if (base.isPresent()) {
      return Optional.of(base.get().getValueAtMoment());
    } else {
      return Optional.empty();
    }
  }

  /**
   * @param bID
   * @return the base-time of all sensors combined in the given buildingID, at the current <b>timerange type</b>.
   */
  public Optional<Long> getBuildingBaseTime(int bID) {
    Optional<Moment<Float>> base = getListOfBuildingStatistics().get(bID).getBase();
    if (base.isPresent()) {
      return Optional.of(base.get().getTimeAtMoment());
    } else {
      return Optional.empty();
    }
  }

  public String getLoadingText() {
    if (businessModel == null) {
      return "Loading...";
    }
    return businessModel.loadingText.toString();
  }
}
