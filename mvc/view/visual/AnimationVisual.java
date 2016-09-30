package mvc.view.visual;

/**
 * Created by jeffrey02px2014 on 7/12/16.
 */
public interface AnimationVisual extends Visual {
  /**
   * Determines whether the visual has finished all of its animations (ie a UI animation)
   * @return true if all animations have finished
   */
  boolean isFinishedAllAnimations();
}
