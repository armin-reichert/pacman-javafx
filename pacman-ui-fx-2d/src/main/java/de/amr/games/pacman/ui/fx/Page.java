package de.amr.games.pacman.ui.fx;

import javafx.scene.layout.Pane;

/**
 * @author Armin Reichert
 */
public interface Page {

  Pane rootPane();

  void setSize(double width, double height);
}
