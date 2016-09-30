package mvc.view;

import processing.core.PGraphics;

/**
 * VisualBar is a container for the flattened image of a data bar.
 * It contains code that redraw/update the flattened image, if requested.
 */
public abstract class VisualBar {
  private PGraphics bar;
  private final String identifier;
  private final float h, w;

  public VisualBar(String identifier, PGraphics bar) {
    this.identifier = identifier;
    this.bar = bar;
    this.h = bar.height;
    this.w = bar.width;
  }

  /**
   * @return the identifier of this visual bar
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * @return the height of this visual bar
   */
  public float getHeight() {
    return h;
  }

  /**
   * @return the width of this visual bar
   */
  public float getWidth() {
    return w;
  }

  /**
   * @return the graphical bar of this visual bar
   */
  public PGraphics getBar() {
    return bar;
  }

  /**
   * Sets the containing bar graphics to the given newBar
   * @param newBar
   */
  public void setBar(PGraphics newBar) {
    bar = newBar;
  }

  /**
   * Sets the re-rendering mechanism for this visual bar
   */
  public abstract void refresh();

}