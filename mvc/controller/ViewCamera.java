package mvc.controller;

public interface ViewCamera {

  void beginAutoCamera();

  void beginManualCamera();

  void beginSpinCamera();

  void zoomBy(float zoom);

  void tick();

  void rotateOrientationBy(float xAmount, float yAmount, float zAmount);

  float getDistanceFromCenter();

}
