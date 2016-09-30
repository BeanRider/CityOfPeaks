package mvc.view;

import java.awt.*;

import mvc.model.IEnergyVizModel;
import mvc.view.visual.AnimationVisual;
import mvc.view.visual.FixedVisual;
import processing.core.PApplet;
import processing.core.PImage;

import static processing.core.PConstants.*;

/**
 * Contains the draw mechanisms for the loading screen
 */
public class LoadingScreen implements FixedVisual, AnimationVisual {

  private int degrees = 90;
  private float scalePercent = 1.45f;
  private int opacity = 20;

  private int blurAmount;

  private PImage bk[];

  public LoadingScreen(PImage[] loadingBackgrounds) {
    bk = loadingBackgrounds;
    blurAmount = loadingBackgrounds.length - 1;
  }

  /**
   * Draws loading graphics
   * @param parentView must not be null
   * @param dataModel
   */
  @Override
  public void draw(CityOfPeaks_View parentView, IEnergyVizModel dataModel) {
    Dimension nativeWH = parentView.getCanvasSize();
    parentView.pushStyle();

    parentView.fill(50, 42, 64);
    parentView.rectMode(CORNER);
    parentView.rect(0, 0, nativeWH.width, nativeWH.height);

    parentView.imageMode(CENTER);
    parentView.tint(255, opacity);
    parentView.image(bk[blurAmount], nativeWH.width / 2, nativeWH.height / 2);
    if (scalePercent < 1.5f) {
      scalePercent += 0.005f;
    } else {
      scalePercent = 1.5f;
    }
    if (opacity < 255) {
      opacity += 5;
    } else {
      opacity = 255;
    }
    if (blurAmount > 0) {
      blurAmount -= 1;
    } else {
      blurAmount = 0;
    }
    parentView.popStyle();

//    parentView.tint(255, 255);

//    parentView.fill(255);

//    int x = nativeWH.width - 120, y = nativeWH.height - 140;
//    parent.ellipseMode(CENTER);
//    parent.noFill();
//    parent.stroke(255, 30);
//    parent.strokeWeight(3);
//    parent.ellipse(x, y, 63, 63);
//
//    parent.strokeWeight(3.5f);
//    degrees = (degrees + 3) % 360;
//    parent.stroke(10, 168, 150);
//    parent.arc(x, y, 63, 63, parent.radians(degrees), parent.radians(degrees + 20));
  }

  @Override
  public void render(PApplet surface) {
    // DO NOTHING
  }

  @Override
  public boolean isFinishedAllAnimations() {
    return opacity == 255;
  }

  @Override
  public Dimension getDimension() {
    try {
      return new Dimension(bk[0].width, bk[0].height);
    } catch (NullPointerException npe) {
      return new Dimension(0, 0);
    }
  }
}
