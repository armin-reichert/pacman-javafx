/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views;

import de.amr.pacmanfx.ui.action.core.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.action.core.QuitHandler;
import de.amr.pacmanfx.ui.input.Input;
import javafx.scene.Node;

import java.util.Optional;
import java.util.function.Supplier;

public interface GameView extends QuitHandler {

    ActionBindingsRegistry actionBindings();

    default void onInput(Input input) {
        actionBindings().executeMatchingAction(input);
    }

    Node rootPane();

    default Optional<Supplier<String>> optTitleSupplier() {
        return Optional.empty();
    }

    void setGameActionContext(GameAppContext appContext);

    void onEnter();

    void onExit();

    default void render() {}
}
