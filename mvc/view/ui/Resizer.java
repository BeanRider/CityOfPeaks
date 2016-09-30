package mvc.view.ui;

import mvc.model.CityOfPeaks_DataModel;
import mvc.view.CityOfPeaks_View;
import processing.core.PConstants;
import processing.core.PImage;

import java.awt.*;
import java.util.Optional;

public class Resizer extends ImageButton {

  public Resizer(PImage staticIcon, Point xy, int w, int h) {
    super(staticIcon, xy, w, h);
  }

  private Optional<PImage> upArrow = Optional.empty();
  private Optional<PImage> downArrow = Optional.empty();
  private ResizerState resizerState = ResizerState.AT_MAX_Y;

  public void setUpOnlyIcon(PImage upArrow) {
    this.upArrow = Optional.of(upArrow);
  }

  public void setDownOnlyIcon(PImage downArrow) {
    this.downArrow = Optional.of(downArrow);
  }

  /**
   * Toggled Icon has precedence to hover icon.
   * @param parentView
   * @param dataModel
   */
  @Override
  public void render(CityOfPeaks_View parentView, CityOfPeaks_DataModel dataModel) {

    if (!upArrow.isPresent()
            || !downArrow.isPresent()
            || resizerState == ResizerState.BETWEEN) {
      super.render(parentView, dataModel);
      return;
    }

    parentView.pushStyle();
    parentView.imageMode(PConstants.CORNER);

    PImage toRender;
    switch (resizerState) {
      case AT_MIN_Y:
        toRender = downArrow.get();
        break;
      case AT_MAX_Y:
        toRender = upArrow.get();
        break;
      default:
        throw new RuntimeException("Between state should be rendered using super draw method, not this logic!");
    }

    if (state == ButtonState.STATIC) {
      parentView.tint(100);
    }

    parentView.image(toRender, cornerXY.x, cornerXY.y, width, height);
    parentView.popStyle();

    if (debugMode) {
      parentView.pushStyle();
      parentView.rectMode(PConstants.CORNER);
      parentView.noFill();
      parentView.strokeWeight(1f);
      parentView.stroke(255, 255, 255);
      parentView.rect(cornerXY.x, cornerXY.y, width, height);
      parentView.popStyle();
    }
  }

  public enum ResizerState {
    AT_MAX_Y, AT_MIN_Y, BETWEEN
  }

  public void setUpOnlyState() {
    this.resizerState = ResizerState.AT_MAX_Y;
  }

  public void setDownOnlyState() {
    this.resizerState = ResizerState.AT_MIN_Y;
  }

  public void setBetweenState() {
    this.resizerState = ResizerState.BETWEEN;
  }
}
