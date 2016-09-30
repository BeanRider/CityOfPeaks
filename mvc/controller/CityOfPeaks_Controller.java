package mvc.controller;

import mvc.model.Building;
import mvc.model.datasource.Moment;
import mvc.model.time.IntervalType;
import mvc.model.time.TimeRangeUtil;
import mvc.view.TimedUIAction;
import mvc.view.ui.UIAction.Action;
import processing.core.PApplet;
import processing.event.MouseEvent;
import processing.event.KeyEvent;

import mvc.model.CityOfPeaks_DataModel;
import mvc.model.CityOfPeaks_Model;
import mvc.model.IEnergyVizModel;
import mvc.view.CityOfPeaks_View;
import mvc.view.IView;
import mvc.view.ui.Interactable;

import java.awt.*;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

/**
 * The contains controller logic of the entire program:
 * Role: modifies the businessModel, accepts user inputs from the view, and updates the viewmodel
 * Position: within the view class
 */
public class CityOfPeaks_Controller implements IController {

  CityOfPeaks_View view = null;
  CityOfPeaks_Model businessModel = null;

  TimedUIAction autoCameraTimer;
  private EventDrivenCamera camera;
  ControllerState controllerState = ControllerState.AUTO_CAMERA;

  public void setCameraSpin(boolean on) {
    if (on) {
      camera.beginSpinCamera();
    } else {
      camera.beginManualCamera();
    }
  }

  private enum ControllerState {
    AUTO_CAMERA, INTERACTION
  }

  private final int FPS = 30;

  public CityOfPeaks_Controller() {
    autoCameraTimer = new TimedUIAction(FPS * 30);
    autoCameraTimer.addCompletionAction(new Action() {
      @Override
      public void act(CityOfPeaks_Controller controller, MouseEvent e) {
        switchToAutoCameraState();
      }
    });
  }

  private void switchToAutoCameraState() {
    controllerState = ControllerState.AUTO_CAMERA;
    camera.beginAutoCamera();
    view.resetAllUI();

    if (!businessModel.isTimeProgressing()) {
      businessModel.togglePlayPause();
      view.playPauseButton.setIsToggled(true);
    }

    setHelperText("Presentation mode: Interact to begin.");
  }

  /**
   * Use this after init() if the view should be swapped
   * @param newView
   */
  public void setView(IView newView) {
    this.view = (CityOfPeaks_View) newView;
    camera = new CityOfPeaks_Camera(view); // TODO REMOVE THIS!
  }

  /**
   * Use this after init() if the model should be swapped
   * @param newModel
   */
  public void setBusinessModel(IEnergyVizModel newModel) {
    this.businessModel = (CityOfPeaks_Model) newModel;
  }

  /**
   * Called by: IView
   * first:   initialize this controller's view,
   * then:    creates & initializes a new instance of businessModel,
   * then:    creates an anonymous new instance of the dataModel,
   * finally: calls the setup method in businessModel
   * @param view
   */
  public void init(IView view) {
    this.view = (CityOfPeaks_View) view;
    this.businessModel = new CityOfPeaks_Model();
    this.view.setDataModel(new CityOfPeaks_DataModel(businessModel));

    // Initializes the businessModel
    businessModel.loadEverything(((CityOfPeaks_View) view).getCanvasSize(), (PApplet) view);

    camera = new CityOfPeaks_Camera(this.view);
  }

  private int tickNum = 0;
  private int tickReset = 1;

  /**
   * Rotates the board every tickReset-th tick
   */
  public void tick() {

    // Camera controls
    switch (controllerState) {
      case AUTO_CAMERA:
        break;
      case INTERACTION:
        // Keep trying to count down until 0.
        autoCameraTimer.tick(this, null);
        break;
    }

    camera.tick();
    view.setHeightFromVisual(camera.getDistanceFromCenter());

    // Check if the current time is over or under good ranges, if so jump back to good range, if auto mode.
    if (controllerState == ControllerState.AUTO_CAMERA && (businessModel.getTimeline().getCurUnix() > 1418101200 || businessModel.getTimeline().getCurUnix() < 1356670800)) {
      businessModel.getTimeline().updateTime(TimeRangeUtil.getUnixInEastern(2013, 1, 1, 0), IntervalType.DAY);
      businessModel.refreshDataAndStats();
      setTimePercentage(0);
      view.redrawGraphs();
    }

    // Time-related controls
    if (tickNum == 0) {
      if (businessModel.isTimeProgressing()) {

        // Search for nearby peaks
        long currentTime = businessModel.getTimeline().getCurUnix();
        Building[] buildings = businessModel.getListOfBuildings();
        List<CityOfPeaks_Model.BuildingStat> peaks = businessModel.getBuildingStatistics();

        boolean slowDown = false;
        for (int bID = 0; bID < businessModel.getListOfBuildings().length; ++bID) {
          Building b = buildings[bID];
          CityOfPeaks_Model.BuildingStat singleBuildingStat = peaks.get(bID);
          Optional<Moment<Float>> singleBuildingPeak = singleBuildingStat.getPeak();

          if (singleBuildingPeak.isPresent()) {
            long peakTime = singleBuildingPeak.get().getTimeAtMoment();
            if ((peakTime > currentTime) && (peakTime - currentTime) < 3600) {
              slowDown = true;
              break;
            }
          }
        }

        // If a peak is upcoming, slow down!
        if (slowDown) {
          businessModel.decrementTimeAcceleration();
          camera.decrementTimeAcceleration();
        } else {
          businessModel.incrementTimeAcceleration();
          camera.incrementTimeAcceleration();
        }

        businessModel.incrementDayPercentage();
        if (businessModel.getDayPercentage() == 0) {
          view.redrawGraphs();
        }
      }
    }
    tickNum = (++tickNum) % tickReset;
  }

  public void setTimePercentage(float newTimePercentage) {
    businessModel.setTimePercentage(newTimePercentage);
  }

  public boolean isPlaying() {
    return businessModel.isTimeProgressing();
  }

  public void togglePlayPause() {
    businessModel.togglePlayPause();
  }

  public void setHelperText(String helperText) {
    view.setHelperText(helperText);
  }

  @Override
  public void parseMouseMoved(MouseEvent e) {

    // Early exit: if presenting, don't allow hover to work
    if (controllerState == ControllerState.AUTO_CAMERA) {
      return;
    }

    boolean onUI = false;
    ListIterator<Interactable> iter = view.getInteractables().listIterator(view.getInteractables().size());
    while (iter.hasPrevious()) {
      Interactable i = iter.previous();
      if (i.isMouseOver(e.getX(), e.getY())) {
        i.mouseHoverAction(this, e);
        onUI = true;
        break;
      } else {
        i.removeHoveredStates();
      }
    }

    if (onUI) {

//      view.setCursor(CityOfPeaks_View.CursorType.CLICKABLE);
    } else if (isDragging) {
      setHelperText("Drag or scroll to orient visual");
//      view.setCursor(CityOfPeaks_View.CursorType.CLOSEDHAND);
    } else {
      setHelperText("Drag or scroll to orient visual");
//      view.setCursor(CityOfPeaks_View.CursorType.OPENHAND);
    }

    // This is needed because somehow processing won't interpret drag after cursor is changed, so now
    // we rely on mouse moved (which works after cursor is changed) to call dragged.
    if (isDragging) {
//      parseMouseDragged(e);
    }
  }

  private void switchToInteractionState() {
    // 1) Set state to interaction
    controllerState = ControllerState.INTERACTION;

    // 2) Stop GLIDE, and start SPIN if time is still progressing.
    if (businessModel.isTimeProgressing()) {
      camera.beginSpinCamera();
    } else {
      camera.beginManualCamera();
    }

    // 3) reset the auto spin timer, and start it.
    autoCameraTimer.restartFromBeginning();
    autoCameraTimer.startCountDown();

    // 4) Update hovered states
    boolean onUI = false;
    ListIterator<Interactable> iter = view.getInteractables().listIterator(view.getInteractables().size());
    while (iter.hasPrevious()) {
      Interactable i = iter.previous();
      if (i.isMouseOver(view.mouseX, view.mouseY)) {
        i.pushHelpTextToController(this, new Point(view.mouseX, view.mouseY));
        onUI = true;
        break;
      } else {
        i.removeHoveredStates();
      }
    }

    if (onUI) {
    } else if (isDragging) {
      setHelperText("Drag or scroll to orient visual");
    } else {
      setHelperText("Drag or scroll to orient visual");
    }
  }

  @Override
  public void parseKeyPressed(KeyEvent e) throws NullPointerException {
    switchToInteractionState();

    switch (e.getKeyCode()) {
      case java.awt.event.KeyEvent.VK_LEFT:
        businessModel.jumpToPrev();
        view.redrawGraphs();  // update visual graphs
        break;
      case java.awt.event.KeyEvent.VK_RIGHT:
        businessModel.jumpToNext();
        view.redrawGraphs();
        break;
      case java.awt.event.KeyEvent.VK_SPACE:
        businessModel.togglePlayPause();
        view.playPauseButton.setIsToggled(!businessModel.isTimeProgressing());
        setCameraSpin(businessModel.isTimeProgressing());
        break;
      case java.awt.event.KeyEvent.VK_S:
        view.toggleSpotlightMode();
        break;
      case java.awt.event.KeyEvent.VK_UP:
        view.increaseSpotlightRadius();
        break;
      case java.awt.event.KeyEvent.VK_DOWN:
        view.decreaseSpotlightRadius();
        break;
      case java.awt.event.KeyEvent.VK_ESCAPE:
        // DO NOTHING
        view.key = 0;
        break;
    }
  }

  /**
   * Re-index the business model data, AND update the view, based on the current time.
   */
  public void refreshData() {
    businessModel.refreshDataAndStats();
    view.redrawGraphs();
  }

  private boolean isDragging = false;
  private Point startDragPoint = new Point(0, 0);
  private Point currentDragPoint = new Point(0, 0);
  private Optional<Float> lastDragAmount = Optional.empty();
  // flipped
//  private Point2D lastTransformation = new Point2D.Float(0, 0);
//  private Point2D lastFinalTransformation = new Point2D.Float(0, 0);

  private Optional<DragDirection> isDraggingHorizontally = Optional.empty();
  public void parseMouseDragged(MouseEvent e) {

    switchToInteractionState();

//    System.out.println("Start Drag");
//    System.out.println(lastDragAmount);
//    isFixedRotatingView = false;

    // First check if any UI is in the way:
    ListIterator<Interactable> iter = view.getInteractables().listIterator(view.getInteractables().size());
    while (iter.hasPrevious()) {
      Interactable i = iter.previous();
      if (i.getFocused()) {
        i.mouseDraggedAction(this, e);
        return;
      }
    }

//    System.out.println("Mouse dragged: tracking camera movement");

    if (isDragging) {

//      System.out.println("Mouse dragged: tracking camera movement");

      currentDragPoint.setLocation(e.getX(), e.getY());

      // Re-calculate only if no direction is there.
      if (!isDraggingHorizontally.isPresent()) {
        isDraggingHorizontally = Optional.of(calcDragDirection(startDragPoint, currentDragPoint));
      }

      // Apply rotations using a copy of the last confirmed matrix...
      switch (isDraggingHorizontally.get()) {
        case VERTICAL:
          // the first call to drag needs to safe-keep the viz orientation.
          if (!lastDragAmount.isPresent()) {
            lastDragAmount = Optional.of(view.getVizXOrientation());
          } else {
            float newDragAmount = (float) -(currentDragPoint.getY() - startDragPoint.getY()) / camera.getDistanceFromCenter();
            view.setVisualOrientation(lastDragAmount.get() + newDragAmount, 0, view.getVizZOrientation());
          }
          break;
        case HORIZONTAL:
          // the first call to drag needs to safe-keep the viz orientation.
          if (!lastDragAmount.isPresent()) {
            lastDragAmount = Optional.of(view.getVizZOrientation());
          } else {
            float newDragAmount = (float) -(currentDragPoint.getX() - startDragPoint.getX()) / camera.getDistanceFromCenter();
            view.setVisualOrientation(view.getVizXOrientation(), 0, lastDragAmount.get() + newDragAmount);
          }
          break;
      }
    }
  }

  private enum DragDirection {
    HORIZONTAL, VERTICAL
  }

  // Helper method for calculating the drag direction
  private DragDirection calcDragDirection(Point startDragPoint, Point currentDragPoint) {
    if (Math.abs(currentDragPoint.getY() - startDragPoint.getY()) > Math.abs(currentDragPoint.getX() - startDragPoint.getX())) {
//      System.out.println("Determined to be vertical!");
      return DragDirection.VERTICAL;
    }
//    System.out.println("Determined to be horizontal!");
    return DragDirection.HORIZONTAL;
  }

  public void parseMousePressed(MouseEvent e) {

    switchToInteractionState();

//    System.out.println("Start: parseMousePressed");

    // Mouse over check
    boolean hasActivatedOne = false;
    boolean wasPressedOnCalendar = false;
    ListIterator<Interactable> iter = view.getInteractables().listIterator(view.getInteractables().size());
    while (iter.hasPrevious()) {
      Interactable i = iter.previous();
      if (i.isMouseOver(e.getX(), e.getY()) && !hasActivatedOne) {
//        System.out.println(i.toString()+ ": Pressed inside");
        i.mousePressedAction(this, e);
        hasActivatedOne = true;
      } else {
//        System.out.println(i.toString() + ": Pressed outside");
        i.mousePressedOutsideAction(this, e);
      }
    }

    // Skip init drag if one of them was clicked on top
    if (hasActivatedOne) {
      return;
    }

    // Init drag: copy position
    isDragging = true;
    startDragPoint.setLocation(e.getX(), e.getY());
    currentDragPoint.setLocation(e.getX(), e.getY());

//    view.setCursor(CityOfPeaks_View.CursorType.CLOSEDHAND);
//    System.out.println("Pressed, dragging == " + isDragging);
  }

  // Released is kind of weird, it will prioritize release of drag before all elements
  public void parseMouseReleased(MouseEvent e) {
//    System.out.println("\n");
//    System.out.println("Mouse Released!");
    isDragging = false;
    isDraggingHorizontally = Optional.empty();
    lastDragAmount = Optional.empty();

    ListIterator<Interactable> iter = view.getInteractables().listIterator(view.getInteractables().size());

    // Mouse released do not need to check for mouse position before action.
    // Buttons will become normal, and focus will disappear
    while (iter.hasPrevious()) {
      Interactable i = iter.previous();
      i.mouseReleasedAction(this, e);
//      view.setCursor(CityOfPeaks_View.CursorType.NORMAL);
//      printDragState();
    }

//    view.setCursor(CityOfPeaks_View.CursorType.OPENHAND);

  }

  // Testing code
  private void printDragState() {
    System.out.println("Drag State ==============");
    System.out.println("isDragging == " + isDragging);
    System.out.println("=========================");
  }

  public void parseMouseWheelEvent(MouseEvent event) {

    switchToInteractionState();

    for (Interactable i : view.getInteractables()) {
      if (i.isMouseOver(event.getX(), event.getY())) {
        i.mouseScrollAction(event, this);
        return;
      }
    }

    camera.zoomBy(event.getCount() * 4);
    view.setHeightFromVisual(camera.getDistanceFromCenter());
  }

  /**
   *
   * @param newTime unix time (seconds)
   */
  public void setTime(long newTime) {
    TimeRangeUtil timeline = businessModel.getTimeline();
    timeline.updateTime(newTime, timeline.getIntervalType());
  }
}
