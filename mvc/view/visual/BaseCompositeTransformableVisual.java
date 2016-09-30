package mvc.view.visual;

import java.util.ArrayList;
import java.util.List;

/**
 * A base class for a visual with child/children, that can be transformed.
 * This delegates the same transformation effects to ALL of its child/children.
 *
 */
public abstract class BaseCompositeTransformableVisual extends BaseTransformableVisual {

  protected List<FixedVisual> fixedVisuals = new ArrayList<>(0);
  protected List<BaseTransformableVisual> visual3D = new ArrayList<>(0);

  @Override
  public void rotateXYZ(float addedX, float addedY, float addedZ) {
    super.rotateXYZ(addedX, addedY, addedZ);
    for (BaseTransformableVisual visualComponent3D : visual3D) {
      visualComponent3D.rotateXYZ(addedX, addedY, addedZ);
    }
  }

  @Override
  public void setRotXYZ(float x, float y, float z) {
    super.setRotXYZ(x, y, z);
    for (BaseTransformableVisual visualComponent3D : visual3D) {
      visualComponent3D.setRotXYZ(x, y, z);
    }
  }

  @Override
  public void addCornerXYZ(float x, float y, float z) {
    super.addCornerXYZ(x, y, z);
    for (BaseTransformableVisual visualComponent3D : visual3D) {
      visualComponent3D.addCornerXYZ(x, y, z);
    }
  }

  @Override
  public void setCornerXYZ(float x, float y, float z) {
    super.setRotXYZ(x, y, z);
    for (BaseTransformableVisual visualComponent3D : visual3D) {
      visualComponent3D.setCornerXYZ(x, y, z);
    }
  }


}
