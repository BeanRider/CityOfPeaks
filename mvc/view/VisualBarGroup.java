package mvc.view;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import mvc.model.IEnergyVizModel;
import mvc.view.visual.BaseTransformableVisual;
import processing.core.PApplet;
import processing.core.PGraphics;

import static processing.core.PConstants.*;

/**
 * Contains a group of VisualBar objects in a list.
 */
public class VisualBarGroup extends BaseTransformableVisual {

  private int xOffset = 0;
  private int yOffset = 0;

  private int contentTotalHeight = 0;

  private float timeProgress = 0;

  // Represents the limits of the visual bar group to render; relative to the origin of this visualbargroup
  private int viewportMinY = 0;
  private int viewportMaxY = 0;

  private final List<VisualBar> flatVisualGraphs;

  private PGraphics output;

  private PApplet renderContext;

  private int sumOfAllVisualBarsHeight = 0;

  public VisualBarGroup(List<VisualBar> listOfVB, PApplet renderContext) {
    this.flatVisualGraphs = listOfVB;
    this.renderContext = renderContext;
    output = renderContext.createGraphics(renderContext.width, viewportMaxY);

    listOfVB.forEach((VisualBar v) -> sumOfAllVisualBarsHeight += v.getHeight());
  }

  public VisualBarGroup(PApplet renderContext) {
    // Empty list
    flatVisualGraphs = new ArrayList<>();
    this.renderContext = renderContext;
    output = renderContext.createGraphics(renderContext.width, viewportMaxY);
  }

  public void add(VisualBar vb) {
    flatVisualGraphs.add(vb);
    sumOfAllVisualBarsHeight += vb.getHeight();
  }

  /**
   * Updates every single visual graph image within {@code flatVisualGraphs} by calling redraw
   * on {@code VisualBar.redraw} on each VisualBar
   */
  @Override
  public void render(PApplet renderContext) {
    flatVisualGraphs.forEach(VisualBar::refresh);
    if (viewportMaxY > 0) {
      output = renderContext.createGraphics(renderContext.width, viewportMaxY);
      updateOutput();
    }
  }

  @Override
  public void draw(CityOfPeaks_View parentView, IEnergyVizModel dataModel) {

    Dimension d = parentView.getCanvasSize();

    if (viewportMaxY == 0) {
      return;
    }

    // Render cached output
    parentView.imageMode(CORNER);
    parentView.image(output, super.xyzTranslation.x, super.xyzTranslation.y);

    // time progress overlay
    parentView.fill(0, 100);
    parentView.noStroke();
    parentView.rectMode(CORNER);
    parentView.rect(super.xyzTranslation.x, super.xyzTranslation.y, timeProgress * d.width, viewportMaxY);

    // scroll bar
    parentView.pushStyle();

    int viewportHeight = viewportMaxY - viewportMinY;
    int insetFromBorder = 4;
    int scrollTrackLength = viewportHeight - insetFromBorder * 2;

    int handleWidth = 6;
    int handleHeight = (int) (viewportHeight * (viewportHeight / (float) sumOfAllVisualBarsHeight));
    float scrollPercent = yOffset / (float) (-1 * contentTotalHeight + viewportMaxY);
    float scrollAmount = scrollPercent * (scrollTrackLength - handleHeight);

    int trackWidth = 2;

    // Track
    parentView.fill(40);
    parentView.rect(
            super.xyzTranslation.x + d.width - insetFromBorder - handleWidth / 2f - trackWidth / 2f,
            super.xyzTranslation.y + insetFromBorder,
            trackWidth,
            scrollTrackLength);

    // Handle
    parentView.fill(255);
    parentView.stroke(50);
    parentView.strokeWeight(0.5f);
    parentView.rect(
            super.xyzTranslation.x + d.width - insetFromBorder - handleWidth,
            super.xyzTranslation.y + insetFromBorder + scrollAmount,
            handleWidth,
            handleHeight,
            3);
    parentView.popStyle();
  }

  @Override
  public Dimension getDimension() {
    return new Dimension(renderContext.width, viewportMaxY - viewportMinY);
  }

  public void scroll(float amount) {
    yOffset += amount;
    if (yOffset >= 0) {
      yOffset = 0;
    }

    if (yOffset <= -1 * contentTotalHeight + viewportMaxY) {
      yOffset = -1 * contentTotalHeight + viewportMaxY;
    }

    // Update the output rendered graphics
    if (somethingDrawnInOutput) {
      output.clear();
    }

    if (viewportMaxY > 0) {
      updateOutput();
    }

//    System.out.println("upperlimit = " + viewportMinY);
//    System.out.println("lowerlimit = " + viewportMaxY);
//    System.out.println("yOffset = " + yOffset);
  }

  private boolean somethingDrawnInOutput = false;
  private void updateOutput() {
    output.beginDraw();
    somethingDrawnInOutput = true;
    // output.pushMatrix();
    // output.translate(cornerXY.x, cornerXY.y);
    int cur = yOffset;
    int i = 0;
    for (VisualBar vb : flatVisualGraphs) {
      if (cur + vb.getHeight() <= viewportMinY || cur >= viewportMaxY) {
//        System.out.println("cur: " + cur);
        // Don't render
      } else {
        // Render bar
        output.imageMode(CORNER);
        output.image(vb.getBar(), 0 + xOffset, cur);
      }
      cur += vb.getHeight();
      ++i;
    }
    contentTotalHeight = cur - yOffset;

    // output.popMatrix();
    output.endDraw();
  }


  public void addViewPortMaxY(int howMuchToIncrease) {
    viewportMaxY += howMuchToIncrease;
    if (viewportMaxY < 0) {
      viewportMaxY = 0;
    }

    // If there is a viewport open, update the rendered output, but first reset the Pgraphics to the new viewport height
    if (viewportMaxY > 0) {
      output = renderContext.createGraphics(renderContext.width, viewportMaxY);
      updateOutput();
    }
  }
}
