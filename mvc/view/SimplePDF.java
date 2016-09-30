package mvc.view;

import processing.core.PApplet;

/**
 * Created by jeffrey02px2014 on 7/4/16.
 */
public class SimplePDF extends PApplet {
  public void setup() {
  }

  public void draw() {
    // Draw something good here
    line(0, 0, width/2, height);

    // Exit the program
    println("Finished.");
    exit();
  }
  public void settings() {
    size(400, 400, SVG, "PeaksMap.svg");
  }


  public static void main(String args[]) {
    PApplet.main(new String[] { "--present", "mvc.view.SimplePDF" });
  }
}
