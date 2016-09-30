package main;

import processing.core.PApplet;


/**
 * Runnable main class for project "City of Peaks".
 *
 * R and GGPLOT
 *
 * event driven timeline:
 * - fast forward until 15%? before the peaks
 * - ease slow down in the 15%? before the peaks
 * - uniform speed during peak regions
 *
 */

public class CoPMain {
  public static void main(String[] args) {
    try {
      PApplet.main(new String[]{"--present", "mvc.view.CityOfPeaks_View"});
    } catch (Exception | Error e) {
      System.exit(130);
    }
  }
}
