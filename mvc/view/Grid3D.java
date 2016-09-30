package mvc.view;

import mvc.model.IEnergyVizModel;
import mvc.view.visual.BaseTransformableVisual;

import java.awt.*;

/**
 * Contains the rending and size of a 3D coordinate grid
 */
public class Grid3D extends BaseTransformableVisual {

  private int numVerticalLines;
  private int numHorizontalLines;
  private int gridSize;
  private Dimension gridDimensions;
//  private PGraphics gridImage;
//  private PGraphics blurImage;

  public void init(CityOfPeaks_View parent) {
//    gridImage = parent.createGraphics(gridDimensions.width, gridDimensions.height);
//    gridImage.beginDraw();
//    gridImage.stroke(141, 172, 174, 230);
//    gridImage.strokeWeight(1f);
//    for (int x = 0; x < numVerticalLines; ++x) {
//      gridImage.line(x * gridSize - gridDimensions.width / 2, -gridDimensions.height / 2,
//              x * gridSize - gridDimensions.width / 2, gridDimensions.height / 2);
//      for (int q = 2; q < 10; q+=2) {
//        gridImage.strokeWeight(q * 2);
//        gridImage.stroke(141, 172, 174, 25);
//        /* Outer fade */
//        gridImage.line(x * gridSize - gridDimensions.width / 2, -gridDimensions.height / 2,
//                x * gridSize - gridDimensions.width / 2, gridDimensions.height / 2);
//      }
//    }
//
//    for (int y = 0; y < numHorizontalLines; ++y) {
//      gridImage.line(-gridDimensions.width / 2, y * gridSize - gridDimensions.height / 2,
//              gridDimensions.width / 2, y * gridSize - gridDimensions.height / 2);
//      for (int q = 2; q < 10; q+=2) {
//        gridImage.strokeWeight(q * 2);
//        gridImage.stroke(141, 172, 174, 25);
//        /* Outer fade */
//        gridImage.line(-gridDimensions.width / 2, y * gridSize - gridDimensions.height / 2,
//                gridDimensions.width / 2, y * gridSize - gridDimensions.height / 2);
//      }
//    }
//    gridImage.endDraw();
  }

  public static class Grid3DBuilder {

    int vertLines;
    int horiLines;
    int cellSize;

    public Grid3DBuilder setLines(int vertLines, int horiLines) {
      if (vertLines < 0 || horiLines < 0) {
        throw new RuntimeException("Number of vertical or horizontal lines < 0!");
      }

      this.vertLines = vertLines;
      this.horiLines = horiLines;
      return this;
    }

    public Grid3DBuilder setCellSize(int cellSize) {
      if(cellSize < 0) {
        throw new RuntimeException("Cell size cannot be < 0");
      }
      this.cellSize = cellSize;
      return this;
    }

    public Grid3D build() {
      return new Grid3D(vertLines, horiLines, cellSize);
    }

  }

  private Grid3D(int vLines, int hLines, int cellSize) {
    this.numVerticalLines = vLines;
    this.numHorizontalLines = hLines;
    this.gridSize = cellSize;
    this.gridDimensions = new Dimension(
            numVerticalLines * gridSize,
            numHorizontalLines * gridSize);
  }

  float brightness = 0;
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

//    parentView.pushStyle();
//    parentView.fill(7, 8, 11, 200);
//    parentView.ellipseMode(parentView.CENTER);
//    parentView.ellipse(0, 0, gridDimensions.width + 500, gridDimensions.height + 500);
//    parentView.popStyle();

    brightness = (float) Math.sin(xyzRot.z * 3f);
    int halfWidth = gridDimensions.width / 2;
    int halfHeight = gridDimensions.height / 2;

    int color = parentView.lerpColor(
            parentView.color(141, 172, 174, 50),
            parentView.color(249, 254, 255, 10), brightness);
    for (int x = 0; x < numVerticalLines; ++x) {
      parentView.stroke(color);
      parentView.strokeWeight(0.5f);
      parentView.line(x * gridSize - halfWidth, -halfHeight,
              x * gridSize - halfWidth, halfHeight);
      for (int q = 2; q < 4; q += 2) {
        parentView.strokeWeight(q * 2);
        parentView.stroke(141, 172, 174, 5);
        /* Outer fade */
        parentView.line(x * gridSize - halfWidth, -halfHeight,
                x * gridSize - halfWidth, halfHeight);
      }

    }

    for (int y = 0; y < numHorizontalLines; ++y) {
      parentView.stroke(color);
      parentView.strokeWeight(0.5f);
      parentView.line(-halfWidth, y * gridSize - halfHeight,
              halfWidth, y * gridSize - halfHeight);
      for (int q = 2; q < 6; q += 2) {
        parentView.strokeWeight(q * 2);
        parentView.stroke(141, 172, 174, 5);
        /* Outer fade */
        parentView.line(-halfWidth, y * gridSize - halfHeight,
                halfWidth, y * gridSize - halfHeight);
      }
    }
    parentView.popMatrix();
  }

  @Override
  public Dimension getDimension() {
    return new Dimension(gridDimensions.width, gridDimensions.height);
  }
}
