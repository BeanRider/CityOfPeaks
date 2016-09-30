package mvc.controller;

import processing.event.KeyEvent;
import processing.event.MouseEvent;

/**
 * Interface for all application controllers
 */
public interface IController {

  /**
   * Parses the mouse event for when mouse is moved,
   * iterating in reverse order of rendering in the view of all UI (last rendered = top priority).
   * The first one to complement with the mouse event will activate its relevant action, and terminates the search.
   * @param e
   */
  void parseMouseMoved(MouseEvent e);

  /**
   * Parses the key event given from the view, modifies the model, and updates the view accordingly.
   * @param e
   */
  void parseKeyPressed(KeyEvent e);
}
