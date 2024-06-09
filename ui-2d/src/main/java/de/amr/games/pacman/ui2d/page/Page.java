package de.amr.games.pacman.ui2d.page;

import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

/**
 * @author Armin Reichert
 *
 * TODO maybe a "page" is nothing else than a scene?
 */
public interface Page {

    Pane rootPane();

    default void onSelected() {};

    default void setSize(double width, double height) {}

    default void onContextMenuRequested(ContextMenuEvent e) {}

    default void onMouseClicked(MouseEvent e) {}

    default void handleKeyboardInput() {}
}
