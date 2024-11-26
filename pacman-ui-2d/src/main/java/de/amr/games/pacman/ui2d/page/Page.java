/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import de.amr.games.pacman.ui2d.GameActionProvider;
import javafx.scene.Node;

/**
 * @author Armin Reichert
 */
public interface Page extends GameActionProvider {
    default Node root() { return (Node) this; }
    void onPageSelected();
    void setSize(double width, double height);
}