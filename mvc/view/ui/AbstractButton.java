package mvc.view.ui;

import java.awt.*;
import java.util.Objects;

import mvc.controller.CityOfPeaks_Controller;
import mvc.model.CityOfPeaks_DataModel;
import processing.event.MouseEvent;
import mvc.view.CityOfPeaks_View;
import mvc.view.ui.UIAction.ActionSuite;

/**
 * Represents a root structure and states of a Button
 */
public abstract class AbstractButton implements Interactable {

  protected boolean isFocused = false;
  protected Point cornerXY;
  protected int width, height;

  protected ActionSuite actionSuite = ActionSuite.DEFAULT;

  public AbstractButton(int xLoc, int yLoc, int w, int h) {
    width = w;
    height = h;
    setCornerXY(xLoc, yLoc);
  }

  @Override
  public abstract void render(CityOfPeaks_View parentView, CityOfPeaks_DataModel dataModel);

  @Override
  public void setCornerXY(int newX, int newY) {
    this.cornerXY = new Point(newX, newY);
  }

  @Override
  public void addCornerXY(int addedX, int addedY) {
    this.cornerXY = new Point(cornerXY.x + addedX, cornerXY.y + addedY);
  }

  @Override
  public Point getCornerXY() {
    return cornerXY;
  }

  /**
   * Mouse detection based on RECTANGLE bounds
   */
  @Override
  public boolean isMouseOver(float mX, float mY) {
    if (mX >= cornerXY.getX() && mX <= cornerXY.getX() + width
            && mY >= cornerXY.getY() && mY <= cornerXY.getY() + height) {
      return true;
    }
    return false;
  }

  @Override
  public void removeHoveredStates() {
    setState(ButtonState.STATIC);
  }

  @Override
  public void checkState(MouseEvent event) {
    if (isFocused) {
      return;
    }

    if (!isMouseOver(event.getX(), event.getY())) {
      throw new IllegalStateException("You didn't check for this AbstractButton's state before activation!");
    }
  }

  @Override
  public void mouseHoverAction(CityOfPeaks_Controller controller, MouseEvent event) {
    checkState(event);
    actionSuite.actHovered(controller, event);
    pushHelpTextToController(controller, new Point(event.getX(), event.getY()));
    setState(ButtonState.ACTIVE);
  }

  @Override
  public void mousePressedAction(CityOfPeaks_Controller controller, MouseEvent event) {
    Objects.requireNonNull(controller);
    Objects.requireNonNull(event);
    checkState(event);

    actionSuite.actPressed(controller, event);
    setState(ButtonState.ACTIVE);
    isFocused = true;
  }

  @Override
  public void mousePressedOutsideAction(CityOfPeaks_Controller controller, MouseEvent event) {
    Objects.requireNonNull(controller);
    Objects.requireNonNull(event);
    actionSuite.actPressedOutside(controller, event);
    setState(ButtonState.STATIC);
    isFocused = false;
  }

  /**
   * By default, this will check the state, but DO NOTHING.
   * @param event
   * @param controller
   */
  @Override
  public void mouseScrollAction(MouseEvent event, CityOfPeaks_Controller controller) {
    Objects.requireNonNull(controller);
    Objects.requireNonNull(event);
    checkState(event);
    actionSuite.actScrolled(controller, event);
  }

  /**
   * By default, this will check the state, but DO NOTHING.
   * @param controller
   * @param event
   */
  @Override
  public void mouseDraggedAction(CityOfPeaks_Controller controller, MouseEvent event) {
    Objects.requireNonNull(controller);
    Objects.requireNonNull(event);
    checkState(event);
    actionSuite.actDragged(controller, event);
  }

  /**
   * By default, this will activate the released action, regardless of states.
   * DEPRECATED: If however, the mouse is on this button at release, AND that this has the focus, then the main action will activate
   * NEW : if this has the focus, then the released action will activate. If a button was pressed, it will have focus,
   *      when it is being dragged around, the element will move, or not depending on the type of button.
   *
   *       A normal button will activate its main logic IF the mouse is also over.
   *       A dragger button does not actually have a main logic, and instead is activated based on mouse movement.
   *
   * @param controller
   * @param event
   */
  @Override
  public void mouseReleasedAction(CityOfPeaks_Controller controller, MouseEvent event) {
    Objects.requireNonNull(controller);
    Objects.requireNonNull(event);
//    checkState(event);


    // Old logic was : isFocused && isMouseOver(event.getX(), event.getY())
    if (isFocused) {
      actionSuite.actReleased(controller, event);
    } else {
      isFocused = false;
    }
  }

  @Override
  public boolean getFocused() {
    return isFocused;
  }

  @Override
  public void bindAction(ActionSuite a) {
    this.actionSuite = a;
  }

  @Override
  public ActionSuite getActionSuite() {
    return actionSuite;
  }

  @Override
  public Dimension getDimension() {
    return new Dimension(width, height);
  }

  @Override
  public String toString() {
    return "AbstractButton [" +
            cornerXY.toString() + ", width = " +
            width + ", height = " +
            height + " ]";
  }

  public boolean isToggled() {
    return isToggled;
  }


  public enum ButtonState {
    STATIC, ROLLOVER, ACTIVE
  }
  protected ButtonState state = ButtonState.STATIC;
  protected Boolean isToggled = false;
  public void setState(ButtonState newState) {
    state = newState;
  }

  public void setIsToggled(boolean t) {
    isToggled = t;
  }

  public void toggle() {
    isToggled = !isToggled;
  }

  protected boolean debugMode = false;

  public void setDebugMode(boolean d) {
    this.debugMode = d;
  }

  protected String helpText = ""; // Default is empty String

  @Override
  public void setHelpText(String helpText) {
    this.helpText = helpText;
  }

  @Override
  public void pushHelpTextToController(CityOfPeaks_Controller c, Point mousePosition) {
    c.setHelperText(helpText);
  }

  @Override
  public void resetToDefault() {
    // DO NOTHING
  }

}

