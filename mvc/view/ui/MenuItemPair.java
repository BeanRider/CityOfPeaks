package mvc.view.ui;

import mvc.controller.CityOfPeaks_Controller;
import processing.core.PConstants;
import processing.core.PImage;
import mvc.view.ui.UIAction.ActionSuite;

/**
 * Contains a single menu item with its:
 * 1. Action
 * 2. Text
 * 3. Icon
 */
public class MenuItemPair {

  private final String title;
  public String getTitle() {
    return this.title;
  }
  private final PImage icon;
  public PImage getIcon() {
    return this.icon;
  }
  private final PImage inverseIcon;
  public PImage getHoverIcon() {
    return this.inverseIcon;
  }
  private final ActionSuite action;
//  public void act(CityOfPeaks_Controller controller) {
//    action.actPressed(controller, );
//  }

  /**
   * Creates a MenuItemPair with given field values
   * @param title displayed name of this item
   * @param icon displayed icon of this item
   * @param event action when this is pressed
   */
  public MenuItemPair(String title, PImage icon, ActionSuite event) {
    this.title = title;
    this.icon = icon;
    this.inverseIcon = icon.copy();
    this.inverseIcon.filter(PConstants.INVERT);
    this.action = event;
  }
}
