package mvc.view;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import mvc.controller.CityOfPeaks_Controller;
import mvc.model.datasource.Moment;
import mvc.model.datasource.NumberDS;
import mvc.view.ui.CalendarModule;
import mvc.view.ui.ImageButton;
import mvc.view.ui.Interactable;
import mvc.view.ui.UIMenu;
import mvc.model.*;
import mvc.view.interpolator.BezierInterpolator;
import mvc.view.interpolator.SpringInterpolator;
import mvc.view.ui.UIAction.Action;
import mvc.view.ui.UIAction.ActionSuite;
import mvc.view.visual.BaseTransformableVisual;
import org.joda.time.DateTime;
import processing.core.*;
import processing.event.*;
import mvc.model.time.TimeRangeUtil;

/**
 * The main PApplet base class, contains view logic of the entire program.
 */
public class CityOfPeaks_View extends PApplet implements IView {

  // MVC
  protected CityOfPeaks_Controller controller = null;
  private CityOfPeaks_DataModel dataModel = null;
  private float cameraYRot;

  /**
   * Entry point to setting up MVC.
   */
  public void setupMVC() {
    controller = new CityOfPeaks_Controller();
    controller.init(this);
    loadState = LoadState.FINISHED_READING_DATA;
  }

  public static PFont FHelL_11;
  public static PFont FHel_11, FHel_14;
  public static PFont FHelB_12, FHelB_14;
  public static PFont FCODA_11, FCODA_12, FCODA_13, FCODA_25, FCODA_18H;
  public static PFont FFIRA_12;

  // All animators
  public SpringInterpolator[] buildingHeightInterp;
  public BezierInterpolator[] outlineOpacityInterp;
  public SpringInterpolator[] flagHeightInterp;
  public BezierInterpolator[] flagOpacityInterp;

  // Popping and fast:
  private float popUpAttraction = 0.4f;
  private float popUpDamping = 0.6f;

  // Slow and gentle:
  private float gentleFallAttraction = 0.005f; // lower attraction = slower
  private float gentleFallDamping = 0.8f;      // higher = more tension

  // GRAPHICAL COMPONENTS
  private Dimension nativeWH;
  private LoadingScreen loadingScreen;
  private BaseTransformableVisual viz3D;
  private PImage openHand, closedHand;

  private VisualBarGroup visualBarGroup;

  // Helper Tool Tip Text
  private String helperTooltipText = "Presentation mode: Interact to begin.";

  public void setHelperText(String newTip) {
    this.helperTooltipText = newTip;
  }

  // Interactive Elements
  private List<Interactable> allUIElements = new ArrayList<>(); // Ordering IS the layering of the UI (1st = bot layer).
//  private ImageButton cameraButton;
  public ImageButton playPauseButton;
  private ImageButton calendarButton;
  private CalendarModule calendar;
  private Interactable bottomBar;
  private UIMenu sortOptions;

  private PGraphics circle;
  private PGraphics g;

  @Override
  public void setup() {
    try {
      nativeWH = new Dimension(displayWidth, displayHeight);

      loadFonts();

      // Loading Screen
      PImage[] blurredTitles = new PImage[9];
      String titlePanelPrefix;
      if (displayWidth > 1280) {
        titlePanelPrefix = "load_";
      } else {
        titlePanelPrefix = "load_sm_";
      }
      blurredTitles[0] = loadImage(titlePanelPrefix + 0 + ".png");
      blurredTitles[1] = loadImage(titlePanelPrefix + 5 + ".png");
      blurredTitles[2] = loadImage(titlePanelPrefix + 10 + ".png");
      blurredTitles[3] = loadImage(titlePanelPrefix + 15 + ".png");
      blurredTitles[4] = loadImage(titlePanelPrefix + 20 + ".png");
      blurredTitles[5] = loadImage(titlePanelPrefix + 25 + ".png");
      blurredTitles[6] = loadImage(titlePanelPrefix + 30 + ".png");
      blurredTitles[7] = loadImage(titlePanelPrefix + 35 + ".png");
      blurredTitles[8] = loadImage(titlePanelPrefix + 42 + ".png");
      loadingScreen = new LoadingScreen(blurredTitles);

      // Cursor
      openHand = loadImage("openHand.png");
      closedHand = loadImage("closedHand.png");

      // Only for screen recordings
      circle = createGraphics(nativeWH.width, nativeWH.height);

      g = createGraphics(100, 100);
      g.beginDraw();
      g.noStroke();
      g.fill(0);
      g.rect(0, 0, nativeWH.width, nativeWH.height);
      g.endDraw();

      smooth();
      frameRate(30);
    } catch (Exception | Error error) {
      error.printStackTrace();
      this.dispose();
      System.exit(1);
    }
  }

  public void setDataModel(IEnergyVizModel dataModel) {
    this.dataModel = (CityOfPeaks_DataModel) dataModel;
  }

  GraphBarTimeProgressive generator;
  PImage flatTotalGraph;
  public void loadGraphics() {

    // Initializes the graph generator
    GraphGenerator graphGenerator = new WhiteGraphBar(nativeWH.width, 120);
    generator = new GraphBarTimeProgressive(nativeWH.width, 200);
    generator.setTitle("Sum");
    generator.setPrimaryColor(255);
    flatTotalGraph = generator.render(this, dataModel, dataModel.getSumEnergyData());

    Building[] listOfBuildings = dataModel.getListOfBuildings();
    List<NumberDS<Float>> listOfSensorData = dataModel.getListOfSensors();

    visualBarGroup = new VisualBarGroup(this);
    // [SENSORS] Generates the flat images and use them to initialize the visualBarGroup
    for (Building b : listOfBuildings) {
      // Foreach sensor within the building:
      for (Sensor s : b.getSensors()) {
        graphGenerator.setPrimaryColor(listOfBuildings[s.getBID()].getColor());
        graphGenerator.setTitle(s.getName() + " / " + s.getAbbrev());
        visualBarGroup.add(
                new VisualBar(s.getName(), graphGenerator.render(this, dataModel, listOfSensorData.get(s.getSID()))) {
                  @Override
                  public void refresh() {
//                    println("Refreshing SITE " + s.getSID() + ": " + this.getIdentifier() + "...");
                    graphGenerator.setPrimaryColor(listOfBuildings[s.getBID()].getColor());
                    graphGenerator.setTitle(s.getName() + " / " + s.getAbbrev());
                    setBar(graphGenerator.render(
                            CityOfPeaks_View.this,
                            dataModel, listOfSensorData.get(s.getSID())));
//                    println("Refresh finished.");
                  }
                });
      }
    }

    // Initialize: 3D visualization graphics
    viz3D = new CityOfPeaks3D(dataModel);

    // Initialize: 2D UI components
//    sortOptions = new UIMenu(10, 10, FHel_14);
//    PImage icon = loadImage("icon.png");
//    sortOptions.addMenuItemPair(new MenuItemPair("by peak value", icon, new ActionSuite() {
//      @Override
//      public void actPressed(CityOfPeaks_View parent) {
//      }
//    }));
//    sortOptions.addMenuItemPair(new MenuItemPair("by peak time", icon, new ActionSuite() {
//      @Override
//      public void actPressed(CityOfPeaks_View parent) {
//      }
//    }));
//    sortOptions.addMenuItemPair(new MenuItemPair("by usage", icon, new ActionSuite() {
//      @Override
//      public void actPressed(CityOfPeaks_View parent) {
//      }
//    }));

    bottomBar = new HUDPullUpBar(visualBarGroup, nativeWH, loadImage("dragUpDownArrow.png"), loadImage("dragUpArrow.png"), loadImage("dragDownArrow.png"));
    bottomBar.setHelpText("Scroll to view graphs");

    int numBuildings = dataModel.getNumberOfBuildings();
    this.buildingHeightInterp = new SpringInterpolator[numBuildings];
    this.outlineOpacityInterp = new BezierInterpolator[numBuildings];
    this.flagHeightInterp = new SpringInterpolator[numBuildings];
    this.flagOpacityInterp = new BezierInterpolator[numBuildings];

    for (int i = 0; i < numBuildings; ++i) {
      this.buildingHeightInterp[i] = new SpringInterpolator(0);
      this.outlineOpacityInterp[i] = new BezierInterpolator(0);
      this.flagHeightInterp[i] = new SpringInterpolator(0);
      this.flagOpacityInterp[i] = new BezierInterpolator(0);
    }

    //    cameraButton deprecated usage
//    this.cameraButton = new ImageButton(
//            loadImage("autoRotateIcon.png"),
//            new Point(139, nativeWH.height - 114),
//            34, 34);
//    cameraButton.setToggledIcon(loadImage("autoRotateOffIcon.png"));
//    ActionSuite pointerSuite = new ActionSuite();
//    cameraButton.bindAction(pointerSuite);
//    pointerSuite.setPressedAction(new Action() {
//      @Override
//      public void act(CityOfPeaks_Controller controller, MouseEvent e) {
//        controller.setRotate(!controller.getRotate());
//        cameraButton.setIsToggled(!controller.getRotate());
//      }
//    });

    playPauseButton = new ImageButton(
            loadImage("pauseIcon.png"),
            new Point(149, nativeWH.height - 114), // used to have x = 180
            34, 34);
    playPauseButton.setToggledIcon(loadImage("playIcon.png"));
    ActionSuite playPauseSuite = new ActionSuite();
    playPauseButton.bindAction(playPauseSuite);
    playPauseSuite.setPressedAction(new Action() {
      @Override
      public void act(CityOfPeaks_Controller controller, MouseEvent e) {
        controller.togglePlayPause();
        playPauseButton.setIsToggled(!controller.isPlaying());

        controller.setCameraSpin(controller.isPlaying());
      }
    });
    playPauseButton.setHelpText("Play/pause time");

    calendarButton = new ImageButton(
            loadImage("calendarIcon.png"),
            new Point(190, nativeWH.height - 114), 34, 34); // used to have x = 221
    ActionSuite calendarToggleSuite = new ActionSuite();
    calendarButton.bindAction(calendarToggleSuite);
    calendarToggleSuite.setPressedAction(new Action() {
      @Override
      public void act(CityOfPeaks_Controller controller, MouseEvent e) {
        // Flip the calendarButton state.
        calendarButton.toggle();

        // Set the calendar accordingly:
        calendar.setVisible(calendarButton.isToggled());
        calendar.setDisplayedMonth(dataModel.getTimeline().getDateForIndex(0).withDayOfMonth(1));
      }
    });

    calendarToggleSuite.setPressedOutsideAction(new Action() {
      @Override
      public void act(CityOfPeaks_Controller controller, MouseEvent e) {

        // DO NOTHING, the button's state will be updated by the calendar if the calendar hides/shows.

//        // If the mouse is currently on the calendar,
//        if (calendar.isMouseOver(e.getX(), e.getY())) {
//          // Do not do anything, let calendar do its thing
//        } else {
//          // Else, turn the toggle off
//          calendarButton.setIsToggled(false);
//
//          // Update the calendar
//          calendar.setVisible(calendarButton.isToggled());
//        }
      }
    });
    calendarButton.setHelpText("Show/hide the calendar");

    calendar = new CalendarModule(221 - 105 + 17 - 41 + 10, nativeWH.height - 114 - 8 * 30 - 20, nativeWH,
            loadImage("arrowLeft.png"), loadImage("arrowRight.png"), dataModel.getTimeline().getDateForIndex(0));
    calendar.setFirstDayLimit(new DateTime(1356670800 * 1000L));
    calendar.setLastDayLimit(new DateTime(1418101200 * 1000L));
    ActionSuite calendarSuit = new ActionSuite();
    calendar.bindAction(calendarSuit);
    calendarSuit.setPressedAction(new Action() {
      @Override
      public void act(CityOfPeaks_Controller controller, MouseEvent e) {
        Optional<DateTime> hoveredDate = calendar.computeHoveredDate();
        if (!hoveredDate.isPresent()) {
          return;
        }

        calendar.setDisplayedMonth(hoveredDate.get().withDayOfMonth(1));

        // talk to the controller, and controller will speak to the model and view
        controller.setTime(hoveredDate.get().getMillis() / 1000L);
        controller.setTimePercentage(0);
        controller.refreshData();
      }
    });
    calendar.bindAction(calendarSuit);
    calendarSuit.setPressedOutsideAction(new Action() {
      @Override
      public void act(CityOfPeaks_Controller controller, MouseEvent e) {
        if (calendarButton.isMouseOver(e.getX(), e.getY())) {
          // DO NOTHING, let calendar button do the toggle hide/show
        } else {
          // Pressed outside of both calendar and calendar toggle button:

          // Turn the toggle off
          calendarButton.setIsToggled(false);

          // Update the calendar
          calendar.setVisible(false);
        }
      }
    });
    calendar.setHelpText("Jump to a new date");

//    allUIElements.add(cameraButton);
    allUIElements.add(playPauseButton);
    allUIElements.add(calendarButton);
    allUIElements.add(calendar);
    allUIElements.add(bottomBar);
//    allUIElements.add(sortOptions);

    loadState = LoadState.FINISHED_LOADING_GRAPHICS;
    println("Finished Loading!");
  }

  /**
   * Loads: all fonts used by the program
   */
  private void loadFonts() {
    FHelL_11 = loadFont("Helvetica-11.vlw");
    FHel_11 = loadFont("Helvetica-11.vlw");
    FHel_14 = loadFont("Helvetica-14.vlw");
    FHelB_12 = loadFont("HelveticaNeue-Bold-12.vlw");
    FHelB_14 = loadFont("Helvetica-Bold-14.vlw");
    FCODA_11 = loadFont("CODA_REG11.vlw");
    FCODA_12 = loadFont("CODA_REG12.vlw");
    FCODA_13 = loadFont("CODA_REG13.vlw");
    FCODA_25 = loadFont("CODA_REG25.vlw");
    FCODA_18H = loadFont("Coda-Heavy-18.vlw");
    FFIRA_12 = loadFont("FiraCode-Bold-12.vlw");
  }

  private LoadState loadState = LoadState.BEFORE_LOADING;

  PVector focusedCentroid = new PVector(0, 0, 0);
  public PVector getFocusedCentroid() {
    return focusedCentroid;
  }

  /**
   * Reverts all UI elements to its default, untouched state.
   */
  public void resetAllUI() {
    for (Interactable i : allUIElements) {
      i.resetToDefault();
    }
  }

  private boolean spotlightMode = false;
  public void toggleSpotlightMode() {
    spotlightMode = !spotlightMode;
  }

  public enum CursorType {
    OPENHAND, CLOSEDHAND, NORMAL, CLICKABLE
  }

  public void setCursor(CursorType c) {
    switch (c) {
      case OPENHAND:
        cursor(openHand);
        break;
      case CLOSEDHAND:
        cursor(closedHand);
        break;
      case NORMAL:
        cursor();
        break;
      case CLICKABLE:
        cursor(HAND);
        break;
    }
  }

  private enum LoadState {
    BEFORE_LOADING, LOADING_GRAPHICS, FINISHED_LOADING_GRAPHICS, FINISHED_READING_DATA, READING_DATA, COMPLETED
  }

//  public Vector4f cameraCenter;

  private float heightFromVisual = 1000f;

  public void setHeightFromVisual(float height) {
    heightFromVisual = height;
  }


  @Override
  public void draw() {
    try {
      background(7, 8, 11);

      switch (loadState) {
        case BEFORE_LOADING:
          if (loadingScreen.isFinishedAllAnimations()) {
            // Start loading data
            loadState = LoadState.READING_DATA;
            thread("setupMVC");
          }
          loadingScreen.draw(this, dataModel);
          break;
        case READING_DATA:
          loadingScreen.draw(this, dataModel);
          if (dataModel != null) {
            fill(255);
            textFont(FCODA_25, 25);
            textAlign(CENTER, TOP);
            text(dataModel.getLoadingText(), displayWidth / 2, displayHeight / 2 + 200);
          }
          break;
        case FINISHED_READING_DATA:
          loadingScreen.draw(this, dataModel);
          loadState = LoadState.LOADING_GRAPHICS;
          // Start loading graphics
          thread("loadGraphics");
          break;
        case LOADING_GRAPHICS:
          // Still loading graphics asynchronously
          loadingScreen.draw(this, dataModel);
          break;
        case FINISHED_LOADING_GRAPHICS:
          loadingScreen.draw(this, dataModel);
          loadState = LoadState.COMPLETED;
          break;
        case COMPLETED:

          controller.tick();


          // Camera transformations goes here
          beginCamera();

//        ortho();
          camera(displayWidth / 2, displayHeight / 2, heightFromVisual,
                  displayWidth / 2, displayHeight / 2, 0,
                  0, 1, 0);
          translate(displayWidth / 2, displayHeight / 2);
          translate(-displayWidth / 2, -displayHeight / 2);
          endCamera();

//        System.out.println("Camera Center:");
//        System.out.println(cameraCenter);
//        System.out.println("Actual Camera:");
//        printCamera();

          hint(ENABLE_DEPTH_TEST);
          hint(DISABLE_DEPTH_MASK);

          // First updates, then draws the entire 3D visualization
          updateAnimationFields();

          Building focusedBuilding = dataModel.getListOfBuildings()[15];
          if (focusedBuilding != null) {
            focusedCentroid = focusedBuilding.getCentroid();
          }

          viz3D.draw(this, dataModel);

          noLights();

          // Draws the 2D bottomBar
          camera();

          // Sum energy graph: render if less than half of nativeWH.height, and progressively decrease opacity as it approaches the limit.
          float sumEnergyGraphDisplayLimit = nativeWH.height / 2f;
          float sumEnergyGraphDisplayRange = nativeWH.height - sumEnergyGraphDisplayLimit;
          // drawn with bottom of graph directly overlapping the timeline.
          float percentOpacity = (bottomBar.getCornerXY().y - sumEnergyGraphDisplayLimit) / (sumEnergyGraphDisplayRange);
          if (percentOpacity < 0) {
            percentOpacity = 0;
          }

          if (visualBarGroup.getDimension().height < sumEnergyGraphDisplayLimit) {
            PImage flatTotalGraph = this.flatTotalGraph.copy();
            flatTotalGraph.loadPixels();
            for (int x = 0; x < flatTotalGraph.width; ++x) {
              for (int y = 0; y < flatTotalGraph.height; ++y) {
                if (x >= dataModel.getDayPercentage() * nativeWH.width) {
                  flatTotalGraph.pixels[x + y * flatTotalGraph.width] = color(0, 0, 0, 0);
                }
              }
            }
            flatTotalGraph.updatePixels();

            pushStyle();
            tint(255, 255 * percentOpacity * 1.5f);
            image(flatTotalGraph, bottomBar.getCornerXY().x, bottomBar.getCornerXY().y - flatTotalGraph.height + 20); // 20 is timeline height hardcoded..
            popStyle();

            blendMode(PConstants.BLEND);
          }


          pushMatrix();
          translate(35, displayHeight - 125);
//        rotate(radians(-15), 0, 1, 0);
          pushStyle();

          // Time text
          fill(255, 255, 255, 200);
          DateTime today = new DateTime(dataModel.getTimeline().getCurUnix() * 1000L);
          String time = String.format("%02d:%02d", today.getHourOfDay(), today.getMinuteOfHour());
          textFont(FCODA_25, 25);
          textAlign(LEFT, TOP);
          text(time, 0, 0);

          textFont(FCODA_13, 13);
          text(today.dayOfWeek().getAsText(), 0, 30);
          text(today.monthOfYear().getAsText() + " " + today.getDayOfMonth() + "th" + ", " + today.year().getAsText(), 0, 48);

          popStyle();
          popMatrix();

          // App Title
          fill(255, 255, 255, 200);
          textFont(FCODA_18H, 18);
          textAlign(LEFT, TOP);
          int totalTitleWidth = Math.round(textWidth("Information in Action | City of Peaks"));
          int boldWidth = Math.round(textWidth("Information in Action"));
          text("Information in Action", displayWidth - 35 - totalTitleWidth, 35);
          fill(255, 255, 255, 150);
          text(" | City of Peaks", displayWidth - 35 - totalTitleWidth + boldWidth, 35);

          // Help text
          fill(255, 255, 255, 160);
          textFont(FCODA_18H, 16);
          textAlign(LEFT, TOP);
          text(helperTooltipText.toUpperCase(), 35, 35);

          for (Interactable i : allUIElements) {
            i.render(this, dataModel);
          }

          // Spotlight only for video
//          if (spotlightMode) {
//            pushStyle();
//
//            PShape spotlight = createShape();
//            spotlight.beginShape();
//            spotlight.noStroke();
//            spotlight.fill(0, 200);
//            spotlight.vertex(0, nativeWH.height);
//            spotlight.vertex(nativeWH.width, nativeWH.height);
//            spotlight.vertex(nativeWH.width, 0);
//            spotlight.vertex(0, 0);
//            spotlight.vertex(0, nativeWH.height);
//
//            spotlight.beginContour();
//            for (int a = 0; a <= 360; a += 1) {
//              spotlight.vertex(mouseX + r * cos(radians(a)), mouseY + r * sin(radians(a)));
//            }
//            spotlight.endContour();
//            spotlight.endShape();
//            shape(spotlight, 0, 0);
//            int numSteps = 50;
//            for (int step = 0; step < numSteps; ++step) {
////            stroke(0, 200 - 50 * log(step + 1));
//              if (step == 0) { // first one overlaps the edge.
////              stroke(0, 70);
//                stroke(0, 180);
//              } else {
//                stroke(0, 130 - (130 * log10(1 + 9 * (step / (float) numSteps))));
//              }
//              noFill();
//              ellipse(mouseX, mouseY, r * 2 - step - 1, r * 2 - step - 1);
//            }
//            popStyle();
//          }

          break;
        default:
          throw new EnumConstantNotPresentException(LoadState.class, loadState.name());
      }
    } catch (Exception | Error error) {
      error.printStackTrace();
      this.dispose();
      System.exit(1);
    }
  }

  private int r = 200;
  public void increaseSpotlightRadius() {
    r += 10;
  }
  public void decreaseSpotlightRadius() {
    r -= 10;
    if (r <= 30) {
      r = 30;
    }
  }

  private float log10 (float x) {
    return (log(x) / log(10));
  }

  /**
   * Time consuming: renders the graph completely
   */
  public void redrawGraphs() {
    visualBarGroup.render(this);
    flatTotalGraph = generator.render(this, dataModel, dataModel.getSumEnergyData());
  }

  // =============================================================
  // USER INPUT
  // Mouse Events: Activation Solely based on layering of elements
  // Key Events:   Global
  // =============================================================

  public List<Interactable> getInteractables() {
    return allUIElements;
  }

  @Override
  public void mousePressed(MouseEvent e) {
    try {
      if (loadState == LoadState.COMPLETED) {
        controller.parseMousePressed(e);
      }
    } catch (Exception | Error error) {
      error.printStackTrace();
      this.dispose();
      System.exit(1);
    }
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    try {
      if (loadState == LoadState.COMPLETED) {
        controller.parseMouseDragged(e);
      }
    } catch (Exception | Error error) {
      error.printStackTrace();
      this.dispose();
      System.exit(1);
    }
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    try {
      if (loadState == LoadState.COMPLETED) {
        controller.parseMouseReleased(e);
      }
    } catch (Exception | Error error) {
      error.printStackTrace();
      this.dispose();
      System.exit(1);
    }
  }

  /**
   * Scrolls the visual graphs
   * @param event the mouse event
   */
  @Override
  public void mouseWheel(MouseEvent event) {
    try {
      float e = event.getCount();
      if (loadState == LoadState.COMPLETED) {
        controller.parseMouseWheelEvent(event);
      }
    } catch (Exception | Error error) {
      error.printStackTrace();
      this.dispose();
      System.exit(1);
    }
  }

  /**
   * Special: doesn't prioritize, checks each Interactable and removes hovered states for each.
   * @param event
   */
  @Override
  public void mouseMoved(MouseEvent event) {
    try {
      if (loadState == LoadState.COMPLETED) {
        controller.parseMouseMoved(event);
      }
    } catch (Exception | Error error) {
      error.printStackTrace();
      this.dispose();
      System.exit(1);
    }
  }

  @Override
  public void keyPressed(KeyEvent event) {
    try {
      if (loadState == LoadState.COMPLETED) {
        controller.parseKeyPressed(event);
      }
    } catch (Exception e) {
      e.printStackTrace();
      this.dispose();
      System.exit(1);
    }
  }

  public void animateBuildingPop(int buildingID, float destinationValue) {
    final SpringInterpolator heightAnimator = buildingHeightInterp[buildingID];
    final float newResult = destinationValue / 12f;     // lowered for better viewing

    // Rising Up (fast)
    heightAnimator.setAttraction(popUpAttraction);
    heightAnimator.setDamping(popUpDamping);
    heightAnimator.setResult(newResult);
  }

  public void animateBuildingFall(int buildingID, float destinationValue) {
    final SpringInterpolator heightAnimator = buildingHeightInterp[buildingID];
    final float newResult = destinationValue / 12f;     // lowered for better viewing

    // Falling down (gentle)
    heightAnimator.setAttraction(gentleFallAttraction);
    heightAnimator.setDamping(gentleFallDamping);
    heightAnimator.setResult(newResult);
  }

  public void animateFlagFall(int buildingID, boolean isInstant, float destinationValue) {
    final SpringInterpolator flagAnimator = flagHeightInterp[buildingID];
    final float newResult = destinationValue / 12f;      // scaled down for better viewing

    if (isInstant) {
      flagAnimator.instantSetVal(newResult);
    } else {
      // Falling down (gentle)
      flagAnimator.setAttraction(gentleFallAttraction);
      flagAnimator.setDamping(gentleFallDamping);
      flagAnimator.setResult(newResult);
    }
  }

  public void animateFlagPop(int buildingID, boolean isInstant, float destinationValue) {
    final SpringInterpolator flagAnimator = flagHeightInterp[buildingID];
    final float newResult = destinationValue / 12f;      // scaled down for better viewing

    if (isInstant) {
      flagAnimator.instantSetVal(newResult);     // lowered for better viewing
    } else {
      // Rising up (fast)
      flagAnimator.setAttraction(popUpAttraction);
      flagAnimator.setDamping(popUpDamping);
      flagAnimator.setResult(newResult);
    }
  }

  private enum BuildingVizState {
    PRE_POP, POP, POST_POP, END_OF_DAY
  }

  /**
   * Given in radians
   * @param x new xRotation
   * @param y new yRotation
   * @param z new zRotation
   */
  public void setVisualOrientation(float x, float y, float z) {
    viz3D.setRotXYZ(x, y, z);
  }

  public Float getVizXOrientation() {
    return viz3D.getRotXYZ().x;
  }

  public float getVizYOrientation() {
    return viz3D.getRotXYZ().y;
  }

  public float getVizZOrientation() {
    return viz3D.getRotXYZ().z;
  }

  /*
   * Opacity of outline: is driven by energy value (as % of daily low/peak, between 20 - 80%?)
   */
  private float outlineOpacityPercent(Optional<Float> energyKW,
                                      Optional<Moment<Float>> energyKW_Max,
                                      Optional<Moment<Float>> energyKW_Min) {
    // no outline if there is no values present or no max
    if (!energyKW.isPresent() || !energyKW_Max.isPresent() || !energyKW_Min.isPresent()) {
      return 0;
    }

    // if max == min, return 1, since it is a flat graph or only one value present
    if (energyKW_Max.get() == energyKW_Min.get()) {
      return 1;
    }

    float rawPercentage = (energyKW.get() - energyKW_Min.get().getValueAtMoment()) /
            (energyKW_Max.get().getValueAtMoment() - energyKW_Min.get().getValueAtMoment());

    if (rawPercentage > 1f) {
      // FIXME Throw an error
//      throw new RuntimeException("RawPercentage (" + rawPercentage + ") of opacity calculated to be >1! Calculations: " +
//              "(" + energyKW.get() + " - " + energyKW_Min.get().getValueAtMoment() + ") /\n" +
//              "            (" + energyKW_Max.get().getValueAtMoment() + " - " + energyKW_Min.get().getValueAtMoment() + ");" );
    }

    float resultUpperbound = 0.8f;
    float resultLowerbound = 0.1f;
    return (rawPercentage * (resultUpperbound - resultLowerbound)) + resultLowerbound;
  }

  public void updateAnimationFields() {
    List<CityOfPeaks_Model.BuildingStat> peaks = dataModel.getListOfBuildingStatistics();

    TimeRangeUtil timeline = dataModel.getTimeline();
    long currentTime = timeline.getCurUnix();
    long startingTime = timeline.getStartUnix();
    long endingTime = timeline.getEndUnix();

    Building[] buildings = dataModel.getListOfBuildings();

    BuildingVizState animationState = BuildingVizState.PRE_POP;

    for (int bID = 0; bID < buildings.length; ++bID) {

      Building b = buildings[bID];
      CityOfPeaks_Model.BuildingStat singleBuildingStat = peaks.get(bID);
      Optional<Moment<Float>> singleBuildingPeak = singleBuildingStat.getPeak();
      Optional<Moment<Float>> singleBuildingBase = singleBuildingStat.getBase();
      Optional<Float> singleBuildingRange = singleBuildingStat.getRange();

      if (!singleBuildingPeak.isPresent() || !singleBuildingBase.isPresent() || !singleBuildingRange.isPresent()) {
        // DO NOTHING
      }

      // Constantly changing/flickering building base outlines
      outlineOpacityInterp[bID].setVel(0.2f);
      outlineOpacityInterp[bID].setResult(
              outlineOpacityPercent(
                      dataModel.getCurrentBuildingEnergyLevel(bID),
                      singleBuildingPeak,
                      singleBuildingBase) * 255f);

      if (!singleBuildingPeak.isPresent()) {
        animateBuildingFall(bID, 0);
        animateFlagFall(bID, false, 0);
        flagOpacityInterp[bID].setResult(0);

        buildingHeightInterp[bID].update();
        outlineOpacityInterp[bID].update();
        flagHeightInterp[bID].update();
        flagOpacityInterp[bID].update();
        continue;
      }

      Moment<Float> peakMoment = singleBuildingPeak.get();
      float peakValue = peakMoment.getValueAtMoment() * 4f;

      // TEMPORAL VISUALIZATION LOGIC:  Using current time vs building peak time to determine this extrusion's animation state.
      long timeBeforePeak = peakMoment.getTimeAtMoment() - currentTime;
      if (-2700 < timeBeforePeak && timeBeforePeak < 900) {
        // pops up 15 mins (data duration) before peak and stays for 45 mins (data duration)
        animationState = BuildingVizState.POP;
      } else if (0 < timeBeforePeak && timeBeforePeak < 1200) { // starts opacity 100 mins before peak
        animationState = BuildingVizState.PRE_POP;
      } else if (-2700 >= timeBeforePeak && currentTime < endingTime){
        // either past the peak time and before end of day
        animationState = BuildingVizState.POST_POP;
      } else {
        // end of day; all poles go down
        animationState = BuildingVizState.END_OF_DAY;
      }

      switch (animationState) {

        // 1. If building viz before peak
        case PRE_POP:
          // Flag dissolve transition
          flagOpacityInterp[bID].setVel(0.1f);
          flagOpacityInterp[bID].setResult(255);
          break;

        // 2. If building viz @peak (2 second duration / before day ends)
        case POP:
          // Pop animation
          animateFlagPop(bID, false, peakValue);
          animateBuildingPop(bID, peakValue);
          break;

        // 3. If building viz @0 and after peak
        case POST_POP:
          // Gentle fall
          animateBuildingFall(bID, 0);

          // Recently added: If past popping state, all building flags must be completely up.
          flagOpacityInterp[bID].setVel(0.08f);
          flagOpacityInterp[bID].setResult(100);
//          TODO Show alternate method
//          flagOpacityInterp[bID].setVel(1);
//          flagOpacityInterp[bID].setResult(255);

          animateFlagPop(bID, true, peakValue);
          break;

        case END_OF_DAY:
          // Gentle Fall
          animateBuildingFall(bID, 0);

          // Fall down pole
          animateFlagFall(bID, false, 0);

          flagOpacityInterp[bID].setVel(0.05f);
          flagOpacityInterp[bID].setResult(0);
          break;

        default:
          throw new RuntimeException("Bad enum: " + animationState);
      }

      buildingHeightInterp[bID].update();
      outlineOpacityInterp[bID].update();
      flagHeightInterp[bID].update();
      flagOpacityInterp[bID].update();
    }
  }

  // =================
  // BUILDING OUTLINES
  // =================

  public float getOutlineOpacity(int buildingId) {
    return outlineOpacityInterp[buildingId].getCurValue();
  }

  // =======
  // PEAKMAP
  // =======

  public float getFlagHeight(int buildingID) {
    return flagHeightInterp[buildingID].getCurVal();
  }

  public float getFlagOpacity(int buildingID) {
    return flagOpacityInterp[buildingID].getCurValue();
  }

  public float getExtrusionHeight(int buildingID) {
    return buildingHeightInterp[buildingID].getCurVal();
  }

  /**
   * @return the dimensions of this program
   */
  public Dimension getCanvasSize() {
    return this.nativeWH;
  }

  @Override
  public void settings() {
    try {
      size(displayWidth, displayHeight, P3D);
    } catch (Exception | Error error) {
      error.printStackTrace();
      this.dispose();
      System.exit(1);
    }
  }

}
