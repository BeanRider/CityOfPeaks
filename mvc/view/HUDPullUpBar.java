package mvc.view;

import mvc.controller.CityOfPeaks_Controller;
import mvc.model.CityOfPeaks_DataModel;

import java.awt.*;
import java.util.Objects;

import mvc.view.ui.Interactable;
import mvc.view.ui.Resizer;
import mvc.view.ui.TimeLabel;
import mvc.view.ui.UIAction.Action;
import processing.core.PImage;
import processing.event.*;
import mvc.view.ui.UIAction.ActionSuite;

/**
 * Contains UI, interaction, and rendering logic of the bottom HUD bar:
 * 1. Pause/Play Button
 * 2. Display the current time
 */
public class HUDPullUpBar implements Interactable {

  private Dimension nativeWH;

  private VisualBarGroup visualBarGroup;
  private Resizer dragHandle;
  private TimeLabel timeline;

  private Interactable[] interactables;

  private final int TIMELINE_HEIGHT = 20;
  private boolean isExtended = false;
  private int startDrag = 0;
  private int yDecreasion;

  /**
   * Invariant: cornerXY + timeline CANNOT > nativeWH.height
   */
  private Point cornerXY;

  public HUDPullUpBar(VisualBarGroup visualBarGroup, Dimension parentWidthHeight, PImage resizeIcon, PImage arrowUp, PImage arrowDown) {
    nativeWH = parentWidthHeight;
    cornerXY = new Point(0, nativeWH.height - TIMELINE_HEIGHT);

    this.visualBarGroup = visualBarGroup;
    this.visualBarGroup.setCornerXYZ(cornerXY.x, cornerXY.y + TIMELINE_HEIGHT, 0);

    interactables = new Interactable[2];

    dragHandle = new Resizer(
            resizeIcon,
            new Point(nativeWH.width - 25 - 16, nativeWH.height - TIMELINE_HEIGHT - 41),
            30, 30);
    dragHandle.setUpOnlyIcon(arrowUp);
    dragHandle.setDownOnlyIcon(arrowDown);
    dragHandle.setHelpText("Reveal sensor graphs");

    ActionSuite eAction = new ActionSuite();
    dragHandle.bindAction(eAction);
    eAction.setDraggedAction(new Action() {
      @Override
      public void act(CityOfPeaks_Controller controller, MouseEvent e) {
        // 1) Move back to position at last distance of drag (yDecreasion)
        addCornerXY(0, -yDecreasion);
        visualBarGroup.addViewPortMaxY(yDecreasion);

        // 2) Calculate the new distance
        yDecreasion = e.getY() - startDrag;
        if (cornerXY.y + yDecreasion >= nativeWH.height - TIMELINE_HEIGHT) {
          yDecreasion = nativeWH.height - TIMELINE_HEIGHT - cornerXY.y; // distance from current-y to the max-y-limit.
          dragHandle.setUpOnlyState();
        } else if (cornerXY.y + yDecreasion <= 10) { // min y value
          yDecreasion = cornerXY.y;
          dragHandle.setDownOnlyState();
        } else {
          dragHandle.setBetweenState();
        }

        // 3) Add the new distance back to this bar, and the viewport limits
        addCornerXY(0, yDecreasion);
        visualBarGroup.addViewPortMaxY(-yDecreasion);
      }
    });

    eAction.setPressedAction(new Action() {
      @Override
      public void act(CityOfPeaks_Controller controller, MouseEvent e) {
        startDrag = e.getY();
      }
    });

    eAction.setReleasedAction(new Action() {
      @Override
      public void act(CityOfPeaks_Controller controller, MouseEvent e) {
        yDecreasion = 0;
      }
    });
    dragHandle.setDebugMode(false);

    timeline = new TimeLabel(nativeWH);
    timeline.setHelpText("Jump to another point in time");

    interactables[0] = dragHandle;
    interactables[1] = timeline;
  }

  @Override
  public void setCornerXY(int newX, int newY) {
    cornerXY = new Point(newX, newY);
    visualBarGroup.setCornerXYZ(newX, newY, 0);
    for (Interactable s : interactables) {
      s.setCornerXY((int) s.getCornerXY().getX(), newY);
    }
  }

  @Override
  public void addCornerXY(int addedX, int addedY) {

    int xCom = addedX;
    int yCom = addedY;

    cornerXY = new Point(cornerXY.x + xCom, cornerXY.y + yCom);
    visualBarGroup.addCornerXYZ(xCom, yCom, 0);
    for (Interactable s: interactables) {
      s.addCornerXY(xCom, yCom);
    }
  }

  @Override
  public Point getCornerXY() {
    return cornerXY;
  }


  @Override
  public void render(CityOfPeaks_View parentView, CityOfPeaks_DataModel dataModel) {
    Objects.requireNonNull(parentView);

    // Sum energy graph: draw if less than half of nativeWH.height, and progressively decrease opacity as it approaches the limit.
    float sumEnergyGraphDisplayLimit = nativeWH.height / 2f;
    float sumEnergyGraphDisplayRange = nativeWH.height - sumEnergyGraphDisplayLimit;
    // drawn with bottom of graph directly overlapping the timeline.
    float percentOpacity = (cornerXY.y - sumEnergyGraphDisplayLimit) / (sumEnergyGraphDisplayRange);
    if (percentOpacity < 0) {
      percentOpacity = 0;
    }

    // Visual bar group: draw only if the content's height > 0.
    if (visualBarGroup.getDimension().height > 0) {
      visualBarGroup.draw(parentView, dataModel);
    }

    // Time Label
    parentView.pushStyle();
    parentView.tint(255, 255 * (1 - percentOpacity * 1.5f));
    timeline.render(parentView, dataModel);
    parentView.popStyle();

    // Resize Handle
    dragHandle.render(parentView, dataModel);
  }

  // ==========
  // USER INPUT
  // ==========

  @Override
  public boolean isMouseOver(float mX, float mY) {

    for (Interactable i : interactables) {
      if (i.isMouseOver(mX, mY)) {
        return true;
      }
    }

    return cornerXY.x <= mX && mX <= cornerXY.x + nativeWH.width
            && cornerXY.y <= mY && mY <= cornerXY.y + nativeWH.height;
  }

  @Override
  public void mouseHoverAction(CityOfPeaks_Controller controller, MouseEvent event) {

    checkState(event);
    // DO NOTHING FOR THIS

    pushHelpTextToController(controller, new Point(event.getX(), event.getY()));

    // Delegate
    for (Interactable i : interactables) {
      if (i.isMouseOver(event.getX(), event.getY())) {
        i.mouseHoverAction(controller, event);
        return;
      }
    }
  }

  @Override
  public void mouseScrollAction(MouseEvent event, CityOfPeaks_Controller controller) {

    checkState(event);

    // Scroll it
    if (visualBarGroup.getDimension().height > 0) {
      this.visualBarGroup.scroll(event.getCount());
    }

    // Delegate
    for (Interactable i : interactables) {
      if (i.isMouseOver(event.getX(), event.getY())) {
        i.mouseScrollAction(event, controller);
        return;
      }
    }
  }

  @Override
  public void mousePressedAction(CityOfPeaks_Controller controller, MouseEvent event) {

    checkState(event);
    // DO NOTHING FOR THIS

    // Delegate
    boolean hasActivatedOne = false; // this helps make sure that only one is activated, and the rest is deactivated.
    for (Interactable s : interactables) {
      if (s.isMouseOver(event.getX(), event.getY()) && !hasActivatedOne) {
        s.mousePressedAction(controller, event);
        hasActivatedOne = true;
      } else {
        s.mousePressedOutsideAction(controller, event);
      }
    }
  }

  @Override
  public void mousePressedOutsideAction(CityOfPeaks_Controller controller, MouseEvent e) {
    Objects.requireNonNull(controller);
    Objects.requireNonNull(e);

    // DO NOTHING FOR THIS

    // Delegate
    for (Interactable i : interactables) {
      i.mousePressedOutsideAction(controller, e);
    }
  }

  @Override
  public void mouseDraggedAction(CityOfPeaks_Controller controller, MouseEvent event) {
    // DO NOTHING FOR THIS

    // Delegate
    for (Interactable s : interactables) {
      if (s.getFocused()) {
        s.mouseDraggedAction(controller, event);
        return;
      }
    }
  }

  /**
   * This does not care if mouse if over when mouse is released, it will make every child lose its focus
   * @param controller
   * @param event
   */
  @Override
  public void mouseReleasedAction(CityOfPeaks_Controller controller, MouseEvent event) {
    // DO NOTHING FOR THIS

    // Delegate
    for (Interactable s : interactables) {
      s.mouseReleasedAction(controller, event);
    }
  }

  @Override
  public boolean getFocused() {
    // DEFAULT to false.
    boolean focused = false;
    for (Interactable i : interactables) {
      focused = focused || i.getFocused();
    }
    return focused;
  }

  @Override
  public void removeHoveredStates() {
    for (Interactable s : interactables) {
      s.removeHoveredStates();
    }
  }

  @Override
  public Dimension getDimension() {
    return new Dimension(nativeWH.width, TIMELINE_HEIGHT + visualBarGroup.getDimension().height);
  }

  @Override
  public void checkState(MouseEvent e) {
    if (!isMouseOver(e.getX(), e.getY())) {
      throw new IllegalStateException("You didn't check for HUDPullUpBar's state before activation!");
    }
  }

  ActionSuite actionSuite = ActionSuite.DEFAULT;
  @Override
  public void bindAction(ActionSuite a) {
    this.actionSuite = a;
  }

  @Override
  public ActionSuite getActionSuite() {
    return actionSuite;
  }

  private String helpText = "";

  @Override
  public void setHelpText(String helpText) {
    this.helpText = helpText;
  }

  @Override
  public void pushHelpTextToController(CityOfPeaks_Controller controller, Point mousePosition) {
    controller.setHelperText(helpText);

    // Delegate
    for (Interactable i : interactables) {
      if (i.isMouseOver(mousePosition.x, mousePosition.y)) {
        i.mouseHoverAction(controller, new MouseEvent(null, 0, 0, 0, mousePosition.x, mousePosition.y, 0, 0));
        return;
      }
    }
  }

  @Override
  public void resetToDefault() {
    addCornerXY(0, nativeWH.height - TIMELINE_HEIGHT - cornerXY.y);
    visualBarGroup.addViewPortMaxY(-2000);
    visualBarGroup.scroll(100000);
    dragHandle.setUpOnlyState();
  }
}

//    parentView.pushStyle();

// HUD Timepiece START
//    parentView.stroke(127, 127, 127, 100);
//    parentView.strokeWeight(1);
//    parentView.noFill();
//    parentView.ellipseMode(CENTER);
//    int scaleDown = 1;
//    int timepieceRadius = 55 / scaleDown;
//    int timepieceDiameter = 110 / scaleDown;
//
//    parentView.pushMatrix();
//    parentView.translate(nativeWH.width / 2, nativeWH.height - 110);
//    parentView.ellipse(0, 0, timepieceDiameter, timepieceDiameter);
//
//    // hour label
//    parentView.textFont(parentView.FCODA_25, 25 / scaleDown);
//    parentView.textAlign(CENTER, CENTER);
//    DateTime today = new DateTime(dataModel.getTimeline().getCurUnix() * 1000L);
//    String hour = String.format("%02d", today.getHourOfDay());
//    parentView.fill(62, 199, 230);
//    parentView.text(hour, 0, 0);
//
//    // arcs
//    int numArcs = 12;
//    int spaceBetweenRect = 1; // pixels
//    int innerRadius = 34 / scaleDown;
//    int outerRadius = 46 / scaleDown;
//    float innerCircum = (float) (Math.PI * innerRadius * 2);
//    float outerCircum = (float) (Math.PI * outerRadius * 2);
//
//    float innerAngleForSpacing = spaceBetweenRect / (float) innerRadius; // radians
//    float outerAngleForSpacing = spaceBetweenRect / (float) outerRadius; // radians
//    float innerAngleForArc = (innerCircum - spaceBetweenRect * numArcs * 2) / (float) innerRadius / (float) numArcs; // radians
//    float outerAngleForArc = (outerCircum - spaceBetweenRect * numArcs * 2) / (float) outerRadius / (float) numArcs; // radians
//
//    PShape singleArc = parentView.createShape();
//    singleArc.beginShape();
//    singleArc.fill(200, 200, 200, 50);
//    singleArc.stroke(127, 127, 127, 100);
//    singleArc.strokeWeight(1);
//    // inner left
//    singleArc.vertex(
//            innerRadius * PApplet.cos(innerAngleForSpacing),
//            -innerRadius * PApplet.sin(innerAngleForSpacing));
////    System.out.println(innerRadius * PApplet.cos(innerAngleForSpacing));
////    System.out.println(-innerRadius * PApplet.sin(innerAngleForSpacing));
//
//    // outer left
//    singleArc.vertex(
//            outerRadius * PApplet.cos(outerAngleForSpacing),
//            -outerRadius * PApplet.sin(outerAngleForSpacing));
//
//    // TODO TOP ARC
//    int subdivisions = 60;
//    for (int i = 0; i < subdivisions; ++i) {
//      float curAngle = (outerAngleForSpacing + (float) (i + 1) * ((outerAngleForArc - outerAngleForSpacing) / (float) subdivisions));
//      singleArc.vertex(
//              outerRadius * PApplet.cos(curAngle),
//              -outerRadius * PApplet.sin(curAngle));
//    }
//
//    // outer right
//    singleArc.vertex(
//            outerRadius * PApplet.cos(outerAngleForSpacing + outerAngleForArc),
//            -outerRadius * PApplet.sin(outerAngleForSpacing + outerAngleForArc));
//
//    // inner right
//    singleArc.vertex(
//            innerRadius * PApplet.cos(innerAngleForSpacing + innerAngleForArc),
//            -innerRadius * PApplet.sin(innerAngleForSpacing + innerAngleForArc));
//
//    // TODO BOTTOM ARC
//    for (int i = 0; i < subdivisions; ++i) {
//      float curAngle = innerAngleForSpacing + innerAngleForArc - (float) (i + 1) * ((innerAngleForArc - innerAngleForSpacing) / (float) subdivisions);
//      singleArc.vertex(
//              innerRadius * PApplet.cos(curAngle),
//              -innerRadius * PApplet.sin(curAngle));
//    }
//
//    // inner left (end)
//    singleArc.vertex(
//            innerRadius * parentView.cos(innerAngleForSpacing),
//            -innerRadius * parentView.sin(innerAngleForSpacing));
//
//    singleArc.endShape();
//
//    parentView.rotate(PApplet.radians(-90));
//    parentView.shapeMode(CORNER);
//    for (int i = 0; i < numArcs; ++i) {
//      parentView.rotate(PApplet.radians(360 / numArcs));
//      if (i < ((dataModel.getDayPercentage() * 24f * 60f) % 60f) / 5f)
//        singleArc.setFill(parentView.color(62, 199, 230));
//      else
//        singleArc.setFill(parentView.color(200, 200, 200, 50));
//      parentView.shape(singleArc);
//    }
// HUD Timepiece END
//    parentView.popMatrix();