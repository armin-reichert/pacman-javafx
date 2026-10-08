/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views;

import de.amr.pacmanfx.ui.action.core.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.PacManGamesEngine;
import de.amr.pacmanfx.ui.action.core.QuitHandler;
import javafx.scene.Node;

import java.util.Optional;
import java.util.function.Supplier;

public interface GameView extends QuitHandler {

    ActionBindingsRegistry actionBindings();

    default void onInput(PacManGamesEngine app) {
        actionBindings().executeMatchingAction(app);
    }

    Node rootPane();

    default Optional<Supplier<String>> optTitleSupplier() {
        return Optional.empty();
    }

    void setEngine(PacManGamesEngine engine);

    void onEnter();

    void onExit();
}
