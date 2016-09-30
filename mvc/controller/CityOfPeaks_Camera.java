package mvc.controller;

import mvc.view.CityOfPeaks_View;

public class CityOfPeaks_Camera implements EventDrivenCamera {

  private CityOfPeaks_View view;

  public CityOfPeaks_Camera(CityOfPeaks_View view) {
    this.view = view;
  }

  private enum CameraMode {
    GLIDE, SPIN, MANUAL
  }

  private CameraMode state = CameraMode.GLIDE;
  private int yRotationGlide = 0;
  private int yRotationGlideReset = 360 * 5;

  // ZOOM
  private float distanceFromCenter = 1000;

  // Time accelration
  @Override
  public void decrementTimeAcceleration() {
    tAcc -= 0.00002f;

    if (tAcc < tAccLowerLimit) {
      tAcc = tAccLowerLimit;
    }
  }

  @Override
  public void incrementTimeAcceleration() {
    tAcc += 0.00002f;
    if (tAcc > tAccUpperLimit) {
      tAcc = tAccUpperLimit;
    }
  }

  private float tAcc = 0f;
  private float tAccUpperLimit = 0.0001f;
  private float tAccLowerLimit = -0.0001f;

  private float tVel = 0.0005f;
  private float tVelUpperLimit = 0.007f;
  private float tVelLowerLimit = 0.001f;

  @Override
  public void beginAutoCamera() {
    state = CameraMode.GLIDE;
//    System.out.println("Auto Camera Mode On");
  }

  @Override
  public void beginManualCamera() {
    state = CameraMode.MANUAL;
//    System.out.println("Manual Camera Mode On");
  }

  @Override
  public void beginSpinCamera() {
    state = CameraMode.SPIN;
//    System.out.println("Spin Camera Mode On");
  }

  @Override
  public void zoomBy(float zoomAmount) {
    this.distanceFromCenter =
            Math.min(
                    Math.max(
                            288f,
                            distanceFromCenter - zoomAmount),
                    1200f); // was 1200f
  }

  @Override
  public void rotateOrientationBy(float xAmount, float yAmount, float zAmount) {
    // TODO
  }

  // TODO DELETE THIS!
  @Override
  public float getDistanceFromCenter() {
    return distanceFromCenter;
  }

  @Override
  public void tick() {
    if (view == null) {
      return;
    }

    //////

    // Accelerate
    tVel += tAcc;

    // Limit speed
    if (tVel > tVelUpperLimit) {
      tVel = tVelUpperLimit;
    } else if (tVel < tVelLowerLimit) {
      tVel = tVelLowerLimit;
    }

    float increment = tVel;

    //////

    switch (state) {
      case GLIDE:
        // Rotate visualization based on top down orientation
        yRotationGlide = (++yRotationGlide) % yRotationGlideReset;
//        System.out.println(0.5f * Math.sin(Math.toRadians(yRotationGlide)));

//        view.setVisualOrientation(
//                (float) (Math.toRadians(50f) + 0.5f * Math.sin(Math.toRadians(yRotationGlide / 5f))),
//                0,
//                view.getVizZOrientation() + (float) Math.toRadians(0.2f) + increment);
        view.setVisualOrientation(
                (float) (Math.toRadians(50f) + 0.5f * Math.sin(Math.toRadians(yRotationGlide / 5f))),
                0,
                view.getVizZOrientation() + increment);

        float newZoom = (float) (700 + 300f * Math.sin(Math.toRadians(yRotationGlide) / 5f));
        setZoom(newZoom);
        break;
      case SPIN:
//        view.setVisualOrientation(view.getVizXOrientation(), 0, view.getVizZOrientation() + (float) Math.toRadians(0.2f) + increment);
                view.setVisualOrientation(view.getVizXOrientation(), 0, view.getVizZOrientation() + increment);

        break;
      case MANUAL:
        // DO NOTHING AT ALL
        break;
    }
  }

  private void setZoom(float newZoom) {
    this.distanceFromCenter =
            Math.min(
                    Math.max(
                            288f,
                            newZoom),
                    1200f); // was 1200f
  }
}
