package mvc.view.visual;

import mvc.model.IEnergyVizModel;
import mvc.view.CityOfPeaks_View;
import processing.core.PApplet;

import java.awt.*;

/**
 * Contains any graphical content and rendering logic, and can be rendered onto a PApplet, which uses java.awt.Frame to
 * update/display/control the window view.
 */
public interface Visual {
  /**
   * Contains draw logic; draws onto given PApplet surface. Usually called on every frame.
   * @param parentView must not be null
   * @param dataModel must not be null
   */
  void draw(CityOfPeaks_View parentView, IEnergyVizModel dataModel);

  /**
   * Contains render logic to update the visual; renders onto a off-screen (cached) graphics buffer.
   * Usually CPU/GPU-intensive, and therefore called only when necessary.
   * Used for reducing rendering complexity, override a case require this method.
   * @param surface
   */
  void render(PApplet surface);

  /**
   * Returns the rectangular dimensions of this visual, at the current state.
   * Useful for laying out this visual relatively in the window.
   * @return a {code: Dimension} of the visual at its current state.
   */
  Dimension getDimension();
}
