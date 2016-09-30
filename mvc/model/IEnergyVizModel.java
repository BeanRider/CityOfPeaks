package mvc.model;

import mvc.model.datasource.NumberDS;
import mvc.model.time.TimeRangeUtil;

/**
 * Interface for all application models
 */
public interface IEnergyVizModel {

  /**
   * @return the array of buildings in the visualization
   */
  Building[] getListOfBuildings();

  /**
   * @return the timeline representing the visual temporal values
   */
  TimeRangeUtil getTimeline();


  float getDayPercentage();

  NumberDS<Float> getSumEnergyData();
}
