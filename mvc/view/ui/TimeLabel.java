package mvc.view.ui;

import mvc.controller.CityOfPeaks_Controller;
import mvc.model.CityOfPeaks_DataModel;
import mvc.view.ui.UIAction.Action;
import mvc.view.CityOfPeaks_View;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Hours;
import processing.event.MouseEvent;

import java.awt.Dimension;

import static processing.core.PConstants.*;

/**
 * Represents a time label UI component
 */
public class TimeLabel extends RectInteractable {

  public TimeLabel(Dimension parentSize) {
    super.h = 22;
    super.w = parentSize.width;
    setCornerXY(0, parentSize.height - h);

    actionSuite.setPressedAction(new Action() {
      @Override
      public void act(CityOfPeaks_Controller controller, MouseEvent e) {
        controller.setTimePercentage(e.getX() / (float) w);
      }
    });
  }

  @Override
  public void removeHoveredStates() {
    // DO NOTHING
  }

  @Override
  public void resetToDefault() {
    // DO NOTHING
  }

  private boolean showProgress = true;
  public void setShowProgress(boolean show) {
    showProgress = show;
  }

  @Override
  public void render(CityOfPeaks_View parentView, CityOfPeaks_DataModel dataModel) {
      parentView.pushStyle();
      parentView.rectMode(CORNER);

      int x = cornerXY.x, y = cornerXY.y;

      float dayPercent = dataModel.getDayPercentage();
      parentView.noStroke();

      // Progressed
      if (showProgress) {
        parentView.fill(90, 90, 90, 100);
        parentView.rect(0, y, w * dayPercent, h);
      }
      // Un-progressed
//      parentView.fill(199, 199, 199, 30);
//      parentView.rect(w * dayPercent + 1, y, w - w * dayPercent, h);


    // This is necessary because there are days with 23 or 25 hours due to fricking DLS
      DateTime beginDate = new DateTime(dataModel.getTimeline().getStartUnix() * 1000L, DateTimeZone.forID("America/New_York"));
      DateTime endDate = beginDate.plusDays(1);
      float numHoursToDisplay = Hours.hoursBetween(beginDate, endDate).getHours();
//      System.out.println("Hours = " + numHoursToDisplay);

      float spacing = w / numHoursToDisplay;
      int centerLine = y + h / 2;
      for (int i = 1; i < numHoursToDisplay; ++i) {
        parentView.fill(255);
        parentView.textAlign(CENTER, CENTER);
        parentView.textFont(CityOfPeaks_View.FCODA_11, 11);
        parentView.text(String.format("%02d", beginDate.plusHours(i).getHourOfDay()),
                Math.round(spacing * i), centerLine);
        if (i != 0) {
//          parentView.textFont(parentView.FHel_11, 11);
//          parentView.text("i", spacing * i, centerLine);
          parentView.strokeWeight(1f);
          parentView.stroke(255, 60);
          parentView.line(
                  spacing * i, centerLine + 9,
                  spacing * i, y + h);
          parentView.stroke(255, 30);
          parentView.line(
                  spacing * i, y + h,
                  spacing * i, y + h + 3);
        }
      }

      parentView.stroke(255, 100);
      parentView.line(x, y + h, w, y + h);
      parentView.popStyle();
    }
}
