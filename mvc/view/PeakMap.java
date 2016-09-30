package mvc.view;

import java.awt.*;
import java.util.Objects;
import java.util.Optional;

import mvc.model.Building;
import mvc.model.CityOfPeaks_DataModel;
import mvc.model.IEnergyVizModel;
import mvc.view.visual.BaseTransformableVisual;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import processing.core.*;

import static processing.core.PConstants.*;

/**
 * Represents the peak extrusion visuals on the 3D map
 */
public class PeakMap extends BaseTransformableVisual {

  private CityOfPeaks_DataModel viewModel = null;

  public PeakMap(CityOfPeaks_DataModel viewModel) {
    this.viewModel = Objects.requireNonNull(viewModel);
  }

  @Override
  public void draw(CityOfPeaks_View parentView, IEnergyVizModel dataModel) {
    Dimension nativeWH = parentView.getCanvasSize();

    parentView.pushMatrix();
    parentView.translate(nativeWH.width / 2, nativeWH.height / 2);
    parentView.rotateX(xyzRot.x);
    parentView.rotateY(xyzRot.y);
    parentView.rotateZ(xyzRot.z);
    parentView.translate(
            xyzTranslation.x,
            xyzTranslation.y,
            xyzTranslation.z);
    PVector c = parentView.getFocusedCentroid();
    parentView.translate(
            -c.x,
            -c.y,
            -c.z);

    Building[] buildings = viewModel.getListOfBuildings();
    for (int i = 0; i < buildings.length; ++i) {

      // Extrusion
      Building b = buildings[i];

      if (parentView.getExtrusionHeight(i) > 2) {
        drawBuildingExtrusion(
                parentView.getExtrusionHeight(i), b,
                parentView.getOutlineOpacity(i) * 0.49f,
                parentView);
      } else {
        float transitionFactor = parentView.getExtrusionHeight(i) / 2f;
        drawBuildingExtrusion(
                parentView.getExtrusionHeight(i), b,
                parentView.getOutlineOpacity(i) * 0.49f * transitionFactor,
                parentView);
      }

      // Flags
      parentView.textFont(CityOfPeaks_View.FCODA_25, 11);

      float flagHeight =  parentView.getFlagHeight(i);

      parentView.pushMatrix();
      float flagOpacity = parentView.getFlagOpacity(i);
      float sphereRadius = 2f;
      parentView.stroke(b.getColor(), flagOpacity);
      parentView.translate(b.getCentroid().x, b.getCentroid().y, 0);
      parentView.line(0, 0, 0, 0, 0, flagHeight - sphereRadius);

        parentView.pushMatrix();
        parentView.fill(b.getColor(), flagOpacity);
        parentView.noStroke();
        parentView.translate(0, 0, flagHeight);
        parentView.rotateZ(-xyzRot.z);
        parentView.rotateX(-xyzRot.x);
        parentView.ellipseMode(CENTER);
        parentView.ellipse(0, 0, 4, 4);
        parentView.popMatrix();

      // Peak Label
//      float[] zero = {0, 0, 0, 1};
//      float[] result = new float[4];
//      parentView.getMatrix().mult(zero, result);
//      float apature = 600; // fog radius


//      parentView.pushMatrix();
//      parentView.pushStyle();
//      parentView.fill(255, 0, 0);
//      parentView.translate(result[0], result[1], result[2]);
//      parentView.sphere(100);
//      parentView.popStyle();
//      parentView.popMatrix();


//      float depthOfField = (apature - new Vector4f(result[0], result[1], result[2], result[3]).distance(parentView.cameraCenter)) / apature;

//      System.out.println(depthOfField);

//      float depthOfField = parentView.camera;
      parentView.fill(b.getColor(), flagOpacity);
      parentView.stroke(b.getColor(), flagOpacity);
      parentView.strokeWeight(1);

      parentView.translate(0, 0, flagHeight - 7);
      parentView.rotateZ(-xyzRot.z);
      parentView.rotateX(-PApplet.radians(90));
      parentView.translate(7, 0, 0);

      parentView.textAlign(PConstants.LEFT, PConstants.TOP);
      parentView.textSize(8);
      parentView.text(b.getName() + " / " + b.getShortName(), 0, 0, 0);

      Optional<Long> buildingPeakTime = viewModel.getBuildingPeakTime(i);
      Optional<Float> buildingPeakValue = viewModel.getBuildingPeakValue(i);

      if (buildingPeakTime.isPresent()) {
        if (buildingPeakValue.isPresent()) {
          DateTime dateTime = new DateTime(buildingPeakTime.get() * 1000L, DateTimeZone.forID("America/New_York"));
          parentView.textSize(11);
          parentView.text(
                  String.format(
                          Math.round(buildingPeakValue.get()) + " kW" +
                  " @" + "%02d:%02d", dateTime.getHourOfDay(), dateTime.getMinuteOfHour()), 0, 10, 0);
        }
      }
      parentView.popMatrix();
    }
    parentView.popMatrix();
  }

  @Override
  public Dimension getDimension() {
    return new Dimension(0, 0); // NOT USING THIS
  }

  private void drawBuildingExtrusion(float h, Building b, float o, CityOfPeaks_View parent) {

    PShape outline = b.getOutline();
    int color = b.getColor();

    float opacity255 = o;

    parent.pushStyle();
    parent.fill(color, opacity255 / 1.2f);
    parent.strokeWeight(0.01f);
    parent.stroke(color, opacity255);
//    parent.noStroke();
    parent.beginShape(QUAD_STRIP);
    for (int i = 0; i < outline.getVertexCount(); ++i) {
      parent.vertex(outline.getVertex(i).x, outline.getVertex(i).y, h);
      parent.vertex(outline.getVertex(i).x, outline.getVertex(i).y, 0);
    }
    parent.endShape();

    // top part
    parent.fill(color, opacity255 / 2);
    parent.stroke(color, opacity255);
    parent.strokeWeight(1.5f);

    parent.beginShape();
    for (int v = 0; v < outline.getVertexCount(); ++v) {
      parent.vertex(outline.getVertex(v).x, outline.getVertex(v).y, h);
    }
    parent.endShape(PConstants.CLOSE);

    parent.popStyle();
  }
}
