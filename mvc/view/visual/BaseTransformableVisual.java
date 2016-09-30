package mvc.view.visual;

import mvc.model.IEnergyVizModel;
import mvc.view.CityOfPeaks_View;
import org.joml.Vector3f;
import processing.core.PApplet;

/**
 * A base class for a visual that can be mutated in its transformation, i.e.: translated, rotated, scaled...
 */
public abstract class BaseTransformableVisual implements TransformableVisual {

  // Transformations: currently limited to 3D rotation and transformation.
  protected Vector3f xyzRot = new Vector3f();
  protected Vector3f xyzTranslation = new Vector3f();

  @Override
  public abstract void draw(CityOfPeaks_View parentView, IEnergyVizModel dataModel);

  @Override
  public void render(PApplet surface) {
    // BY DEFAULT, DO NOTHING
  }

  @Override
  public Vector3f getRotXYZ() {
    return new Vector3f(xyzRot);
  }

  @Override
  public void rotateXYZ(float addedX, float addedY, float addedZ) {
    xyzRot = xyzRot.add(addedX, addedY, addedZ);

    double twoPI = 2 * Math.PI;

    double truncatedX = xyzRot.x;
    if (truncatedX > twoPI) {
      truncatedX = truncatedX - (truncatedX / twoPI) * twoPI;
    }

    double truncatedY = xyzRot.y;
    if (truncatedY > twoPI) {
      truncatedY = truncatedY - (truncatedY / twoPI) * twoPI;
    }

    double truncatedZ = xyzRot.z;
    if (truncatedZ > twoPI) {
      truncatedZ = truncatedZ - (truncatedZ / twoPI) * twoPI;
    }

    setRotXYZ((float) truncatedX, (float) truncatedY, (float) truncatedZ);
  }

  @Override
  public void setRotXYZ(float x, float y, float z) {
    xyzRot = new Vector3f(x, y, z);
  }

  @Override
  public void addCornerXYZ(float addedX, float addedY, float addedZ) {
    this.xyzTranslation = xyzTranslation.add(addedX, addedY, addedZ);
  }

  @Override
  public void setCornerXYZ(float x, float y, float z) {
    this.xyzTranslation = new Vector3f(x, y, z);
  }

}
