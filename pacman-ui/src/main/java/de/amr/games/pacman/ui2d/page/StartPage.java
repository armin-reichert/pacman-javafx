/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.page;

import javafx.scene.Node;
import org.tinylog.Logger;

import java.util.Optional;

public interface StartPage {
    Node root();
    default Optional<Node> startButton() { return Optional.empty(); }
    default void start() { Logger.info("{} starts", getClass().getSimpleName()); }
}
