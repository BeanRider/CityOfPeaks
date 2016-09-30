package mvc.view;

import mvc.model.datasource.Moment;
import mvc.model.datasource.NumberDS;
import mvc.model.Building;
import mvc.model.CityOfPeaks_DataModel;
import mvc.model.CityOfPeaks_Model;
import processing.core.PGraphics;
import mvc.model.time.IntervalType;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static processing.core.PConstants.*;

/**
 * Represents a graph bar that displays a graph that changes with time; caches times before the mutation for efficient
 * rendering.
 */
public class GraphBarTimeProgressive implements GraphGenerator {

  private int w, h;
  private int color;

  private final int topPadding = 100;
  private final int botPadding = 0;
  private final int leftPadding = 0;
  private final int rightPadding = 0;
  private int contentHeight;
  private int contentWidth;

  private class LabelInfo {
    public Point xy;
    public Dimension wh;
    public String content;
    public int color;
    LabelInfo(Point xy, Dimension wh, String content, int c) {
      this.xy = xy;
      this.wh = wh;
      this.content = content;
      this.color = c;
    }
  }

  /**
   * A GraphBar Origin at (0, 0), with given width and height
   * @param w the width
   * @param h the height
   */
  public GraphBarTimeProgressive(int w, int h) {
    this.w = w;
    this.h = h;
    this.contentWidth = w - leftPadding - rightPadding;
    this.contentHeight = h - topPadding - botPadding;
  }

  @Override
  public void resize(int w, int h) {
    this.w = w;
    this.h = h;
    this.contentWidth = w - leftPadding - rightPadding;
    this.contentHeight = h - topPadding - botPadding;
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

    PGraphics cachedGraph = parentView.createGraphics(w, h);

    cachedGraph.beginDraw();
    cachedGraph.clear();
    // Graph
    if (!source.getRange().isPresent()) {
      // DO NOTHING, don't draw a graph
    } else {
      renderGraphCurve(cachedGraph, source, dataModel);
    }
    cachedGraph.endDraw();
    return cachedGraph;
  }

  // Draws the graph curve given the output, data source, and parent
  // Preconditions: range value exists; min value exists
  private void renderGraphCurve(PGraphics output, NumberDS<Float> source, CityOfPeaks_DataModel dataModel) {

    List<Optional<Float>> indexedArray = source.getIndexedArray();

    float segmentWidth = contentWidth / (float) (indexedArray.size());

    if (segmentWidth < 0.5f) {
      segmentWidth = 0.5f;
    }

//    // Yearly is too packed
//    if (dataModel.getTimeline().getIntervalType() == IntervalType.YEAR) {
//      segmentWidth *= 54f; // 18 hour interval
//    }

    Point graphOrigin = new Point(leftPadding, topPadding + contentHeight);
    float oldX = graphOrigin.x;
    float oldY = graphOrigin.y;

    int sampleNumber = 0;

    int sampleGap = 1;
    if (dataModel.getTimeline().getIntervalType() == IntervalType.YEAR) {
      sampleGap = 54;
    }

    List<List<LabelInfo>> textPlacements = new ArrayList<>();
    output.fill(90, 90, 90, 150);
    output.noStroke();
    int dataLength = source.getIndexedArray().size();
    for (int index = 0; index < dataLength; index += sampleGap) {
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
        // Value exists, and ...
        if (index == 0 || !indexedArray.get(index - sampleGap).isPresent()) {
          // 1st value of a curve (first index OR first value after absence): begin the curve.
          oldX = leftPadding + sampleNumber * segmentWidth;
          oldY = graphOrigin.y - source.diffFromMin(value.get()).get() / source.getRange().get() * contentHeight;

          output.beginShape();
          output.vertex(oldX, h);
          output.vertex(oldX, oldY); // first control point twice
          output.curveVertex(oldX, oldY); // first control point twice
        } else if (index == (indexedArray.size() - sampleGap)) {
          // Last index of the given array, therefore: end the curve (final ctrl point twice).
          oldX = leftPadding + sampleNumber * segmentWidth;
          oldY = graphOrigin.y - source.diffFromMin(value.get()).get() / source.getRange().get() * contentHeight;
          output.curveVertex(oldX, oldY);
          output.vertex(oldX, oldY);
          output.vertex(oldX, h);
          output.endShape();
        } else {
          if (index == dataLength) {
            oldX = leftPadding + sampleNumber * segmentWidth;
            oldY = graphOrigin.y - source.diffFromMin(value.get()).get() / source.getRange().get() * contentHeight;
            output.curveVertex(oldX, oldY);
            output.vertex(oldX, oldY);
            output.vertex(oldX, h);
            output.endShape();
          } else {
            // Between start and end values in a curve: simply plot
            oldX = leftPadding + sampleNumber * segmentWidth;
            oldY = graphOrigin.y - source.diffFromMin(value.get()).get() / source.getRange().get() * contentHeight;
            output.curveVertex(oldX, oldY);
          }
        }
        Building[] buildings = dataModel.getListOfBuildings();
        List<CityOfPeaks_Model.BuildingStat> peaks = dataModel.getListOfBuildingStatistics();

        // Search for any building peaks @current index, then save it into the list
        List<LabelInfo> posAtIndex = null;
        for (int bID = 0; bID < buildings.length; ++bID) {
          Building b = buildings[bID];
          CityOfPeaks_Model.BuildingStat singleBuildingStat = peaks.get(bID);
          Optional<Moment<Float>> singleBuildingPeak = singleBuildingStat.getPeak();
          if (singleBuildingPeak.isPresent()) {

            // if the peak time == current time
            if (singleBuildingPeak.get().getTimeAtMoment() == dataModel.getTimeline().getDateForIndex(index).getMillis() / 1000) {
              output.pushStyle();

              // Create a new list.
              if (posAtIndex == null) {
                posAtIndex = new ArrayList<>();
                textPlacements.add(posAtIndex);
              }

              // Add the position
              posAtIndex.add(
                      new LabelInfo(
                          new Point((int) oldX, 0),
                          new Dimension((int) output.textWidth(b.getName()), 20), b.getShortName(),
                          b.getColor()));
            }
          }
        }
      }
      ++sampleNumber;
    }

    for (List<LabelInfo> l : textPlacements) {
      int index = 0;
      for (LabelInfo p : l) {
        // Note: processing handles transformations differently: to rotate around a point, first translate to that
        // point, then do the rotation.
        // It is like moving the canvas to a correct position, then placing the object.

        // In OpenGL: you first rotate then translate the object to its correct place to rotate about that point.
        // It is like moving the object to its correct orientation relative to the world.
        output.pushMatrix();
        output.translate(p.xy.x, h - 27 - 25 * index);
        output.rotate((float) Math.toRadians(-90f));
        output.fill(p.color);
        output.textFont(CityOfPeaks_View.FFIRA_12, 12);
        output.textAlign(LEFT, CENTER);
        output.text(p.content, 0, 0);
        output.popMatrix();

        output.popStyle();

        index++;
      }
      output.pushMatrix();
      output.pushStyle();
      output.translate(l.get(0).xy.x, h - 10);
      output.noStroke();
      output.fill(255);
      output.ellipse(0, 0, 4, 4);
      output.popStyle();
      output.popMatrix();
    }

//    graphStrokeImage.beginDraw();
//    graphStrokeImage.clear();
//    sampleNumber = lastTimelineIndex;
//    graphStrokeImage.stroke(231, 198, 109);
//    graphStrokeImage.strokeWeight(3);
//    graphStrokeImage.noFill();
//    for (int index = lastTimelineIndex; index <= currentTimelineIndex; index += sampleGap) {
//
//      Optional<Float> value = indexedArray.get(index);
//
//      if (!value.isPresent()) {
//        // Absence of a value:
//        if (index > 0 && indexedArray.get(index - sampleGap).isPresent()) {
//          // Reached the edge of a curve: end the curve (final ctrl point twice).
//          graphStrokeImage.curveVertex(oldX, oldY);
//          graphStrokeImage.curveVertex(oldX, oldY);
//          graphStrokeImage.endShape();
//        }
//      } else {
//        // Value exists:
//        if (index == 0 || !indexedArray.get(index - sampleGap).isPresent()) {
//          // 1st value of a curve (first index OR first value after absence): begin the curve.
//          oldX = oX + leftPadding +
//                  sampleNumber * segmentWidth;
////          oldY = oY + topPadding + contentHeight -
////                  graphStrokeImage.diffFromMin(value.get()).get() / source.getRange().get() * contentHeight;
//          oldY = oY + topPadding + source.diffFromMin(value.get()).get() / source.getRange().get() * contentHeight;
//
//          graphStrokeImage.beginShape();
//          graphStrokeImage.curveVertex(oldX, oldY); // first control point twice
//          graphStrokeImage.curveVertex(oldX, oldY); // first control point twice
////          graphStrokeImage.curveVertex(oldX, oldY);
//        } else if (index == (indexedArray.dataLength() - sampleGap)) {
//          // Last index of the given array, therefore: end the curve (final ctrl point twice).
//          graphStrokeImage.curveVertex(oldX, oldY);
//          graphStrokeImage.curveVertex(oldX, oldY);
//          graphStrokeImage.endShape();
//        } else if (index == currentTimelineIndex) {
//          oldX = oX + leftPadding +
//                  sampleNumber * segmentWidth;
////          oldY = oY + topPadding + contentHeight -
////                  source.diffFromMin(value.get()).get() / source.getRange().get() * contentHeight;
//          oldY = oY + topPadding + source.diffFromMin(value.get()).get() / source.getRange().get() * contentHeight;
//          graphStrokeImage.curveVertex(oldX, oldY);
//          graphStrokeImage.curveVertex(oldX, oldY);
//          graphStrokeImage.endShape();
//        } else {
//          // Between start and end values in a curve: plot
//          oldX = oX + leftPadding + sampleNumber * segmentWidth;
//          oldY = oY + topPadding + source.diffFromMin(value.get()).get() / source.getRange().get() * contentHeight;
//          graphStrokeImage.curveVertex(oldX, oldY);
//        }
//      }
//      ++sampleNumber;
//    }
//
//    graphStrokeImage.endDraw();
//    output.blendMode(ADD);
//    output.image(graphStrokeImage, 0, 0);

//    for (int i = 0; i < textPlacements.dataLength(); ++i) {
//      LabelInfo pd = textPlacements.get(i);
//      System.out.println("Placement index: " + i + "; " + pd.xy + "; dataLength = " + textPlacements.dataLength());
//    }
  }

  private String title = "N?A";

  @Override
  public void setTitle(String name) {
    this.title = name;
  }

}
