package mvc.view;

import mvc.model.Building;
import mvc.model.CityOfPeaks_Model;
import mvc.model.datasource.Moment;
import mvc.model.time.IntervalType;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import processing.core.PApplet;
import processing.core.PFont;
import processing.data.Table;

import java.awt.*;
import java.util.List;
import java.util.Optional;

public class BuildingPeakPlotView extends PApplet implements IView {

  private Dimension graphSize = new Dimension(1289, 1280);
  private PFont FCODA_11, FCODA_12, FCODA_13, FCODA_25;
  private CityOfPeaks_Model model;
  private int topPadding = 25;
  private int leftPadding = 30;
  private int paddedWidth = graphSize.width - leftPadding * 2;
  private int paddedHeight = graphSize.height - topPadding * 2;

  private int rowHeight = 25;

  private Table dataCoverage;

  @Override
  public void setup() {
//    this.savePath();
    loadFonts();
    model = new CityOfPeaks_Model();
    model.loadEverything(graphSize, this);
    dataCoverage = loadTable("data_coverage_ 1357016400_to_1388552400.csv", "header");
  }

  PApplet svgResult = this;
  @Override
  public void draw() {
    long startTime = System.nanoTime();

    boolean isBlack = true;

    if (isBlack) {
      svgResult.background(10);
    } else {
      svgResult.background(255);
    }

    // =========================================
    // DATA RANGE:
    // START: (Very first row)
    // UNIX  = 1320733800 (s)
    // HUMAN = 11/8/2011, 1:30AM GMT -5:00

    // END (Last non-zero row):
    // UNIX  = 1418143500 (s)
    // HUMAN = 12/9/2014 11:45AM GMT -5:00

    // =========================================
    // VISUAL RANGE:
    // START: first full day when 32, and most other building energy, is present
    // UNIX  = 1356670800 (s)
    // HUMAN = 12/28/2012 12AM GMT -5:00

    // END:
    // UNIX  = 1418101200 (s)
    // HUMAN = 12/9/2014 12AM GMT -5:00

    // =========================================
    // ONE YEAR VISUAL RANGE:
    // START:
    // UNIX  = 1356670800 (s)
    // HUMAN = 1/1/2013 12AM GMT -5:00

    // END:
    // UNIX  = 1388552400 (s)
    // HUMAN = 1/1/2014 12AM GMT -5:00

    DateTime endDate = new DateTime(1388552400000L, DateTimeZone.forID("America/New_York"));
    DateTime startDate = new DateTime(1356670800000L, DateTimeZone.forID("America/New_York"));

    DateTime tempEndDate = startDate.plusDays(50);

    int index = 0;

    svgResult.rectMode(CENTER);
    svgResult.textFont(FCODA_13, 13);

    int buildingLabelWidth = 184;
    int dataCoverageColumnWidth = 25 + 60;
    int chartWidth = paddedWidth - buildingLabelWidth - dataCoverageColumnWidth;
    int timeLabelHeight = 60;
    System.out.println(chartWidth);
    int barWidth = Math.round(chartWidth * (15 / 1440f));

    int chartStartX = leftPadding + buildingLabelWidth + dataCoverageColumnWidth;


    // Top time labels
    svgResult.pushStyle();
    svgResult.textAlign(CENTER, CENTER);
    if (isBlack) {
      svgResult.fill(150);
      svgResult.stroke(35);
    } else {
      svgResult.fill(150);
      svgResult.stroke(220);
    }
    for (int i = 0; i <= 24; i += 4) {

      float x = chartStartX + (i / 24f) * chartWidth;

      svgResult.text(String.format("%02d:00", i),
              chartStartX + (i / 24f) * chartWidth,
              topPadding + timeLabelHeight / 2);
      if (i != 0 && i != 24) {
        svgResult.line(x, topPadding + timeLabelHeight, x, topPadding + timeLabelHeight + model.getNumberOfBuildings() * rowHeight);
      }
    }
    svgResult.popStyle();

    // Horizontal rules
    svgResult.pushStyle();
    if (isBlack) {
      svgResult.stroke(25);
    } else {
      svgResult.stroke(240);
    }
    for (int i = 0; i < model.getListOfBuildings().length; ++i) {
      float x = chartStartX;
      float y = topPadding + timeLabelHeight + i * rowHeight;
      svgResult.line(
              x,
              y,
              x + chartWidth,
              y);
    }
    svgResult.popStyle();

    svgResult.pushStyle();
    svgResult.textAlign(LEFT, CENTER);
    // Left building labels
    for (int i = 0; i < model.getListOfBuildings().length; ++i) {
      Building bI = model.getListOfBuildings()[i];
      svgResult.fill(bI.getColor());
      svgResult.text(bI.getName().toUpperCase(),
              leftPadding + 5,
              topPadding + timeLabelHeight + i * rowHeight);
    }
    svgResult.popStyle();

    // Data Coverage Pie Charts:
    svgResult.pushStyle();
    svgResult.ellipseMode(CENTER);
    svgResult.noStroke();
    int pieX = Math.round(leftPadding + buildingLabelWidth + dataCoverageColumnWidth / 2f);
    for (int i = 0; i < model.getNumberOfBuildings(); ++i) {
      float buildingDataCoverage = dataCoverage.getRow(0).getFloat(i);
      System.out.println(buildingDataCoverage);

      // Draw Pies
      float startDegree = radians(-90);
      float endDegree = startDegree + radians(360) * buildingDataCoverage;
      int pieY = topPadding + timeLabelHeight + i * rowHeight;

      if (isBlack) {
        svgResult.fill(45);
      } else {
        svgResult.fill(200);
      }
      svgResult.ellipse(
              pieX,
              pieY,
              rowHeight - 4,
              rowHeight - 4);
      if (isBlack) {
        svgResult.fill(240);
      } else {
        svgResult.fill(100);
      }
      svgResult.arc(
              pieX,
              pieY,
              rowHeight - 4,
              rowHeight - 4,
              startDegree,
              endDegree
      );

    }
    svgResult.popStyle();


    svgResult.noStroke();
    if (isBlack) {
      svgResult.fill(255, 255, 255, 7);
    } else {
      svgResult.fill(0, 0, 0, 7);
    }
    // Starting from the first date where data is available, loop until the last date where data is available.
    for (DateTime d = startDate; d.getMillis() < endDate.getMillis(); d = d.plusDays(1)) {
      // Re-index building peaks
      if (index < 500 || index > 1000) {
        System.out.println("Unix = " + d.getMillis());
      }
      index++;

      model.getTimeline().updateTime(d.getMillis() / 1000L, IntervalType.DAY);
      model.refreshDataAndStats();

      List<CityOfPeaks_Model.BuildingStat> stats = model.getBuildingStatistics();
      for (int i = 0; i < stats.size(); i++) {

        CityOfPeaks_Model.BuildingStat bState = stats.get(i);
        Optional<Moment<Float>> bPeak = bState.getPeak();

        if (bPeak.isPresent()) {
          Moment<Float> peakMoment = bState.getPeak().get();
          DateTime timeAtMoment = new DateTime(
                  peakMoment.getTimeAtMoment() * 1000L,
                  DateTimeZone.forID("America/New_York"));

//          if (timeAtMoment.getHourOfDay() == 0
//                  && timeAtMoment.getMinuteOfHour() == 0) {
//            System.out.println("Building with midnight peak: " + model.getListOfBuildings()[i].getName() + ";");
//            System.out.println("Date: " + timeAtMoment.getYear() + "/" + timeAtMoment.getMonthOfYear() + "/" + timeAtMoment.getDayOfMonth());
//            System.out.println("Interator Date: " + d.getYear() + "/" + d.getMonthOfYear() + "/" + d.getDayOfMonth());
//
//          }

          svgResult.rect(
                  chartStartX + (timeAtMoment.getMinuteOfDay() / 1440f) * chartWidth,
                  topPadding + timeLabelHeight + i * rowHeight,
                  barWidth,
                  rowHeight);
        }
      }
    }

    // Draw only once
    exit();
    System.out.println("I took " + (System.nanoTime() - startTime) + " nanoseconds to generate the plot graph!");
  }

  // PDF uses createFont instead of loadFont!
  private void loadFonts() {
    FCODA_11 = createFont("Coda", 11);
    FCODA_12 = createFont("Coda", 12);
    FCODA_13 = createFont("Coda", 13);
    FCODA_25 = createFont("Coda", 25);
  }

  @Override
  public void settings() {
    size(graphSize.width, graphSize.height, PDF, "PeaksMap.pdf"); // Using PDF for textMode(SHAPE) rendering.
  }

  public static void main(String args[]) {
    PApplet.main(new String[] { "--present", "mvc.view.BuildingPeakPlotView" });
  }

}
