/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import javafx.scene.Node;

/**
 * @author Armin Reichert
 */
public interface Page {
    default Node root() { return (Node) this; }
    default void enterPage() {}
    default void setSize(double width, double height) {}
}