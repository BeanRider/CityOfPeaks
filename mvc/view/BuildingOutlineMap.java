package mvc.view;

import java.awt.*;
import java.util.Objects;

import mvc.model.Building;
import mvc.model.IEnergyVizModel;
import mvc.view.visual.BaseTransformableVisual;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;
import processing.core.PVector;

/**
 * Holds rendering logic and all building PShapes; transformations allowed
 */
public class BuildingOutlineMap extends BaseTransformableVisual {

  private Building[] toRender = new Building[0];

//  public BuildingOutlineMap(Building[] listOfBuildingsToRender, IEnergyVizModel e) {
//    toRender = listOfBuildingsToRender;
//  }

  @Override
  public void render(PApplet surface) {
//    Building[] bListVM = toRender;
//    for (int i = 0; i < bListVM.length; ++i) {
//      if (surface.getExtrusionHeight(i) < 2) {
//
//        Building building = bListVM[i];
//        PShape outline = building.getOutline();
//        int color = building.getColor();
//
//        parentView.fill(color, parentView.getOutlineOpacity(i) * (1 - parentView.getExtrusionHeight(i) / 2f));
//        parentView.stroke(color, parentView.getOutlineOpacity(i));
//        parentView.strokeWeight(1.5f);
//
//        parentView.beginShape();
//        for (int v = 0; v < outline.getVertexCount(); ++v) {
//          parentView.vertex(outline.getVertex(v).x, outline.getVertex(v).y);
//        }
//        parentView.endShape(PConstants.CLOSE);
//      }
//    }
  }

  @Override
  public void draw(CityOfPeaks_View parentView, IEnergyVizModel dataModel) {

    Objects.requireNonNull(dataModel);

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
    parentView.shapeMode(PConstants.CORNER);


    Building[] bListVM = dataModel.getListOfBuildings();
    for (int i = 0; i < bListVM.length; ++i) {
      if (parentView.getExtrusionHeight(i) < 2) {

        Building building = bListVM[i];
        PShape outline = building.getOutline();
        int color = building.getColor();

        parentView.fill(color, parentView.getOutlineOpacity(i) * (1 - parentView.getExtrusionHeight(i) / 2f));
        parentView.stroke(color, parentView.getOutlineOpacity(i));
        parentView.strokeWeight(1.5f);

        parentView.beginShape();
        for (int v = 0; v < outline.getVertexCount(); ++v) {
          parentView.vertex(outline.getVertex(v).x, outline.getVertex(v).y);
        }
        parentView.endShape(PConstants.CLOSE);
      }
    }
    parentView.popMatrix();
  }

  @Override
  public Dimension getDimension() {
    return new Dimension(0, 0); // NOT USING
  }
}
