package mvc.view;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import mvc.model.datasource.NumberDS;
import mvc.model.CityOfPeaks_DataModel;
import mvc.model.IEnergyVizModel;
import mvc.model.time.TimeRangeUtil;
import org.joda.time.Hours;
import processing.core.PGraphics;
import mvc.model.time.IntervalType;

import static processing.core.PConstants.CENTER;
import static processing.core.PConstants.LEFT;
import static processing.core.PConstants.TOP;

/**
 * Represents a single data line graph that shows the peak value
 */
public class WhiteGraphBar implements GraphGenerator {

  private int w, h;
  private int color;

  private int padding = 0;
  private int paddingW = 0;
  private int paddedH;
  private int paddedW;
  private final int oX = 0, oY = 0;

  /**
   * A GraphBar Origin at (0, 0), with given width and height
   * @param w the width
   * @param h the height
   */
  public WhiteGraphBar(int w, int h) {
    this.w = w;
    this.h = h;
    this.paddedW = w - 2 * paddingW;
    this.paddedH = h - 2 * padding;
  }

  @Override
  public void resize(int w, int h) {
    this.w = w;
    this.h = h;
    this.paddedW = w - 2 * paddingW;
    this.paddedH = h - 2 * padding;
  }

  @Override
  public void setPrimaryColor(int color) {
    this.color = color;
  }

  @Override
  public PGraphics render(CityOfPeaks_View parentView, CityOfPeaks_DataModel dataModel, NumberDS<Float> source) {
    Objects.requireNonNull(source);
    Objects.requireNonNull(parentView);
    Objects.requireNonNull(dataModel);

    PGraphics output = parentView.createGraphics(w, h);
    output.beginDraw();

    // 1. Background
    output.noStroke();
    output.fill(10, 230); // Black w/ slight transparency
    output.rect(oX, oY, w, h);

    // two lines stroking the top and bottom, inner style
    output.stroke(255, 40);
    output.line(oX, oY, oX + w, oY);
    output.line(oX, oY + h, oX + w, oY + h);
    output.noStroke();

    // 22 vertical hour lines
    output.stroke(255, 20);
    output.strokeWeight(1.5f);
    TimeRangeUtil timeline = dataModel.getTimeline();
    float numHours = Hours.hoursBetween(timeline.toDateTimeFor(TimeRangeUtil.START), timeline.toDateTimeFor(TimeRangeUtil.END)).getHours();
    for (int i = 0; i < numHours; i += 1) {
      float x = oX + (i / numHours) * w;
      if (i != 0) {
        output.line(
                x, oY,
                x, oY + h);
      }
    }

    // 2. Graph
    if (!source.getRange().isPresent()) {
      // DO NOTHING, don't draw a graph
    } else {
      makeGraphCurve(output, source, dataModel);

      // 3. Max point
      int red = output.color(255, 81, 63);

      List<Optional<Float>> indexedList = source.getIndexedArray();
      Optional<Integer> indexOfLocalMax = source.getIndexOfLocalMax();
      Optional<Float> valueOfLocalMax = source.getLocalMax();

      float segmentWidth = paddedW / (float) (indexedList.size());

      if (indexOfLocalMax.isPresent() && valueOfLocalMax.isPresent()) {

        Optional<Float> diffFromMin = source.diffFromMin(valueOfLocalMax.get());
        Optional<Float> range = source.getRange();

        if (diffFromMin.isPresent() && range.isPresent()) {
          float maxX = paddingW + indexOfLocalMax.get() * segmentWidth;
          float maxY = padding + paddedH - diffFromMin.get() / range.get() * paddedH;

          float maxLabelX = maxX + 5;
          float maxLabelY = maxY - 3;

          output.fill(red);
          output.textFont(CityOfPeaks_View.FCODA_12, 12);
          float textHeight = output.textDescent() + output.textAscent();
          float textWidth = output.textWidth(String.valueOf(valueOfLocalMax.get()));
          if (maxX + 5 + textWidth > w) {
            maxLabelX = maxX - 5 - textWidth;
          }
          if (maxY - 3 - textHeight < 0) {
            maxLabelY = maxY + 3 + textHeight;
          }
          output.text(valueOfLocalMax.get(), maxLabelX, maxLabelY);

          output.noStroke();
          output.ellipseMode(CENTER);

          output.fill(0, 40);
          output.ellipse(maxX + 1, maxY + 1, 7, 7);
          output.ellipse(maxX + 2, maxY + 2, 7, 7);

          output.fill(255);
          output.ellipse(maxX, maxY, 7, 7);

          output.fill(red);
          output.ellipse(maxX, maxY, 5, 5);
        }
      }
    }

    // Name
    output.textAlign(LEFT, TOP);
    output.textFont(CityOfPeaks_View.FCODA_12, 12);
    output.fill(color);
    output.text(title, 10, 10);

    output.endDraw();
    return output;
  }

  // Draws the graph curve given the output, data source, and parent
  // Preconditions: range value exists; min value exists
  private void makeGraphCurve(PGraphics output, NumberDS<Float> source, IEnergyVizModel dataModel) {

    List<Optional<Float>> indexedArray = source.getIndexedArray();

    float segmentWidth = paddedW / (float) (indexedArray.size());
    if (dataModel.getTimeline().getIntervalType() == IntervalType.YEAR) {
      // Yearly is too packed
      segmentWidth *= 54f; // 18 hour interval
    }
    float oldX = paddingW;
    float oldY = padding;

    output.stroke(255);
    output.strokeWeight(2);
    output.noFill();

    int sampleNumber = 0;

    int sampleGap = 1;
    if (dataModel.getTimeline().getIntervalType() == IntervalType.YEAR) {
      sampleGap = 54;
    }
    for (int index = 0; index < indexedArray.size(); index += sampleGap) {

      Optional<Float> value = indexedArray.get(index);

      if (!value.isPresent()) {
        // Absence of a value:
        if (index > 0 && indexedArray.get(index - sampleGap).isPresent()) {
          // Reached the edge of a curve: end the curve (final ctrl point twice).
          output.curveVertex(oldX, oldY);
          output.curveVertex(oldX, oldY);
          output.endShape();
        }
      } else {
        // Value exists:
        if (index == 0 || !indexedArray.get(index - sampleGap).isPresent()) {
          // 1st value of a curve (first index OR first value after absence): begin the curve.
          oldX = oX + paddingW +
                  sampleNumber * segmentWidth;
//          oldY = oY + padding + paddedH -
//                  source.diffFromMin(value.get()).get() / source.getRange().get() * paddedH;
          oldY = padding + oY + paddedH - source.diffFromMin(value.get()).get() / source.getRange().get() * paddedH;;


          output.beginShape();
          output.curveVertex(oldX, oldY);             // first control point twice
          output.curveVertex(oldX, oldY);             // first control point twice
        } else if (index == (indexedArray.size() - sampleGap)) {
          // Last index of the given array, therefore: end the curve (final ctrl point twice).
          oldX = oX + paddingW +
                  sampleNumber * segmentWidth;
          oldY = padding + oY + paddedH - source.diffFromMin(value.get()).get() / source.getRange().get() * paddedH;;
          output.curveVertex(oldX, oldY);
          output.curveVertex(oldX, oldY);
          output.endShape();
        } else {
          // Between start and end values in a curve: plot
//          float newPointX =
//          float newPointY =
          oldX = oX + paddingW + sampleNumber * segmentWidth;
//          oldY = padding + oY + paddedH - source.diffFromMin(value.get()).get() / source.getRange().get() * paddedH;
          oldY = padding + oY + paddedH - source.diffFromMin(value.get()).get() / source.getRange().get() * paddedH;;
          output.curveVertex(oldX, oldY);
        }
      }
      ++sampleNumber;
    }
  }

  private String title = "NA";

  @Override
  public void setTitle(String name) {
    this.title = name;
  }
}
