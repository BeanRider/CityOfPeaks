package mvc.view.ui.UIAction;

import mvc.controller.CityOfPeaks_Controller;
import processing.event.MouseEvent;

/**
 * Created by jeffrey02px2014 on 5/15/16.
 */
public abstract class Action {

  public static final Action DEFAULT = new Action() {
    @Override
    public void act(CityOfPeaks_Controller controller, MouseEvent e) {
      // DO NOTING
    }
  };

  public abstract void act(CityOfPeaks_Controller controller, MouseEvent e);
}