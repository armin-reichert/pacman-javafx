/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import javafx.scene.Node;
import org.tinylog.Logger;

public interface StartPage {
    Node root();
    default void start() { Logger.info("{} starts", getClass().getSimpleName()); }
}
