package mvc.view.interpolator;

import java.awt.geom.Point2D;

/**
 * Created by jeffrey02px2014 on 1/16/16.
 */
public class BezierInterpolator {

  float startValue = 0;
  float curValue = 0;
  float curTime = 0;

  float timeVelocity = 0.1f;

  boolean isInterpolating = false;

  float finalValue;

  public BezierInterpolator(float initialValue) {
    startValue = initialValue;
    curValue = initialValue;
  }

  public void setVel(float newTimeVelocity) {
    this.timeVelocity = newTimeVelocity;
  }

  public float getCurValue() {
    return curValue;
  }

  public void update() {
    if (isInterpolating) {
      curTime += timeVelocity;

      // reached 100% time, stop and record startValue as current time.
      if (curTime >= 1f) {
        curTime = 1f;
        isInterpolating = false;
        startValue = curValue;
      }

      curValue = startValue + ((finalValue - startValue) * bezierBlend(curTime));
    }
  }

  public void setResult(float newResult) {
    isInterpolating = true;
    curTime = 0;
    startValue = curValue;
    finalValue = newResult;
  }

  /**
   * Takes in a value from 0 to 1, then outputs an animation percentage after
   * converted with a bezier animation curve
   * @param t time percentage
   * @return animation percentage
   */
  private float bezierBlend(float t) {

// 0.585, 0.040, 0.000, 1.585

    // Version 1
//    Point2D.Float p0 = new Point2D.Float(0.000f, 0.000f);
//    Point2D.Float p1 = new Point2D.Float(0.585f, 0.040f);
//    Point2D.Float p2 = new Point2D.Float(0.000f, 1.585f);
//    Point2D.Float p3 = new Point2D.Float(1.000f, 1.000f);

    // Version 2
    Point2D.Float p0 = new Point2D.Float(0.000f, 0.000f);
    Point2D.Float p1 = new Point2D.Float(0.815f, 0.060f);
    Point2D.Float p2 = new Point2D.Float(0.285f, 1.650f);
    Point2D.Float p3 = new Point2D.Float(1.000f, 1.000f);

    Point2D.Float result = sumFloatPoints(
            scaleFloat(p0, (float) (1 * Math.pow(t, 0) * Math.pow((1 - t), 3))),
            scaleFloat(p1, (float) (3 * Math.pow(t, 1) * Math.pow((1 - t), 2))),
            scaleFloat(p2, (float) (3 * Math.pow(t, 2) * Math.pow((1 - t), 1))),
            scaleFloat(p3, (float) (1 * Math.pow(t, 3) * Math.pow((1 - t), 0))));

    return result.y;
  }

  private Point2D.Float scaleFloat(Point2D.Float toScale, float amount) {
    return new Point2D.Float(toScale.x * amount, toScale.y * amount);
  }

  private Point2D.Float sumFloatPoints(Point2D.Float... points) {
    Point2D.Float result = new Point2D.Float(0, 0);
    for (Point2D.Float pt : points)  {
      result = new Point2D.Float(result.x + pt.x, result.y + pt.y);
    }
    return result;
  }

}
