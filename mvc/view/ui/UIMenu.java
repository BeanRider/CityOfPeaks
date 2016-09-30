package mvc.view.ui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import mvc.controller.CityOfPeaks_Controller;
import mvc.model.CityOfPeaks_DataModel;
import mvc.view.ui.UIAction.Action;
import processing.event.MouseEvent;
import mvc.view.CityOfPeaks_View;
import processing.core.PFont;
import processing.core.PImage;
import processing.core.PShape;
import mvc.view.ui.UIAction.ActionSuite;

import static processing.core.PConstants.*;

/**
 * An expandable menu consisting of:
 * an expander-button,
 * a uniformly-dimensioned menu with selectable items,
 */
public class UIMenu implements Interactable {

  private int x, y;
  private Action e;

  /**
   * Expander
   */
  private final float expanderWidth = 35;
  private boolean isExpanded = false;
  private boolean isOnExpander = false;

  /**
   * Menu Options
   */
  private final float rowHeight = 40, rowWidth = 190;
  private final float padding = 3;

  private List<MenuItemPair> items = new ArrayList<>();
  private int hoveredData = -1, selectedData = -1;

  /**
   * [INVARIANT]: if expanded: {@code totalWidth} = {@code expanderWidth} + {@code rowWidth}
   *                    else : {@code totalWidth} = {@code expanderWidth}
   * [INVARIANT]: if expanded: {@code totalHeight} = {@code rowHeight} * {@code items.size()} + {@code padding}
   *                    else : {@code totalHeight} = {@code rowHeight} * {@code items.size()} + {@code padding}
   */
  private float totalWidth = expanderWidth;
  private float totalHeight = rowHeight * items.size() + padding;

  /**
   * Graphical fields
   */
  private PShape background;
  private PFont font;

  public UIMenu(int x, int y, PFont font) {
    this.x = x;
    this.y = y;
    this.font = font;
  }

  /**
   * Adds the given {@code MenuItemPair} to the list of menu options {@code items}
   * @param newPair must not be null
   */
  public void addMenuItemPair(MenuItemPair newPair) {
    Objects.requireNonNull(newPair);
    items.add(newPair);
    this.totalHeight = rowHeight * items.size() + padding;
  }

  @Override
  public void setCornerXY(int newX, int newY) {
    this.x = newX;
    this.y = newY;
  }

  @Override
  public Point getCornerXY() {
    return new Point(x, y);
  }

  @Override
  public void addCornerXY(int addedX, int addedY) {
    x += addedX;
    y += addedY;
  }

  /**
   * Contains draw logic; renders onto given parent
   * @param parentView must not be null
   * @param dataModel
   */
  @Override
  public void render(CityOfPeaks_View parentView, CityOfPeaks_DataModel dataModel) {
    Objects.requireNonNull(parentView);
    // 1. Draw background
    PImage behindMenu = parentView.get().get(
            Math.round(x), Math.round(y), Math.round(totalWidth), Math.round(totalHeight));
    behindMenu.filter(BLUR, 5);
    parentView.pushStyle();
    parentView.imageMode(CORNER);
    parentView.image(behindMenu, x, y);
    parentView.fill(255, 200);
    parentView.noStroke();
    parentView.rectMode(CORNER);
    parentView.rect(x, y, totalWidth, totalHeight, 4);

    // 2. Draw expander
    parentView.textAlign(CENTER, CENTER);
    parentView.textFont(font, 14);
    if (isOnExpander) parentView.fill(10);
    else parentView.fill(55);
    parentView.pushMatrix();
    parentView.translate(x + expanderWidth / 2, y + totalHeight / 2);
    parentView.rotate(-HALF_PI);
    parentView.translate(-(x + expanderWidth / 2), -(y + totalHeight / 2));
    parentView.text("Sort", x + expanderWidth / 2 , y + totalHeight / 2);
    parentView.popMatrix();

    if (isExpanded) {
      parentView.strokeWeight(1);
      parentView.stroke(200);
      parentView.line(x + expanderWidth, y,
              x + expanderWidth, y + totalHeight - 1);
    }

    // 3. Draw items
    if (isExpanded) {
      for (int i = 0; i < items.size(); ++i) {
        MenuItemPair pair = items.get(i);
        float midRowY = y + rowHeight / 2 + i * rowHeight;
        parentView.textAlign(CENTER, CENTER);
        if (hoveredData == i) {
          parentView.fill(17, 91, 226, 200);
          parentView.noStroke();
          parentView.rectMode(CORNER);
          parentView.rect(x + expanderWidth + padding, y + rowHeight * i + padding,
                  rowWidth - padding * 2, rowHeight - padding, 3);

          parentView.println(hoveredData);

          parentView.imageMode(CENTER);
          parentView.image(pair.getHoverIcon(), x + expanderWidth + padding + 20, midRowY);
          parentView.fill(255);
          parentView.textAlign(LEFT, CENTER);
          parentView.textFont(font, 14);
          parentView.text(pair.getTitle(), x + expanderWidth + padding + 40, midRowY);
        } else {
          parentView.imageMode(CENTER);
          parentView.image(pair.getIcon(), x + expanderWidth + padding + 20, midRowY);
          parentView.textFont(font, 14);
          parentView.fill(0);
          parentView.textAlign(LEFT, CENTER);
          parentView.text(pair.getTitle(), x + expanderWidth + padding + 40, midRowY);
        }
      }
    }

    // 4. Draw hover
    if (isOnExpander) {
      // TODO Draw expander highlight
    }
    parentView.popStyle();
  }

  /**
   * True if mouse is within totalwidth and totalheight
   */
  @Override
  public boolean isMouseOver(float mX, float mY) {
    if (mX < this.x || mX > this.x + totalWidth
            || mY < this.y || mY > this.y + totalHeight) {
      return false;
    }
    return true;
  }

  /**
   * Call when hovered on (assuming mouse is moved on, no clicks)
   * @param controller the hovered parent
   * @param event
   */
  @Override
  public void mouseHoverAction(CityOfPeaks_Controller controller, MouseEvent event) {
//    Objects.requireNonNull(controller);
//    if (!isMouseOver(event.getX(), event.getY())) {
//      removeHoveredStates();
//      return;
//    } else if (isExpanded) {
//      if (!controller.isPlaying()) {
//        controller.loop();
//      }
//      if (controller.mouseX < this.x + this.expanderWidth && controller.mouseY < this.y + this.totalHeight) {
//        // On the expander
//        isOnExpander = true;
//        hoveredData = -1;
//      } else {
//        // On one of the items
//        isOnExpander = false;
//        hoveredData = (int) (Math.floor((controller.mouseY - y) / rowHeight));
//        if (!isWithinRange(hoveredData)) {
//          hoveredData = -1;
//        }
//      }
//    } else {
//      if (!controller.isLooping()) {
//        controller.loop();
//      }
//      // Update
//      isOnExpander = true;
//    }
  }

  @Override
  public void mouseScrollAction(MouseEvent event, CityOfPeaks_Controller controller) {
    // DO NOTHING
  }

  /**
   * Call when pressed on (assuming mouse has clicked and hoveredEvent called before)
   * @param controller
   * @param event
   */
  @Override
  public void mousePressedAction(CityOfPeaks_Controller controller, MouseEvent event) {
    Objects.requireNonNull(controller);
    checkState(event);
    if (isOnExpander) {
      isExpanded = !isExpanded;
      if (isExpanded) {
        totalWidth = expanderWidth + rowWidth;
        totalHeight = rowHeight * items.size() + padding;
      } else {
        totalWidth = expanderWidth;
        totalHeight = rowHeight * items.size() + padding;
      }
    } else {
      if (isExpanded && isWithinRange(hoveredData)) {
        selectedData = hoveredData;
//        items.get(selectedData).act(controller);
        this.isExpanded = false;
        removeHoveredStates();
        totalWidth = expanderWidth;
        totalHeight = rowHeight * items.size() + padding;
      }
    }
  }

  @Override
  public void mousePressedOutsideAction(CityOfPeaks_Controller controller, MouseEvent e) {
    // DO NOTHING
  }

  @Override
  public void mouseDraggedAction(CityOfPeaks_Controller controller, MouseEvent event) {
    // DO NOTHING
  }

  @Override
  public void mouseReleasedAction(CityOfPeaks_Controller controller, MouseEvent event) {
    // DO NOTHING
  }

  @Override
  public boolean getFocused() {
    return false;
  }

  @Override
  public Dimension getDimension() {
    return new Dimension((int) Math.ceil(totalWidth),(int) Math.ceil(totalHeight));
  }


  @Override
  public void removeHoveredStates() {
    this.hoveredData = -1;
    this.isOnExpander = false;
  }

  private boolean isWithinRange(int index) {
    return index >= 0 && index < items.size();
  }

  @Override
  public void checkState(MouseEvent event) {
    if (!isMouseOver(event.getX(), event.getY())) {
      throw new IllegalStateException("You didn't check for this UIMenu's state before activation!");
    }
  }

  ActionSuite actionSuite = ActionSuite.DEFAULT;
  @Override
  public void bindAction(ActionSuite a) {
    actionSuite = a;
  }

  @Override
  public ActionSuite getActionSuite() {
    return actionSuite;
  }

  @Override
  public void setHelpText(String helpText) {
    // DO NOTHING
  }

  @Override
  public void pushHelpTextToController(CityOfPeaks_Controller c, Point mousePosition) {
    // DO NOTHING
  }

  @Override
  public void resetToDefault() {
    // DO NOTHING
  }

}
