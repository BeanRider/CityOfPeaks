package mvc.model;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Objects;

import mvc.model.datasource.GeoUtil;
import mvc.model.datasource.StringParseUtil;
import processing.core.PApplet;
import processing.core.PShape;
import processing.core.PVector;
import processing.data.Table;
import processing.data.TableRow;

import static processing.core.PConstants.*;

public class Building {

  private final int BUILDING_ID;
  private final String USE;
  private final int FLOORS;
  private final String NAME;
  private int COLOR;
  private final PVector CENTROID;
  private PShape OUTLINE;
  private final Sensor[] SENSORS;
  private List<String> VERTICES_AS_STRING;

  private final Table buildingTypeRenameTable;
  private final Point2D.Double ORIGIN;
  private final Dimension NATIVE_WH;
  private String SHORTNAME;

  public Building(int bID, String shortName, String usage, int floors, String name, Table colorMap, PVector center, List<String> vertices,
                  Point2D.Double originLatLong, Dimension nativeWH, Sensor[] sensors) {
    BUILDING_ID = bID;
    SHORTNAME = Objects.requireNonNull(shortName);
    NAME = Objects.requireNonNull(name);
    ORIGIN = Objects.requireNonNull(originLatLong);
    NATIVE_WH = Objects.requireNonNull(nativeWH);
    USE = Objects.requireNonNull(usage);
    FLOORS = floors;
    buildingTypeRenameTable = Objects.requireNonNull(colorMap);
    CENTROID = Objects.requireNonNull(center);
    SENSORS = Objects.requireNonNull(sensors);
    VERTICES_AS_STRING = Objects.requireNonNull(vertices);
  }

  public void initColorAndOutline(PApplet parentView) {
    COLOR = getBuildingColor(false, parentView);
    OUTLINE = buildShape(VERTICES_AS_STRING, parentView);
  }

  private PShape buildShape(List<String> verticesAsStringSets, PApplet parentView) {
    parentView.color(0, 0, 0);
    // Make an outline of this particular shape
    PShape outline = parentView.createShape();
    outline.beginShape();
    outline.fill(COLOR, 40);
//    outline.fill(172, 228, 232, 40);
    outline.strokeWeight(1.5f);
    outline.stroke(COLOR);
//    outline.stroke(172, 228, 232);
    for (String s: verticesAsStringSets) {
      PVector v = GeoUtil.convertGeoToScreen(
              StringParseUtil.parseCentroidString(s),
              ORIGIN,
              NATIVE_WH.width,
              NATIVE_WH.height); // Converts to a PVector using 1280 X 800
      outline.vertex(v.x, v.y); // adds adjustment to keep it centered
    }
    outline.endShape(CLOSE);
    return outline;
  }

  public int getBID() {
    return BUILDING_ID;
  }

  public String getUse() {
    return USE;
  }

  public int getFloors() {
    return FLOORS;
  }

  public String getName() {
    return NAME;
  }

  public int getColor() {
    return COLOR;
  }

  public PVector getCentroid() {
    return CENTROID;
  }

  public PShape getOutline() {
    return OUTLINE;
  }

  public Sensor[] getSensors() {
    return SENSORS;
  }

  /**
   * Get building color from the mapping
   * @param isNewName version of naming (new / old)
   * @return new java.awt.Color
   */
  public int getBuildingColor(boolean isNewName, PApplet parentView) {
    String buildingColorAsString = null;
    for (int i = 0; i < buildingTypeRenameTable.getRowCount(); ++i) {
      TableRow row = buildingTypeRenameTable.getRow(i);
      if (isNewName && row.getString("newName").equals(USE)) {
        buildingColorAsString = row.getString("newRGB");  // switch to "rgb" if old colors
      } else if (!isNewName && row.getString("oldName").equals(USE)) {
        buildingColorAsString = row.getString("newRGB");  // switch to "rgb" if old colors
      }
    }

    try {
      Objects.requireNonNull(buildingColorAsString);
    } catch (NullPointerException e) {
      e.printStackTrace();
    }
    String[] buildingRGBAsString = buildingColorAsString.split(",");
    int[] buildingRGB = new int[3];
    for (int i = 0; i < 3; ++i) {
      buildingRGB[i] = Integer.parseInt(buildingRGBAsString[i].trim());
    }
    return parentView.color(buildingRGB[0], buildingRGB[1], buildingRGB[2]);
  }

  public String getShortName() {
    return SHORTNAME;
  }
}
