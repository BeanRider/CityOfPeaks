package mvc.view.interpolator;

/**
 * Code assimilated from Processing.org book step17
 * Represents an interpolator for animation effects
 */
public class SpringInterpolator {

  final float DAMPING = 0.6f;
  final float ATTRACTION = 0.4f;

  float curValue;
  float vel;
  float acc;

  float force = 0;
  float mass = 1;

  float damping = DAMPING;
  float attraction = ATTRACTION;

  boolean isInterpolating = false;
  public float result;

  public SpringInterpolator(float initialValue) {
    curValue = initialValue;
  }

  public void setAttraction(float newAttraction) {
    this.attraction = newAttraction;
  }

  public void setDamping(float newDamping) {
    this.damping = newDamping;
  }

  public SpringInterpolator(float initialValue, float damping, float attraction) {
    curValue = initialValue;
    this.damping = damping;
    this.attraction = attraction;
  }

  public float getCurVal() {
//    System.out.println("Get current value: " + curValue);
    return curValue;
  }

  public void instantSetVal(float newValue) {
    this.curValue = newValue;
  }

  public void update() {
    if (isInterpolating) {
      force += attraction * (result - curValue);
    }

    acc = force / mass;
    vel = (vel + acc) * damping;
    curValue += vel;

    force = 0;
  }

  public void setResult(float newResult) {
    isInterpolating = true;
    result = newResult;
  }
}
