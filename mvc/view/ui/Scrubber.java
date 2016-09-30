package mvc.view.ui;

import mvc.view.CityOfPeaks_View;
import processing.core.PVector;

/**
 * The scrubber CONTROLS the values of the current time.
 */
public class Scrubber {

  // Graphically
  private float mainX;
  private float mainY;
  private float mainW;

  int headDiameter = 12;
  int headHeight = 10;

  int bkHeight = 2;

  // Abstractly
  private final CityOfPeaks_View CANVAS;
  private ValueRange valuePercentage = new ValueRange(0, 1);
  private float curPercentage = valuePercentage.begin();

  // Data-wise
  private final ValueRange valueRange;
  private final float curValue;

  /**
   * [x, y)
   */
  final class ValueRange {
    private final float x, y;

    ValueRange(float x, float y) {
      this.x = x;
      this.y = y;
    }

    float begin() {
      return x;
    }

    float end() {
      return y;
    }
  }

  public Scrubber(PVector beginXY, float width, ValueRange valueRange, CityOfPeaks_View canvas) {
    mainX = beginXY.x;
    mainY = beginXY.y;
    mainW = width;
    CANVAS = canvas;

    this.valueRange = valueRange;
    curValue = valueRange.begin();
  }

//  void display() {
//    int num15Mins = timeRange.length();
//    int num15MinsPast = timeRange.getCurIdx();
//    float segmentWidth = (float) (DETECTED_WIDTH - xMargin * 2) / num15Mins;
//
//    // Background
//    CANVAS.noStroke();
//    CANVAS.fill(40);
//    CANVAS.rect(mainX, mainY, mainW, bkHeight);
//    CANVAS.fill(255, 230);
//    CANVAS.rect(mainX, mainY, Math.round(num15MinsPast * segmentWidth), bkHeight);
//
//    // Tail part
//    float tailX = mainX + Math.round(num15MinsPast * segmentWidth);
//
//    CANVAS.strokeWeight(1);
//    CANVAS.stroke(0, 20);
//    CANVAS.line(tailX + -2, mainY - 17,
//            tailX + -2, DETECTED_HEIGHT + yMargin - 1);
//
//    CANVAS.stroke(0, 20);
//    CANVAS.line(tailX + -1, mainY - 17,
//            tailX + -1, DETECTED_HEIGHT + yMargin - 1);
//
//    CANVAS.stroke(255);
//    CANVAS.line(tailX + 0, mainY - 17,
//            tailX + 0, DETECTED_HEIGHT + yMargin - 1);
//
//    CANVAS.stroke(0, 20);
//    CANVAS.line(tailX + 1, mainY - 17,
//            tailX + 1, DETECTED_HEIGHT + yMargin - 1);
//
//    CANVAS.stroke(0, 20);
//    CANVAS.line(tailX + 2, mainY - 17,
//            tailX + 2, DETECTED_HEIGHT + yMargin - 1);
//
//    // Head part
//    CANVAS.stroke(0, 100);
//    CANVAS.strokeWeight(0.5f);
//    CANVAS.fill(250);
//    CANVAS.ellipseMode(CENTER);
//    CANVAS.ellipse(mainX + Math.round(num15MinsPast * segmentWidth),
//            mainY,
//            headDiameter, headDiameter);
//  }
//
//  boolean isMouseOver() {
//    float segmentWidth = (float) (DETECTED_WIDTH - xMargin * 2) / timeRange.length();
//    float xDistance = Math.abs(mouseX - (mainX + Math.round(timeRange.getCurIdx() * segmentWidth)));
//    float yDistance = Math.abs(mouseY - mainY);
//    float distance = (float) Math.sqrt(xDistance*xDistance + yDistance*yDistance);
//    if (distance < (float) ((headDiameter + 0.5f) / 2.0f)) {
//      return true;
//    }
//    return false;
//  }
//
//  float getCurXPos() {
//    int num15Mins = timeRange.length();
//    int num15MinsPast = timeRange.getCurIdx();
//    float segmentWidth = (float) (DETECTED_WIDTH - xMargin * 2) / num15Mins;
//    return mainX + Math.round(num15MinsPast * segmentWidth);
//  }
//
//  float getSegWidth() {
//    int num15Mins = timeRange.length();
//    return (float) (DETECTED_WIDTH - xMargin * 2) / num15Mins;
//  }
//
//  void setNewPos(int newX) {
//
//    // 1. Find which segment is best
//    int segNo = Math.round((newX - mainX) / getSegWidth());
//    // 2. Change timeline
//    int currentNo = timeRange.getCurIdx();
//
//    CANVAS.println("input = " + newX);
//    CANVAS.println("Updating from: " + currentNo + " to " + segNo);
//    int amount = Math.abs(segNo - currentNo);
//    boolean isForward = true;
//    if (segNo < currentNo){
//      isForward = false;
//    }
//    for (int i = 0; i < amount; ++i) {
//      if (isForward) {
//        if (timeRange.getCurIdx() == timeRange.getEndIdx()) {
//          break;
//        }
//        timeRange.increment();
//      } else {
//        if (timeRange.getCurIdx() == 0) {
//          break;
//        }
//        timeRange.decrement();
//
//      }
//    }
//  }
}