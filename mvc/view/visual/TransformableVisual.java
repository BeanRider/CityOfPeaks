package mvc.view.visual;

import org.joml.Vector3f;

/**
 * Represents a {code: Visual} that can be transformed, i.e.: translated, rotated, scaled...
 */
public interface TransformableVisual extends Visual {

  /**
   * @return the rotation xyz of this 3D component
   */
  Vector3f getRotXYZ();

  /**
   * Sets the corner xy position of this visual component
   * @param x
   * @param y
   * @param z
   */
  void setCornerXYZ(float x, float y, float z);

  /**
   * Adds to the corner xy position of this visual component
   * @param addedX
   * @param addedY
   * @param addedZ
   */
  void addCornerXYZ(float addedX, float addedY, float addedZ);

  /**
   * Sets the rotation xyz of this 3D component
   * @param x
   * @param y
   * @param z
   */
  void setRotXYZ(float x, float y, float z);

  /**
   * Adds to the rotation xyz of this 3D component
   * @param addedX
   * @param addedY
   * @param addedZ
   */
  void rotateXYZ(float addedX, float addedY, float addedZ);

}
