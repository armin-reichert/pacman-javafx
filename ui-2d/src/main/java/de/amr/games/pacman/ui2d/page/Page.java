package de.amr.games.pacman.ui2d.page;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

/**
 * @author Armin Reichert
 */
public interface Page {

    Pane rootPane();

    void onSelected();

    void setSize(double width, double height);

    default void onMouseClicked(MouseEvent e) {}

    default void handleKeyboardInput() {}
}
