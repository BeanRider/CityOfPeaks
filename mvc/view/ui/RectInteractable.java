package mvc.view.ui;

import java.awt.*;
import java.util.Objects;

import mvc.controller.CityOfPeaks_Controller;
import mvc.model.CityOfPeaks_DataModel;
import processing.event.MouseEvent;
import mvc.view.CityOfPeaks_View;
import mvc.view.ui.UIAction.ActionSuite;

/**
 * Represents a selectable UI component, specifically rect shaped for mouse detection.
 */
public abstract class RectInteractable implements Interactable {

  protected Point cornerXY = new Point(0, 0);
  protected int w, h;
  protected ActionSuite actionSuite = ActionSuite.DEFAULT;

  @Override
  public void setCornerXY(int newX, int newY) {
    cornerXY = new Point(newX, newY);
  }

  @Override
  public Point getCornerXY() {
    return cornerXY;
  }

  @Override
  public void addCornerXY(int addedX, int addedY) {
    cornerXY = new Point(cornerXY.x + addedX, cornerXY.y + addedY);
  }

  @Override
  public abstract void render(CityOfPeaks_View parentView, CityOfPeaks_DataModel dataModel);

  @Override
  public boolean isMouseOver(float mX, float mY) {
    return cornerXY.x <= mX && mX <= cornerXY.x + w
            && cornerXY.y <= mY && mY <= cornerXY.y + h;
  }

  @Override
  public void mouseHoverAction(CityOfPeaks_Controller controller, MouseEvent event) {
    checkState(event);
    pushHelpTextToController(controller, new Point(event.getX(), event.getY()));
  }

  @Override
  public void mousePressedAction(CityOfPeaks_Controller controller, MouseEvent event) {
    Objects.requireNonNull(controller);
    Objects.requireNonNull(event);
    checkState(event);
    actionSuite.actPressed(controller, event);
  }

  @Override
  public void mousePressedOutsideAction(CityOfPeaks_Controller controller, MouseEvent event) {
    Objects.requireNonNull(controller);
    Objects.requireNonNull(event);
    actionSuite.actPressedOutside(controller, event);
  }

  @Override
  public abstract void removeHoveredStates();

  @Override
  public Dimension getDimension() {
    return new Dimension(w, h);
  }

  /**
   * By default, this will check the state, but DO NOTHING.
   * @param controller
   * @param event
   */
  @Override
  public void mouseDraggedAction(CityOfPeaks_Controller controller, MouseEvent event) {
    // DO NOTHING
  }

  /**
   * By default, this will check the state, but DO NOTHING
   * @param event mouse event
   * @param controller
   */
  @Override
  public void mouseScrollAction(MouseEvent event, CityOfPeaks_Controller controller) {
    checkState(event);
    // DO NOTHING
  }

  /**
   * By default, this will check the state, but DO NOTHING
   * @param event mouse event
   * @param controller
   */
  @Override
  public void mouseReleasedAction(CityOfPeaks_Controller controller, MouseEvent event) {
    // DO NOTHING
  }

  @Override
  public void checkState(MouseEvent event) {
    if (!isMouseOver(event.getX(), event.getY())) {
      throw new IllegalStateException("You didn't check for this RectInteractable's state before activation!");
    }
  }

  @Override
  public boolean getFocused() {
    // DEFAULT RETURN FALSE
    return false;
  }

  @Override
  public void bindAction(ActionSuite a) {
    this.actionSuite = a;
  }

  @Override
  public ActionSuite getActionSuite() {
    return this.actionSuite;
  }

  private String helpText = "";

  @Override
  public void setHelpText(String helpText) {
    this.helpText = helpText;
  }

  @Override
  public void pushHelpTextToController(CityOfPeaks_Controller controller, Point mousePosition) {
    controller.setHelperText(helpText);
  }

}
