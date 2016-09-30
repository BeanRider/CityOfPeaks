package mvc.view;

import mvc.model.datasource.NumberDS;
import mvc.model.CityOfPeaks_DataModel;
import processing.core.PGraphics;

/**
 * The core logic of the interpretation of the data in graph form
 */
public interface GraphGenerator {

  /**
   * Resize the graph visualizer
   * @param w width
   * @param h height
   */
  void resize(int w, int h);

  /**
   * Sets the color to visualize
   * @param color color
   */
  void setPrimaryColor(int color);

  /**
   * Creates a PGraphics using given the source with the given parentView
   * @param parentView
   * @param dataModel
   * @param source  @return PGraphics that saves the graph image
   */
  PGraphics render(CityOfPeaks_View parentView, CityOfPeaks_DataModel dataModel, NumberDS<Float> source);

  void setTitle(String name);
}
