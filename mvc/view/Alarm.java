package mvc.view;

import mvc.controller.CityOfPeaks_Controller;
import mvc.view.ui.UIAction.Action;
import processing.event.MouseEvent;

/**
 * Created by jeffrey02px2014 on 6/19/16.
 */
public interface Alarm {

  void tick(CityOfPeaks_Controller c, MouseEvent e);

  void restartFromBeginning();

  void startCountDown();

  void pauseCountDown();

  boolean queryCompletionStatus();

  void addCompletionAction(Action a);
}
